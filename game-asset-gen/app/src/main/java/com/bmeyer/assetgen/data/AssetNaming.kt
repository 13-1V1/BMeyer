package com.bmeyer.assetgen.data

/**
 * Deterministic, filesystem-safe naming for saved/exported assets. Pure string
 * work (timestamp is passed in, never read from the clock) so it is fully
 * unit-testable and reproducible.
 */
object AssetNaming {

    private const val MAX_SLUG = 40
    private val UNSAFE = Regex("[^a-z0-9]+")

    /** e.g. slug("Fire Knight!!") -> "fire-knight". */
    fun slug(text: String): String {
        val s = text.trim().lowercase()
            .replace(UNSAFE, "-")
            .trim('-')
            .take(MAX_SLUG)
            .trim('-')
        return s.ifEmpty { "asset" }
    }

    /**
     * Builds a PNG filename like `spriteforge_pixel-art_fire-knight_1721700000000.png`.
     * @param epochMillis caller-supplied timestamp — keeps this pure/testable.
     */
    fun fileName(
        userPrompt: String,
        style: StylePreset,
        epochMillis: Long,
    ): String {
        val stylePart = slug(style.label)
        val promptPart = slug(userPrompt)
        return "spriteforge_${stylePart}_${promptPart}_$epochMillis.png"
    }
}
