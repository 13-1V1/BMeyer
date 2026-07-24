package com.bmeyer.assetgen.gen

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * Streams a model bundle (.zip) from a URL to a cache file, reporting progress
 * and honoring coroutine cancellation. Uses only java.net (no extra deps).
 *
 * MediaPipe's on-device SD 1.5 model is a converted bundle that users host
 * themselves, so this works with any direct HTTPS link to such a .zip; the
 * downloaded file is then unzipped by [ModelManager].
 */
class ModelDownloader(private val context: Context) {

    /**
     * @param url direct link to a .zip bundle.
     * @param onProgress (downloadedBytes, totalBytes); totalBytes is -1 if the
     *   server doesn't report a content length.
     * @return the downloaded cache file. Caller installs it, then deletes it.
     */
    suspend fun downloadToCache(
        url: String,
        onProgress: (Long, Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val target = File(context.cacheDir, "model_download.zip")
        if (target.exists()) target.delete()

        val connection = openFollowingRedirects(url)
        val total = connection.contentLengthLong
        try {
            connection.inputStream.use { input ->
                target.outputStream().use { out ->
                    val buffer = ByteArray(64 * 1024)
                    var downloaded = 0L
                    onProgress(0L, total)
                    while (true) {
                        coroutineContext.ensureActive() // cooperative cancellation
                        val n = input.read(buffer)
                        if (n < 0) break
                        out.write(buffer, 0, n)
                        downloaded += n
                        onProgress(downloaded, total)
                    }
                }
            }
        } catch (t: Throwable) {
            target.delete() // don't leave a half-written bundle behind
            throw t
        } finally {
            connection.disconnect()
        }
        target
    }

    /** Opens a GET connection, manually following up to 5 redirects (incl. the
     *  cross-host CDN hops that Hugging Face / Drive use). */
    private fun openFollowingRedirects(startUrl: String): HttpURLConnection {
        var current = startUrl
        var redirects = 0
        while (true) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                requestMethod = "GET"
            }
            val code = conn.responseCode
            if (code in 300..399) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (location == null || redirects >= 5) error("Too many redirects")
                current = URL(URL(current), location).toString()
                redirects++
                continue
            }
            if (code !in 200..299) {
                conn.disconnect()
                error("Download failed: HTTP $code")
            }
            return conn
        }
    }
}
