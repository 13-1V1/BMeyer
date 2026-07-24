package com.motley.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SynergyBattleTest {

    private fun attacksUntilFirstBurst(events: List<BattleEvent>, side: Side): Int {
        var attacks = 0
        for (e in events) {
            if (e is BattleEvent.Attack && e.side == side) attacks++
            if (e is BattleEvent.Burst && e.side == side) return attacks
        }
        return Int.MAX_VALUE // never burst
    }

    @Test
    fun `ACTS_FIRST overrides raw Speed`() {
        // A slow creature (ancient + giant) is given Skyborne-style priority via a custom synergy;
        // it should still strike before a much faster opponent.
        val priorityFactory = CreatureFactory(
            listOf(Synergy("TestPriority", setOf("ancient", "giant"), "acts first",
                setOf(SynergyEffect.ACTS_FIRST)))
        )
        val slowButFirst = Battler("Slowpoke",
            priorityFactory.grow(listOf(StarterEssences.ANCIENT, StarterEssences.GIANT)))
        val speedster = Battler("Zippy",
            priorityFactory.grow(listOf(StarterEssences.FEATHERED, StarterEssences.MIST)))
        assertTrue(slowButFirst.spd < speedster.spd, "test setup: the priority creature must be slower")

        val result = BattleResolver(Random(1)).resolve(listOf(slowButFirst), listOf(speedster))
        val firstAttacker = result.events.filterIsInstance<BattleEvent.Attack>().first().side
        assertEquals(Side.A, firstAttacker, "the ACTS_FIRST creature should strike first despite lower Speed")
    }

    @Test
    fun `FORTIFY hardens Defence as the creature attacks`() {
        val mech = Battler("Mech", StarterEssences.factory.grow(   // robo + mad = War Machine
            listOf(StarterEssences.ROBO, StarterEssences.MAD)))
        assertTrue(mech.has(SynergyEffect.FORTIFY), "War Machine should grant FORTIFY")
        val startDef = mech.def

        val wall = Battler("Wall", StarterEssences.factory.grow(
            listOf(StarterEssences.ANCIENT, StarterEssences.GIANT)), CreatureProgress(level = 25))
        BattleResolver(Random(4)).resolve(listOf(mech), listOf(wall))

        assertTrue(mech.def > startDef, "attacking should have raised Defence (was $startDef, now ${mech.def})")
    }

    @Test
    fun `CONTAGION is granted by the Miasma pairing`() {
        val plague = StarterEssences.factory.grow(listOf(StarterEssences.FUNGAL, StarterEssences.VENOM))
        assertTrue(SynergyEffect.CONTAGION in plague.synergies.flatMap { it.effects },
            "fungal + venom should unlock Miasma / CONTAGION")
    }

    @Test
    fun `BRACED softens Momentum burst follow-ups`() {
        // Same battle, two configs: with the reduction a Titan takes less burst-hit damage.
        fun titanDamageTaken(reduction: Double): Int {
            val burster = Battler("Zap",  // Overload -> FAST_MOMENTUM -> bursts; EMBER vs THORN = super
                StarterEssences.factory.grow(listOf(StarterEssences.MAD, StarterEssences.ELECTRIC)),
                CreatureProgress(level = 10))
            val titan = Battler("Titan",  // ancient + giant = Titan / BRACED, and very tanky
                StarterEssences.factory.grow(listOf(StarterEssences.ANCIENT, StarterEssences.GIANT)),
                CreatureProgress(level = 60))
            assertTrue(titan.has(SynergyEffect.BRACED), "ancient + giant should grant BRACED")
            val cfg = BattleConfig(bracedReduction = reduction, maxRounds = 8)
            val result = BattleResolver(Random(1), cfg).resolve(listOf(burster), listOf(titan))
            return result.events.filterIsInstance<BattleEvent.Attack>()
                .filter { it.defender == "Titan" }.sumOf { it.damage }
        }
        assertTrue(
            titanDamageTaken(0.5) < titanDamageTaken(1.0),
            "the BRACED reduction should lower total burst damage taken",
        )
    }

    @Test
    fun `FAST_MOMENTUM reaches a burst in fewer hits`() {
        // Overload (mad + electric) has FAST_MOMENTUM; an ordinary EMBER creature does not. Both
        // pound a durable EMBER wall (neutral hits), so only the Momentum rate differs.
        fun wall() = Battler("Wall", StarterEssences.factory.grow(
            listOf(StarterEssences.ROBO, StarterEssences.GIANT)), CreatureProgress(level = 40))

        val overload = Battler("Overload", StarterEssences.factory.grow(
            listOf(StarterEssences.MAD, StarterEssences.ELECTRIC)), CreatureProgress(level = 40))
        val control = Battler("Plain", StarterEssences.factory.grow(
            listOf(StarterEssences.EMBER, StarterEssences.SOLAR)), CreatureProgress(level = 40))

        val overloadBurst = attacksUntilFirstBurst(
            BattleResolver(Random(2)).resolve(listOf(overload), listOf(wall())).events, Side.A)
        val controlBurst = attacksUntilFirstBurst(
            BattleResolver(Random(2)).resolve(listOf(control), listOf(wall())).events, Side.A)

        assertTrue(
            overloadBurst < controlBurst,
            "Overload should burst in fewer hits ($overloadBurst) than a plain creature ($controlBurst)",
        )
    }
}
