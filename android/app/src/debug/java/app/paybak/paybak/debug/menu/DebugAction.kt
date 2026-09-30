package app.paybak.paybak.debug.menu

import android.app.Activity
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.navigation.MainNavigator

/** What a debug action can reach. */
class DebugContext(val app: PaybakApplication, val navigator: MainNavigator, val activity: Activity)

/** One row of the debug menu; [run] happens after the menu closes. */
data class DebugAction(
    val title: String,
    val subtitle: String? = null,
    val run: DebugContext.() -> Unit,
)

/** A titled group of rows (each module has its own file of actions). */
data class DebugSection(val title: String, val actions: List<DebugAction>)
