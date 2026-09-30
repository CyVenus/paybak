package app.paybak.paybak.service.camera

import android.os.Build

/**
 * The receipt camera's feed (app-architecture §4): CameraX on devices; on the emulator the bundled
 * receipt image stands in, so the real OCR path still runs. STUB owned by lane C (M9).
 */
object CameraSource {
    /** True on emulators, where the feed is simulated. */
    val isSimulated: Boolean
        get() = Build.FINGERPRINT.contains("generic") || Build.PRODUCT.contains("sdk")
}
