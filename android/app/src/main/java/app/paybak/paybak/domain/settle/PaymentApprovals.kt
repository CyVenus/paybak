package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.calc.joinNames
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentStatus

/**
 * The payer's side of a confirm: payments you made that the friend has just confirmed, found by
 * comparing the ledger before and after a change, so any source counts (the friend's confirm, a
 * sync). The payment-approved scene celebrates them.
 */
object PaymentApprovals {
    /**
     * Your payments to a friend that were pending in [before] and are confirmed in [after], in
     * [after]'s order. Claims a friend made to you are confirmed by you, so they never count.
     */
    fun approved(before: Ledger, after: Ledger): List<Payment> {
        val wasPending =
            before.payments.filter { it.status == PaymentStatus.Pending }.mapTo(HashSet()) { it.id }
        return after.payments.filter {
            it.isYoursToAFriend() && it.status == PaymentStatus.Confirmed && it.id in wasPending
        }
    }

    /** Payments [after] adds that you recorded paying a friend, waiting for their confirm. */
    fun recorded(before: Ledger, after: Ledger): List<Payment> {
        val known = before.payments.mapTo(HashSet()) { it.id }
        return after.payments.filter {
            it.id !in known &&
                it.isYoursToAFriend() &&
                it.recordedBy == ME &&
                it.status == PaymentStatus.Pending
        }
    }

    /**
     * The scene's line. One friend in one currency: "Meera confirmed ₹450" (several payments add
     * up). Otherwise "Meera and Kabir confirmed your payments".
     */
    fun headline(payments: List<Payment>, ledger: Ledger): String {
        val names = payments.map { it.toId }.distinct().map { ledger.person(it)?.firstName ?: "Someone" }
        val currency = payments.map { it.currency }.distinct().singleOrNull()
        if (names.size == 1 && currency != null) {
            return "${names[0]} confirmed ${Money.format(payments.sumOf { it.amount }, currency)}"
        }
        return "${joinNames(names)} confirmed your payments"
    }

    private fun Payment.isYoursToAFriend() = fromId == ME && toId != ME
}
