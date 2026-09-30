package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.model.ME
import java.time.LocalDate

/**
 * The Export screen's rows that start ticked for a date range (§6.5): each group with a record in
 * range (an expense date, a payment involving you, a component status change), plus
 * [WITHOUT_A_GROUP] for direct expenses, payments and loans.
 */
fun LedgerView.exportDefaultTicks(start: LocalDate, end: LocalDate): Set<String> {
    fun inRange(d: LocalDate) = !d.isBefore(start) && !d.isAfter(end)
    val ticked = mutableSetOf<String>()
    liveExpenses()
        .filter { inRange(it.date) }
        .forEach {
            ticked += groupOf(it)?.name ?: WITHOUT_A_GROUP
        }
    confirmedPayments()
        .filter { inRange(it.date) && ME in setOf(it.fromId, it.toId) }
        .forEach {
            ticked += it.groupId?.let { id -> group(id)?.name } ?: WITHOUT_A_GROUP
        }
    ledger.components
        .filter { inRange(localDate(it.statusChangedAt)) }
        .forEach {
            group(it.projectId)?.name?.let(ticked::add)
        }
    return ticked
}

/** The earliest record date: the start of the "All time" range. */
fun LedgerView.firstRecordDate(): LocalDate? =
    (ledger.expenses.map { it.date } +
            ledger.payments.map { it.date } +
            ledger.loans.map { it.date })
        .minOrNull()
