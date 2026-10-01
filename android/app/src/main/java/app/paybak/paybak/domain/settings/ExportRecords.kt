package app.paybak.paybak.domain.settings

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.WITHOUT_A_GROUP
import app.paybak.paybak.domain.calc.exportDefaultTicks
import app.paybak.paybak.domain.calc.firstRecordDate
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Rate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/** Export's range chips (screens-settings §9). */
enum class ExportRange {
    ThisMonth,
    Last3Months,
    AllTime,
}

/** The days a range covers, both included. */
data class ExportPeriod(val start: LocalDate, val end: LocalDate) {
    /** "1 Sep – 30 Sep 2026"; with two years, "12 Mar 2025 – 30 Sep 2026". */
    val label: String
        get() =
            if (start.year == end.year) Dates.rangeWithYear(start, end)
            else "${Dates.short(start)} ${start.year} – ${Dates.short(end)} ${end.year}"

    operator fun contains(day: LocalDate): Boolean = !day.isBefore(start) && !day.isAfter(end)
}

/** This month, the last three months (this one included) or everything up to today. */
fun LedgerView.exportPeriod(range: ExportRange): ExportPeriod {
    val monthStart = today.withDayOfMonth(1)
    return when (range) {
        ExportRange.ThisMonth -> ExportPeriod(monthStart, Dates.lastDayOfMonth(today))
        ExportRange.Last3Months ->
            ExportPeriod(monthStart.minusMonths(2), Dates.lastDayOfMonth(today))
        ExportRange.AllTime -> ExportPeriod(firstRecordDate()?.coerceAtMost(today) ?: today, today)
    }
}

/** A row of Export's group list: a group or project, or [NO_GROUP] ("Without a group"). */
data class ExportGroup(val id: String, val name: String)

/** The key of "Without a group": direct expenses, payments and loans. */
const val NO_GROUP = "withoutGroup"

/** Every group and project the user belongs to (archived ones too), then "Without a group". */
fun LedgerView.exportGroups(): List<ExportGroup> =
    ledger.groups.filter { ME in it.memberIds }.map { ExportGroup(it.id, it.name) } +
        ExportGroup(NO_GROUP, WITHOUT_A_GROUP)

/** The rows that start ticked: those with a record in [period] (§6.5). */
fun LedgerView.exportDefaultSelection(period: ExportPeriod): Set<String> {
    val names = exportDefaultTicks(period.start, period.end)
    return exportGroups().filter { it.name in names }.map { it.id }.toSet()
}

enum class ExportType(val label: String) {
    Expense("Expense"),
    Payment("Payment"),
    Loan("Loan"),
}

/**
 * One exported record with each person's share (for a payment or loan, the person who received
 * it). Amounts are minor units of [currency]; [defaultAmount] is in the default currency at the
 * record's saved rate.
 */
data class ExportRecord(
    val date: LocalDate,
    val group: ExportGroup,
    val type: ExportType,
    val title: String,
    val category: String?,
    val paidBy: String,
    val amount: Long,
    val currency: String,
    val rate: Rate?,
    val defaultAmount: Long,
    val shares: List<Pair<String, Long>>,
)

/**
 * The records of [period] in the [selected] rows, row by row in the list's order and oldest first
 * within a row: live expenses, confirmed payments (direct ones only when they involve the user)
 * and loans (always "Without a group").
 */
fun LedgerView.exportRecords(period: ExportPeriod, selected: Set<String>): List<ExportRecord> {
    val order = exportGroups()
    val groups = order.associateBy { it.id }
    fun rowOf(groupId: String?) = groups[groupId ?: NO_GROUP]?.takeIf { it.id in selected }
    fun name(id: String) = if (id == ME) "You" else person(id)?.name ?: "Someone"

    val expenses =
        liveExpenses()
            .filter { it.date in period }
            .mapNotNull { expense ->
                val group = rowOf(expense.groupId) ?: return@mapNotNull null
                ExportRecord(
                    expense.date,
                    group,
                    ExportType.Expense,
                    expense.title,
                    Category.of(expense.category).label,
                    name(expense.payerId),
                    expense.amount,
                    expense.currency,
                    expense.rate,
                    toDefault(expense.amount, expense.currency, expense.rate),
                    expense.split.rows.filter { it.included }.map { name(it.personId) to it.share },
                )
            }
    val payments =
        confirmedPayments()
            .filter { it.date in period && (it.groupId != null || ME in setOf(it.fromId, it.toId)) }
            .mapNotNull { payment ->
                val group = rowOf(payment.groupId) ?: return@mapNotNull null
                val payee = if (payment.toId == ME) "you" else name(payment.toId)
                ExportRecord(
                    payment.date,
                    group,
                    ExportType.Payment,
                    "${name(payment.fromId)} paid $payee",
                    null,
                    name(payment.fromId),
                    payment.amount,
                    payment.currency,
                    payment.rate,
                    toDefault(payment.amount, payment.currency, payment.rate),
                    listOf(name(payment.toId) to payment.amount),
                )
            }
    val loans =
        ledger.loans
            .filter { it.date in period }
            .mapNotNull { loan ->
                val group = rowOf(null) ?: return@mapNotNull null
                ExportRecord(
                    loan.date,
                    group,
                    ExportType.Loan,
                    loan.title,
                    null,
                    name(loan.lenderId),
                    loan.amount,
                    loan.currency,
                    loan.rate,
                    toDefault(loan.amount, loan.currency, loan.rate),
                    listOf(name(loan.borrowerId) to loan.amount),
                )
            }
    return (expenses + payments + loans).sortedWith(
        compareBy({ order.indexOf(it.group) }, { it.date })
    )
}

/**
 * The records as CSV (screens-settings §9, proposal): one row per record, amounts as plain numbers
 * with two decimals, shares as "Name ₹700; Name ₹700". Fields are quoted where needed.
 */
fun exportCsv(records: List<ExportRecord>, defaultCurrency: String): String {
    val header =
        listOf(
            "Date",
            "Group",
            "Type",
            "Title",
            "Category",
            "Paid by",
            "Amount",
            "Currency",
            "Rate",
            "Amount ($defaultCurrency)",
            "Shares",
        )
    val rows =
        records.map { record ->
            listOf(
                record.date.toString(),
                record.group.name,
                record.type.label,
                record.title,
                record.category.orEmpty(),
                record.paidBy,
                plainAmount(record.amount, record.currency),
                record.currency,
                record.rate?.value.orEmpty(),
                plainAmount(record.defaultAmount, defaultCurrency),
                record.shares.joinToString("; ") { (who, share) ->
                    "$who ${Money.format(share, record.currency)}"
                },
            )
        }
    return (listOf(header) + rows).joinToString("\r\n", postfix = "\r\n") { fields ->
        fields.joinToString(",") { csvField(it) }
    }
}

/** 70000 paise → "700.00". */
fun plainAmount(minor: Long, currency: String): String =
    BigDecimal(minor)
        .movePointLeft(Money.currency(currency).exponent)
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()

private fun csvField(text: String): String =
    if (text.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + text.replace("\"", "\"\"") + "\""
    } else {
        text
    }
