package com.bmeyer.appmanager.data

/**
 * Immutable snapshot of one installed app plus the usage/size metrics we sort
 * and filter on. Icons are loaded lazily in the UI (by [packageName]) so this
 * model stays cheap to hold for hundreds of apps.
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    /** app + data + cache bytes, or -1 when usage access hasn't been granted. */
    val sizeBytes: Long,
    /** epoch millis of last foreground use, or 0 when unknown / never. */
    val lastUsedMillis: Long,
    /** total foreground time in millis, or 0 when unknown. */
    val usageMillis: Long,
    /** epoch millis the app was first installed. */
    val firstInstallMillis: Long,
    /** ApplicationInfo.category (CATEGORY_GAME, …), or -1 when undefined. */
    val category: Int,
)
