package com.bmeyer.assetgen.gen

import android.content.Context
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Owns the on-device Stable-Diffusion model files. The MediaPipe Image
 * Generator needs a *directory* of converted SD 1.5 weights (several hundred MB
 * to ~1.5 GB) — far too large to ship inside an APK, so the user imports a
 * model bundle (a .zip of the converted files) once and it lives in app
 * storage from then on.
 */
class ModelManager(context: Context) {

    /** Where the extracted model directory lives. */
    val modelDir: File = File(context.filesDir, MODEL_DIR_NAME)

    /** Absolute path handed to MediaPipe's `setImageGeneratorModelDirectory`. */
    val modelPath: String get() = modelDir.absolutePath

    /** True once a non-empty model directory has been imported. */
    fun isInstalled(): Boolean =
        modelDir.isDirectory && (modelDir.listFiles()?.isNotEmpty() == true)

    /**
     * Extracts a model bundle (.zip) into [modelDir], replacing any previous
     * install. Guards against zip-slip path traversal. Returns bytes written.
     */
    fun installFromZip(input: InputStream): Long {
        clear()
        modelDir.mkdirs()
        var written = 0L
        ZipInputStream(input.buffered()).use { zip ->
            var entry = zip.nextEntry
            val buffer = ByteArray(64 * 1024)
            while (entry != null) {
                val target = File(modelDir, entry.name).canonicalFile
                // zip-slip: refuse entries that escape the model directory.
                if (!target.path.startsWith(modelDir.canonicalPath + File.separator) &&
                    target.path != modelDir.canonicalPath
                ) {
                    throw SecurityException("Illegal zip entry path: ${entry.name}")
                }
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    target.outputStream().use { out ->
                        var n = zip.read(buffer)
                        while (n >= 0) {
                            out.write(buffer, 0, n)
                            written += n
                            n = zip.read(buffer)
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return written
    }

    /** Delete the installed model, freeing storage. */
    fun clear() {
        if (modelDir.exists()) modelDir.deleteRecursively()
    }

    companion object {
        private const val MODEL_DIR_NAME = "sd_model"
    }
}
