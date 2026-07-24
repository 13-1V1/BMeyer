package com.motley.engine

import kotlin.math.roundToInt

/**
 * The visible life stages of a creature. The art re-composites at each milestone (a Sprout is a
 * cute blob; a Mature form is the striking creature), so players *watch it grow*.
 */
enum class GrowthStage { SPROUT, JUVENILE, MATURE, AWAKENED }

/**
 * A creature's persistent progression — its [level], banked [xp] toward the next level, and how
 * many times it has [blooms] (prestige-reset). Immutable: growth functions return a new copy, so
 * progression is easy to reason about and test.
 */
data class CreatureProgress(
    val level: Int = 1,
    val xp: Long = 0,
    val blooms: Int = 0,
) {
    init {
        require(level >= 1) { "level starts at 1" }
        require(xp >= 0) { "xp cannot be negative" }
        require(blooms >= 0) { "blooms cannot be negative" }
    }

    val stage: GrowthStage get() = Leveling.stageFor(level)
}

/**
 * Leveling rules — the numbers behind "level infinitely, but level 500 is practically unreachable,
 * and raw level is never the whole game" (GDD §7a).
 *
 * Two curves do the work:
 *  - [xpToNext] rises polynomially, so each level costs more than the last — the climb to high
 *    levels is astronomical (tens of thousands of hours), never gated by a hard cap.
 *  - [levelMultiplier] is **asymptotic**: stats grow toward a ceiling with hard diminishing
 *    returns, so a level-500 creature is *meaningfully* but not *absurdly* stronger than a
 *    level-200 one. Power stays in build, synergy, and type matchup — not in the level number.
 */
object Leveling {
    const val BASE_XP = 20L          // xpToNext(1)
    const val MAX_MULTIPLIER = 3.0   // the stat ceiling a creature approaches but never reaches
    const val MULT_SCALE = 60.0      // how quickly it approaches the ceiling
    const val BLOOM_MIN_LEVEL = 50   // you may Bloom once a creature reaches this
    const val BLOOM_BONUS_PER = 0.02 // permanent stat bonus each Bloom grants

    /** XP required to advance from [level] to level + 1. Strictly increasing. */
    fun xpToNext(level: Int): Long = BASE_XP * level.toLong() * level.toLong()

    /** Asymptotic stat multiplier for a given [level]: 1.0 at level 1, approaching MAX_MULTIPLIER. */
    fun levelMultiplier(level: Int): Double =
        1.0 + (MAX_MULTIPLIER - 1.0) * (1.0 - 1.0 / (1.0 + (level - 1) / MULT_SCALE))

    /** Permanent multiplier from having Bloomed [blooms] times. */
    fun bloomBonus(blooms: Int): Double = 1.0 + BLOOM_BONUS_PER * blooms

    fun stageFor(level: Int): GrowthStage = when {
        level < 10 -> GrowthStage.SPROUT
        level < 25 -> GrowthStage.JUVENILE
        level < 50 -> GrowthStage.MATURE
        else -> GrowthStage.AWAKENED
    }

    /** Bank [amount] XP, leveling up as many times as it affords. Pure — returns new progress. */
    fun gainXp(progress: CreatureProgress, amount: Long): CreatureProgress {
        require(amount >= 0) { "xp gain cannot be negative" }
        var level = progress.level
        var xp = progress.xp + amount
        while (xp >= xpToNext(level)) {
            xp -= xpToNext(level)
            level++
        }
        return progress.copy(level = level, xp = xp)
    }

    fun canBloom(progress: CreatureProgress): Boolean = progress.level >= BLOOM_MIN_LEVEL

    /**
     * **Bloom** (prestige rebirth, D11): sacrifice a maxed creature's levels for a permanent bonus.
     * The creature is not lost — it returns to level 1 with one more Bloom, which lifts its stats
     * forever. The endless soft-cap → reset → boost loop that gives infinite leveling a purpose.
     */
    fun bloom(progress: CreatureProgress): CreatureProgress {
        require(canBloom(progress)) { "must reach level $BLOOM_MIN_LEVEL to Bloom" }
        return CreatureProgress(level = 1, xp = 0, blooms = progress.blooms + 1)
    }
}

/**
 * Applies progression to a creature's base kit. Only the *magnitude* of the stats grows — the
 * ratios (the shape the essences authored) are preserved — so leveling never erases the identity a
 * player built. Effective total = base total × level multiplier × bloom bonus.
 */
object Growth {
    fun effectiveStats(base: Stats, progress: CreatureProgress): Stats {
        val m = Leveling.levelMultiplier(progress.level) * Leveling.bloomBonus(progress.blooms)
        return Stats(
            hp = (base.hp * m).roundToInt(),
            atk = (base.atk * m).roundToInt(),
            def = (base.def * m).roundToInt(),
            spd = (base.spd * m).roundToInt(),
        )
    }
}
