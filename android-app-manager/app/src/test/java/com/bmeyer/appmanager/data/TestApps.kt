package com.bmeyer.appmanager.data

import android.content.pm.ApplicationInfo

/** Factory for AppInfo fixtures used across the unit tests. */
fun app(
    pkg: String,
    label: String = pkg,
    size: Long = 0,
    lastUsed: Long = 0,
    usage: Long = 0,
    install: Long = 0,
    category: Int = ApplicationInfo.CATEGORY_UNDEFINED,
    system: Boolean = false,
) = AppInfo(
    packageName = pkg,
    label = label,
    isSystemApp = system,
    sizeBytes = size,
    lastUsedMillis = lastUsed,
    usageMillis = usage,
    firstInstallMillis = install,
    category = category,
)
