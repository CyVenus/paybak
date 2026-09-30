package app.paybak.paybak.debug

import android.content.Intent
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.StartTarget

/** Release builds have no debug hooks: they always start normally. */
object DebugLaunch {
    fun startTarget(intent: Intent, app: PaybakApplication, firstLaunch: Boolean): StartTarget? =
        null
}
