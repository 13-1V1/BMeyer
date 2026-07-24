package com.motley.engine

/**
 * A mechanical effect a synergy grants in battle. Kept as a small closed set the [BattleResolver]
 * understands, so synergies actually *do* something rather than just being flavor text.
 */
enum class SynergyEffect {
    /** Priority: this creature acts before non-priority creatures regardless of SPD. */
    ACTS_FIRST,

    /** This creature builds Momentum faster — each of its effective hits gains extra tempo. */
    FAST_MOMENTUM,
}

/**
 * A **synergy** fires when a creature's essences contain every id in [requires]. This is the
 * "team-building = combining ideas" hook (GDD depth axis 2): certain essence pairings unlock a
 * named combo, so *which* essences you fuse — not just how many — shapes how the creature plays.
 * [effects] are the mechanical payoffs the battle resolver reads.
 */
data class Synergy(
    val name: String,
    val requires: Set<String>,
    val description: String,
    val effects: Set<SynergyEffect> = emptySet(),
) {
    init {
        require(requires.size >= 2) { "a synergy needs at least two essences" }
    }

    fun isActiveFor(essenceIds: Set<String>): Boolean = essenceIds.containsAll(requires)
}
