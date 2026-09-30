package app.paybak.paybak.feature.pickers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import app.paybak.paybak.data.sampleSize
import app.paybak.paybak.domain.model.newId
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Receipts, payment proofs and project covers (app-architecture §3.7): JPEGs in the app's `photos/`
 * folder, referenced from the ledger by file name.
 */
object LedgerPhotos {
    private const val LONG_SIDE = 1600
    private const val QUALITY = 85

    private fun dir(context: Context) = File(context.filesDir, "photos").apply { mkdirs() }

    /**
     * Copies the picked image upright and scaled down; returns its file name, null if unreadable.
     */
    suspend fun save(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = decode(context, uri) ?: return@withContext null
                val name = "${newId()}.jpg"
                File(dir(context), name).outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it)
                }
                name
            } catch (_: IOException) {
                null
            } catch (_: SecurityException) {
                null
            }
        }

    suspend fun load(context: Context, name: String): ImageBitmap? =
        withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(File(dir(context), name).path)?.asImageBitmap()
        }

    private fun decode(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val longSide = maxOf(bounds.outWidth, bounds.outHeight)
        if (longSide <= 0) return null
        val options =
            BitmapFactory.Options().apply { inSampleSize = sampleSize(longSide, LONG_SIDE) }
        val bitmap =
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return null
        val degrees =
            resolver.openInputStream(uri)?.use {
                when (
                    ExifInterface(it)
                        .getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL,
                        )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

/** Opens the system photo picker; [onPicked] gets the saved file name. */
@Composable
fun rememberPhotoPicker(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latest by rememberUpdatedState(onPicked)
    val launcher =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) scope.launch { LedgerPhotos.save(context, uri)?.let(latest) }
        }
    return { launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
}

/** A saved photo, once loaded; null without one (bundled demo art draws its own placeholder). */
@Composable
fun rememberLedgerPhoto(file: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by
        produceState<ImageBitmap?>(null, file) {
            value = file?.let { LedgerPhotos.load(context, it) }
        }
    return image
}
