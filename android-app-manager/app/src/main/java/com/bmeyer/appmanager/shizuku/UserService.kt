package com.bmeyer.appmanager.shizuku

import com.bmeyer.appmanager.IUserService
import java.io.BufferedReader
import kotlin.system.exitProcess

/**
 * Runs inside the privileged process Shizuku spawns (shell/ADB or root uid), so
 * it can invoke `pm uninstall` directly — no per-app confirmation dialog. This
 * class must have a public no-arg constructor; Shizuku instantiates it by name.
 */
class UserService() : IUserService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun uninstall(packageName: String): String = try {
        val process = Runtime.getRuntime()
            .exec(arrayOf("pm", "uninstall", "--user", "0", packageName))
        val out = process.inputStream.bufferedReader().use(BufferedReader::readText)
        val err = process.errorStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        val combined = (out + err).trim()
        if (combined.contains("Success", ignoreCase = true)) "Success"
        else combined.ifEmpty { "Failed (exit ${process.exitValue()})" }
    } catch (t: Throwable) {
        "Error: ${t.message}"
    }
}
