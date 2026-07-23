package com.bmeyer.assetgen.gen

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator.ImageGeneratorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device text-to-image backend using MediaPipe's Image Generator
 * (Stable Diffusion 1.5, runs fully offline once the model is present).
 *
 * The model directory is supplied by [ModelManager]; this class is only
 * [isReady] when that model is installed. The heavy native init is done lazily
 * on first use and the [ImageGenerator] is reused across generations.
 *
 * Note: MediaPipe's base API takes only a positive prompt + iteration count +
 * seed. The composed negative prompt is not consumed here (the architecture
 * doesn't support it without the conditioning plugins), but we keep composing
 * it for the procedural preview and any future backend.
 */
class MediaPipeAssetGenerator(
    private val context: Context,
    private val models: ModelManager,
) : AssetGenerator {

    override val displayName: String = "On-device SD 1.5"

    @Volatile
    private var generator: ImageGenerator? = null
    private val lock = Any()

    override fun isReady(): Boolean = models.isInstalled()

    private fun obtain(): ImageGenerator {
        generator?.let { return it }
        synchronized(lock) {
            generator?.let { return it }
            val options = ImageGeneratorOptions.builder()
                .setImageGeneratorModelDirectory(models.modelPath)
                .build()
            return ImageGenerator.createFromOptions(context, options).also { generator = it }
        }
    }

    override suspend fun generate(
        request: GenRequest,
        onProgress: (Float) -> Unit,
    ): Bitmap = withContext(Dispatchers.Default) {
        check(isReady()) { "On-device model is not installed" }
        val gen = obtain()
        val iterations = request.prompt.steps.coerceIn(1, 50)

        // Iterative API so we can report per-step progress. The final step's
        // result carries the finished image.
        gen.setInputs(request.prompt.positive, iterations, request.seed)
        var last = gen.execute(false)
        onProgress(1f / iterations)
        for (step in 1 until iterations) {
            last = gen.execute(step == iterations - 1)
            onProgress((step + 1).toFloat() / iterations)
        }
        BitmapExtractor.extract(last.generatedImage())
    }

    override fun close() {
        synchronized(lock) {
            generator?.close()
            generator = null
        }
    }
}
