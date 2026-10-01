package app.paybak.paybak.debug.menu

import android.os.Handler
import android.os.Looper
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus

/** Long enough to lock the device first. */
private const val DELAY_MILLIS = 5_000L

/**
 * The debug menu's Activity section, owned by lane A (app-architecture §3.10): post the local
 * notifications now instead of waiting for 9:00 pm, the month's last evening or the day after a due
 * date. Each posts the latest inbox item of its kind (a demo has them all).
 */
internal val ActivityDebugActions: List<DebugAction> =
    listOf(
        DebugAction("Fire the Kabir reminder now", "The latest Payment reminder") {
            postLatest(InboxType.PaymentReminder)
        },
        DebugAction("Post the monthly summary now") { postLatest(InboxType.MonthlySummary) },
        DebugAction("Post Rohan’s overdue alert", "The latest Payment overdue") {
            postLatest(InboxType.PaymentOverdue)
        },
        DebugAction(
            "Deliver the next notification in 5 s",
            "A pending claim to you, else the latest inbox item",
        ) {
            Handler(Looper.getMainLooper()).postDelayed({ postNext() }, DELAY_MILLIS)
        },
    )

private fun DebugContext.postLatest(type: InboxType) {
    app.ledger.ledger.value.inbox
        .filter { it.type == type }
        .maxByOrNull { it.createdAt }
        ?.let { app.notifications.postInboxItem(it.id) }
}

private fun DebugContext.postNext() {
    val ledger = app.ledger.ledger.value
    val claim =
        ledger.payments
            .filter { it.toId == ME && it.status == PaymentStatus.Pending }
            .maxByOrNull { it.createdAt }
    if (claim != null) {
        app.notifications.postPaymentToConfirm(claim.id)
    } else {
        ledger.inbox.maxByOrNull { it.createdAt }?.let { app.notifications.postInboxItem(it.id) }
    }
}
