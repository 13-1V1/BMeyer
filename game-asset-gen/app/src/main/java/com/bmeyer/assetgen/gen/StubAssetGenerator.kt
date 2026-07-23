package com.bmeyer.assetgen.gen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * A dependency-free, fully offline placeholder generator. It paints a
 * deterministic procedural image seeded by the prompt so the whole app —
 * generate, preview, background-removal, gallery, export — works end to end
 * before (or instead of) the multi-gigabyte on-device model is installed.
 *
 * It is intentionally NOT a diffusion model; it just proves the pipeline and
 * gives every prompt a distinct, repeatable look. [isReady] is always true.
 */
class StubAssetGenerator : AssetGenerator {

    override val displayName: String = "Preview (procedural)"

    override fun isReady(): Boolean = true

    override suspend fun generate(request: GenRequest, onProgress: (Float) -> Unit): Bitmap {
        val size = request.prompt.size.coerceIn(64, 1024)
        val rng = java.util.Random(seedFor(request))

        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Background: diagonal gradient between two seeded hues.
        val bgA = hsvColor(rng.nextFloat() * 360f, 0.35f, 0.95f)
        val bgB = hsvColor(rng.nextFloat() * 360f, 0.45f, 0.80f)
        Paint().apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                bgA, bgB, Shader.TileMode.CLAMP
            )
        }.also { canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), it) }

        // Simulate diffusion steps so the progress bar behaves like the real one.
        val steps = request.prompt.steps.coerceIn(1, 50)
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f
        val cy = size / 2f
        for (step in 1..steps) {
            val t = step / steps.toFloat()
            val radius = size * (0.10f + 0.30f * rng.nextFloat())
            val ox = cx + (rng.nextFloat() - 0.5f) * size * 0.4f
            val oy = cy + (rng.nextFloat() - 0.5f) * size * 0.4f
            val hue = rng.nextFloat() * 360f
            shapePaint.shader = RadialGradient(
                ox, oy, radius,
                hsvColor(hue, 0.7f, 0.95f, alpha = 200),
                hsvColor(hue, 0.9f, 0.55f, alpha = 60),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(ox, oy, radius, shapePaint)
            onProgress(t)
            delay(8) // keep the UI responsive and the progress visible
        }

        // A solid central "subject" mass so background removal has something to keep.
        shapePaint.shader = null
        shapePaint.color = hsvColor(rng.nextFloat() * 360f, 0.8f, 0.9f)
        canvas.drawCircle(cx, cy, size * 0.22f, shapePaint)

        onProgress(1f)
        return bmp
    }

    private fun seedFor(request: GenRequest): Long {
        val h = request.prompt.positive.hashCode().toLong()
        return (h shl 21) xor request.seed.toLong() xor 0x5DEECE66DL
    }

    private fun hsvColor(h: Float, s: Float, v: Float, alpha: Int = 255): Int {
        val hh = ((h % 360f) + 360f) % 360f
        val c = v * s
        val x = c * (1 - abs((hh / 60f) % 2 - 1))
        val m = v - c
        val (r, g, b) = when {
            hh < 60 -> Triple(c, x, 0f)
            hh < 120 -> Triple(x, c, 0f)
            hh < 180 -> Triple(0f, c, x)
            hh < 240 -> Triple(0f, x, c)
            hh < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return (alpha shl 24) or
            (((r + m) * 255).toInt() shl 16) or
            (((g + m) * 255).toInt() shl 8) or
            ((b + m) * 255).toInt()
    }
}
