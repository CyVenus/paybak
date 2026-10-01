package app.paybak.paybak.feature.payments

import app.paybak.paybak.domain.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * Payments friends have just confirmed (`PaymentApprovals.approved`), waiting for the
 * payment-approved scene ([PaymentApprovedHost]). It lives as long as the process, so approvals
 * that arrive while no screen can show them wait. Everything waiting makes one scene.
 */
class PaymentApprovalQueue {
    private val waitingState = MutableStateFlow<List<Payment>>(emptyList())

    val waiting: StateFlow<List<Payment>> = waitingState.asStateFlow()

    fun add(payments: List<Payment>) {
        if (payments.isNotEmpty()) waitingState.update { it + payments }
    }

    /** Takes everything waiting, for one scene. */
    fun takeAll(): List<Payment> = waitingState.getAndUpdate { emptyList() }

    /** Sign out and Delete account: approvals of the old account never show. */
    fun clear() {
        waitingState.value = emptyList()
    }
}
