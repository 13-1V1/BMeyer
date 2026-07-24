package com.motley.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SessionTest {

    private fun playerWithTeam(): Player = Player()
        .collect("mad").collect("electric").collect("ember").collect("solar").collect("beast").collect("thorn")
        .grow("Blaze", listOf("mad", "electric"))
        .grow("Cinder", listOf("ember", "solar"))
        .grow("Fang", listOf("beast", "thorn"))

    @Test
    fun `an encounter needs a creature`() {
        assertFailsWith<IllegalArgumentException> {
            Session.encounter(Player(), Random(1), opponentLevel = 5)
        }
    }

    @Test
    fun `fielded creatures earn xp from an encounter`() {
        val before = playerWithTeam()
        val after = Session.encounter(before, Random(1), opponentLevel = 6).player
        for (serial in 1..3) {
            val xpBefore = before.bySerial(serial)!!.progress
            val xpAfter = after.bySerial(serial)!!.progress
            assertTrue(
                xpAfter.level > xpBefore.level || xpAfter.xp > xpBefore.xp,
                "creature $serial should have gained xp",
            )
        }
    }

    @Test
    fun `the same seed replays the same encounter`() {
        val p = playerWithTeam()
        val a = Session.encounter(p, Random(7), opponentLevel = 6)
        val b = Session.encounter(p, Random(7), opponentLevel = 6)
        assertEquals(a.won, b.won)
        assertEquals(a.battle.events, b.battle.events)
        assertEquals(a.player, b.player)
    }

    @Test
    fun `a full mini-campaign compounds progress`() {
        // Collect -> grow -> fight several encounters -> creatures grow. The whole loop, headless.
        var player = playerWithTeam()
        val startLevelSum = player.roster.sumOf { it.progress.level }
        repeat(5) { i ->
            player = Session.encounter(player, Random(i.toLong()), opponentLevel = 4).player
        }
        val endLevelSum = player.roster.sumOf { it.progress.level }
        assertTrue(endLevelSum > startLevelSum, "five encounters should have leveled the roster")
    }
}
