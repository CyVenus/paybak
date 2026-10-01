package app.paybak.paybak.service.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import app.paybak.paybak.MainActivity
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.view
import app.paybak.paybak.domain.LedgerSnapshot
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.inboxLink
import app.paybak.paybak.domain.calc.inboxText
import app.paybak.paybak.domain.calc.paymentFor
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.service.NotificationActionReceiver
import app.paybak.paybak.service.ReminderAlarmReceiver
import app.paybak.paybak.ui.theme.PbColors
import java.time.LocalTime

/**
 * Paybak's local notifications (app-architecture §4, domain.md §10.1, activity §7). Nothing comes
 * from a server: an alarm wakes the app at the scheduler's next moment (09:00, 20:00, the reminder
 * time), `tick` creates the inbox items, and [sync] posts the new ones on their channel when their
 * Settings toggle is on. A friend's claim posts "Payment to confirm" with Confirm and Not received.
 * Read items and handled claims leave the shade. All calls run on the main thread.
 */
class NotificationService(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)
    private val app: PaybakApplication
        get() = context.applicationContext as PaybakApplication

    /** Inbox ids this process has already seen; null until the first [sync]. */
    private var seen: Set<String>? = null
    private var channelsReady = false

    /** Posts what arrived since the last call, clears what's handled and sets the next alarm. */
    fun sync(snapshot: LedgerSnapshot) {
        ensureChannels()
        val ledger = snapshot.ledger
        val previous = seen
        seen = ledger.inbox.mapTo(HashSet()) { it.id } + previous.orEmpty()
        if (previous != null) {
            NotificationPlan.toPost(ledger.inbox, previous, snapshot.view.now, ledger.settings.push)
                .forEach { post(snapshot.view, it) }
        }
        clearHandled(ledger)
        scheduleNextTick(ledger)
    }

    /** Posts "Payment to confirm" (Confirm / Not received) for a friend's claim [paymentId]. */
    fun postPaymentToConfirm(paymentId: String) {
        val view = app.ledger.view
        val payment = view.ledger.payment(paymentId) ?: return
        if (payment.status != PaymentStatus.Pending) return
        if (!view.ledger.settings.push.paymentsToConfirm) return
        ensureChannels()
        val pronoun = view.person(payment.fromId)?.pronoun?.subject ?: "they"
        val body =
            context.getString(
                R.string.activity_push_claim_body,
                view.first(payment.fromId),
                pronoun,
                Money.format(payment.amount, payment.currency),
                view.paymentFor(payment),
            )
        val link = "paybak://activity?claim=$paymentId"
        val confirm =
            PendingIntent.getBroadcast(
                context,
                CLAIM_TAG.hashCode() + paymentId.hashCode(),
                Intent(context, NotificationActionReceiver::class.java)
                    .setAction(NotificationActionReceiver.ACTION_CONFIRM)
                    .putExtra(NotificationActionReceiver.EXTRA_PAYMENT_ID, paymentId),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val notification =
            builder(NotificationChannelKind.Payments, link)
                .setContentTitle(context.getString(R.string.activity_push_claim_title))
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .addAction(0, context.getString(R.string.pb_confirm), confirm)
                .addAction(
                    0,
                    context.getString(R.string.pb_not_received),
                    openLink("$link&action=notReceived"),
                )
                .build()
        notify(claimTag(paymentId), notification)
    }

    /** Posts the inbox item [itemId] now, however old: the debug menu's "Fire … now". */
    fun postInboxItem(itemId: String) {
        val view = app.ledger.view
        val item = view.ledger.inbox.firstOrNull { it.id == itemId } ?: return
        ensureChannels()
        post(view, item)
    }

    /** Removes the claim's notification once it's handled (Confirm from the shade). */
    fun cancelClaim(paymentId: String) = manager.cancel(claimTag(paymentId), NOTIFICATION_ID)

    private fun post(view: LedgerView, item: InboxItem) {
        val (title, body) = view.inboxText(item)
        val notification =
            builder(NotificationPlan.channel(item.type), view.inboxLink(item))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .build()
        notify(inboxTag(item.id), notification)
    }

    private fun builder(channel: NotificationChannelKind, link: String?) =
        NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.notification_small_icon)
            .setColor(PbColors.Bg.Inverse.toArgb())
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openLink(link))

    /** Opens the app on [link] (app-architecture §2.6); MainActivity hands it to the navigator. */
    private fun openLink(link: String?): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        link?.let { intent.putExtra(MainActivity.EXTRA_LINK, it) }
        return PendingIntent.getActivity(
            context,
            link.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun notify(tag: String, notification: Notification) {
        if (!canPost()) return
        try {
            manager.notify(tag, NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // The permission was revoked between the check and the post.
        }
    }

    private fun canPost(): Boolean =
        manager.areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED)

    /** Read inbox items and claims that are no longer pending leave the shade. */
    private fun clearHandled(ledger: Ledger) {
        val unread = ledger.inbox.filterNot { it.read }.mapTo(HashSet()) { inboxTag(it.id) }
        val pending =
            ledger.payments
                .filter { it.status == PaymentStatus.Pending }
                .mapTo(HashSet()) { claimTag(it.id) }
        manager.activeNotifications
            .mapNotNull { it.tag }
            .filter {
                (it.startsWith(INBOX_TAG) && it !in unread) ||
                    (it.startsWith(CLAIM_TAG) && it !in pending)
            }
            .forEach { manager.cancel(it, NOTIFICATION_ID) }
    }

    /**
     * Wakes the app at the scheduler's next moment (inexact, allowed while idle). A pinned debug
     * clock doesn't follow the wall clock, so nothing is scheduled then.
     */
    private fun scheduleNextTick(ledger: Ledger) {
        val alarms = context.getSystemService<AlarmManager>() ?: return
        val wake =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, ReminderAlarmReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        if (app.clock.pinned.value != null) {
            alarms.cancel(wake)
            return
        }
        val at =
            NotificationPlan.nextTick(
                app.clock.now(),
                app.clock.zone,
                LocalTime.parse(ledger.settings.reminderSchedule.time),
            )
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), wake)
    }

    private fun ensureChannels() {
        if (channelsReady) return
        channelsReady = true
        val channels =
            listOf(
                NotificationChannelKind.Payments to
                    (R.string.activity_channel_payments to
                        NotificationManagerCompat.IMPORTANCE_HIGH),
                NotificationChannelKind.Reminders to
                    (R.string.activity_channel_reminders to
                        NotificationManagerCompat.IMPORTANCE_DEFAULT),
                NotificationChannelKind.Summaries to
                    (R.string.activity_channel_summaries to
                        NotificationManagerCompat.IMPORTANCE_DEFAULT),
                NotificationChannelKind.Activity to
                    (R.string.activity_channel_activity to
                        NotificationManagerCompat.IMPORTANCE_DEFAULT),
            )
        manager.createNotificationChannelsCompat(
            channels.map { (kind, spec) ->
                NotificationChannelCompat.Builder(kind.id, spec.second)
                    .setName(context.getString(spec.first))
                    .build()
            }
        )
    }

    private companion object {
        const val NOTIFICATION_ID = 1
        const val CLAIM_TAG = "claim:"
        const val INBOX_TAG = "inbox:"

        fun claimTag(paymentId: String) = CLAIM_TAG + paymentId

        fun inboxTag(itemId: String) = INBOX_TAG + itemId
    }
}
