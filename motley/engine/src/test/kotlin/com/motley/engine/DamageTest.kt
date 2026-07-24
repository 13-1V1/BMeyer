package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DamageTest {

    private val cfg = BattleConfig()

    @Test
    fun `effectiveness scales damage monotonically`() {
        val superHit = Damage.compute(100, 50, TypeChart.SUPER_EFFECTIVE, cfg)
        val neutral = Damage.compute(100, 50, TypeChart.NEUTRAL, cfg)
        val resisted = Damage.compute(100, 50, TypeChart.NOT_VERY_EFFECTIVE, cfg)
        assertTrue(superHit > neutral, "super ($superHit) should beat neutral ($neutral)")
        assertTrue(neutral > resisted, "neutral ($neutral) should beat resisted ($resisted)")
    }

    @Test
    fun `damage is never less than one`() {
        assertEquals(1, Damage.compute(atk = 1, def = 999, effectiveness = TypeChart.NOT_VERY_EFFECTIVE, config = cfg))
    }

    @Test
    fun `crits hit harder`() {
        val normal = Damage.compute(100, 50, TypeChart.NEUTRAL, cfg, variance = 1.0, crit = false)
        val crit = Damage.compute(100, 50, TypeChart.NEUTRAL, cfg, variance = 1.0, crit = true)
        assertTrue(crit > normal)
    }

    @Test
    fun `higher attack-to-defence ratio deals more`() {
        val strongVsWeak = Damage.compute(120, 40, TypeChart.NEUTRAL, cfg)
        val weakVsStrong = Damage.compute(40, 120, TypeChart.NEUTRAL, cfg)
        assertTrue(strongVsWeak > weakVsStrong)
    }
}
