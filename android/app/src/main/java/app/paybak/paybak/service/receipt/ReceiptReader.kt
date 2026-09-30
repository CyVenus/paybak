package app.paybak.paybak.service.receipt

import android.graphics.Bitmap
import app.paybak.paybak.domain.model.ReceiptScan

/**
 * Reads a receipt photo with on-device text recognition (ML Kit, bundled model) and parses it into
 * a [ReceiptScan] (app-architecture §4.1). STUB owned by lane C (M9): it reads nothing yet.
 */
object ReceiptReader {
    /** Null when nothing could be read ("We couldn’t read this receipt."). */
    suspend fun read(image: Bitmap): ReceiptScan? = null
}
