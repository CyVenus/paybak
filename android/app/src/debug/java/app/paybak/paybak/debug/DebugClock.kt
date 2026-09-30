package app.paybak.paybak.debug

import android.content.Context
import androidx.core.content.edit
import app.paybak.paybak.PaybakApplication
import java.time.Instant

/**
 * A pinned clock survives relaunches in debug builds until "Use real time", so a relaunch keeps the
 * Figma-parity state (app-architecture §3.8).
 */
internal object DebugClock {
    private const val PREFS = "debug"
    private const val KEY_PINNED = "pinned_clock"

    fun pin(app: PaybakApplication, moment: Instant?) {
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (moment == null) remove(KEY_PINNED) else putString(KEY_PINNED, moment.toString())
        }
        app.clock.pin(moment)
    }

    /** Re-pins a clock pinned in an earlier launch. */
    fun restore(app: PaybakApplication) {
        val saved =
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PINNED, null)
        if (app.clock.pinned.value == null && saved != null) app.clock.pin(Instant.parse(saved))
    }
}
