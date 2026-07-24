package com.motley.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpponentsTest {

    @Test
    fun `generates a team of the requested size and level`() {
        val team = Opponents.team(Random(1), level = 12, size = 3)
        assertEquals(3, team.size)
        assertTrue(team.all { it.level == 12 })
    }

    @Test
    fun `the same seed generates the same team`() {
        fun recipes(seed: Long) =
            Opponents.team(Random(seed), level = 5).map { it.creature.recipe }
        assertEquals(recipes(99), recipes(99))
    }

    @Test
    fun `wild types are not handed out as fodder`() {
        // Across many rolls, generated enemies never use hidden essences (Wild stays rare/special).
        for (seed in 0L until 20L) {
            val team = Opponents.team(Random(seed), level = 10, size = 3)
            assertTrue(team.none { it.type == Type.WILD }, "seed $seed produced a Wild fodder enemy")
        }
    }

    @Test
    fun `generated opponents can actually battle`() {
        val player = Opponents.team(Random(1), level = 10, namePrefix = "Player")
        val enemy = Opponents.team(Random(2), level = 10, namePrefix = "Enemy")
        val result = BattleResolver(Random(3)).resolve(player, enemy)
        assertTrue(result.rounds >= 1)
        assertEquals(BattleEvent.Victory(result.outcome), result.events.last())
    }
}
