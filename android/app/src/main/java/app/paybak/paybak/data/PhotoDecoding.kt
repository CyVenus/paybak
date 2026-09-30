package app.paybak.paybak.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.IOException
import kotlin.math.min

/**
 * Reads the image at [uri] upright (following its EXIF orientation), crops the centre square and
 * scales it to [size] × [size] px. Null when [uri] isn't a readable image.
 */
internal fun decodeSquarePhoto(resolver: ContentResolver, uri: Uri, size: Int): Bitmap? =
    try {
        decodeSampled(resolver, uri, size)?.squareCrop(size, exifRotation(resolver, uri))
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }

/** The largest power of two that keeps a decoded short side of at least [target] px. */
internal fun sampleSize(shortSide: Int, target: Int): Int {
    var sample = 1
    while (shortSide / (sample * 2) >= target) sample *= 2
    return sample
}

/** Decodes at a reduced sample size, so large camera photos stay cheap. */
private fun decodeSampled(resolver: ContentResolver, uri: Uri, size: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val shortSide = min(bounds.outWidth, bounds.outHeight)
    if (shortSide <= 0) return null
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(shortSide, size) }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private fun exifRotation(resolver: ContentResolver, uri: Uri): Int =
    resolver.openInputStream(uri)?.use { stream ->
        when (
            ExifInterface(stream)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } ?: 0

/** The centre square, scaled to [size] px and turned by [degrees], in one pass. */
private fun Bitmap.squareCrop(size: Int, degrees: Int): Bitmap {
    val side = min(width, height)
    val scale = size.toFloat() / side
    val matrix =
        Matrix().apply {
            postScale(scale, scale)
            postRotate(degrees.toFloat())
        }
    return Bitmap.createBitmap(
        this,
        (width - side) / 2,
        (height - side) / 2,
        side,
        side,
        matrix,
        true,
    )
}
