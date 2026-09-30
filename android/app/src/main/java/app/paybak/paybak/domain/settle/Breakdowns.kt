package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.SettlePlan
import app.paybak.paybak.domain.calc.SettleRow
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.calc.simplifiedFootnoteGroups
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.ME

/**
 * One person on a balance breakdown (screens-settle §1–2): their net as an unsigned amount, what
 * it's for, and under it the red "Overdue 3 days" badge or "Due Sun 4 Oct".
 */
data class BreakdownRow(
    val friendId: String,
    val name: String,
    val subtitle: String,
    val amount: String,
    val dueLabel: String?,
    val overdue: String?,
)

/**
 * The screen behind a Home balance card: the total ("+₹2,900", "−₹1,850", or "₹0" when there is
 * none) with Home's caption, one row per person, and the simplified-debts footnotes.
 */
data class Breakdown(
    val total: String,
    val caption: String,
    val rows: List<BreakdownRow>,
    val footnotes: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = rows.isEmpty()
}

/** You’re owed: everyone whose net is in your favour, overdue first (§1.2). */
fun LedgerView.owedBreakdown(plan: SettlePlan = settlePlan()): Breakdown {
    val totals = homeTotals()
    if (plan.get.isEmpty()) return Breakdown(Money.format(0, defaultCurrency), NOBODY_OWES, emptyList())
    return Breakdown(
        Money.format(totals.owed, defaultCurrency, MoneySign.Signed),
        totals.owedCaption,
        plan.get.map(::breakdownRow),
    )
}

/**
 * You owe: everyone you pay (§2.2), with "{Group} uses simplified debts, so you pay {Name}
 * directly." for each group whose plan re-routes your debt.
 */
fun LedgerView.oweBreakdown(plan: SettlePlan = settlePlan()): Breakdown {
    val totals = homeTotals()
    if (plan.pay.isEmpty()) return Breakdown(Money.format(0, defaultCurrency), OWE_NOBODY, emptyList())
    return Breakdown(
        Money.format(-totals.owe, defaultCurrency, MoneySign.Signed),
        totals.oweCaption,
        plan.pay.map(::breakdownRow),
        simplifiedFootnotes(),
    )
}

private fun LedgerView.breakdownRow(row: SettleRow): BreakdownRow {
    val due = row.due
    val overdue = due != null && Dates.isOverdue(due, today)
    return BreakdownRow(
        row.friendId,
        first(row.friendId),
        row.context,
        Money.format(row.amount, defaultCurrency),
        dueLabel = due?.takeUnless { overdue }?.let(Dates::dueLabel),
        overdue = due?.takeIf { overdue }?.let { Dates.dueBadge(it, today) },
    )
}

private fun LedgerView.simplifiedFootnotes(): List<String> =
    simplifiedFootnoteGroups().mapNotNull { name ->
        val payees =
            ledger.groups
                .filter { it.name == name && it.simplifyDebts && it.kind == GroupKind.Group }
                .map { group -> groupPlan(group.id).filter { it.debtorId == ME } }
                .firstOrNull { it.isNotEmpty() }
                ?.map { first(it.creditorId) } ?: return@mapNotNull null
        "$name uses simplified debts, so you pay ${listPhrase(payees)} directly."
    }

/** "Kabir", "Kabir and Priya", "Kabir, Priya and Dev". */
internal fun listPhrase(items: List<String>): String =
    if (items.size <= 1) items.joinToString()
    else items.dropLast(1).joinToString(", ") + " and " + items.last()

private const val NOBODY_OWES = "Nobody owes you right now"
private const val OWE_NOBODY = "You don’t owe anyone right now"
