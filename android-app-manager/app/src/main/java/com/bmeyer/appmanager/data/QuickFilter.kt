package com.bmeyer.appmanager.data

/**
 * One-tap "clean house" presets shown as chips. Everything except [ALL] leans
 * on usage/size data, so those require the Usage-access grant to be meaningful.
 */
enum class QuickFilter(val label: String, val requiresUsageAccess: Boolean) {
    ALL("All", false),
    NEVER_USED("Never used", true),
    UNUSED_30("Unused 30d+", true),
    UNUSED_90("Unused 90d+", true),
    LARGE("≥ 500 MB", true);

    fun matches(app: AppInfo, nowMillis: Long): Boolean = when (this) {
        ALL -> true
        NEVER_USED -> app.lastUsedMillis <= 0L
        UNUSED_30 -> app.lastUsedMillis <= 0L || nowMillis - app.lastUsedMillis > DAY_MS * 30
        UNUSED_90 -> app.lastUsedMillis <= 0L || nowMillis - app.lastUsedMillis > DAY_MS * 90
        LARGE -> app.sizeBytes >= 500L * 1024 * 1024
    }

    companion object {
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
