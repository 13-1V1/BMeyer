package com.motley.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SaveCodecTest {

    private fun samplePlayer(): Player = Player()
        .collect("mad").collect("electric").collect("robo").collect("ancient").collect("giant")
        .grow("Rusty", listOf("mad", "robo"))
        .grow("Sparky", listOf("mad", "electric"))
        .grow("Titan", listOf("ancient", "giant"))
        .awardXp(1, 5_000)      // level Rusty up
        .awardXp(3, 2_000_000)  // push Titan past level 50 so it can Bloom
        .bloom(3)

    @Test
    fun `a player round-trips through save and load unchanged`() {
        val player = samplePlayer()
        val restored = SaveCodec.decode(SaveCodec.encode(player))
        assertEquals(player, restored)
    }

    @Test
    fun `restored creatures keep their level, xp and blooms`() {
        val player = samplePlayer()
        val restored = SaveCodec.decode(SaveCodec.encode(player))
        assertEquals(player.bySerial(1)!!.progress, restored.bySerial(1)!!.progress)
        assertEquals(player.bySerial(3)!!.progress.blooms, restored.bySerial(3)!!.progress.blooms)
    }

    @Test
    fun `an empty player round-trips`() {
        assertEquals(Player(), SaveCodec.decode(SaveCodec.encode(Player())))
    }

    @Test
    fun `garbage input is rejected`() {
        assertFailsWith<IllegalArgumentException> { SaveCodec.decode("not a save file") }
    }
}
