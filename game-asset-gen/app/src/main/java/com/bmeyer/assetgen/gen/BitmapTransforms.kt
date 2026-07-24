package com.bmeyer.assetgen.gen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.bmeyer.assetgen.data.BackgroundRemover
import com.bmeyer.assetgen.data.SheetLayout
import com.bmeyer.assetgen.data.SpriteSheetPacker

/**
 * Thin Android bridge that feeds real [Bitmap]s through the pure, unit-tested
 * algorithms in `data/`. Keeping the pixel math in `data/` (plain IntArray) is
 * what lets it run under JVM tests with no device.
 */
object BitmapTransforms {

    /**
     * Returns a copy of [src] with a uniform background cleared to transparent,
     * using the edge-seeded flood fill in [BackgroundRemover].
     */
    fun removeBackground(
        src: Bitmap,
        tolerance: Int = BackgroundRemover.DEFAULT_TOLERANCE,
    ): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val out = BackgroundRemover.removeBackground(pixels, w, h, tolerance)
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Composites equally-sized [frames] into a single sprite sheet using
     * [SpriteSheetPacker]'s computed layout. All frames should share the size
     * of the first; mismatched frames are scaled to fit their cell.
     */
    fun toSpriteSheet(
        frames: List<Bitmap>,
        columns: Int? = null,
        padding: Int = 0,
    ): Bitmap {
        require(frames.isNotEmpty()) { "no frames" }
        val fw = frames.first().width
        val fh = frames.first().height
        val layout: SheetLayout =
            SpriteSheetPacker.pack(frames.size, fw, fh, columns, padding)

        val sheet = Bitmap.createBitmap(
            layout.sheetWidth, layout.sheetHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(sheet)
        frames.forEachIndexed { i, frame ->
            val r = layout.frames[i]
            canvas.drawBitmap(frame, null, Rect(r.x, r.y, r.x + r.w, r.y + r.h), null)
        }
        return sheet
    }
}
