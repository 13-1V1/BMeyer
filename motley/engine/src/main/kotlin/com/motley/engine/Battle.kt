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
 * A creature *in battle* — a [Creature] plus its [progress] and mutable current HP. The battler
 * fights with its **effective** (leveled + Bloomed) stats, so raising a creature actually makes it
 * hit harder and last longer — this is where the growth loop pays off. Battlers are throwaway
 * per-battle state; the underlying [Creature] is never mutated, so a creature can fight any number
 * of times without wear.
 */
class Battler(
    val name: String,
    val creature: Creature,
    val progress: CreatureProgress = CreatureProgress(),
) {
    /** Base kit scaled by level and Blooms (see [Growth]); a level-1 creature fights at base. */
    val stats: Stats = Growth.effectiveStats(creature.stats, progress)

    var currentHp: Int = stats.hp
        private set

    /** The mechanical effects this creature's active synergies grant in battle. */
    val synergyEffects: Set<SynergyEffect> = creature.synergies.flatMap { it.effects }.toSet()

    /** The status conditions this creature can inflict on hit (from its abilities). */
    val onHitStatuses: List<AbilityStatus> = Abilities.onHitFor(creature)

    private val statuses = LinkedHashMap<Status, Int>() // status -> remaining turns

    val level: Int get() = progress.level
    val type: Type get() = creature.type
    val atk: Int get() = stats.atk
    val def: Int get() = stats.def
    val isFainted: Boolean get() = currentHp <= 0

    val baseSpd: Int get() = stats.spd
    /** Speed after status: CHILL halves it, which can flip the turn order. */
    val spd: Int get() = if (Status.CHILL in statuses) (stats.spd * StatusRules.CHILL_SPD_FACTOR).toInt() else stats.spd

    val activeStatuses: Set<Status> get() = statuses.keys.toSet()

    fun has(effect: SynergyEffect): Boolean = effect in synergyEffects
    fun has(status: Status): Boolean = status in statuses

    /** Inflict a [status] for [duration] turns (refreshes to the longer of the two if already present). */
    internal fun inflict(status: Status, duration: Int) {
        statuses[status] = max(statuses[status] ?: 0, duration)
    }

    /** Apply [amount] damage, floored at 0 HP. Returns true if this hit caused a faint. */
    internal fun takeDamage(amount: Int): Boolean {
        val wasStanding = !isFainted
        currentHp = max(0, currentHp - amount)
        return wasStanding && isFainted
    }

    /**
     * End-of-round status tick: applies damage-over-time (Wild creatures take extra — the D4
     * counter), then counts every active status down one turn, dropping expired ones. Returns the
     * total DoT dealt (already applied) and whether it caused a faint.
     */
    internal fun tickStatuses(): StatusTick {
        if (statuses.isEmpty()) return StatusTick(0, false)
        var damage = 0
        for (status in statuses.keys) {
            val frac = StatusRules.dotFraction(status)
            if (frac <= 0.0) continue
            var d = stats.hp * frac
            if (type == Type.WILD) d *= StatusRules.WILD_DOT_MULTIPLIER
            damage += max(1, d.roundToInt())
        }
        val fainted = if (damage > 0) takeDamage(damage) else false
        val iterator = statuses.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value <= 1) iterator.remove() else entry.setValue(entry.value - 1)
        }
        return StatusTick(damage, fainted)
    }
}

/** Result of one [Battler.tickStatuses] call. */
data class StatusTick(val damage: Int, val fainted: Boolean)

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

    /** [side]'s creature was afflicted with [status]. */
    data class StatusInflicted(val side: Side, val name: String, val status: Status) : BattleEvent

    /** [side]'s creature took damage-over-time from its statuses. */
    data class StatusDamage(val side: Side, val name: String, val damage: Int) : BattleEvent

    /** [side]'s creature was paralyzed and lost its action. */
    data class ActionSkipped(val side: Side, val name: String, val status: Status) : BattleEvent

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
