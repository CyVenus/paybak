package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reschedules the pending local notifications after a reboot. STUB owned by lane A (M6): declared
 * in the manifest by M2.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
