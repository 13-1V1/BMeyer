package com.motley.engine

import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatusTest {

    private val f = StarterEssences.factory
    private fun battler(name: String, vararg ids: String) =
        Battler(name, f.grow(ids.map { StarterEssences.require(it) }))

    @Test
    fun `burn deals damage-over-time and then wears off`() {
        val b = battler("Torch", "ember", "thorn")
        b.inflict(Status.BURN, duration = 2)
        val hpStart = b.currentHp

        val first = b.tickStatuses()
        assertTrue(first.damage > 0 && b.currentHp < hpStart)
        assertTrue(b.has(Status.BURN), "still burning after one of two turns")

        b.tickStatuses()
        assertFalse(b.has(Status.BURN), "burn should expire after its duration")
        val settled = b.currentHp
        assertEquals(settled, b.tickStatuses().let { b.currentHp }, "no more DoT once it wears off")
    }

    @Test
    fun `chill halves speed while active`() {
        val b = battler("Frostbit", "beast", "thorn")
        assertEquals(b.baseSpd, b.spd)
        b.inflict(Status.CHILL, duration = 3)
        assertEquals((b.baseSpd * StatusRules.CHILL_SPD_FACTOR).toInt(), b.spd)
    }

    @Test
    fun `wild creatures take extra status damage (the D4 counter)`() {
        val wild = battler("Horror", "void", "mad")   // Type.WILD
        val normal = battler("Beastie", "beast", "thorn")
        assertEquals(Type.WILD, wild.type)

        wild.inflict(Status.BURN, 3)
        normal.inflict(Status.BURN, 3)

        val wildDot = wild.tickStatuses().damage
        val normalDot = normal.tickStatuses().damage
        assertEquals((wild.stats.hp * StatusRules.BURN_DOT * StatusRules.WILD_DOT_MULTIPLIER).roundToInt(), wildDot)
        assertEquals((normal.stats.hp * StatusRules.BURN_DOT).roundToInt(), normalDot)
    }

    @Test
    fun `a status team hard-counters glass-cannon Wild types`() {
        // D4 in a real battle: Wilds have no type weakness, but a DoT-heavy team melts them.
        fun statusTeam() = listOf(
            battler("Venomous", "venom", "fungal"),  // double POISON
            battler("Pyre", "ember", "beast"),        // BURN
            battler("Stinger", "electric", "venom"),  // PARALYZE + POISON
        )
        fun wildTeam() = listOf(
            battler("Void", "void", "mad"),
            battler("Glitch", "glitch", "beast"),
            battler("Dream", "dream", "electric"),
        )
        var wins = 0
        for (seed in 0L until 15L) {
            if (BattleResolver(Random(seed)).resolve(statusTeam(), wildTeam()).outcome == Outcome.A_WINS) wins++
        }
        assertTrue(wins >= 11, "status should reliably counter Wild types (won $wins/15)")
    }
}
