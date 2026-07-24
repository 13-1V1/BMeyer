package com.motley.engine

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Resolves a **3v3 turn-based battle** — the other half of the core loop from [CreatureFactory].
 *
 * Format: singles with a bench of three. Each side's first standing creature is *active*; when it
 * faints the next steps in; a team loses when all three have fainted. Within a round both actives
 * act in SPD order (ties favour Side A, deterministically).
 *
 * The two depth axes from the design are live here:
 *  - **[TypeChart]** effectiveness scales damage (exploit weaknesses).
 *  - **[Momentum]** accrues per side as you land effective hits; on a burst the active creature
 *    takes an immediate *extra* action — so exploiting the type chart compounds.
 *
 * Randomness (damage roll + crits) is injected via [Random], so a fixed seed makes a battle fully
 * deterministic and reproducible in tests.
 */
class BattleResolver(
    private val random: Random = Random.Default,
    private val config: BattleConfig = BattleConfig(),
    private val policy: MovePolicy = BasicAi,
) {
    fun resolve(teamA: List<Battler>, teamB: List<Battler>): BattleResult {
        require(teamA.isNotEmpty() && teamB.isNotEmpty()) { "both teams need at least one creature" }

        val teams = mapOf(Side.A to teamA, Side.B to teamB)
        val momentum = mutableMapOf(Side.A to Momentum.START, Side.B to Momentum.START)
        val events = mutableListOf<BattleEvent>()

        fun active(side: Side): Battler? = teams.getValue(side).firstOrNull { !it.isFainted }
        fun defeated(side: Side): Boolean = active(side) == null

        var rounds = 0
        while (rounds < config.maxRounds && !defeated(Side.A) && !defeated(Side.B)) {
            rounds++
            val order = turnOrder(active(Side.A)!!, active(Side.B)!!)
            for (side in order) {
                if (defeated(Side.A) || defeated(Side.B)) break
                takeAction(side, ::active, momentum, events)
            }
            // End of round: statuses tick (damage-over-time, then durations count down).
            for (side in listOf(Side.A, Side.B)) {
                val creature = active(side) ?: continue
                val tick = creature.tickStatuses()
                if (tick.damage > 0) events += BattleEvent.StatusDamage(side, creature.name, tick.damage)
                if (tick.fainted) events += BattleEvent.Faint(side, creature.name)
            }
        }

        val outcome = when {
            defeated(Side.A) -> Outcome.B_WINS
            defeated(Side.B) -> Outcome.A_WINS
            else -> decideByHp(teamA, teamB) // hit the round cap — settle on remaining HP
        }
        events += BattleEvent.Victory(outcome)

        return BattleResult(
            outcome = outcome,
            rounds = rounds,
            survivorsA = teamA.count { !it.isFainted },
            survivorsB = teamB.count { !it.isFainted },
            events = events,
        )
    }

    /**
     * Turn order: an `ACTS_FIRST` synergy (e.g. Skyborne) takes priority over raw Speed; otherwise
     * the faster creature goes first. Ties break in Side A's favour so results stay deterministic.
     */
    private fun turnOrder(a: Battler, b: Battler): List<Side> {
        val aFirst = a.has(SynergyEffect.ACTS_FIRST)
        val bFirst = b.has(SynergyEffect.ACTS_FIRST)
        return when {
            aFirst != bFirst -> if (aFirst) listOf(Side.A, Side.B) else listOf(Side.B, Side.A)
            a.spd >= b.spd -> listOf(Side.A, Side.B)
            else -> listOf(Side.B, Side.A)
        }
    }

    /**
     * One creature's action for the round: a base attack, plus an extra attack each time a Momentum
     * burst fires. Bounded — a burst resets momentum to zero, and a single follow-up hit can't
     * immediately rebuild it to the threshold, so the chain always terminates.
     */
    private fun takeAction(
        side: Side,
        active: (Side) -> Battler?,
        momentum: MutableMap<Side, Int>,
        events: MutableList<BattleEvent>,
    ) {
        val attacker = active(side) ?: return

        // Paralysis can steal the whole action (base attack and any burst follow-ups).
        if (attacker.has(Status.PARALYZE) && random.nextDouble() < StatusRules.PARALYZE_SKIP_CHANCE) {
            events += BattleEvent.ActionSkipped(side, attacker.name, Status.PARALYZE)
            return
        }

        var isBurstFollowUp = false // the first swing is normal; extra swings after a burst are not
        while (true) {
            val defender = active(side.opponent) ?: break
            val triggeredBurst = attackOnce(side, attacker, defender, isBurstFollowUp, momentum, events)
            if (!triggeredBurst || active(side.opponent) == null) break
            isBurstFollowUp = true // the extra action a burst grants is a follow-up hit
        }

        // FORTIFY (War Machine): a turn spent attacking hardens the creature's Defence.
        if (attacker.has(SynergyEffect.FORTIFY)) attacker.fortify(config.fortifyPerAttack)
    }

    /** Resolves a single hit. Returns true if it triggered a Momentum burst (an extra action). */
    private fun attackOnce(
        side: Side,
        attacker: Battler,
        defender: Battler,
        isBurstFollowUp: Boolean,
        momentum: MutableMap<Side, Int>,
        events: MutableList<BattleEvent>,
    ): Boolean {
        val move = policy.choose(attacker, defender, attacker.moves)
        val eff = TypeChart.effectiveness(attacker.type, defender.type)
        val variance = config.varianceMin + random.nextDouble() * (1.0 - config.varianceMin)
        val crit = random.nextDouble() < config.critChance
        var dmg = Damage.compute(attacker.atk, defender.def, eff, config, variance, crit, move.power)

        // BRACED (Titan): the extra hit a Momentum burst grants is softened.
        if (isBurstFollowUp && defender.has(SynergyEffect.BRACED)) {
            dmg = (dmg * config.bracedReduction).roundToInt().coerceAtLeast(1)
        }

        val fainted = defender.takeDamage(dmg)
        events += BattleEvent.Attack(side, attacker.name, defender.name, move.name, dmg, eff, crit)
        if (fainted) events += BattleEvent.Faint(side.opponent, defender.name)

        // The chosen move's status (if any) lands on a still-standing target; CONTAGION (Miasma)
        // makes it linger extra turns.
        val inflicts = move.inflicts
        if (!fainted && inflicts != null && random.nextDouble() < inflicts.chance) {
            val extra = if (attacker.has(SynergyEffect.CONTAGION)) config.contagionExtraDuration else 0
            defender.inflict(inflicts.status, inflicts.duration + extra)
            events += BattleEvent.StatusInflicted(side.opponent, defender.name, inflicts.status)
        }

        // Momentum: FAST_MOMENTUM (Overload) adds a point on top of any gaining hit.
        val base = Momentum.delta(eff)
        val gain = if (attacker.has(SynergyEffect.FAST_MOMENTUM) && base > 0) base + 1 else base
        momentum[side] = (momentum.getValue(side) + gain).coerceAtLeast(0)
        if (Momentum.triggersBurst(momentum.getValue(side))) {
            momentum[side] = Momentum.START
            events += BattleEvent.Burst(side, attacker.name)
            return true
        }
        return false
    }

    private fun decideByHp(teamA: List<Battler>, teamB: List<Battler>): Outcome {
        val hpA = teamA.sumOf { it.currentHp }
        val hpB = teamB.sumOf { it.currentHp }
        return when {
            hpA > hpB -> Outcome.A_WINS
            hpB > hpA -> Outcome.B_WINS
            else -> Outcome.DRAW
        }
    }
}
