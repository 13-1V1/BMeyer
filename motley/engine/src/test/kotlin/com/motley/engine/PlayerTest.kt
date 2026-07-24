package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerTest {

    private fun starter() = Player()
        .collect("mad").collect("electric").collect("robo").collect("thorn").collect("ancient")

    @Test
    fun `collecting requires a real essence`() {
        assertFailsWith<IllegalArgumentException> { Player().collect("banana") }
    }

    @Test
    fun `growing requires collected essences and at least two`() {
        val p = Player().collect("mad")
        assertFailsWith<IllegalArgumentException> { p.grow("Solo", listOf("mad")) }           // too few
        assertFailsWith<IllegalArgumentException> { p.grow("Thief", listOf("mad", "electric")) } // electric not owned
    }

    @Test
    fun `grown creatures get unique increasing serials`() {
        val p = starter().grow("Rusty", listOf("mad", "robo")).grow("Sparky", listOf("mad", "electric"))
        val serials = p.roster.map { it.serial }
        assertEquals(listOf(1, 2), serials)
        assertEquals(3, p.nextSerial)
        assertEquals("Rusty", p.bySerial(1)?.nickname)
        assertNull(p.bySerial(99))
    }

    @Test
    fun `awarding xp levels the right creature`() {
        val p = starter().grow("Rusty", listOf("mad", "robo")).awardXp(1, 10_000)
        assertTrue(p.bySerial(1)!!.progress.level > 1)
    }

    @Test
    fun `a creature remembers its recipe and identity (signal)`() {
        val c = starter().grow("Rusty", listOf("mad", "robo")).bySerial(1)!!
        assertEquals(listOf("mad", "robo"), c.recipe)
        assertEquals(1, c.serial)
        assertEquals("Rusty", c.nickname)
    }

    @Test
    fun `nicknames cannot break the save format`() {
        assertFailsWith<IllegalArgumentException> {
            OwnedCreature(1, "bad;name", StarterEssences.factory.grow(listOf(StarterEssences.MAD, StarterEssences.ROBO)))
        }
    }
}
