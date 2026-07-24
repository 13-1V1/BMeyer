package com.bmeyer.assetgen.gen

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

/**
 * Saves a generated asset to the device's shared Pictures collection as a PNG
 * (preserving transparency). Uses the scoped-storage MediaStore path on
 * API 29+ and a direct file write on older devices.
 *
 * `minSdk` is 26 and `compileSdk` 35, so every API-gated call here is guarded
 * by `Build.VERSION.SDK_INT` — the same NewApi discipline the sibling app
 * learned the hard way.
 */
object GalleryExport {

    private const val SUBDIR = "SpriteForge"

    /** @return the saved content Uri (API 29+) or file Uri, or null on failure. */
    fun savePng(context: Context, bitmap: Bitmap, displayName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, bitmap, displayName)
        } else {
            saveLegacy(bitmap, displayName)
        }
    }

    private fun saveViaMediaStore(context: Context, bitmap: Bitmap, name: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$SUBDIR"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: return null
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(bitmap: Bitmap, name: String): Uri? {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            SUBDIR
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, name)
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
