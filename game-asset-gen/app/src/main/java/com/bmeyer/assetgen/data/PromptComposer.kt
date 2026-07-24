package com.bmeyer.assetgen.data

/** The fully-composed prompt handed to a generator backend. */
data class ComposedPrompt(
    val positive: String,
    val negative: String,
    val steps: Int,
    val size: Int,
    val seamless: Boolean,
    val wantsTransparency: Boolean,
)

/**
 * Turns a user's short description plus the chosen [StylePreset] and
 * [AssetType] into a full positive/negative prompt for the diffusion backend.
 *
 * Deliberately pure and deterministic so it is unit-testable on the JVM and so
 * two identical requests compose byte-for-byte identical prompts (useful for
 * caching and reproducibility).
 */
object PromptComposer {

    /** Terms we always steer away from for game-asset generation. */
    private const val BASE_NEGATIVE =
        "text, watermark, signature, extra limbs, deformed, cropped, low quality, cluttered background"

    /** Valid diffusion-step range the UI slider and generators clamp to. */
    const val MIN_STEPS = 4
    const val MAX_STEPS = 50

    fun compose(
        userPrompt: String,
        style: StylePreset,
        type: AssetType,
        steps: Int = style.steps,
    ): ComposedPrompt {
        val subject = userPrompt.trim().ifEmpty { "game asset" }

        val positive = joinDistinct(
            subject,
            type.framing,
            style.positive,
            "game asset, high quality",
        )

        val negativeParts = mutableListOf(BASE_NEGATIVE, style.negative)
        if (type.wantsTransparency) {
            negativeParts += "busy scenery, drop shadow on ground"
        }
        val negative = joinDistinct(*negativeParts.toTypedArray())

        return ComposedPrompt(
            positive = positive,
            negative = negative,
            steps = steps.coerceIn(MIN_STEPS, MAX_STEPS),
            size = type.defaultSize,
            seamless = type.wantsSeamless,
            wantsTransparency = type.wantsTransparency,
        )
    }

    /**
     * Comma-joins fragments, dropping blanks and case-insensitive duplicate
     * comma-separated terms while preserving first-seen order. Keeps composed
     * prompts tidy even when a preset and the base list overlap.
     */
    private fun joinDistinct(vararg fragments: String): String {
        val seen = LinkedHashMap<String, String>()
        for (fragment in fragments) {
            for (raw in fragment.split(',')) {
                val term = raw.trim()
                if (term.isEmpty()) continue
                val key = term.lowercase()
                if (!seen.containsKey(key)) seen[key] = term
            }
        }
        return seen.values.joinToString(", ")
    }
}
