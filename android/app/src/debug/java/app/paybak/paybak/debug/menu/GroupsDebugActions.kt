package app.paybak.paybak.debug.menu

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import app.paybak.paybak.data.ledger.lanes.guestJoined
import app.paybak.paybak.feature.friends.addFriendFromCode
import app.paybak.paybak.service.contacts.ContactsDirectory
import kotlinx.coroutines.launch

private const val MEERA_LINK = "https://paybak.app/i/meera"
private const val ANANYA = "p-ananya"

/** The debug menu's Groups & Friends section, owned by lane B (app-architecture §3.10). */
internal val GroupsDebugActions: List<DebugAction> =
    listOf(
        DebugAction("Simulate a QR scan of paybak.app/i/meera", "Adds Meera if needed, opens her") {
            val activity = activity as? ComponentActivity ?: return@DebugAction
            activity.lifecycleScope.launch {
                val directory = ContactsDirectory.directory(app, app.ledger.ledger.value.people)
                val own = app.profileStore.profile.value.username
                addFriendFromCode(MEERA_LINK, own, directory, app.ledger, navigator)
            }
        },
        DebugAction("Ananya joins Paybak", "The guest loses the Guest tag") {
            app.ledger.guestJoined(ANANYA)
        },
    )
