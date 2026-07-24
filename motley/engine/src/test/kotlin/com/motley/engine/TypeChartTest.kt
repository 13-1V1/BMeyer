package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class TypeChartTest {

    @Test
    fun `triangle cycles correctly`() {
        // EMBER > THORN > TIDE > EMBER
        assertEquals(TypeChart.SUPER_EFFECTIVE, TypeChart.effectiveness(Type.EMBER, Type.THORN))
        assertEquals(TypeChart.SUPER_EFFECTIVE, TypeChart.effectiveness(Type.THORN, Type.TIDE))
        assertEquals(TypeChart.SUPER_EFFECTIVE, TypeChart.effectiveness(Type.TIDE, Type.EMBER))
    }

    @Test
    fun `losing side is not very effective`() {
        assertEquals(TypeChart.NOT_VERY_EFFECTIVE, TypeChart.effectiveness(Type.THORN, Type.EMBER))
        assertEquals(TypeChart.NOT_VERY_EFFECTIVE, TypeChart.effectiveness(Type.TIDE, Type.THORN))
        assertEquals(TypeChart.NOT_VERY_EFFECTIVE, TypeChart.effectiveness(Type.EMBER, Type.TIDE))
    }

    @Test
    fun `same type is neutral`() {
        for (t in listOf(Type.EMBER, Type.THORN, Type.TIDE)) {
            assertEquals(TypeChart.NEUTRAL, TypeChart.effectiveness(t, t))
        }
    }

    @Test
    fun `wild is neutral both ways against everything`() {
        for (t in Type.entries) {
            assertEquals(TypeChart.NEUTRAL, TypeChart.effectiveness(Type.WILD, t))
            assertEquals(TypeChart.NEUTRAL, TypeChart.effectiveness(t, Type.WILD))
        }
    }
}
