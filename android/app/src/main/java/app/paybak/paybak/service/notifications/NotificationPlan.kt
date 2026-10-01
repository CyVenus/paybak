package app.paybak.paybak.service.notifications

import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.PushSettings
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/** The four channels (app-architecture §4); ids are stable, names are the user's. */
enum class NotificationChannelKind(val id: String) {
    Payments("payments"),
    Reminders("reminders"),
    Summaries("summaries"),
    Activity("activity"),
}

/**
 * What the device should post and when to wake up next (domain.md §10.1). Pure: the service turns
 * it into notifications and an alarm.
 */
object NotificationPlan {
    /**
     * An inbox item is only news for a short while: one older than this (seeded history, a demo
     * load, an alert that fired while the phone slept) shows in the inbox but isn't posted.
     */
    val FRESH: Duration = Duration.ofMinutes(10)

    /** The scheduler's daily moments: recurring and overdue at 09:00, the summary at 20:00. */
    private val dailyJobs = listOf(LocalTime.of(9, 0), LocalTime.of(20, 0))

    fun channel(type: InboxType): NotificationChannelKind =
        when (type) {
            InboxType.PaymentConfirmed,
            InboxType.PaymentNotReceived -> NotificationChannelKind.Payments
            InboxType.PaymentReminder,
            InboxType.PaymentOverdue -> NotificationChannelKind.Reminders
            InboxType.MonthlySummary -> NotificationChannelKind.Summaries
            InboxType.NewExpenseInGroup,
            InboxType.ExpenseFlagged,
            InboxType.FlagResolved -> NotificationChannelKind.Activity
        }

    /** The Settings › Notifications toggle that governs [type]. */
    fun isOn(type: InboxType, push: PushSettings): Boolean =
        when (type) {
            InboxType.PaymentConfirmed,
            InboxType.PaymentNotReceived -> push.paymentsToConfirm
            InboxType.PaymentReminder -> push.reminders
            InboxType.PaymentOverdue -> push.overdueAlerts
            InboxType.MonthlySummary -> push.monthlySummary
            InboxType.NewExpenseInGroup,
            InboxType.ExpenseFlagged,
            InboxType.FlagResolved -> push.addedToExpense
        }

    /**
     * The unread items that arrived since [seen] was taken, are still fresh and are switched on.
     */
    fun toPost(
        inbox: List<InboxItem>,
        seen: Set<String>,
        now: Instant,
        push: PushSettings,
    ): List<InboxItem> = inbox.filter {
        it.id !in seen &&
            !it.read &&
            !it.createdAt.isAfter(now) &&
            Duration.between(it.createdAt, now) <= FRESH &&
            isOn(it.type, push)
    }

    /**
     * The next moment `tick` has work: 09:00, 20:00 or the reminder time, whichever comes first
     * after [now] in [zone]. The alarm wakes the app then, `tick` creates the inbox items and the
     * service posts them.
     */
    fun nextTick(now: Instant, zone: ZoneId, reminderTime: LocalTime): Instant {
        val times = (dailyJobs + reminderTime).distinct()
        val today = now.atZone(zone).toLocalDate()
        return (0L..1L)
            .flatMap { offset -> times.map { today.plusDays(offset).atTime(it).atZone(zone) } }
            .map { it.toInstant() }
            .filter { it.isAfter(now) }
            .min()
    }
}
