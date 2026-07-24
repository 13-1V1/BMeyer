package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RevealTest {

    private val f = StarterEssences.factory
    private fun grow(vararg ids: String) = f.grow(ids.map { StarterEssences.require(it) })

    @Test
    fun `reveal tier reflects the rarest essence, or Wild`() {
        assertEquals(RevealTier.COMMON, Reveal.tierOf(grow("mad", "electric")))       // all common
        assertEquals(RevealTier.RARE, Reveal.tierOf(grow("solar", "mad")))            // solar is RARE
        assertEquals(RevealTier.WILD, Reveal.tierOf(grow("void", "mad")))             // hidden -> Wild
    }

    @Test
    fun `every reveal runs the four phases in order`() {
        val beats = Reveal.forTier(RevealTier.COMMON).beats
        assertEquals(
            listOf(RevealPhase.ANTICIPATION, RevealPhase.SILENCE, RevealPhase.BURST, RevealPhase.SETTLE),
            beats.map { it.phase },
        )
    }

    @Test
    fun `rarer reveals build longer and burst brighter`() {
        val common = Reveal.forTier(RevealTier.COMMON)
        val wild = Reveal.forTier(RevealTier.WILD)

        assertTrue(wild.totalMs > common.totalMs, "a Wild reveal should last longer overall")
        assertTrue(
            wild.beat(RevealPhase.ANTICIPATION).durationMs > common.beat(RevealPhase.ANTICIPATION).durationMs,
            "rarer = longer anticipation",
        )
        assertTrue(
            wild.beat(RevealPhase.SILENCE).durationMs > common.beat(RevealPhase.SILENCE).durationMs,
            "rarer = longer held silence",
        )
        assertTrue(
            wild.beat(RevealPhase.BURST).intensity > common.beat(RevealPhase.BURST).intensity,
            "rarer = brighter burst",
        )
    }

    @Test
    fun `the held silence really is silent`() {
        val silence = Reveal.forTier(RevealTier.RARE).beat(RevealPhase.SILENCE)
        assertEquals(0.0, silence.intensity)
        assertTrue(silence.sfx.isEmpty(), "the pause has no sound — that's the point")
    }

    @Test
    fun `Wild reveals get their own burst sting`() {
        assertEquals("reveal_burst_wild", Reveal.forTier(RevealTier.WILD).beat(RevealPhase.BURST).sfx)
        assertEquals("reveal_burst", Reveal.forTier(RevealTier.COMMON).beat(RevealPhase.BURST).sfx)
    }
}
