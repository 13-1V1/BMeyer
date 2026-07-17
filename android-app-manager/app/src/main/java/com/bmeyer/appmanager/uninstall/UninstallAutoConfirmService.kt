package com.bmeyer.appmanager.uninstall

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Auto-taps the system "Uninstall / OK" button on the package-installer's
 * confirmation dialog, but ONLY while [active] is set — i.e. during a bulk
 * uninstall the user explicitly started. This turns Android's mandatory
 * per-app confirmation into a hands-free batch without root or Shizuku.
 *
 * It never acts on its own: outside an active batch, or on any window that
 * isn't the package installer, it does nothing.
 */
class UninstallAutoConfirmService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!active.get()) return
        val pkg = event?.packageName?.toString() ?: return
        if (!isInstaller(pkg)) return
        val root = rootInActiveWindow ?: return
        try {
            clickConfirm(root)
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {}

    private fun isInstaller(pkg: String): Boolean =
        pkg.contains("packageinstaller", ignoreCase = true) ||
            pkg.contains("packagemanager", ignoreCase = true)

    private fun clickConfirm(root: AccessibilityNodeInfo): Boolean {
        // The positive dialog button is android:id/button1 across AOSP/OEMs.
        root.findAccessibilityNodeInfosByViewId("android:id/button1")?.forEach { node ->
            if (clickSelfOrParent(node)) return true
        }
        // Fallback: match the button by its label.
        for (label in CONFIRM_LABELS) {
            root.findAccessibilityNodeInfosByText(label)?.forEach { node ->
                if (clickSelfOrParent(node)) return true
            }
        }
        return false
    }

    private fun clickSelfOrParent(node: AccessibilityNodeInfo?): Boolean {
        var n = node
        while (n != null) {
            if (n.isClickable && n.isEnabled) {
                return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            n = n.parent
        }
        return false
    }

    companion object {
        /** Set true only while a user-initiated bulk uninstall is running. */
        val active = AtomicBoolean(false)

        private val CONFIRM_LABELS = listOf("OK", "Uninstall", "UNINSTALL", "Delete", "DELETE")

        /** Whether the user has enabled this accessibility service in Settings. */
        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val component = ComponentName(context, UninstallAutoConfirmService::class.java)
            val flattened = component.flattenToString()
            val shortName = "${context.packageName}/${UninstallAutoConfirmService::class.java.name}"
            return enabled.split(':').any {
                it.equals(flattened, ignoreCase = true) || it.equals(shortName, ignoreCase = true)
            }
        }
    }
}
