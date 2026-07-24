package com.motley.engine

/**
 * A move a creature can use on its turn. [power] scales the base damage (a heavy strike hits for
 * more; a status-focused move trades damage for a reliable affliction). [inflicts] is the status it
 * can apply on hit, if any. Moves take the *creature's* type, so the type triangle still governs
 * effectiveness — moves add the *decision* of hit-hard vs. apply-status, not a second type axis.
 */
data class Move(
    val name: String,
    val power: Double,
    val inflicts: AbilityStatus? = null,
) {
    val isStatusMove: Boolean get() = inflicts != null
}

/**
 * The move catalog. Every creature gets a basic [STRIKE] plus one signature move per ability seed
 * on its essences — so a creature's kit (and the choices it offers in battle) come straight from
 * the essences the player combined.
 */
object Moves {
    val STRIKE = Move("Strike", power = 1.0)

    /** ability seed -> its signature move. Status moves reuse the [Abilities] infliction table. */
    private val powerByAbility: Map<String, Double> = mapOf(
        "Ignite" to 0.9, "Shock" to 0.9, "Chill" to 0.9, "Envenom" to 0.85, "Spore" to 0.8,
        "Frenzy" to 1.15, "Overclock" to 1.25, "Flare" to 1.2, "Maul" to 1.2, "Stomp" to 1.15,
        "Barbs" to 1.0, "Gust" to 1.05, "Endure" to 0.7, "Veil" to 0.9, "Douse" to 1.0,
        "Pressure" to 1.15, "Squall" to 1.1, "Unmake" to 1.3, "Lucid" to 1.25, "Corrupt" to 1.3,
        "Erupt" to 1.2, "Overheat" to 1.0, "Entangle" to 0.85, "Freeze" to 0.9, "Fracture" to 1.3,
    )

    fun forAbility(ability: String): Move? {
        val power = powerByAbility[ability] ?: return null
        return Move(ability, power, inflicts = Abilities.onHit[ability])
    }

    /** A creature's full movepool: Strike first, then a signature move for each known ability. */
    fun forCreature(creature: Creature): List<Move> =
        listOf(STRIKE) + creature.abilities.mapNotNull { forAbility(it) }
}

/** Chooses which move a creature uses on its turn. The UI will implement this for the player. */
fun interface MovePolicy {
    fun choose(attacker: Battler, defender: Battler, moves: List<Move>): Move
}

/**
 * The default AI: score every move and take the best, deterministically (no RNG, so battles stay
 * reproducible). Score = expected damage (power × type effectiveness) plus a bonus for landing a
 * *new* status the defender doesn't already have — so it hits for damage when that's best, and
 * reaches for a status move exactly when the affliction is worth more than the raw hit.
 */
object BasicAi : MovePolicy {
    override fun choose(attacker: Battler, defender: Battler, moves: List<Move>): Move {
        val eff = TypeChart.effectiveness(attacker.type, defender.type)
        return moves.maxByOrNull { score(it, eff, defender) } ?: Moves.STRIKE
    }

    private fun score(move: Move, effectiveness: Double, defender: Battler): Double {
        val damage = move.power * effectiveness
        val statusValue = move.inflicts
            ?.takeIf { !defender.has(it.status) }
            ?.let { 0.6 * it.chance }
            ?: 0.0
        return damage + statusValue
    }
}
