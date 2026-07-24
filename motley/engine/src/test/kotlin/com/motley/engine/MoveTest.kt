package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveTest {

    private val f = StarterEssences.factory
    private fun battler(name: String, vararg ids: String) =
        Battler(name, f.grow(ids.map { StarterEssences.require(it) }))

    @Test
    fun `a movepool is Strike plus a move per ability`() {
        val creature = f.grow(listOf(StarterEssences.MAD, StarterEssences.ELECTRIC)) // Frenzy, Shock
        val moves = Moves.forCreature(creature)
        assertEquals(Moves.STRIKE, moves.first())
        assertTrue(moves.any { it.name == "Frenzy" } && moves.any { it.name == "Shock" })
        assertTrue(moves.first { it.name == "Shock" }.isStatusMove, "Shock should carry a status")
        assertFalse(Moves.STRIKE.isStatusMove)
    }

    @Test
    fun `the AI reaches for a status move when the affliction is worth it`() {
        val attacker = battler("Toxic", "venom", "fungal") // Envenom / Spore both poison
        val foe = battler("Prey", "beast", "thorn")         // neutral matchup, no status yet
        assertTrue(
            BasicAi.choose(attacker, foe, attacker.moves).isStatusMove,
            "with a fresh target, poisoning beats a plain hit",
        )
    }

    @Test
    fun `the AI stops wasting a status move once it has landed`() {
        val attacker = battler("Toxic", "venom", "fungal")
        val foe = battler("Prey", "beast", "thorn")
        foe.inflict(Status.POISON, 3) // already poisoned
        assertFalse(
            BasicAi.choose(attacker, foe, attacker.moves).isStatusMove,
            "no point re-applying poison — hit for damage instead",
        )
    }

    @Test
    fun `move choice is deterministic`() {
        val attacker = battler("Blaze", "mad", "electric")
        val foe = battler("Bulwark", "ancient", "giant")
        assertEquals(
            BasicAi.choose(attacker, foe, attacker.moves),
            BasicAi.choose(attacker, foe, attacker.moves),
        )
    }
}
