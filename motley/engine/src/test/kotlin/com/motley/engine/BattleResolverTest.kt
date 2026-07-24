package com.motley.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BattleResolverTest {

    private val f = StarterEssences.factory
    private fun battler(name: String, vararg ids: String) =
        Battler(name, f.grow(ids.map { StarterEssences.require(it) }))

    // An EMBER team (super-effective into THORN) vs a THORN team (resisted into EMBER).
    private fun emberTeam() = listOf(
        battler("Blaze", "mad", "electric"),
        battler("Cinder", "ember", "solar"),
        battler("Spark", "electric", "mad"),
    )
    private fun thornTeam() = listOf(
        battler("Bulwark", "ancient", "giant"),
        battler("Bramble", "thorn", "fungal"),
        battler("Fang", "beast", "thorn"),
    )

    @Test
    fun `both teams must be non-empty`() {
        assertFailsWith<IllegalArgumentException> {
            BattleResolver().resolve(emptyList(), thornTeam())
        }
    }

    @Test
    fun `a battle always terminates with a decisive result`() {
        val result = BattleResolver(Random(1)).resolve(emberTeam(), thornTeam())
        assertTrue(result.rounds in 1..BattleConfig().maxRounds)
        assertNotEquals(Outcome.DRAW, result.outcome, "these teams shouldn't stalemate")
        // the winner keeps at least one creature standing
        val winnerSurvivors = if (result.outcome == Outcome.A_WINS) result.survivorsA else result.survivorsB
        assertTrue(winnerSurvivors >= 1)
        assertEquals(BattleEvent.Victory(result.outcome), result.events.last())
    }

    @Test
    fun `same seed produces an identical battle`() {
        val a = BattleResolver(Random(42)).resolve(emberTeam(), thornTeam())
        val b = BattleResolver(Random(42)).resolve(emberTeam(), thornTeam())
        assertEquals(a.outcome, b.outcome)
        assertEquals(a.rounds, b.rounds)
        assertEquals(a.events, b.events)
    }

    @Test
    fun `type advantage wins regardless of the dice`() {
        // Across many seeds, the super-effective side should always take it.
        for (seed in 0L until 25L) {
            val result = BattleResolver(Random(seed)).resolve(emberTeam(), thornTeam())
            assertEquals(Outcome.A_WINS, result.outcome, "EMBER should beat THORN on seed $seed")
        }
    }

    @Test
    fun `exploiting weakness builds a Momentum burst`() {
        val result = BattleResolver(Random(7)).resolve(emberTeam(), thornTeam())
        val bursts = result.events.filterIsInstance<BattleEvent.Burst>()
        assertTrue(bursts.any { it.side == Side.A }, "the type-dominant side should burst at least once")
    }

    private fun leveled(name: String, level: Int, vararg ids: String) =
        Battler(name, f.grow(ids.map { StarterEssences.require(it) }), CreatureProgress(level = level))

    @Test
    fun `between identical teams the higher level wins`() {
        // Same creatures, same (neutral) matchup — only levels differ, so grinding should tell.
        for (seed in 0L until 12L) {
            val low = listOf(leveled("Lo1", 1, "mad", "electric"), leveled("Lo2", 1, "ember", "solar"))
            val high = listOf(leveled("Hi1", 20, "mad", "electric"), leveled("Hi2", 20, "ember", "solar"))
            val result = BattleResolver(Random(seed)).resolve(low, high)
            assertEquals(Outcome.B_WINS, result.outcome, "level 20 should beat level 1 on seed $seed")
        }
    }

    @Test
    fun `a lower-level team with type advantage still beats a moderate level lead`() {
        // The thesis, proven through the real resolver: out-think beats out-grind. A level-1 EMBER
        // team overcomes a level-8 THORN team because super-effective hits + Momentum compound
        // faster than raw stat scaling. (At a *large* gap, status effects and bulk make it a real
        // contest — which is intended; grinding shouldn't be worthless.)
        for (seed in 0L until 15L) {
            val ember = listOf(leveled("Blaze", 1, "mad", "electric"), leveled("Cinder", 1, "ember", "solar"))
            val thorn = listOf(leveled("Bulwark", 8, "ancient", "giant"), leveled("Bramble", 8, "thorn", "fungal"))
            val result = BattleResolver(Random(seed)).resolve(ember, thorn)
            assertEquals(Outcome.A_WINS, result.outcome, "type advantage should beat an 8-level lead on seed $seed")
        }
    }

    @Test
    fun `a fainted creature is replaced by the next on the bench`() {
        val result = BattleResolver(Random(3)).resolve(emberTeam(), thornTeam())
        val faints = result.events.filterIsInstance<BattleEvent.Faint>()
        assertTrue(faints.isNotEmpty(), "someone should faint in a decisive battle")
        // Side B (the loser) should lose all three for the battle to end.
        assertEquals(3, faints.count { it.side == Side.B })
    }
}
