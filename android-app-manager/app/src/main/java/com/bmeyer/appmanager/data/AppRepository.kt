package com.bmeyer.appmanager.data

import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
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
 * (last used, foreground time, open count) and storage size. Everything runs
 * off the main thread; call [loadApps] from a coroutine.
 */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    /** Window for aggregating usage stats: roughly the last year. */
    private val usageWindowMillis = 365L * 24 * 60 * 60 * 1000

    /** Shorter window for counting opens (event scans get heavy over a year). */
    private val openWindowMillis = 30L * 24 * 60 * 60 * 1000

    @Suppress("DEPRECATION")
    suspend fun loadApps(
        includeSystem: Boolean,
        hasUsageAccess: Boolean,
        nowMillis: Long,
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val usageByPkg: Map<String, UsageSnapshot> =
            if (hasUsageAccess) queryUsage(nowMillis) else emptyMap()
        val openCounts: Map<String, Int> =
            if (hasUsageAccess) queryOpenCounts(nowMillis) else emptyMap()

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

                val (firstInstall, lastUpdate) = packageTimes(appInfo.packageName)

                AppInfo(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    isSystemApp = appInfo.isSystem(),
                    sizeBytes = size,
                    lastUsedMillis = usage?.lastUsed ?: 0L,
                    usageMillis = usage?.foreground ?: 0L,
                    firstInstallMillis = firstInstall,
                    lastUpdateMillis = lastUpdate,
                    openCount = openCounts[appInfo.packageName] ?: 0,
                    category = appInfo.category,
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

    /** Counts foreground launches per package over the open window. */
    private fun queryOpenCounts(nowMillis: Long): Map<String, Int> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return runCatching {
            val events = usm.queryEvents(nowMillis - openWindowMillis, nowMillis)
            val counts = HashMap<String, Int>()
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                // MOVE_TO_FOREGROUND (==1) is also ACTIVITY_RESUMED on API 29+.
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    val pkg = event.packageName ?: continue
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
            counts
        }.onFailure { Log.w(TAG, "event query failed", it) }.getOrDefault(emptyMap())
    }

    /** Returns (firstInstallTime, lastUpdateTime) for a package. */
    private fun packageTimes(packageName: String): Pair<Long, Long> = runCatching {
        val pi = pm.getPackageInfo(packageName, 0)
        pi.firstInstallTime to pi.lastUpdateTime
    }.getOrDefault(0L to 0L)

    private fun ApplicationInfo.isSystem(): Boolean =
        (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0

    companion object {
        private const val TAG = "AppRepository"
    }
}
