package com.bmeyer.appmanager.data

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCategoryTest {

    @Test fun any_matchesEverything() {
        assertTrue(AppCategory.ANY.matches(app("a", category = ApplicationInfo.CATEGORY_GAME)))
        assertTrue(AppCategory.ANY.matches(app("b", category = ApplicationInfo.CATEGORY_UNDEFINED)))
    }

    @Test fun named_matchesOwnValueOnly() {
        val game = app("g", category = ApplicationInfo.CATEGORY_GAME)
        assertTrue(AppCategory.GAME.matches(game))
        assertFalse(AppCategory.AUDIO.matches(game))
    }

    @Test fun other_catchesUndefinedAndUnlisted() {
        assertTrue(AppCategory.OTHER.matches(app("u", category = ApplicationInfo.CATEGORY_UNDEFINED)))
        // CATEGORY_ACCESSIBILITY (8) has no named entry → should fall into OTHER.
        assertTrue(AppCategory.OTHER.matches(app("acc", category = 8)))
        assertFalse(AppCategory.OTHER.matches(app("game", category = ApplicationInfo.CATEGORY_GAME)))
    }

    @Test fun of_mapsValues() {
        assertEquals(AppCategory.GAME, AppCategory.of(ApplicationInfo.CATEGORY_GAME))
        assertEquals(AppCategory.SOCIAL, AppCategory.of(ApplicationInfo.CATEGORY_SOCIAL))
        assertEquals(AppCategory.OTHER, AppCategory.of(ApplicationInfo.CATEGORY_UNDEFINED))
        assertEquals(AppCategory.OTHER, AppCategory.of(8))
    }
}
