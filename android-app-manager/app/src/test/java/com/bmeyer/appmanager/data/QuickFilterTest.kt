package com.bmeyer.appmanager.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class QuickFilterTest {

    private val now = 1_000_000_000_000L
    private val day = TimeUnit.DAYS.toMillis(1)

    @Test fun all_matchesEverything() {
        assertTrue(QuickFilter.ALL.matches(app("a", lastUsed = now), now))
        assertTrue(QuickFilter.ALL.matches(app("b", lastUsed = 0), now))
    }

    @Test fun neverUsed() {
        assertTrue(QuickFilter.NEVER_USED.matches(app("a", lastUsed = 0), now))
        assertFalse(QuickFilter.NEVER_USED.matches(app("b", lastUsed = now), now))
    }

    @Test fun unused30_includesNeverAndOld() {
        assertTrue(QuickFilter.UNUSED_30.matches(app("never", lastUsed = 0), now))
        assertTrue(QuickFilter.UNUSED_30.matches(app("old", lastUsed = now - 31 * day), now))
        assertFalse(QuickFilter.UNUSED_30.matches(app("recent", lastUsed = now - 10 * day), now))
    }

    @Test fun unused90_threshold() {
        assertTrue(QuickFilter.UNUSED_90.matches(app("old", lastUsed = now - 91 * day), now))
        assertFalse(QuickFilter.UNUSED_90.matches(app("mid", lastUsed = now - 60 * day), now))
    }

    @Test fun large_threshold() {
        val mb = 1024L * 1024
        assertTrue(QuickFilter.LARGE.matches(app("big", size = 500 * mb), now))
        assertFalse(QuickFilter.LARGE.matches(app("small", size = 499 * mb), now))
        assertFalse(QuickFilter.LARGE.matches(app("unknown", size = -1), now))
    }
}
