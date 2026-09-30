package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires a scheduled reminder, overdue alert or monthly summary at its time (domain.md §10.1). STUB
 * owned by lane A (M6): declared in the manifest by M2.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
