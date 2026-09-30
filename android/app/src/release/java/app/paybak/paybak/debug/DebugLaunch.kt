package app.paybak.paybak.debug

import android.content.Intent
import app.paybak.paybak.StartTarget
import app.paybak.paybak.data.ProfileStore

/** Release builds have no debug hooks: they always start normally. */
object DebugLaunch {
    fun startTarget(
        intent: Intent,
        profileStore: ProfileStore,
        firstLaunch: Boolean,
    ): StartTarget? = null
}
