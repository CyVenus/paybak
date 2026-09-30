package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles a notification's actions without opening the app: "Confirm" on a payment claim
 * (app-architecture §2.6). STUB owned by lane A (M6): declared in the manifest by M2.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
