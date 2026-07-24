package com.motley.engine

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

    /** Faster creature acts first; a tie is broken in Side A's favour so results stay deterministic. */
    private fun turnOrder(a: Battler, b: Battler): List<Side> =
        if (a.spd >= b.spd) listOf(Side.A, Side.B) else listOf(Side.B, Side.A)

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
        do {
            val defender = active(side.opponent) ?: return
            val burst = attackOnce(side, attacker, defender, momentum, events)
        } while (burst && active(side.opponent) != null)
    }

    /** Resolves a single hit. Returns true if it triggered a Momentum burst (an extra action). */
    private fun attackOnce(
        side: Side,
        attacker: Battler,
        defender: Battler,
        momentum: MutableMap<Side, Int>,
        events: MutableList<BattleEvent>,
    ): Boolean {
        val eff = TypeChart.effectiveness(attacker.type, defender.type)
        val variance = config.varianceMin + random.nextDouble() * (1.0 - config.varianceMin)
        val crit = random.nextDouble() < config.critChance
        val dmg = Damage.compute(attacker.atk, defender.def, eff, config, variance, crit)

        val fainted = defender.takeDamage(dmg)
        events += BattleEvent.Attack(side, attacker.name, defender.name, dmg, eff, crit)
        if (fainted) events += BattleEvent.Faint(side.opponent, defender.name)

        momentum[side] = Momentum.accrue(momentum.getValue(side), eff)
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
