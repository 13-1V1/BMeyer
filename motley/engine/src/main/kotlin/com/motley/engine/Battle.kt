package com.motley.engine

import kotlin.math.max
import kotlin.math.roundToInt

/** Which team a creature fights for. */
enum class Side { A, B;
    val opponent: Side get() = if (this == A) B else A
}

/** The result of a battle. */
enum class Outcome { A_WINS, B_WINS, DRAW }

/**
 * A creature *in battle* — a [Creature] plus its mutable current HP. Battlers are throwaway
 * per-battle state; the underlying [Creature] is never mutated, so a creature can fight any number
 * of times without wear.
 */
class Battler(val name: String, val creature: Creature) {
    var currentHp: Int = creature.stats.hp
        private set

    val type: Type get() = creature.type
    val atk: Int get() = creature.stats.atk
    val def: Int get() = creature.stats.def
    val spd: Int get() = creature.stats.spd
    val isFainted: Boolean get() = currentHp <= 0

    /** Apply [amount] damage, floored at 0 HP. Returns true if this hit caused a faint. */
    internal fun takeDamage(amount: Int): Boolean {
        val wasStanding = !isFainted
        currentHp = max(0, currentHp - amount)
        return wasStanding && isFainted
    }
}

/** Structured events, in order — the record a UI can animate and tests can assert against. */
sealed interface BattleEvent {
    data class Attack(
        val side: Side,
        val attacker: String,
        val defender: String,
        val damage: Int,
        val effectiveness: Double,
        val crit: Boolean,
    ) : BattleEvent

    /** [side] is the side whose creature fainted. */
    data class Faint(val side: Side, val name: String) : BattleEvent

    /** A Momentum burst fired for [side] — [name] gets an immediate extra action. */
    data class Burst(val side: Side, val name: String) : BattleEvent

    data class Victory(val outcome: Outcome) : BattleEvent
}

data class BattleResult(
    val outcome: Outcome,
    val rounds: Int,
    val survivorsA: Int,
    val survivorsB: Int,
    val events: List<BattleEvent>,
)

/** Tunable battle constants — balance lives in data, not in the resolver. */
data class BattleConfig(
    val basePower: Int = 12,
    val varianceMin: Double = 0.85,
    val critChance: Double = 1.0 / 16.0,
    val critMultiplier: Double = 1.5,
    val maxRounds: Int = 200,
)

/**
 * The damage formula, factored out as a pure function so it is trivially unit-testable with no RNG
 * (pass `variance = 1.0`, `crit = false` for the deterministic core). Damage scales with the
 * attack/defence ratio and the type [effectiveness] multiplier, and never drops below 1.
 */
object Damage {
    fun compute(
        atk: Int,
        def: Int,
        effectiveness: Double,
        config: BattleConfig,
        variance: Double = 1.0,
        crit: Boolean = false,
    ): Int {
        val critMult = if (crit) config.critMultiplier else 1.0
        val raw = config.basePower * (atk.toDouble() / def) * effectiveness * variance * critMult
        return max(1, raw.roundToInt())
    }
}
