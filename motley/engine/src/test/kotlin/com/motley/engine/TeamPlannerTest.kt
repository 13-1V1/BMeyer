package com.motley.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamPlannerTest {

    private fun oneOfEachTypePlayer(): Player = Player()
        .collect("mad").collect("electric").collect("beast").collect("thorn").collect("tide").collect("frost")
        .grow("Ember", listOf("mad", "electric"))   // EMBER  (roster lead by default)
        .grow("Thorny", listOf("beast", "thorn"))   // THORN
        .grow("Tidal", listOf("tide", "frost"))     // TIDE

    @Test
    fun `scouting reports the foe you'll face first`() {
        val enemy = Opponents.team(Random(1), level = 5)
        assertEquals(enemy.first().type, TeamPlanner.scoutLead(enemy))
    }

    @Test
    fun `bestOrder leads with the strongest matchup`() {
        val roster = oneOfEachTypePlayer().roster
        // vs a THORN lead, the EMBER creature (super-effective) should be moved to the front.
        assertEquals(Type.EMBER, TeamPlanner.bestOrder(roster, Type.THORN).first().creature.type)
        // vs a TIDE lead, THORN is the super-effective answer.
        assertEquals(Type.THORN, TeamPlanner.bestOrder(roster, Type.TIDE).first().creature.type)
    }

    @Test
    fun `scouting never wins less than fielding blindly`() {
        val player = oneOfEachTypePlayer()
        var naiveWins = 0
        var scoutWins = 0
        for (seed in 0L until 40L) {
            if (Session.encounter(player, Random(seed), opponentLevel = 3, scout = false).won) naiveWins++
            if (Session.encounter(player, Random(seed), opponentLevel = 3, scout = true).won) scoutWins++
        }
        assertTrue(
            scoutWins >= naiveWins,
            "scouting should never hurt in aggregate (scout $scoutWins vs blind $naiveWins)",
        )
    }
}
