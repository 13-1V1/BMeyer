package com.motley.engine

enum class Stat { HP, ATK, DEF, SPD }

/**
 * A creature's four base stats.
 *
 * In Motley the *total* is fixed by design — a creature built from more essences is wilder, not
 * stronger — so [total] is an invariant [CreatureFactory] guarantees (it always equals
 * [CreatureFactory.STAT_BUDGET]). What changes with essence count is the *shape* of the spread.
 */
data class Stats(val hp: Int, val atk: Int, val def: Int, val spd: Int) {
    val total: Int get() = hp + atk + def + spd

    operator fun get(stat: Stat): Int = when (stat) {
        Stat.HP -> hp
        Stat.ATK -> atk
        Stat.DEF -> def
        Stat.SPD -> spd
    }

    fun asList(): List<Int> = listOf(hp, atk, def, spd)
}

/**
 * Relative, non-negative contribution shape across the four stats. An essence declares the *shape*
 * of what it brings (e.g. "mostly attack"), never absolute numbers — [CreatureFactory] normalizes
 * the combined shape against a fixed budget. That's what keeps balance under the designer's control
 * regardless of how many essences a player fuses.
 */
data class StatWeights(val hp: Double, val atk: Double, val def: Double, val spd: Double) {
    init {
        require(hp >= 0 && atk >= 0 && def >= 0 && spd >= 0) { "weights must be non-negative" }
        require(hp + atk + def + spd > 0) { "at least one weight must be positive" }
    }

    val sum: Double get() = hp + atk + def + spd

    operator fun get(stat: Stat): Double = when (stat) {
        Stat.HP -> hp
        Stat.ATK -> atk
        Stat.DEF -> def
        Stat.SPD -> spd
    }
}
