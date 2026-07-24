package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InvestmentTest {

    @Test
    fun `winning grants more xp than losing`() {
        assertTrue(BattleReward.xp(won = true, opponentLevel = 10) > BattleReward.xp(won = false, opponentLevel = 10))
    }

    @Test
    fun `tougher opponents are worth more`() {
        assertTrue(BattleReward.xp(true, opponentLevel = 50) > BattleReward.xp(true, opponentLevel = 5))
    }

    @Test
    fun `idle training accrues over time but caps per session`() {
        assertEquals(Training.IDLE_XP_PER_MIN * 30, Training.idleXp(30))
        val capped = Training.idleXp(10_000)
        assertEquals(Training.IDLE_XP_PER_MIN * Training.IDLE_SESSION_CAP_MIN, capped)
    }

    @Test
    fun `puzzle training scales with solves`() {
        assertEquals(Training.PUZZLE_XP_PER_SOLVE * 3, Training.puzzleXp(3))
        assertEquals(0L, Training.puzzleXp(0))
    }

    @Test
    fun `a catalyst must grant xp`() {
        assertFailsWith<IllegalArgumentException> { Catalyst("Dud", 0) }
        assertEquals(200L, Catalyst("Sunburst", 200).xp)
    }

    @Test
    fun `investment paths feed the same leveling loop`() {
        // A training session and a catalyst both push a fresh creature's level up.
        var p = CreatureProgress()
        p = Leveling.gainXp(p, Training.puzzleXp(solves = 5))
        p = Leveling.gainXp(p, Catalyst("Sunburst", 200).xp)
        assertTrue(p.level > 1, "combined investment should have leveled the creature")
    }
}
