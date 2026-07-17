package com.bmeyer.appmanager.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class FormatTest {

    @Test fun size_unknownIsDash() = assertEquals("—", formatSize(-1))

    @Test fun size_bytes() {
        assertEquals("0 B", formatSize(0))
        assertEquals("512 B", formatSize(512))
        assertEquals("1023 B", formatSize(1023))
    }

    @Test fun size_scalesUp() {
        assertEquals("1.0 KB", formatSize(1024))
        assertEquals("1.5 KB", formatSize(1536))
        assertEquals("1.0 MB", formatSize(1024L * 1024))
        assertEquals("2.0 GB", formatSize(2L * 1024 * 1024 * 1024))
    }

    @Test fun duration_unknownOrZeroIsDash() {
        assertEquals("—", formatDuration(0))
        assertEquals("—", formatDuration(-5))
    }

    @Test fun duration_formats() {
        assertEquals("<1m", formatDuration(TimeUnit.SECONDS.toMillis(30)))
        assertEquals("1m", formatDuration(TimeUnit.MINUTES.toMillis(1)))
        assertEquals("45m", formatDuration(TimeUnit.MINUTES.toMillis(45)))
        assertEquals(
            "1h 12m",
            formatDuration(TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(12)),
        )
    }

    @Test fun lastUsed_relative() {
        val now = 1_000_000_000_000L
        val day = TimeUnit.DAYS.toMillis(1)
        assertEquals("never", formatLastUsed(0, now))
        assertEquals("today", formatLastUsed(now, now))
        assertEquals("yesterday", formatLastUsed(now - day, now))
        assertEquals("3d ago", formatLastUsed(now - 3 * day, now))
        assertEquals("1mo ago", formatLastUsed(now - 45 * day, now))
        assertEquals("1y ago", formatLastUsed(now - 400 * day, now))
    }
}
