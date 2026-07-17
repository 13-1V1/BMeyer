package com.bmeyer.appmanager.ui

import android.content.pm.ApplicationInfo
import com.bmeyer.appmanager.data.AppCategory
import com.bmeyer.appmanager.data.QuickFilter
import com.bmeyer.appmanager.data.SortOption
import com.bmeyer.appmanager.data.app
import org.junit.Assert.assertEquals
import org.junit.Test

class UiStateTest {

    private val mb = 1024L * 1024

    // "Recently used" must be near real now, since unused-stats compare against
    // System.currentTimeMillis(); a small epoch value would read as ~1970 (ancient).
    private val recent = System.currentTimeMillis()

    // Two apps never used (lastUsed = 0), two used recently; one has unknown size (-1).
    private val apps = listOf(
        app("com.game.one", "Alpha Game", size = 300 * mb, lastUsed = 0, category = ApplicationInfo.CATEGORY_GAME),
        app("com.audio.two", "Beta Audio", size = 100 * mb, lastUsed = recent, category = ApplicationInfo.CATEGORY_AUDIO),
        app("com.other.three", "Gamma", size = 200 * mb, lastUsed = 0),
        app("com.social.four", "Delta Social", size = -1, lastUsed = recent, category = ApplicationInfo.CATEGORY_SOCIAL),
    )

    private fun state(
        sort: SortOption = SortOption.LARGEST,
        query: String = "",
        category: AppCategory = AppCategory.ANY,
        quickFilter: QuickFilter = QuickFilter.ALL,
        selected: Set<String> = emptySet(),
    ) = UiState(
        allApps = apps,
        sort = sort,
        query = query,
        category = category,
        quickFilter = quickFilter,
        selected = selected,
    )

    @Test fun sort_largestFirst_unknownLast() {
        val visible = state(sort = SortOption.LARGEST).visibleApps
        assertEquals("com.game.one", visible.first().packageName)
        assertEquals("com.social.four", visible.last().packageName) // size -1 sorts last
    }

    @Test fun sort_smallestKnownAfterUnknown() {
        val order = state(sort = SortOption.SMALLEST).visibleApps.map { it.packageName }
        assertEquals("com.social.four", order.first()) // -1 is smallest
        assertEquals("com.game.one", order.last())
    }

    @Test fun sort_byName() {
        val order = state(sort = SortOption.NAME_ASC).visibleApps.map { it.label }
        assertEquals(listOf("Alpha Game", "Beta Audio", "Delta Social", "Gamma"), order)
    }

    @Test fun search_matchesLabelOrPackageCaseInsensitive() {
        assertEquals(1, state(query = "audio").visibleApps.size)
        assertEquals(1, state(query = "GAMMA").visibleApps.size)
        assertEquals(1, state(query = "com.social").visibleApps.size)
        assertEquals(0, state(query = "nope").visibleApps.size)
    }

    @Test fun category_filters() {
        val games = state(category = AppCategory.GAME).visibleApps
        assertEquals(1, games.size)
        assertEquals("com.game.one", games.first().packageName)
    }

    @Test fun quickFilter_neverUsed() {
        val never = state(quickFilter = QuickFilter.NEVER_USED).visibleApps.map { it.packageName }
        assertEquals(setOf("com.game.one", "com.other.three"), never.toSet())
    }

    @Test fun selectedReclaimable_ignoresUnknownSizes() {
        val s = state(selected = setOf("com.game.one", "com.social.four"))
        assertEquals(300 * mb, s.selectedReclaimableBytes) // social.four (-1) excluded
    }

    @Test fun totals_sumKnownSizes() {
        val s = state()
        assertEquals(600 * mb, s.totalBytes) // 300 + 200 + 100, -1 excluded
        assertEquals(600 * mb, s.visibleTotalBytes)
    }

    @Test fun dashboard_unusedStats() {
        val s = state()
        assertEquals(2, s.unusedCount) // two never-used apps
        assertEquals(500 * mb, s.unusedReclaimableBytes) // 300 + 200
    }
}
