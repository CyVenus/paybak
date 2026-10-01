package app.paybak.paybak.domain.insights

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.MonthBar
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.calc.WITHOUT_A_GROUP
import app.paybak.paybak.domain.calc.insightExpenses
import app.paybak.paybak.domain.calc.insights
import app.paybak.paybak.domain.calc.loanDetail
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import java.time.LocalDate
import java.time.YearMonth

/**
 * One bar row of Insights (insights §2.6): a category, a group ("Without a group" has no [groupId])
 * or a friend. [percent]s add up to 100 across a list (largest remainder).
 */
data class InsightRow(
    val key: String,
    val label: String,
    val amount: Long,
    val percent: Int,
    /** The icon key of a category or group row. */
    val icon: String? = null,
    val personId: String? = null,
    val groupId: String? = null,
)

/** A loan's status badge in "Lent vs borrowed": Paid back, Overdue or its next due date. */
sealed interface LoanBadge {
    data object PaidBack : LoanBadge

    data object Overdue : LoanBadge

    data class Due(val date: LocalDate) : LoanBadge
}

/** "Kabir · Bike service" with its badge. */
data class LoanLine(
    val loanId: String,
    val personId: String,
    val label: String,
    val badge: LoanBadge?,
)

/** The Insights report for one month (insights §2.2–2.6), everything in the default currency. */
data class InsightsPage(
    val month: YearMonth,
    val monthLabel: String,
    val total: Long,
    val trend: String?,
    val chart: List<MonthBar>,
    val chartDescription: String,
    val categories: List<InsightRow>,
    val groups: List<InsightRow>,
    val friends: List<InsightRow>,
    /** "Lent vs borrowed since April". */
    val lentTitle: String,
    val lent: Long,
    val borrowed: Long,
    val loans: List<LoanLine>,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
)

/** The month of the earliest expense you have a share in: how far back Insights goes. */
fun LedgerView.firstInsightsMonth(): YearMonth? =
    liveExpenses().filter { myShare(it) != 0L }.minOfOrNull { it.date }?.let(YearMonth::from)

fun LedgerView.insightsPage(month: YearMonth): InsightsPage {
    val report = insights(month)
    val current = YearMonth.from(today)
    val first = firstInsightsMonth()
    val since = report.since
    val loans =
        ledger.loans
            .filter { YearMonth.from(it.date) in since..month }
            .sortedByDescending { it.date }
    return InsightsPage(
        month = month,
        monthLabel = "${Dates.monthName(month.month)} ${month.year}",
        total = report.total,
        trend = report.trend,
        chart = report.chart,
        chartDescription =
            "Your share by month: " +
                report.chart.joinToString(", ") {
                    "${Dates.monthName(it.month.month)} ${Money.format(it.total, defaultCurrency)}"
                },
        categories =
            report.categories.map {
                val category = Category.of(it.key)
                InsightRow(it.key, category.label, it.amount, it.percent, icon = category.icon)
            },
        groups = groupRows(month),
        friends = friendRows(month),
        lentTitle =
            "Lent vs borrowed since ${Dates.monthName(since.month)}" +
                if (since.year != month.year) " ${since.year}" else "",
        lent =
            loans.filter { it.lenderId == ME }.sumOf { toDefault(it.amount, it.currency, it.rate) },
        borrowed =
            loans
                .filter { it.borrowerId == ME }
                .sumOf { toDefault(it.amount, it.currency, it.rate) },
        loans =
            loans.map { loan ->
                LoanLine(
                    loan.id,
                    loan.friendId,
                    "${first(loan.friendId)} · ${loan.title}",
                    loanBadge(loan.id),
                )
            },
        hasPrevious = first != null && month > first,
        hasNext = month < current,
    )
}

/** Your share per group; expenses with friends outside any group are "Without a group". */
private fun LedgerView.groupRows(month: YearMonth): List<InsightRow> {
    val totals = LinkedHashMap<String?, Long>()
    for ((expense, share) in insightExpenses(month)) {
        totals[expense.groupId] = (totals[expense.groupId] ?: 0L) + share
    }
    return rows(totals.mapKeys { it.key ?: "" }) { key, amount, percent ->
        val group = group(key)
        InsightRow(
            key = key.ifEmpty { WITHOUT_A_GROUP },
            label = group?.name ?: WITHOUT_A_GROUP,
            amount = amount,
            percent = percent,
            icon = group?.icon ?: "people",
            groupId = group?.id,
        )
    }
}

/**
 * Your share per friend (insights §2.6 #8): each expense's share of yours is split evenly across
 * the other people on it, leftover paise rotating fairly, so the bars still add up to the total.
 */
private fun LedgerView.friendRows(month: YearMonth): List<InsightRow> {
    val totals = LinkedHashMap<String, Long>()
    var counter = 0
    for ((expense, share) in insightExpenses(month)) {
        val others =
            expense.split.rows.filter { it.included && it.personId != ME }.map { it.personId }
        if (others.isEmpty()) continue
        val split = Splits.equal(share, others, counter)
        counter = split.counter
        split.shares.forEach { (person, amount) ->
            totals[person] = (totals[person] ?: 0L) + amount
        }
    }
    return rows(totals) { key, amount, percent ->
        InsightRow(key, first(key), amount, percent, personId = key)
    }
}

/** Rows sorted by amount (largest first), ₹0 left out, percentages by largest remainder. */
private fun rows(
    totals: Map<String, Long>,
    row: (key: String, amount: Long, percent: Int) -> InsightRow,
): List<InsightRow> {
    val sorted = totals.filterValues { it != 0L }.entries.sortedByDescending { it.value }
    val percent = Splits.largestRemainderPercent(sorted.associate { it.key to it.value })
    return sorted.map { row(it.key, it.value, percent.getValue(it.key)) }
}

private fun LedgerView.loanBadge(loanId: String): LoanBadge? {
    val detail = loanDetail(loanId) ?: return null
    return when {
        detail.paidBackOn != null -> LoanBadge.PaidBack
        detail.rows.any { it.overdue } -> LoanBadge.Overdue
        else ->
            detail.rows
                .firstOrNull { it.installment.paidOn == null }
                ?.installment
                ?.due
                ?.let(LoanBadge::Due)
    }
}
