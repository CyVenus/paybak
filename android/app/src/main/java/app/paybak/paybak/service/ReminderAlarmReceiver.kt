package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.paybak.paybak.service.notifications.tickAndNotify

/**
 * The scheduler's alarm (domain.md §10.1): runs `tick`, which creates the reminders, overdue alerts
 * and the month-end summary due by now; the notification service then posts the new inbox items and
 * sets the next alarm.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        tickAndNotify(context)
    }
}
