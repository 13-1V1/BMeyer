package com.motley.engine

/**
 * A grown creature — the output of [CreatureFactory.grow].
 *
 * Beyond its kit, a creature carries **signal**: the exact [recipe] it was grown from. That is the
 * design's anti-slop moat — a creature always reads as an authored artifact ("Mad + Robo +
 * Electric, grown by you"), never an anonymous random pull. Player-facing identity (nickname,
 * serial number, creator, birth date) is attached at a higher layer around this core.
 */
data class Creature(
    val type: Type,
    val stats: Stats,
    val abilities: List<String>,
    val synergies: List<Synergy>,
    val artPrompt: String,
    val recipe: List<String>,
) {
    val isWild: Boolean get() = type == Type.WILD
}
