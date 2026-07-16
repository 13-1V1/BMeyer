package com.bmeyer.appmanager.data

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the installed-app inventory and enriches each entry with usage-stats
 * (last used, foreground time) and storage size. Everything runs off the main
 * thread; call [loadApps] from a coroutine.
 */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    /** Window for aggregating usage stats: roughly the last year. */
    private val usageWindowMillis = 365L * 24 * 60 * 60 * 1000

    @Suppress("DEPRECATION")
    suspend fun loadApps(
        includeSystem: Boolean,
        hasUsageAccess: Boolean,
        nowMillis: Long,
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val usageByPkg: Map<String, UsageSnapshot> =
            if (hasUsageAccess) queryUsage(nowMillis) else emptyMap()

        val storageStatsManager = if (hasUsageAccess) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        } else null

        val user = Process.myUserHandle()
        val selfPackage = context.packageName

        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != selfPackage }
            .filter { includeSystem || !it.isSystem() }
            .map { appInfo ->
                val usage = usageByPkg[appInfo.packageName]
                val size = storageStatsManager?.let { ssm ->
                    runCatching {
                        val s = ssm.queryStatsForPackage(
                            appInfo.storageUuid,
                            appInfo.packageName,
                            user,
                        )
                        s.appBytes + s.dataBytes + s.cacheBytes
                    }.getOrNull()
                } ?: -1L

                AppInfo(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    isSystemApp = appInfo.isSystem(),
                    sizeBytes = size,
                    lastUsedMillis = usage?.lastUsed ?: 0L,
                    usageMillis = usage?.foreground ?: 0L,
                    firstInstallMillis = firstInstall(appInfo.packageName),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private data class UsageSnapshot(val lastUsed: Long, val foreground: Long)

    private fun queryUsage(nowMillis: Long): Map<String, UsageSnapshot> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return runCatching {
            usm.queryAndAggregateUsageStats(nowMillis - usageWindowMillis, nowMillis)
                .mapValues { (_, stats) ->
                    UsageSnapshot(stats.lastTimeUsed, stats.totalTimeInForeground)
                }
        }.onFailure { Log.w(TAG, "usage query failed", it) }.getOrDefault(emptyMap())
    }

    @Suppress("DEPRECATION")
    private fun firstInstall(packageName: String): Long = runCatching {
        pm.getPackageInfo(packageName, 0).firstInstallTime
    }.getOrDefault(0L)

    private fun ApplicationInfo.isSystem(): Boolean =
        (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0

    companion object {
        private const val TAG = "AppRepository"
    }
}
