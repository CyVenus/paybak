package app.paybak.paybak.service.qr

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** What a scan gave: the code's text, nothing (cancelled), or no scanner on this device. */
sealed interface QrScan {
    data class Scanned(val text: String) : QrScan

    data object Cancelled : QrScan

    data object Unavailable : QrScan
}

/**
 * Scans a friend's QR code with Google's code scanner: its own camera UI, no camera permission
 * (app-architecture §4). Devices without Google Play services report [QrScan.Unavailable].
 */
object QrScanner {
    suspend fun scan(context: Context): QrScan = suspendCancellableCoroutine { continuation ->
        val options =
            GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        runCatching { GmsBarcodeScanning.getClient(context, options).startScan() }
            .onFailure { continuation.resume(QrScan.Unavailable) }
            .onSuccess { task ->
                task
                    .addOnSuccessListener { barcode ->
                        val text = barcode.rawValue
                        continuation.resume(
                            if (text == null) QrScan.Cancelled else QrScan.Scanned(text)
                        )
                    }
                    .addOnCanceledListener { continuation.resume(QrScan.Cancelled) }
                    .addOnFailureListener { continuation.resume(QrScan.Unavailable) }
            }
    }
}
