package app.paybak.paybak

import android.app.Application
import app.paybak.paybak.data.ProfileStore
import app.rive.RiveLog
import app.rive.runtime.kotlin.core.Rive

class PaybakApplication : Application() {
    /** The saved profile, shared by every screen. */
    val profileStore: ProfileStore by lazy { ProfileStore(this) }

    override fun onCreate() {
        super.onCreate()
        // Loads the native Rive libraries. Required before any app.rive API, and not automatic:
        // the AAR removes its androidx.startup initializer. Without it every illustration stays
        // blank.
        Rive.init(this)
        if (BuildConfig.DEBUG) {
            RiveLog.logger = RiveLog.LogcatLogger()
        }
    }
}
