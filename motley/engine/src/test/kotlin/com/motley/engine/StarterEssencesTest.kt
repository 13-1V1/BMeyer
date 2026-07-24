package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StarterEssencesTest {

    @Test
    fun `catalog is non-empty with unique ids`() {
        assertTrue(StarterEssences.all.isNotEmpty())
        val ids = StarterEssences.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "essence ids must be unique")
    }

    @Test
    fun `require returns known essences and fails on unknown`() {
        assertEquals("Mad", StarterEssences.require("mad").displayName)
        assertFailsWith<IllegalArgumentException> { StarterEssences.require("nonsense") }
    }

    @Test
    fun `every essence ability maps to a move`() {
        // Guards against adding an essence but forgetting to register its signature move.
        for (essence in StarterEssences.all) {
            val ability = essence.ability ?: continue
            assertTrue(Moves.forAbility(ability) != null, "ability '$ability' (${essence.id}) has no move")
        }
    }

    @Test
    fun `hidden essences are consistently flagged`() {
        val hidden = StarterEssences.all.filter { it.hidden }
        assertTrue(hidden.isNotEmpty(), "there should be some hidden essences")
        for (h in hidden) assertEquals(Rarity.HIDDEN, h.rarity)
    }

    @Test
    fun `every essence can be grown into a valid creature`() {
        // Pair each essence with a common one and confirm the factory produces a legal creature.
        val partner = StarterEssences.require("thorn")
        for (essence in StarterEssences.all) {
            if (essence.id == partner.id) continue
            val c = StarterEssences.factory.grow(listOf(essence, partner))
            assertEquals(CreatureFactory.STAT_BUDGET, c.stats.total)
            assertTrue(c.stats.asList().all { it >= CreatureFactory.STAT_FLOOR })
        }
    }
}
