package com.bmeyer.assetgen

import com.bmeyer.assetgen.data.BackgroundRemover
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundRemoverTest {

    private val WHITE = 0xFFFFFFFF.toInt()
    private val RED = 0xFFFF0000.toInt()

    private fun alpha(p: Int) = (p ushr 24) and 0xFF

    @Test
    fun `clears a uniform background but keeps the subject`() {
        val w = 5; val h = 5
        val px = IntArray(w * h) { WHITE }
        // A red subject in the center (not touching any border).
        px[2 * w + 2] = RED
        val out = BackgroundRemover.removeBackground(px, w, h, tolerance = 10)

        // All border/background pixels are now transparent.
        assertEquals(0, alpha(out[0]))
        assertEquals(0, alpha(out[w * h - 1]))
        // The subject pixel keeps full alpha and its colour.
        assertEquals(RED, out[2 * w + 2])
        assertEquals(255, alpha(out[2 * w + 2]))
    }

    @Test
    fun `interior pixel matching bg colour is preserved when enclosed by subject`() {
        // 5x5: red ring around a single white interior pixel; white border too.
        val w = 5; val h = 5
        val px = IntArray(w * h) { WHITE }
        // Draw a red 3x3 ring at rows/cols 1..3, leaving center (2,2) white.
        for (y in 1..3) for (x in 1..3) {
            if (!(x == 2 && y == 2)) px[y * w + x] = RED
        }
        val out = BackgroundRemover.removeBackground(px, w, h, tolerance = 10)

        // Border cleared...
        assertEquals(0, alpha(out[0]))
        // ...but the enclosed white pixel is unreachable from the border, so kept.
        assertEquals(255, alpha(out[2 * w + 2]))
        assertEquals(WHITE, out[2 * w + 2])
    }

    @Test
    fun `does not mutate the input array`() {
        val w = 3; val h = 3
        val px = IntArray(w * h) { WHITE }
        val copy = px.copyOf()
        BackgroundRemover.removeBackground(px, w, h)
        assertTrue(px.contentEquals(copy))
    }

    @Test
    fun `higher tolerance removes near-background shades`() {
        val w = 4; val h = 4
        val nearWhite = 0xFFF5F5F5.toInt()
        val px = IntArray(w * h) { WHITE }
        // A ring-adjacent near-white blob connected to the border.
        px[0] = nearWhite
        px[1] = nearWhite
        val strict = BackgroundRemover.removeBackground(px, w, h, tolerance = 2)
        val loose = BackgroundRemover.removeBackground(px, w, h, tolerance = 40)
        // Loose clears more (or equal) pixels than strict.
        assertTrue(
            BackgroundRemover.transparentCount(loose) >=
                BackgroundRemover.transparentCount(strict)
        )
    }

    @Test
    fun `rejects mismatched dimensions`() {
        var threw = false
        try {
            BackgroundRemover.removeBackground(IntArray(3), 2, 2)
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun `single row image is returned unchanged`() {
        val px = intArrayOf(WHITE, WHITE, RED)
        val out = BackgroundRemover.removeBackground(px, 3, 1)
        // height < 2 short-circuit: nothing cleared.
        assertNotEquals(0, alpha(out[0]))
    }
}
