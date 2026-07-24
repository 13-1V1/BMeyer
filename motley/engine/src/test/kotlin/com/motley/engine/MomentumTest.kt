package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MomentumTest {

    @Test
    fun `hitting a weakness gains the most tempo`() {
        assertEquals(Momentum.GAIN_SUPER, Momentum.delta(TypeChart.SUPER_EFFECTIVE))
        assertEquals(Momentum.GAIN_NEUTRAL, Momentum.delta(TypeChart.NEUTRAL))
        assertEquals(-Momentum.LOSS_RESISTED, Momentum.delta(TypeChart.NOT_VERY_EFFECTIVE))
    }

    @Test
    fun `momentum never drops below zero`() {
        assertEquals(0, Momentum.accrue(0, TypeChart.NOT_VERY_EFFECTIVE))
    }

    @Test
    fun `exploiting weaknesses snowballs toward a burst`() {
        var m = Momentum.START
        assertFalse(Momentum.triggersBurst(m))
        repeat(2) { m = Momentum.accrue(m, TypeChart.SUPER_EFFECTIVE) } // +2, +2 = 4
        assertTrue(Momentum.triggersBurst(m))
    }
}
