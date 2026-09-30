package app.paybak.paybak.service.notifications

import android.content.Context
import app.paybak.paybak.domain.LedgerSnapshot

/**
 * Paybak's local notifications (app-architecture §4, domain.md §10.1): channels, scheduling
 * reminders at 21:00, the month-end summary and overdue alerts, and the "Payment to confirm" push
 * with its actions. [sync] runs after every ledger change. STUB owned by lane A (M6): it does
 * nothing yet; keep the signatures.
 */
class NotificationService(private val context: Context) {
    /** Reschedules everything the new [snapshot] needs and cancels what's settled. */
    fun sync(snapshot: LedgerSnapshot) = Unit

    /** Posts "Payment to confirm" (Confirm / Not received) for a friend's claim [paymentId]. */
    fun postPaymentToConfirm(paymentId: String) = Unit
}
