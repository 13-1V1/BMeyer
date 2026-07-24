package com.motley.engine

/**
 * **Momentum** — the tempo mechanic (GDD depth axis 1), inspired by SMT's Press Turn. Exploiting a
 * weakness earns tempo that builds toward an extra action; hitting a resist bleeds it. This is what
 * lets a *small* type chart feel deep: exploiting it compounds, so a well-sequenced turn snowballs
 * and a sloppy one stalls — skill and board-reading matter more than raw level.
 *
 * Kept as a pure scoring function so the eventual battle layer (and its tests) stay deterministic.
 */
object Momentum {
    const val START = 0
    const val BURST_THRESHOLD = 4
    const val GAIN_SUPER = 2
    const val GAIN_NEUTRAL = 1
    const val LOSS_RESISTED = 1

    /** Momentum delta for a single hit of the given [effectiveness] (see [TypeChart]). */
    fun delta(effectiveness: Double): Int = when {
        effectiveness > TypeChart.NEUTRAL -> GAIN_SUPER
        effectiveness < TypeChart.NEUTRAL -> -LOSS_RESISTED
        else -> GAIN_NEUTRAL
    }

    /** Fold a hit into a running [current] momentum total, clamped so it never drops below zero. */
    fun accrue(current: Int, effectiveness: Double): Int =
        (current + delta(effectiveness)).coerceAtLeast(0)

    /** Whether [momentum] has reached the burst threshold (spend it for an extra action). */
    fun triggersBurst(momentum: Int): Boolean = momentum >= BURST_THRESHOLD
}
