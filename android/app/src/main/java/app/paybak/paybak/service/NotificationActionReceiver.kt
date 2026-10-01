package app.paybak.paybak.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.service.notifications.withLedger

/**
 * Handles a notification's actions without opening the app (app-architecture §2.6): "Confirm" on a
 * payment claim confirms it, which updates Home and the Activity tab, and clears the notification.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CONFIRM) return
        val paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID) ?: return
        withLedger(context) { app, ledger ->
            val payment = ledger.ledger.value.payment(paymentId)
            if (payment?.status == PaymentStatus.Pending) ledger.confirmPayment(paymentId)
            app.notifications.cancelClaim(paymentId)
        }
    }

    companion object {
        const val ACTION_CONFIRM = "app.paybak.paybak.action.CONFIRM_PAYMENT"
        const val EXTRA_PAYMENT_ID = "paymentId"
    }
}
