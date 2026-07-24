package com.bmeyer.assetgen.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bmeyer.assetgen.data.AssetType
import com.bmeyer.assetgen.data.AssetNaming
import com.bmeyer.assetgen.data.PromptComposer
import com.bmeyer.assetgen.data.StylePreset
import com.bmeyer.assetgen.gen.AssetGenerator
import com.bmeyer.assetgen.gen.BitmapTransforms
import com.bmeyer.assetgen.gen.GalleryExport
import com.bmeyer.assetgen.gen.GenRequest
import com.bmeyer.assetgen.gen.MediaPipeAssetGenerator
import com.bmeyer.assetgen.gen.ModelDownloader
import com.bmeyer.assetgen.gen.ModelManager
import com.bmeyer.assetgen.gen.StubAssetGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random

/** One finished generation kept in the session gallery. */
data class GalleryEntry(
    val bitmap: Bitmap,
    val fileName: String,
    val prompt: String,
    val style: StylePreset,
    val seed: Int,
)

/** The single source of truth for the whole screen. */
data class UiState(
    val prompt: String = "",
    val style: StylePreset = StylePreset.DEFAULT,
    val type: AssetType = AssetType.DEFAULT,
    val steps: Int = StylePreset.DEFAULT.steps,
    val removeBackground: Boolean = true,
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val engineLabel: String = "",
    val modelInstalled: Boolean = false,
    val result: Bitmap? = null,
    val gallery: List<GalleryEntry> = emptyList(),
    val message: String? = null,
    // On-device model download.
    val modelUrl: String = "",
    val isDownloading: Boolean = false,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = -1,
) {
    /** Background removal only makes sense for cut-out asset types. */
    val backgroundToggleEnabled: Boolean get() = type.wantsTransparency
    val canGenerate: Boolean get() = !isGenerating && !isDownloading

    /** Download fraction 0f..1f, or null when the total size is unknown. */
    val downloadFraction: Float?
        get() = if (isDownloading && totalBytes > 0) {
            (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
        } else null
}

/**
 * Drives generation. Picks the best available backend at generate time: the
 * on-device diffusion engine when its model is installed, otherwise the
 * procedural preview so the app is always usable.
 */
class AssetGenViewModel(private val appContext: Context) : ViewModel() {

    private val models = ModelManager(appContext)
    private val downloader = ModelDownloader(appContext)
    private val onDevice: AssetGenerator = MediaPipeAssetGenerator(appContext, models)
    private val preview: AssetGenerator = StubAssetGenerator()

    private var downloadJob: Job? = null

    private val _state = MutableStateFlow(
        UiState(
            modelInstalled = models.isInstalled(),
            engineLabel = activeEngine().displayName,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private fun activeEngine(): AssetGenerator =
        if (onDevice.isReady()) onDevice else preview

    fun setPrompt(text: String) = _state.update { it.copy(prompt = text) }

    fun setStyle(style: StylePreset) = _state.update {
        // Snap the step count to the newly-chosen preset's recommendation; the
        // user can still override it with the slider afterward.
        it.copy(style = style, steps = style.steps)
    }

    fun setType(type: AssetType) = _state.update {
        // Keep the toggle coherent when switching to a non-transparent type.
        it.copy(type = type, removeBackground = it.removeBackground && type.wantsTransparency)
    }

    fun setSteps(steps: Int) = _state.update {
        it.copy(steps = steps.coerceIn(PromptComposer.MIN_STEPS, PromptComposer.MAX_STEPS))
    }

    fun toggleRemoveBackground() =
        _state.update { it.copy(removeBackground = !it.removeBackground) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    fun generate() {
        val s = _state.value
        if (s.isGenerating) return
        val engine = activeEngine()
        val composed = PromptComposer.compose(s.prompt, s.style, s.type, s.steps)
        val seed = Random.nextInt(0, Int.MAX_VALUE)

        _state.update {
            it.copy(
                isGenerating = true,
                progress = 0f,
                engineLabel = engine.displayName,
                message = null,
            )
        }

        viewModelScope.launch {
            try {
                var bmp = engine.generate(GenRequest(composed, seed)) { p ->
                    _state.update { it.copy(progress = p) }
                }
                if (s.removeBackground && s.type.wantsTransparency) {
                    bmp = withContext(Dispatchers.Default) { BitmapTransforms.removeBackground(bmp) }
                }
                val entry = GalleryEntry(
                    bitmap = bmp,
                    fileName = AssetNaming.fileName(s.prompt, s.style, seedStamp(seed)),
                    prompt = s.prompt,
                    style = s.style,
                    seed = seed,
                )
                _state.update {
                    it.copy(
                        isGenerating = false,
                        progress = 1f,
                        result = bmp,
                        gallery = listOf(entry) + it.gallery,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        message = "Generation failed: ${e.message ?: e.javaClass.simpleName}",
                    )
                }
            }
        }
    }

    /** Save the current result to the device gallery as a transparent PNG. */
    fun saveCurrent() {
        val s = _state.value
        val bmp = s.result ?: return
        viewModelScope.launch {
            val name = AssetNaming.fileName(s.prompt, s.style, seedStamp(s.gallery.firstOrNull()?.seed ?: 0))
            val uri: Uri? = withContext(Dispatchers.IO) {
                GalleryExport.savePng(appContext, bmp, name)
            }
            _state.update {
                it.copy(message = if (uri != null) "Saved to Pictures/SpriteForge" else "Save failed")
            }
        }
    }

    /** Import a converted SD 1.5 model bundle (.zip) from a content Uri. */
    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(message = "Importing model…") }
            val ok = withContext(Dispatchers.IO) {
                try {
                    appContext.contentResolver.openInputStream(uri)?.use { input ->
                        models.installFromZip(input)
                    } ?: return@withContext false
                    models.isInstalled()
                } catch (e: Exception) {
                    false
                }
            }
            _state.update {
                it.copy(
                    modelInstalled = models.isInstalled(),
                    engineLabel = activeEngine().displayName,
                    message = if (ok) "On-device model installed" else "Model import failed",
                )
            }
        }
    }

    fun setModelUrl(url: String) = _state.update { it.copy(modelUrl = url) }

    /**
     * Download a model bundle (.zip) from [UiState.modelUrl], unzip it into the
     * on-device model directory, and switch the active engine to it. Cancelable
     * via [cancelDownload].
     */
    fun downloadModel() {
        val url = _state.value.modelUrl.trim()
        if (url.isEmpty() || _state.value.isDownloading) return

        _state.update {
            it.copy(isDownloading = true, downloadedBytes = 0, totalBytes = -1, message = null)
        }
        downloadJob = viewModelScope.launch {
            try {
                val file: File = downloader.downloadToCache(url) { downloaded, total ->
                    _state.update { it.copy(downloadedBytes = downloaded, totalBytes = total) }
                }
                _state.update { it.copy(message = "Installing model…") }
                val installed = withContext(Dispatchers.IO) {
                    FileInputStream(file).use { models.installFromZip(it) }
                    file.delete()
                    models.isInstalled()
                }
                _state.update {
                    it.copy(
                        isDownloading = false,
                        modelInstalled = models.isInstalled(),
                        engineLabel = activeEngine().displayName,
                        message = if (installed) "On-device model installed" else "Install failed",
                    )
                }
            } catch (e: CancellationException) {
                _state.update { it.copy(isDownloading = false, message = "Download canceled") }
                throw e // let the coroutine actually cancel
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isDownloading = false,
                        message = "Download failed: ${e.message ?: e.javaClass.simpleName}",
                    )
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
    }

    /** A stable per-seed pseudo-timestamp for file naming (no clock in tests). */
    private fun seedStamp(seed: Int): Long = seed.toLong() and 0xFFFFFFFFL

    override fun onCleared() {
        downloadJob?.cancel()
        onDevice.close()
        preview.close()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AssetGenViewModel(context.applicationContext) as T
            }
    }
}
