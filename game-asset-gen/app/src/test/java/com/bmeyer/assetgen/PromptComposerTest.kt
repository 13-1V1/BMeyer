package com.bmeyer.assetgen

import com.bmeyer.assetgen.data.AssetType
import com.bmeyer.assetgen.data.PromptComposer
import com.bmeyer.assetgen.data.StylePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptComposerTest {

    @Test
    fun `includes user subject, framing and style cues`() {
        val p = PromptComposer.compose("fire knight", StylePreset.PIXEL_ART, AssetType.CHARACTER)
        assertTrue(p.positive.startsWith("fire knight"))
        assertTrue(p.positive.contains("full-body character"))
        assertTrue(p.positive.contains("pixel art"))
    }

    @Test
    fun `empty prompt falls back to a generic subject`() {
        val p = PromptComposer.compose("   ", StylePreset.FLAT_ICON, AssetType.ITEM)
        assertTrue(p.positive.startsWith("game asset"))
    }

    @Test
    fun `size and steps come from type and style`() {
        val p = PromptComposer.compose("crate", StylePreset.PAINTERLY, AssetType.ITEM)
        assertEquals(AssetType.ITEM.defaultSize, p.size)
        assertEquals(StylePreset.PAINTERLY.steps, p.steps)
    }

    @Test
    fun `transparency flag propagates from type`() {
        assertTrue(PromptComposer.compose("hero", StylePreset.HAND_DRAWN, AssetType.CHARACTER).wantsTransparency)
        assertFalse(PromptComposer.compose("grass", StylePreset.HAND_DRAWN, AssetType.TILE).wantsTransparency)
    }

    @Test
    fun `seamless flag set only for tiles`() {
        assertTrue(PromptComposer.compose("grass", StylePreset.PIXEL_ART, AssetType.TILE).seamless)
        assertFalse(PromptComposer.compose("hero", StylePreset.PIXEL_ART, AssetType.CHARACTER).seamless)
    }

    @Test
    fun `composition is deterministic`() {
        val a = PromptComposer.compose("goblin", StylePreset.LOW_POLY, AssetType.CHARACTER)
        val b = PromptComposer.compose("goblin", StylePreset.LOW_POLY, AssetType.CHARACTER)
        assertEquals(a, b)
    }

    @Test
    fun `explicit steps override the preset default`() {
        val p = PromptComposer.compose("crate", StylePreset.PIXEL_ART, AssetType.ITEM, steps = 8)
        assertEquals(8, p.steps)
    }

    @Test
    fun `steps are clamped to the valid range`() {
        val low = PromptComposer.compose("x", StylePreset.PIXEL_ART, AssetType.ITEM, steps = 1)
        val high = PromptComposer.compose("x", StylePreset.PIXEL_ART, AssetType.ITEM, steps = 999)
        assertEquals(PromptComposer.MIN_STEPS, low.steps)
        assertEquals(PromptComposer.MAX_STEPS, high.steps)
    }

    @Test
    fun `omitting steps uses the preset default`() {
        val p = PromptComposer.compose("x", StylePreset.PAINTERLY, AssetType.ITEM)
        assertEquals(StylePreset.PAINTERLY.steps, p.steps)
    }

    @Test
    fun `no duplicate comma terms in positive prompt`() {
        val p = PromptComposer.compose("game asset", StylePreset.PIXEL_ART, AssetType.CHARACTER)
        val terms = p.positive.split(",").map { it.trim().lowercase() }
        assertEquals(terms.size, terms.toSet().size)
    }
}
