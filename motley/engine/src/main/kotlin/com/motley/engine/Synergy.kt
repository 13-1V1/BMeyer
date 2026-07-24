package com.motley.engine

/**
 * A **synergy** fires when a creature's essences contain every id in [requires]. This is the
 * "team-building = combining ideas" hook (GDD depth axis 2): certain essence pairings unlock a
 * named combo, so *which* essences you fuse — not just how many — shapes how the creature plays.
 */
data class Synergy(
    val name: String,
    val requires: Set<String>,
    val description: String,
) {
    init {
        require(requires.size >= 2) { "a synergy needs at least two essences" }
    }

    fun isActiveFor(essenceIds: Set<String>): Boolean = essenceIds.containsAll(requires)
}
