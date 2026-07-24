package com.bmeyer.assetgen.gen

import android.graphics.Bitmap
import com.bmeyer.assetgen.data.ComposedPrompt

/** One generation request: the composed prompt plus a reproducibility seed. */
data class GenRequest(
    val prompt: ComposedPrompt,
    val seed: Int,
)

/**
 * Abstraction over "text → image" backends. The app talks only to this
 * interface, so the on-device diffusion engine and the procedural fallback are
 * interchangeable and the UI never needs to know which is active.
 */
interface AssetGenerator {

    /** Human-readable name shown in the UI ("On-device SD 1.5", "Preview"). */
    val displayName: String

    /** True when this backend can actually produce a real generation now. */
    fun isReady(): Boolean

    /**
     * Generate a square bitmap for [request]. Long-running; call off the main
     * thread. [onProgress] is invoked with 0f..1f as steps complete.
     */
    suspend fun generate(request: GenRequest, onProgress: (Float) -> Unit = {}): Bitmap

    /** Release any native resources. Safe to call more than once. */
    fun close() {}
}
