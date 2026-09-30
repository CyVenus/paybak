package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.ME
import java.time.Instant
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.roundToInt

/** A bar of Insights: [percent] adds up to 100 across its list (largest remainder). */
data class InsightShare(val key: String, val label: String, val amount: Long, val percent: Int)

data class MonthBar(val month: YearMonth, val label: String, val total: Long, val heightPt: Int)

/** Insights for one month (§6.4); amounts in the default currency. */
data class InsightsReport(
    val month: YearMonth,
    val total: Long,
    val trend: String?,
    val chart: List<MonthBar>,
    val categories: List<InsightShare>,
    val groups: List<InsightShare>,
    val lent: Long,
    val borrowed: Long,
    val since: YearMonth,
)

/** Monthly totals by category and group: the raw sums behind [insights]. */
data class MonthTotals(
    val total: Long,
    val categories: Map<String, Long>,
    val groups: Map<String, Long>,
)

const val WITHOUT_A_GROUP = "Without a group"
private const val CHART_MONTHS = 6
private const val CHART_HEIGHT_PT = 120

/**
 * Your share of the group and friend expenses dated in [month] (projects, payments, loans and
 * drafts are left out), in the default currency.
 */
fun LedgerView.insightExpenses(month: YearMonth, asOf: Instant? = null): List<Pair<Expense, Long>> =
    liveExpenses(asOf)
        .filter { YearMonth.from(it.date) == month && myShare(it) != 0L }
        .filter { expense -> expense.groupId?.let { group(it)?.isProject } != true }
        .map { it to toDefault(myShare(it), it.currency, it.rate) }

fun LedgerView.monthTotals(month: YearMonth, asOf: Instant? = null): MonthTotals {
    val categories = LinkedHashMap<String, Long>()
    val groups = LinkedHashMap<String, Long>()
    var total = 0L
    for ((expense, share) in insightExpenses(month, asOf)) {
        total += share
        categories[expense.category] = (categories[expense.category] ?: 0) + share
        val key = groupOf(expense)?.name ?: WITHOUT_A_GROUP
        groups[key] = (groups[key] ?: 0) + share
    }
    return MonthTotals(total, categories, groups)
}

fun LedgerView.insights(month: YearMonth): InsightsReport {
    val totals = monthTotals(month)
    val months = (CHART_MONTHS - 1 downTo 0).map { month.minusMonths(it.toLong()) }
    val monthTotals = months.map { monthTotals(it).total }
    val max = monthTotals.maxOrNull() ?: 0
    val chart =
        months.zip(monthTotals) { m, total ->
            val height =
                if (max == 0L) 0 else (CHART_HEIGHT_PT * total.toDouble() / max).roundToInt()
            MonthBar(m, Dates.shortMonth(m.month), total, height)
        }
    val previous = monthTotals[monthTotals.size - 2]
    val trend =
        if (previous == 0L) null
        else {
            val pct = (abs(totals.total - previous) * 100.0 / previous).roundToInt()
            val previousName = Dates.monthName(month.minusMonths(1).month)
            when {
                totals.total > previous -> "Up $pct% from $previousName"
                totals.total < previous -> "Down $pct% from $previousName"
                else -> "Same as $previousName"
            }
        }
    val since = months.first()
    val loans = ledger.loans.filter { !YearMonth.from(it.date).isBefore(since) }
    return InsightsReport(
        month = month,
        total = totals.total,
        trend = trend,
        chart = chart,
        categories = shares(totals.categories) { Category.of(it).label },
        groups = shares(totals.groups) { it },
        lent = loans.filter { it.lenderId == ME }.sumOf { it.amount },
        borrowed = loans.filter { it.borrowerId == ME }.sumOf { it.amount },
        since = since,
    )
}

private fun shares(values: Map<String, Long>, label: (String) -> String): List<InsightShare> {
    val sorted = values.filterValues { it != 0L }.entries.sortedByDescending { it.value }
    val percent = Splits.largestRemainderPercent(sorted.associate { it.key to it.value })
    return sorted.map { InsightShare(it.key, label(it.key), it.value, percent.getValue(it.key)) }
}
