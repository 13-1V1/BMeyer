package com.motley.engine

/**
 * The **hatch / reveal** — Motley's signature moment (GDD §11, D13). When a grown creature is
 * shown for the first time, it isn't a flat "here it is": it's a paced set-piece —
 * **anticipation → held silence → burst → settle** — whose cadence is gated by rarity (rarer =
 * longer build, longer held breath, brighter burst, unique sting).
 *
 * This models the moment as **structured data** a UI and audio layer can play back beat by beat.
 * It's pure and deterministic, so the pacing can be tuned and tested without any front-end.
 */

/** How special the reveal is. WILD is the rare hidden family — the biggest, strangest reveal. */
enum class RevealTier { COMMON, UNCOMMON, RARE, WILD }

/** The four movements of the reveal, always in this order. */
enum class RevealPhase { ANTICIPATION, SILENCE, BURST, SETTLE }

/**
 * One beat of the reveal: a [phase] held for [durationMs], with an [intensity] (0..1, drives
 * shake/particles/brightness), an audio [sfx] cue, and an optional on-screen [caption].
 */
data class RevealBeat(
    val phase: RevealPhase,
    val durationMs: Int,
    val intensity: Double,
    val sfx: String,
    val caption: String,
)

data class RevealSequence(val tier: RevealTier, val beats: List<RevealBeat>) {
    val totalMs: Int get() = beats.sumOf { it.durationMs }
    fun beat(phase: RevealPhase): RevealBeat = beats.first { it.phase == phase }
}

object Reveal {
    // How much the build-up stretches per tier — rarer creatures make you wait, and it pays off.
    private fun stretch(tier: RevealTier): Double = when (tier) {
        RevealTier.COMMON -> 1.0
        RevealTier.UNCOMMON -> 1.3
        RevealTier.RARE -> 1.7
        RevealTier.WILD -> 2.2
    }

    /** The reveal tier for a creature: WILD if hidden, otherwise the rarest essence in its recipe. */
    fun tierOf(creature: Creature): RevealTier {
        if (creature.isWild) return RevealTier.WILD
        val topRarity = creature.recipe
            .mapNotNull { StarterEssences.byId[it]?.rarity }
            .maxByOrNull { it.ordinal } ?: Rarity.COMMON
        return when (topRarity) {
            Rarity.COMMON -> RevealTier.COMMON
            Rarity.UNCOMMON -> RevealTier.UNCOMMON
            Rarity.RARE -> RevealTier.RARE
            Rarity.HIDDEN -> RevealTier.WILD
        }
    }

    fun forCreature(creature: Creature): RevealSequence = forTier(tierOf(creature))

    fun forTier(tier: RevealTier): RevealSequence {
        val f = stretch(tier)
        val wild = tier == RevealTier.WILD
        val burstIntensity = (0.55 + 0.15 * tier.ordinal).coerceAtMost(1.0)

        return RevealSequence(
            tier = tier,
            beats = listOf(
                RevealBeat(
                    RevealPhase.ANTICIPATION, durationMs = (1000 * f).toInt(),
                    intensity = 0.5, sfx = "reveal_build",
                    caption = if (wild) "Something is wrong with this seed…" else "The seed trembles…",
                ),
                // The held breath — audio drops out; the pause is the whole trick.
                RevealBeat(
                    RevealPhase.SILENCE, durationMs = (250 * f).toInt(),
                    intensity = 0.0, sfx = "", caption = "",
                ),
                RevealBeat(
                    RevealPhase.BURST, durationMs = 300,
                    intensity = burstIntensity,
                    sfx = if (wild) "reveal_burst_wild" else "reveal_burst",
                    caption = if (wild) "Something unknown blooms." else "It blooms!",
                ),
                RevealBeat(
                    RevealPhase.SETTLE, durationMs = 900,
                    intensity = 0.3, sfx = "reveal_settle",
                    caption = "Meet your creature.",
                ),
            ),
        )
    }
}
