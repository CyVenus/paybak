package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import java.time.LocalDate

enum class HomeState {
    FirstDay,
    Active,
    AllSettled,
}

enum class DueAction {
    Remind,
    Settle,
}

/** A Home "Due soon" row (§6.1): a friend with the item, or a group you owe ("Your share"). */
data class DueSoonRow(
    val title: String,
    val detail: String,
    val amount: Long,
    val badge: String,
    val overdue: Boolean,
    val action: DueAction,
    val due: LocalDate,
    val obligation: Obligation,
)

/** A Recent activity row on Home (§6.1): a timeline event with Home's own copy. */
data class HomeActivityRow(
    val event: TimelineEvent,
    val title: String,
    val subtitle: String,
    val amount: String,
    val primary: Boolean,
    val date: String,
)

data class HomeSummary(
    val state: HomeState,
    val totals: HomeTotals,
    val dueSoon: List<DueSoonRow>,
    val recent: List<HomeActivityRow>,
    val pendingClaims: List<ClaimCard>,
)

/** Overdue or due within 2 days, at most 3; your group debts show the group (§6.1). */
fun LedgerView.dueSoon(): List<DueSoonRow> =
    openItems()
        .filter { it.due != null && !it.due.isAfter(today.plusDays(2)) }
        .map { item ->
            val due = item.due!!
            val badge = Dates.dueBadge(due, today)
            val overdue = due.isBefore(today)
            if (item.debtorId == ME && item.kind == ObligationKind.Group) {
                DueSoonRow(
                    group(item.ref)?.name ?: item.title,
                    "Your share",
                    item.amount,
                    badge,
                    overdue,
                    DueAction.Settle,
                    due,
                    item,
                )
            } else {
                val action = if (item.owedToMe) DueAction.Remind else DueAction.Settle
                DueSoonRow(
                    first(item.friendId),
                    item.title,
                    item.amount,
                    badge,
                    overdue,
                    action,
                    due,
                    item,
                )
            }
        }
        .sortedBy { it.due }
        .take(3)

fun LedgerView.homeSummary(events: List<TimelineEvent> = timeline()): HomeSummary {
    val totals = homeTotals()
    val claims = pendingClaims()
    val state =
        when {
            ledger.isEmpty -> HomeState.FirstDay
            totals.owed == 0L && totals.owe == 0L && claims.isEmpty() -> HomeState.AllSettled
            else -> HomeState.Active
        }
    return HomeSummary(
        state,
        totals,
        dueSoon(),
        events.filter { it.home }.take(3).map(::homeRow),
        claims,
    )
}

private fun LedgerView.homeRow(event: TimelineEvent): HomeActivityRow {
    val date = Dates.rowDate(localDate(event.at), today)
    if (event.kind == TimelineKind.Payment) {
        val payment = ledger.payment(event.ref)!!
        val toMe = payment.toId == ME
        return HomeActivityRow(
            event,
            event.title,
            payment.method.label,
            Money.format(
                if (toMe) payment.amount else -payment.amount,
                payment.currency,
                MoneySign.Debit,
            ),
            toMe,
            date,
        )
    }
    val expense = ledger.expense(event.ref)!!
    if (expense.payerId == ME) {
        return HomeActivityRow(
            event,
            expense.title,
            "You paid · ${expense.split.rows.size} people",
            Money.format(expense.amount, expense.currency),
            true,
            date,
        )
    }
    val word = if (iOwePayer(expense)) "You owe" else "Your share"
    return HomeActivityRow(
        event,
        expense.title,
        "${groupOf(expense)?.name ?: first(expense.payerId)} · $word",
        Money.format(-myShare(expense), expense.currency, MoneySign.Debit),
        false,
        date,
    )
}

/** A pending payment you recorded, still waiting for the friend to confirm. */
fun LedgerView.pendingPaymentTo(friendId: String) =
    ledger.payments.lastOrNull {
        it.fromId == ME && it.toId == friendId && it.status == PaymentStatus.Pending
    }
