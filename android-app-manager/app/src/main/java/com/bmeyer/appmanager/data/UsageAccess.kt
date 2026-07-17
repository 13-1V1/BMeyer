package com.bmeyer.appmanager.data

import android.app.AppOpsManager
import android.content.Context
import android.os.Process

/** Helpers around the PACKAGE_USAGE_STATS "app ops" special permission. */
object UsageAccess {

    /** True when the user has granted "Usage access" for this app in Settings. */
    fun isGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
