package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CreatureFactoryTest {

    private val f = StarterEssences.factory
    private fun e(vararg ids: String) = ids.map { StarterEssences.require(it) }

    @Test
    fun `needs at least two essences`() {
        assertFailsWith<IllegalArgumentException> { f.grow(e("mad")) }
        assertFailsWith<IllegalArgumentException> { f.grow(emptyList()) }
    }

    @Test
    fun `stat total always equals the fixed budget`() {
        // Across every essence count from 2 up to the whole catalog, the budget is conserved.
        val ids = StarterEssences.all.map { it.id }
        for (n in 2..ids.size) {
            val creature = f.grow(e(*ids.take(n).toTypedArray()))
            assertEquals(
                CreatureFactory.STAT_BUDGET, creature.stats.total,
                "combining $n essences should still total the budget",
            )
        }
    }

    @Test
    fun `every stat sits at or above the floor`() {
        val creature = f.grow(e("electric", "mad")) // def-weight 0 essence combo
        for (v in creature.stats.asList()) {
            assertTrue(v >= CreatureFactory.STAT_FLOOR, "stat $v below floor")
        }
    }

    @Test
    fun `stats are independent of combination order`() {
        val a = f.grow(e("mad", "robo", "electric"))
        val b = f.grow(e("electric", "mad", "robo"))
        assertEquals(a.stats, b.stats)
        assertEquals(a.type, b.type)
    }

    @Test
    fun `more essences means a flatter spread, not more power`() {
        val specialist = f.grow(e("mad", "electric"))              // attack-heavy, spiky
        val generalist = f.grow(e("mad", "electric", "robo", "ancient", "tide", "feathered"))

        assertEquals(specialist.stats.total, generalist.stats.total) // same budget
        fun range(s: Stats) = s.asList().max() - s.asList().min()
        assertTrue(
            range(specialist.stats) > range(generalist.stats),
            "specialist range ${range(specialist.stats)} should exceed generalist range ${range(generalist.stats)}",
        )
    }

    @Test
    fun `type follows the dominant element`() {
        assertEquals(Type.EMBER, f.grow(e("mad", "electric")).type)     // both EMBER
        assertEquals(Type.THORN, f.grow(e("thorn", "ancient")).type)    // both THORN
        assertEquals(Type.TIDE, f.grow(e("tide", "frost")).type)        // both TIDE
    }

    @Test
    fun `any hidden essence makes the creature Wild`() {
        val c = f.grow(e("void", "mad", "electric"))
        assertEquals(Type.WILD, c.type)
        assertTrue(c.isWild)
    }

    @Test
    fun `hidden essences skew glass-cannon`() {
        val wild = f.grow(e("void", "glitch"))
        assertTrue(wild.stats.atk > wild.stats.def, "Wild creature should hit harder than it defends")
    }

    @Test
    fun `abilities, recipe order and art prompt carry through`() {
        val c = f.grow(e("mad", "robo", "electric"))
        assertEquals(listOf("mad", "robo", "electric"), c.recipe)
        assertEquals(listOf("Frenzy", "Overclock", "Shock"), c.abilities)
        assertTrue(c.artPrompt.startsWith("wild-eyed"))   // "mad" fragment leads
        assertTrue(c.artPrompt.contains("chrome plating"))
    }
}
