package com.bmeyer.assetgen.data

import kotlin.math.ceil
import kotlin.math.sqrt

/** A frame's destination rectangle within the packed sheet. */
data class FrameRect(val x: Int, val y: Int, val w: Int, val h: Int)

/** Full sprite-sheet layout: overall size plus each frame's placement. */
data class SheetLayout(
    val sheetWidth: Int,
    val sheetHeight: Int,
    val columns: Int,
    val rows: Int,
    val frames: List<FrameRect>,
)

/**
 * Computes a uniform grid layout for packing equally-sized frames into a single
 * sprite sheet. Pure arithmetic — no Android Bitmap here; the UI layer just
 * blits each source frame into `frames[i]`.
 */
object SpriteSheetPacker {

    /**
     * @param frameCount number of frames (must be >= 1).
     * @param frameWidth per-frame width in px (> 0).
     * @param frameHeight per-frame height in px (> 0).
     * @param columns fixed column count; if null, uses a near-square grid.
     * @param padding transparent gutter in px between adjacent frames (>= 0).
     */
    fun pack(
        frameCount: Int,
        frameWidth: Int,
        frameHeight: Int,
        columns: Int? = null,
        padding: Int = 0,
    ): SheetLayout {
        require(frameCount >= 1) { "frameCount must be >= 1" }
        require(frameWidth > 0 && frameHeight > 0) { "frame size must be positive" }
        require(padding >= 0) { "padding must be >= 0" }

        val cols = (columns ?: ceil(sqrt(frameCount.toDouble())).toInt()).coerceIn(1, frameCount)
        val rows = ceil(frameCount.toDouble() / cols).toInt()

        val sheetWidth = cols * frameWidth + (cols - 1) * padding
        val sheetHeight = rows * frameHeight + (rows - 1) * padding

        val frames = ArrayList<FrameRect>(frameCount)
        for (i in 0 until frameCount) {
            val col = i % cols
            val row = i / cols
            frames += FrameRect(
                x = col * (frameWidth + padding),
                y = row * (frameHeight + padding),
                w = frameWidth,
                h = frameHeight,
            )
        }
        return SheetLayout(sheetWidth, sheetHeight, cols, rows, frames)
    }
}
