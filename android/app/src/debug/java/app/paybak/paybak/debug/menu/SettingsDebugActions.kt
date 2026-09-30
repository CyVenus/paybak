package app.paybak.paybak.debug.menu

import app.paybak.paybak.data.ledger.actions.setRemindersMuted

private const val ROHAN = "p-rohan"

/** The debug menu's Settings section (lane C): data for Muted friends. */
internal val SettingsDebugActions: List<DebugAction> =
    listOf(
        DebugAction("Mute Rohan’s reminders", "Fills Settings › Muted friends") {
            if (app.ledger.ledger.value.people.any { it.id == ROHAN }) {
                app.ledger.setRemindersMuted(ROHAN, true)
            }
        }
    )
