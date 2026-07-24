package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SynergyTest {

    private val f = StarterEssences.factory
    private fun e(vararg ids: String) = ids.map { StarterEssences.require(it) }

    @Test
    fun `synergy needs at least two essences`() {
        assertFailsWith<IllegalArgumentException> { Synergy("Solo", setOf("mad"), "nope") }
    }

    @Test
    fun `isActiveFor requires every listed essence`() {
        val s = Synergy("Overload", setOf("mad", "electric"), "…")
        assertTrue(s.isActiveFor(setOf("mad", "electric", "robo")))
        assertTrue(!s.isActiveFor(setOf("mad", "robo")))
    }

    @Test
    fun `combining the right pair unlocks the combo`() {
        val overload = f.grow(e("mad", "electric")).synergies.map { it.name }
        assertEquals(listOf("Overload"), overload)

        val warMachine = f.grow(e("mad", "robo")).synergies.map { it.name }
        assertEquals(listOf("War Machine"), warMachine)
    }

    @Test
    fun `no matching pair means no synergy`() {
        assertTrue(f.grow(e("tide", "feathered")).synergies.isEmpty())
    }

    @Test
    fun `the new content synergies unlock from their pairings`() {
        assertTrue("Volcano" in f.grow(e("magma", "ember")).synergies.map { it.name })
        assertTrue("Permafrost" in f.grow(e("glacier", "frost")).synergies.map { it.name })
        assertTrue("Overgrowth" in f.grow(e("root", "thorn")).synergies.map { it.name })
    }

    @Test
    fun `every starter synergy references real catalog essences`() {
        for (s in StarterEssences.synergies) {
            for (id in s.requires) {
                assertTrue(id in StarterEssences.byId, "synergy '${s.name}' references unknown essence '$id'")
            }
        }
    }
}
