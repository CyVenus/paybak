package app.paybak.paybak.service.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.paybak.paybak.domain.model.newId
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Receipt photos taken in the scan camera: JPEGs in the app's `photos/` folder, next to the picked
 * ones (app-architecture §3.7), referenced from the expense by file name.
 */
object ReceiptPhotos {
    private const val QUALITY = 85

    private fun dir(context: Context) = File(context.filesDir, "photos").apply { mkdirs() }

    /** Saves [image] and returns its file name; null if it couldn't be written. */
    suspend fun save(context: Context, image: Bitmap): String? =
        withContext(Dispatchers.IO) {
            val name = "${newId()}.jpg"
            try {
                File(dir(context), name).outputStream().use {
                    image.compress(Bitmap.CompressFormat.JPEG, QUALITY, it)
                }
                name
            } catch (_: IOException) {
                null
            }
        }

    suspend fun load(context: Context, name: String): Bitmap? =
        withContext(Dispatchers.IO) { BitmapFactory.decodeFile(File(dir(context), name).path) }
}
