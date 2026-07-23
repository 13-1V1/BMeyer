package com.bmeyer.assetgen

import com.bmeyer.assetgen.data.SpriteSheetPacker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteSheetPackerTest {

    @Test
    fun `near-square grid for 4 frames`() {
        val l = SpriteSheetPacker.pack(4, 32, 32)
        assertEquals(2, l.columns)
        assertEquals(2, l.rows)
        assertEquals(64, l.sheetWidth)
        assertEquals(64, l.sheetHeight)
        assertEquals(4, l.frames.size)
    }

    @Test
    fun `fixed column count controls layout`() {
        val l = SpriteSheetPacker.pack(5, 16, 16, columns = 5)
        assertEquals(5, l.columns)
        assertEquals(1, l.rows)
        assertEquals(80, l.sheetWidth)
        assertEquals(16, l.sheetHeight)
    }

    @Test
    fun `frames are laid out left-to-right, top-to-bottom`() {
        val l = SpriteSheetPacker.pack(3, 10, 10, columns = 2)
        assertEquals(0, l.frames[0].x); assertEquals(0, l.frames[0].y)
        assertEquals(10, l.frames[1].x); assertEquals(0, l.frames[1].y)
        assertEquals(0, l.frames[2].x); assertEquals(10, l.frames[2].y)
    }

    @Test
    fun `padding adds gutters between frames only`() {
        val l = SpriteSheetPacker.pack(4, 10, 10, columns = 2, padding = 2)
        // 2 cols: 10 + 2 + 10 = 22 wide, same tall.
        assertEquals(22, l.sheetWidth)
        assertEquals(22, l.sheetHeight)
        assertEquals(12, l.frames[1].x) // second column offset by width+padding
    }

    @Test
    fun `partial last row still fits all frames`() {
        val l = SpriteSheetPacker.pack(7, 8, 8, columns = 3)
        assertEquals(3, l.columns)
        assertEquals(3, l.rows) // ceil(7/3)
        assertEquals(7, l.frames.size)
    }

    @Test
    fun `single frame`() {
        val l = SpriteSheetPacker.pack(1, 64, 48)
        assertEquals(1, l.columns)
        assertEquals(1, l.rows)
        assertEquals(64, l.sheetWidth)
        assertEquals(48, l.sheetHeight)
    }

    @Test
    fun `invalid inputs are rejected`() {
        listOf(
            { SpriteSheetPacker.pack(0, 8, 8) },
            { SpriteSheetPacker.pack(1, 0, 8) },
            { SpriteSheetPacker.pack(1, 8, -1) },
        ).forEach { call ->
            var threw = false
            try { call() } catch (e: IllegalArgumentException) { threw = true }
            assertTrue(threw)
        }
    }
}
