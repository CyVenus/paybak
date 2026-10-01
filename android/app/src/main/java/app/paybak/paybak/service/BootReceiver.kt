package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.paybak.paybak.service.notifications.tickAndNotify

/** A reboot clears alarms: catch up with `tick` and schedule the next one (domain.md §10.1). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        tickAndNotify(context)
    }
}
