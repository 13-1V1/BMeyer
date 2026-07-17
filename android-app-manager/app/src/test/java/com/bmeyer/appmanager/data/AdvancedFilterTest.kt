package com.bmeyer.appmanager.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class AdvancedFilterTest {

    private val now = 1_000_000_000_000L
    private val day = TimeUnit.DAYS.toMillis(1)
    private val minute = TimeUnit.MINUTES.toMillis(1)
    private val mb = 1024L * 1024

    @Test fun empty_matchesEverything() {
        val f = AdvancedFilter()
        assertFalse(f.isActive)
        assertTrue(f.matches(app("a", lastUsed = 0, size = -1), now))
    }

    @Test fun lastUsed_within_excludesOldAndNever() {
        val f = AdvancedFilter(lastUsedMode = TimeMode.WITHIN, lastUsedDays = 30)
        assertTrue(f.matches(app("recent", lastUsed = now - 10 * day), now))
        assertFalse(f.matches(app("old", lastUsed = now - 40 * day), now))
        assertFalse(f.matches(app("never", lastUsed = 0), now))
    }

    @Test fun lastUsed_olderThan_includesNever() {
        val f = AdvancedFilter(lastUsedMode = TimeMode.OLDER_THAN, lastUsedDays = 30)
        assertTrue(f.matches(app("old", lastUsed = now - 40 * day), now))
        assertTrue(f.matches(app("never", lastUsed = 0), now))
        assertFalse(f.matches(app("recent", lastUsed = now - 5 * day), now))
    }

    @Test fun compound_usedWithinYearAndOverFiveMinutes() {
        // The user's example: used within the last year AND for more than 5 minutes.
        val f = AdvancedFilter(
            lastUsedMode = TimeMode.WITHIN,
            lastUsedDays = 365,
            minUsageMinutes = 5,
        )
        assertTrue(f.matches(app("keep", lastUsed = now - 100 * day, usage = 10 * minute), now))
        assertFalse(f.matches(app("barely", lastUsed = now - 100 * day, usage = 2 * minute), now))
        assertFalse(f.matches(app("stale", lastUsed = now - 400 * day, usage = 60 * minute), now))
        assertEquals(2, f.activeCount)
    }

    @Test fun openCount_bounds() {
        val f = AdvancedFilter(minOpenCount = 3, maxOpenCount = 10)
        assertTrue(f.matches(app("mid", openCount = 5), now))
        assertFalse(f.matches(app("few", openCount = 1), now))
        assertFalse(f.matches(app("many", openCount = 50), now))
    }

    @Test fun size_bounds_excludeUnknown() {
        val f = AdvancedFilter(minSizeMb = 100)
        assertTrue(f.matches(app("big", size = 200 * mb), now))
        assertFalse(f.matches(app("small", size = 50 * mb), now))
        assertFalse(f.matches(app("unknown", size = -1), now)) // unknown can't satisfy a size filter
    }

    @Test fun installed_within() {
        val f = AdvancedFilter(installedMode = TimeMode.WITHIN, installedDays = 7)
        assertTrue(f.matches(app("new", install = now - 2 * day), now))
        assertFalse(f.matches(app("old", install = now - 30 * day), now))
    }
}
