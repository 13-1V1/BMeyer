package com.bmeyer.appmanager.ui

import java.util.concurrent.TimeUnit
import kotlin.math.abs

/** Human-readable byte size, e.g. 1.4 GB. Returns "—" when unknown (-1). */
fun formatSize(bytes: Long): String {
    if (bytes < 0) return "—"
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024
    var i = 0
    while (value >= 1024 && i < units.lastIndex) {
        value /= 1024
        i++
    }
    return String.format("%.1f %s", value, units[i])
}

/** Total foreground time as "3h 12m", "45m", or "—" when zero/unknown. */
fun formatDuration(millis: Long): String {
    if (millis <= 0) return "—"
    val h = TimeUnit.MILLISECONDS.toHours(millis)
    val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m"
        else -> "<1m"
    }
}

/** Relative "last used": "today", "3d ago", "2mo ago", or "never". */
fun formatLastUsed(lastUsedMillis: Long, nowMillis: Long): String {
    if (lastUsedMillis <= 0) return "never"
    val diff = abs(nowMillis - lastUsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        days <= 0 -> "today"
        days == 1L -> "yesterday"
        days < 30 -> "${days}d ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}
