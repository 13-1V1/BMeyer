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
import com.bmeyer.assetgen.gen.ModelManager
import com.bmeyer.assetgen.gen.StubAssetGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val removeBackground: Boolean = true,
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val engineLabel: String = "",
    val modelInstalled: Boolean = false,
    val result: Bitmap? = null,
    val gallery: List<GalleryEntry> = emptyList(),
    val message: String? = null,
) {
    /** Background removal only makes sense for cut-out asset types. */
    val backgroundToggleEnabled: Boolean get() = type.wantsTransparency
    val canGenerate: Boolean get() = !isGenerating
}

/**
 * Drives generation. Picks the best available backend at generate time: the
 * on-device diffusion engine when its model is installed, otherwise the
 * procedural preview so the app is always usable.
 */
class AssetGenViewModel(private val appContext: Context) : ViewModel() {

    private val models = ModelManager(appContext)
    private val onDevice: AssetGenerator = MediaPipeAssetGenerator(appContext, models)
    private val preview: AssetGenerator = StubAssetGenerator()

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

    fun setStyle(style: StylePreset) = _state.update { it.copy(style = style) }

    fun setType(type: AssetType) = _state.update {
        // Keep the toggle coherent when switching to a non-transparent type.
        it.copy(type = type, removeBackground = it.removeBackground && type.wantsTransparency)
    }

    fun toggleRemoveBackground() =
        _state.update { it.copy(removeBackground = !it.removeBackground) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    fun generate() {
        val s = _state.value
        if (s.isGenerating) return
        val engine = activeEngine()
        val composed = PromptComposer.compose(s.prompt, s.style, s.type)
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

    /** A stable per-seed pseudo-timestamp for file naming (no clock in tests). */
    private fun seedStamp(seed: Int): Long = seed.toLong() and 0xFFFFFFFFL

    override fun onCleared() {
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
