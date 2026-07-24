package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProgressionTest {

    @Test
    fun `each level costs more than the last`() {
        for (level in 1..499) {
            assertTrue(Leveling.xpToNext(level + 1) > Leveling.xpToNext(level))
        }
    }

    @Test
    fun `level multiplier starts at one and is bounded`() {
        assertEquals(1.0, Leveling.levelMultiplier(1), 1e-9)
        assertTrue(Leveling.levelMultiplier(500) < Leveling.MAX_MULTIPLIER)
        assertTrue(Leveling.levelMultiplier(90) > Leveling.levelMultiplier(40)) // still monotonic
    }

    @Test
    fun `stat gains diminish hard at high levels`() {
        val earlyGain = Leveling.levelMultiplier(40) - Leveling.levelMultiplier(1)
        val lateGain = Leveling.levelMultiplier(500) - Leveling.levelMultiplier(200)
        assertTrue(
            lateGain < earlyGain,
            "L200->500 gain ($lateGain) should be far smaller than L1->40 ($earlyGain)",
        )
    }

    @Test
    fun `growth stages track level thresholds`() {
        assertEquals(GrowthStage.SPROUT, Leveling.stageFor(1))
        assertEquals(GrowthStage.JUVENILE, Leveling.stageFor(10))
        assertEquals(GrowthStage.MATURE, Leveling.stageFor(25))
        assertEquals(GrowthStage.AWAKENED, Leveling.stageFor(50))
    }

    @Test
    fun `banking exactly enough xp levels up once`() {
        val p = Leveling.gainXp(CreatureProgress(level = 1), Leveling.xpToNext(1))
        assertEquals(2, p.level)
        assertEquals(0L, p.xp)
    }

    @Test
    fun `a big xp haul levels up multiple times and keeps the remainder`() {
        val p = Leveling.gainXp(CreatureProgress(), 10_000)
        assertTrue(p.level > 5)
        assertTrue(p.xp in 0 until Leveling.xpToNext(p.level))
    }

    @Test
    fun `effective stats scale magnitude but preserve the authored shape`() {
        val base = StarterEssences.factory.grow(
            listOf(StarterEssences.MAD, StarterEssences.ELECTRIC)
        ).stats // attack is the standout stat
        val leveled = Growth.effectiveStats(base, CreatureProgress(level = 20))

        assertTrue(leveled.total > base.total, "leveling should raise the total")
        // ATK was the highest base stat; it must still be the highest after scaling.
        assertEquals(base.asList().indexOf(base.asList().max()), leveled.asList().indexOf(leveled.asList().max()))
    }

    @Test
    fun `blooming requires a mature creature and grants a permanent bonus`() {
        assertFailsWith<IllegalArgumentException> { Leveling.bloom(CreatureProgress(level = 10)) }

        val ready = CreatureProgress(level = Leveling.BLOOM_MIN_LEVEL, xp = 500)
        val bloomed = Leveling.bloom(ready)
        assertEquals(1, bloomed.level)
        assertEquals(0L, bloomed.xp)
        assertEquals(1, bloomed.blooms)

        val base = Stats(60, 60, 60, 60)
        val before = Growth.effectiveStats(base, CreatureProgress(level = 1, blooms = 0))
        val after = Growth.effectiveStats(base, CreatureProgress(level = 1, blooms = 1))
        assertTrue(after.total > before.total, "a Bloom should permanently raise stats")
    }

    @Test
    fun `a clever low-level hit beats a brute-force high-level one`() {
        // The heart of GDD 7a: type advantage and smart play should out-do raw grinding.
        val base = Stats(hp = 60, atk = 100, def = 50, spd = 60)
        val cfg = BattleConfig()

        val cleverAtk = Growth.effectiveStats(base, CreatureProgress(level = 40)).atk
        val bruteAtk = Growth.effectiveStats(base, CreatureProgress(level = 90)).atk

        val cleverSuperHit = Damage.compute(cleverAtk, 60, TypeChart.SUPER_EFFECTIVE, cfg)
        val bruteNeutralHit = Damage.compute(bruteAtk, 60, TypeChart.NEUTRAL, cfg)

        assertTrue(
            cleverSuperHit > bruteNeutralHit,
            "a level-40 super-effective hit ($cleverSuperHit) should beat a level-90 neutral one ($bruteNeutralHit)",
        )
    }
}
