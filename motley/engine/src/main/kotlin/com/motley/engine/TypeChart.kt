package com.motley.engine

/**
 * The type triangle — the small pure function the design flags as the ideal first piece of code.
 *
 * `EMBER` beats `THORN`, `THORN` beats `TIDE`, `TIDE` beats `EMBER`. [Type.WILD] is neutral both
 * ways: a Wild creature can never be countered by matchup (nor counter one), by design — it lives
 * and dies on stats and abilities. See the GDD's hidden-types section.
 */
object TypeChart {
    const val SUPER_EFFECTIVE = 2.0
    const val NEUTRAL = 1.0
    const val NOT_VERY_EFFECTIVE = 0.5

    /** attacker -> the type it is super-effective against. */
    private val beats = mapOf(
        Type.EMBER to Type.THORN,
        Type.THORN to Type.TIDE,
        Type.TIDE to Type.EMBER,
    )

    /** Damage multiplier an [attacker] type deals into a [defender] type. */
    fun effectiveness(attacker: Type, defender: Type): Double = when {
        attacker == Type.WILD || defender == Type.WILD -> NEUTRAL
        beats[attacker] == defender -> SUPER_EFFECTIVE
        beats[defender] == attacker -> NOT_VERY_EFFECTIVE
        else -> NEUTRAL
    }
}
