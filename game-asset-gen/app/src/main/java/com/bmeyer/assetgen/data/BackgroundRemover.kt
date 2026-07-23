package com.bmeyer.assetgen.data

/**
 * Edge-seeded flood-fill background removal, operating purely on an ARGB
 * `IntArray` (the exact layout `Bitmap.getPixels` produces). No Android types,
 * so the whole algorithm is unit-testable on the JVM.
 *
 * The idea: a generated game asset sits on a fairly uniform background. We seed
 * a flood fill from every border pixel and clear (alpha = 0) any pixel that is
 * within [tolerance] colour distance of the seed reference AND connected to the
 * border through other background pixels. Interior pixels that merely happen to
 * match the background colour are preserved, because they aren't reachable.
 */
object BackgroundRemover {

    /** Sensible default colour tolerance (0..441, i.e. max RGB distance). */
    const val DEFAULT_TOLERANCE = 40

    /**
     * @param pixels ARGB pixels, row-major, length must equal width*height.
     * @param tolerance max Euclidean RGB distance from the border reference
     *   colour for a pixel to count as background.
     * @return a NEW array; input is not mutated. Matched pixels have alpha 0.
     */
    fun removeBackground(
        pixels: IntArray,
        width: Int,
        height: Int,
        tolerance: Int = DEFAULT_TOLERANCE,
    ): IntArray {
        require(width > 0 && height > 0) { "width/height must be positive" }
        require(pixels.size == width * height) { "pixels size != width*height" }

        val out = pixels.copyOf()
        if (width < 2 || height < 2) return out

        val ref = averageBorderColor(pixels, width, height)
        val refR = (ref ushr 16) and 0xFF
        val refG = (ref ushr 8) and 0xFF
        val refB = ref and 0xFF
        val tol2 = tolerance.toLong() * tolerance.toLong()

        val visited = BooleanArray(pixels.size)
        // Explicit index stack — recursion would blow the stack on big images.
        val stack = ArrayDeque<Int>()

        fun consider(i: Int) {
            if (i < 0 || i >= pixels.size || visited[i]) return
            visited[i] = true
            val p = pixels[i]
            val dr = ((p ushr 16) and 0xFF) - refR
            val dg = ((p ushr 8) and 0xFF) - refG
            val db = (p and 0xFF) - refB
            val dist2 = dr.toLong() * dr + dg.toLong() * dg + db.toLong() * db
            if (dist2 <= tol2) {
                out[i] = p and 0x00FFFFFF // clear alpha
                stack.addLast(i)
            }
        }

        // Seed from all four borders.
        for (x in 0 until width) {
            consider(x)                          // top row
            consider((height - 1) * width + x)   // bottom row
        }
        for (y in 0 until height) {
            consider(y * width)                  // left column
            consider(y * width + (width - 1))    // right column
        }

        while (stack.isNotEmpty()) {
            val i = stack.removeLast()
            val x = i % width
            val y = i / width
            if (x > 0) consider(i - 1)
            if (x < width - 1) consider(i + 1)
            if (y > 0) consider(i - width)
            if (y < height - 1) consider(i + width)
        }
        return out
    }

    /** Count of fully-transparent (alpha 0) pixels — handy for tests/stats. */
    fun transparentCount(pixels: IntArray): Int =
        pixels.count { (it ushr 24) and 0xFF == 0 }

    private fun averageBorderColor(pixels: IntArray, width: Int, height: Int): Int {
        var r = 0L; var g = 0L; var b = 0L; var n = 0L
        fun acc(i: Int) {
            val p = pixels[i]
            r += (p ushr 16) and 0xFF
            g += (p ushr 8) and 0xFF
            b += p and 0xFF
            n++
        }
        for (x in 0 until width) {
            acc(x)
            acc((height - 1) * width + x)
        }
        for (y in 1 until height - 1) {
            acc(y * width)
            acc(y * width + (width - 1))
        }
        if (n == 0L) return 0
        return (((r / n) and 0xFF) shl 16).toInt() or
            (((g / n) and 0xFF) shl 8).toInt() or
            ((b / n) and 0xFF).toInt()
    }
}
