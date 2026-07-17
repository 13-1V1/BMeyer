package com.bmeyer.appmanager.data

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process

/** Helpers around the PACKAGE_USAGE_STATS "app ops" special permission. */
object UsageAccess {

    /**
     * True when the user has granted "Usage access" for this app in Settings.
     *
     * `unsafeCheckOpNoThrow` only exists on API 29+; on API 26–28 we must use
     * the older `checkOpNoThrow`, or calling the newer method throws
     * NoSuchMethodError and crashes at launch. Wrapped defensively so a check
     * failure never brings the app down — it just reports "not granted".
     */
    fun isGranted(context: Context): Boolean = runCatching {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val uid = Process.myUid()
        val pkg = context.packageName
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, pkg)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, pkg)
        }
        mode == AppOpsManager.MODE_ALLOWED
    }.getOrDefault(false)
}
