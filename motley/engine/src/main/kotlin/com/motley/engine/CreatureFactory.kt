package com.motley.engine

import kotlin.math.roundToInt

/**
 * Grows a [Creature] from a combination of essences under a **fixed stat budget**.
 *
 * The load-bearing design rule (GDD §6): combining more essences makes a creature *wilder, not
 * stronger*. Every creature's stats sum to exactly [STAT_BUDGET], so essence count changes the
 * **shape** of the spread — few essences → a peaked specialist, many → an even generalist — never
 * the total power. Stacking essences is a strategic trade-off, never a power cheat.
 *
 * The engine is deterministic (same essences, in any order, → identical stats and type) and pure
 * Kotlin with no Android dependencies, so it is fully unit-testable on the JVM.
 */
class CreatureFactory(
    private val synergies: List<Synergy> = emptyList(),
) {
    fun grow(essences: List<Essence>): Creature {
        require(essences.size >= MIN_ESSENCES) {
            "a creature needs at least $MIN_ESSENCES essences (got ${essences.size})"
        }

        val ids = essences.map { it.id }
        return Creature(
            type = deriveType(essences),
            stats = distributeBudget(essences),
            abilities = essences.mapNotNull { it.ability },
            synergies = synergies.filter { it.isActiveFor(ids.toSet()) },
            artPrompt = essences.joinToString(", ") { it.artFragment },
            recipe = ids,
        )
    }

    /**
     * Aggregate every essence's relative [StatWeights], then spread [STAT_BUDGET] across the four
     * stats in proportion — after seating a [STAT_FLOOR] under each so nothing is dead. Order-free
     * (pure summation), so the result is independent of the order essences were combined in.
     */
    private fun distributeBudget(essences: List<Essence>): Stats {
        val agg = DoubleArray(4)
        for (e in essences) {
            agg[0] += e.weights.hp
            agg[1] += e.weights.atk
            agg[2] += e.weights.def
            agg[3] += e.weights.spd
        }
        val sum = agg.sum()
        val distributable = STAT_BUDGET - STAT_FLOOR * 4

        val raw = IntArray(4) { i -> STAT_FLOOR + (distributable * (agg[i] / sum)).roundToInt() }
        correctRoundingDrift(raw, agg)
        return Stats(raw[0], raw[1], raw[2], raw[3])
    }

    /**
     * Per-stat rounding can drift the total by a point or two; nudge it back so the
     * `stats.total == STAT_BUDGET` invariant holds exactly. Excess points are shaved from the
     * lowest-weight stats and deficits added to the highest-weight ones, so corrections respect the
     * intended shape and stay deterministic.
     */
    private fun correctRoundingDrift(raw: IntArray, agg: DoubleArray) {
        val byWeightDesc = (0..3).sortedByDescending { agg[it] }
        var drift = raw.sum() - STAT_BUDGET
        while (drift > 0) {
            val idx = byWeightDesc.lastOrNull { raw[it] > STAT_FLOOR } ?: break
            raw[idx]--
            drift--
        }
        while (drift < 0) {
            raw[byWeightDesc.first()]++
            drift++
        }
    }

    /**
     * A creature's type is [Type.WILD] if any hidden essence is present; otherwise it is the
     * element that pulls hardest across the combination (each essence pulls its own element in
     * proportion to its total stat weight). Ties break toward the earlier [Element] declaration,
     * keeping the result deterministic.
     */
    private fun deriveType(essences: List<Essence>): Type {
        if (essences.any { it.hidden }) return Type.WILD

        val pull = DoubleArray(Element.entries.size)
        for (e in essences) pull[e.element.ordinal] += e.weights.sum

        val dominant = Element.entries.maxByOrNull { pull[it.ordinal] }!!
        return when (dominant) {
            Element.EMBER -> Type.EMBER
            Element.THORN -> Type.THORN
            Element.TIDE -> Type.TIDE
        }
    }

    companion object {
        const val MIN_ESSENCES = 2
        const val STAT_BUDGET = 300
        const val STAT_FLOOR = 20
    }
}
