package com.bmeyer.assetgen

import com.bmeyer.assetgen.data.AssetNaming
import com.bmeyer.assetgen.data.StylePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetNamingTest {

    @Test
    fun `slug lowercases and hyphenates`() {
        assertEquals("fire-knight", AssetNaming.slug("Fire Knight!!"))
    }

    @Test
    fun `slug trims leading and trailing separators`() {
        assertEquals("hero", AssetNaming.slug("  ***hero***  "))
    }

    @Test
    fun `empty or symbol-only slug falls back`() {
        assertEquals("asset", AssetNaming.slug("   "))
        assertEquals("asset", AssetNaming.slug("!!!"))
    }

    @Test
    fun `slug is length bounded`() {
        val long = "a".repeat(200)
        assertTrue(AssetNaming.slug(long).length <= 40)
    }

    @Test
    fun `file name is deterministic and png`() {
        val name = AssetNaming.fileName("Fire Knight", StylePreset.PIXEL_ART, 1721700000000L)
        assertEquals("spriteforge_pixel-art_fire-knight_1721700000000.png", name)
    }

    @Test
    fun `file name has no unsafe characters`() {
        val name = AssetNaming.fileName("weird/\\:name?", StylePreset.FLAT_ICON, 42L)
        assertTrue(name.matches(Regex("[a-z0-9_.\\-]+")))
    }
}
