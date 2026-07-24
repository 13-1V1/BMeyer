package com.motley.engine

/**
 * Status conditions — the tactical layer of battle. Some tick damage over time, some hobble a
 * creature's speed or steal its turn. Crucially, they are the **counter to Wild glass cannons**
 * (D4): a Wild creature has no type weakness, but it takes extra damage-over-time (see
 * [StatusRules.WILD_DOT_MULTIPLIER]), so a status-teching team melts one before it can swing.
 */
enum class Status { BURN, POISON, CHILL, PARALYZE }

object StatusRules {
    const val BURN_DOT = 1.0 / 16    // fraction of max HP per tick
    const val POISON_DOT = 1.0 / 8
    const val CHILL_SPD_FACTOR = 0.5 // CHILL halves effective Speed
    const val PARALYZE_SKIP_CHANCE = 0.25
    const val WILD_DOT_MULTIPLIER = 2.0 // D4: Wild types are hard-countered by status damage

    fun dotFraction(status: Status): Double = when (status) {
        Status.BURN -> BURN_DOT
        Status.POISON -> POISON_DOT
        else -> 0.0
    }

    fun isDamaging(status: Status): Boolean = dotFraction(status) > 0.0
}

/** What an ability inflicts when it lands: a [status] with a roll [chance] and a [duration] in turns. */
data class AbilityStatus(val status: Status, val chance: Double, val duration: Int)

/**
 * Maps ability seeds (authored on essences) to the status they can inflict on hit. Data, not logic,
 * so designers can tune infliction without touching the battle resolver.
 */
object Abilities {
    val onHit: Map<String, AbilityStatus> = mapOf(
        "Ignite" to AbilityStatus(Status.BURN, chance = 0.30, duration = 3),      // ember
        "Shock" to AbilityStatus(Status.PARALYZE, chance = 0.25, duration = 2),   // electric
        "Chill" to AbilityStatus(Status.CHILL, chance = 0.35, duration = 3),      // frost
        "Envenom" to AbilityStatus(Status.POISON, chance = 0.35, duration = 4),   // venom
        "Spore" to AbilityStatus(Status.POISON, chance = 0.30, duration = 3),     // fungal
        "Overheat" to AbilityStatus(Status.BURN, chance = 0.35, duration = 3),    // plasma
        "Entangle" to AbilityStatus(Status.CHILL, chance = 0.35, duration = 3),   // root
        "Freeze" to AbilityStatus(Status.CHILL, chance = 0.40, duration = 3),     // glacier
    )
}
