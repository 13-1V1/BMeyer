package com.bmeyer.appmanager.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.bmeyer.appmanager.IUserService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku

/**
 * Optional privileged backend for genuinely silent bulk uninstall. When Shizuku
 * is installed, running, and permission-granted, [uninstall] removes packages
 * with no per-app dialog. Everything degrades gracefully: if Shizuku isn't
 * available the caller falls back to the standard intent queue.
 */
object ShizukuManager {

    private const val PERMISSION_REQUEST_CODE = 4210
    private const val SERVICE_VERSION = 1

    enum class Availability { UNAVAILABLE, NEEDS_PERMISSION, READY }

    private var permissionDeferred: CompletableDeferred<Boolean>? = null

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { code, grantResult ->
            if (code == PERMISSION_REQUEST_CODE) {
                permissionDeferred?.complete(grantResult == PackageManager.PERMISSION_GRANTED)
            }
        }

    init {
        runCatching { Shizuku.addRequestPermissionResultListener(permissionListener) }
    }

    /** Current readiness of the Shizuku backend, safe to call anytime. */
    fun availability(): Availability {
        val alive = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        if (!alive) return Availability.UNAVAILABLE
        // Pre-v11 Shizuku used a manifest permission model we don't support.
        if (runCatching { Shizuku.isPreV11() }.getOrDefault(true)) return Availability.UNAVAILABLE
        val granted = runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        return if (granted) Availability.READY else Availability.NEEDS_PERMISSION
    }

    /** Requests Shizuku permission if needed; returns whether it's now granted. */
    suspend fun ensurePermission(): Boolean {
        when (availability()) {
            Availability.READY -> return true
            Availability.UNAVAILABLE -> return false
            Availability.NEEDS_PERMISSION -> Unit
        }
        val deferred = CompletableDeferred<Boolean>()
        permissionDeferred = deferred
        val requested = runCatching { Shizuku.requestPermission(PERMISSION_REQUEST_CODE) }.isSuccess
        if (!requested) return false
        return withTimeoutOrNull(60_000) { deferred.await() } ?: false
    }

    /**
     * Binds the privileged service and uninstalls each package in turn.
     * Returns a package -> result-message map ("Success" or an error string).
     */
    suspend fun uninstall(
        context: Context,
        packages: List<String>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): Map<String, String> {
        if (packages.isEmpty()) return emptyMap()

        val connected = CompletableDeferred<IUserService?>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                connected.complete(
                    if (binder != null && binder.pingBinder()) {
                        IUserService.Stub.asInterface(binder)
                    } else null
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        val args = Shizuku.UserServiceArgs(
            ComponentName(context.packageName, UserService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("uninstall")
            .debuggable(false)
            .version(SERVICE_VERSION)

        val results = LinkedHashMap<String, String>()
        try {
            Shizuku.bindUserService(args, connection)
            val service = withTimeoutOrNull(15_000) { connected.await() }
                ?: return packages.associateWith { "Error: Shizuku service unavailable" }

            packages.forEachIndexed { index, pkg ->
                results[pkg] = runCatching { service.uninstall(pkg) }
                    .getOrElse { "Error: ${it.message}" }
                onProgress(index + 1, packages.size)
            }
        } catch (t: Throwable) {
            packages.forEach { results.putIfAbsent(it, "Error: ${t.message}") }
        } finally {
            runCatching { Shizuku.unbindUserService(args, connection, true) }
        }
        return results
    }
}
