package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Loan
import java.time.temporal.ChronoUnit

/** An installment row: "Paid 14 Sep · 2 days late", "Overdue 4 days" (red), "Due Fri 30 Oct". */
data class InstallmentRow(val installment: Installment, val label: String, val overdue: Boolean)

/** The loan detail's numbers (§9). */
data class LoanDetail(
    val loan: Loan,
    val paid: Long,
    val remaining: Long,
    val percentPaid: Int,
    val paidBackOn: String?,
    val meta: String,
    val rows: List<InstallmentRow>,
    val lastReminder: String?,
)

fun LedgerView.loanDetail(loanId: String): LoanDetail? {
    val loan = loan(loanId) ?: return null
    val installments = installments(loan)
    val paid = confirmedPayments().filter { it.loanId == loanId }.sumOf { it.amount }
    val remaining = maxOf(0, loan.amount - paid)
    val rows = installments.map { installment ->
        val due = installment.due
        when {
            installment.paidOn != null -> {
                val late = if (due == null) 0 else ChronoUnit.DAYS.between(due, installment.paidOn)
                val suffix = if (late > 0) " · $late day${if (late == 1L) "" else "s"} late" else ""
                InstallmentRow(installment, "Paid ${Dates.short(installment.paidOn)}$suffix", false)
            }
            due != null && due.isBefore(today) ->
                InstallmentRow(installment, Dates.dueBadge(due, today), true)
            due != null -> InstallmentRow(installment, Dates.dueLabel(due), false)
            else -> InstallmentRow(installment, "", false)
        }
    }
    val last = ledger.reminders.filter { it.loanId == loanId }.maxByOrNull { it.sentAt }
    return LoanDetail(
        loan = loan,
        paid = paid,
        remaining = remaining,
        percentPaid =
            if (loan.amount == 0L) 0
            else Math.round(paid * 100.0 / loan.amount).toInt().coerceAtMost(100),
        paidBackOn =
            if (remaining == 0L)
                installments.lastOrNull()?.paidOn?.let { "Paid back on ${Dates.short(it)}" }
            else null,
        meta = "${loan.title} · ${Dates.loanMetaDate(loan.date, today)}",
        rows = rows,
        lastReminder = last?.let { "Last reminder sent ${Dates.day(localDate(it.sentAt))}" },
    )
}
