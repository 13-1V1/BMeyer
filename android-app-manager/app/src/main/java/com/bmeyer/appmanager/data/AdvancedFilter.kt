package com.bmeyer.appmanager.data

/** How a time-based (age) constraint is interpreted. */
enum class TimeMode(val label: String) {
    ANY("Any time"),
    WITHIN("Within"),
    OLDER_THAN("Older than"),
    NEVER("Never"),
}

/**
 * A bundle of search constraints, all AND-combined: every active field must
 * match for an app to pass. This is what powers "used within the year AND for
 * more than 5 minutes"-style refined searches. Numeric bounds are null when
 * unset; time constraints default to [TimeMode.ANY].
 */
data class AdvancedFilter(
    // Last used (foreground)
    val lastUsedMode: TimeMode = TimeMode.ANY,
    val lastUsedDays: Int = 30,

    // Installed / first seen
    val installedMode: TimeMode = TimeMode.ANY,
    val installedDays: Int = 30,

    // Total foreground time, in minutes
    val minUsageMinutes: Int? = null,
    val maxUsageMinutes: Int? = null,

    // Times opened (within the app's open-count window)
    val minOpenCount: Int? = null,
    val maxOpenCount: Int? = null,

    // Storage size, in MB
    val minSizeMb: Int? = null,
    val maxSizeMb: Int? = null,
) {
    /** True when any constraint is set (drives the "Filters • N" badge). */
    val activeCount: Int
        get() = listOf(
            lastUsedMode != TimeMode.ANY,
            installedMode != TimeMode.ANY,
            minUsageMinutes != null,
            maxUsageMinutes != null,
            minOpenCount != null,
            maxOpenCount != null,
            minSizeMb != null,
            maxSizeMb != null,
        ).count { it }

    val isActive: Boolean get() = activeCount > 0

    fun matches(app: AppInfo, nowMillis: Long): Boolean {
        if (!matchesAge(lastUsedMode, lastUsedDays, app.lastUsedMillis, nowMillis)) return false
        // "Never installed" is meaningless — NEVER is ignored for install age.
        if (installedMode != TimeMode.NEVER &&
            !matchesAge(installedMode, installedDays, app.firstInstallMillis, nowMillis)
        ) return false

        val usageMin = app.usageMillis / 60_000
        if (minUsageMinutes != null && usageMin < minUsageMinutes) return false
        if (maxUsageMinutes != null && usageMin > maxUsageMinutes) return false

        if (minOpenCount != null && app.openCount < minOpenCount) return false
        if (maxOpenCount != null && app.openCount > maxOpenCount) return false

        if (minSizeMb != null || maxSizeMb != null) {
            if (app.sizeBytes < 0) return false // unknown size can't satisfy a size filter
            val sizeMb = app.sizeBytes / (1024 * 1024)
            if (minSizeMb != null && sizeMb < minSizeMb) return false
            if (maxSizeMb != null && sizeMb > maxSizeMb) return false
        }
        return true
    }

    private fun matchesAge(mode: TimeMode, days: Int, timeMillis: Long, now: Long): Boolean {
        val windowMs = days.toLong() * DAY_MS
        return when (mode) {
            TimeMode.ANY -> true
            TimeMode.NEVER -> timeMillis <= 0L
            TimeMode.WITHIN -> timeMillis > 0L && now - timeMillis <= windowMs
            TimeMode.OLDER_THAN -> timeMillis <= 0L || now - timeMillis > windowMs
        }
    }

    companion object {
        private const val DAY_MS = 24L * 60 * 60 * 1000

        /** Serialize to a compact string for SharedPreferences persistence. */
        fun encode(f: AdvancedFilter): String = listOf(
            f.lastUsedMode.name,
            f.lastUsedDays,
            f.installedMode.name,
            f.installedDays,
            f.minUsageMinutes ?: "",
            f.maxUsageMinutes ?: "",
            f.minOpenCount ?: "",
            f.maxOpenCount ?: "",
            f.minSizeMb ?: "",
            f.maxSizeMb ?: "",
        ).joinToString("|")

        /** Inverse of [encode]; returns a default filter on any malformed input. */
        fun decode(s: String): AdvancedFilter = runCatching {
            val p = s.split("|")
            AdvancedFilter(
                lastUsedMode = TimeMode.valueOf(p[0]),
                lastUsedDays = p[1].toInt(),
                installedMode = TimeMode.valueOf(p[2]),
                installedDays = p[3].toInt(),
                minUsageMinutes = p[4].toIntOrNull(),
                maxUsageMinutes = p[5].toIntOrNull(),
                minOpenCount = p[6].toIntOrNull(),
                maxOpenCount = p[7].toIntOrNull(),
                minSizeMb = p[8].toIntOrNull(),
                maxSizeMb = p[9].toIntOrNull(),
            )
        }.getOrDefault(AdvancedFilter())
    }
}
