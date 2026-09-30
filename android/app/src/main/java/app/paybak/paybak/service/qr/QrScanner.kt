package app.paybak.paybak.service.qr

import android.content.Context

/**
 * Scans a friend's QR code with Google's code scanner (no camera permission; app-architecture §4).
 * STUB owned by lane B (M4): it scans nothing yet.
 */
object QrScanner {
    /** The scanned text (an invite link), or null when cancelled or unavailable. */
    suspend fun scan(context: Context): String? = null
}
