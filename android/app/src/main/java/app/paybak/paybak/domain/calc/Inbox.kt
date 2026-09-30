package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxType
import java.time.LocalDate

/** An inbox item with its copy (§6.8) and the internal link it opens (app-architecture §2.6). */
data class InboxRow(
    val item: InboxItem,
    val title: String,
    val body: String,
    val time: String,
    val today: Boolean,
    val link: String?,
)

/** Newest first; "Today" rows show the time, the others the row date. */
fun LedgerView.inboxRows(): List<InboxRow> =
    ledger.inbox
        .sortedByDescending { it.createdAt }
        .map { item ->
            val (title, body) = inboxText(item)
            val day = localDate(item.createdAt)
            InboxRow(
                item,
                title,
                body,
                if (day == today) Dates.time(item.createdAt, zone) else Dates.rowDate(day, today),
                day == today,
                inboxLink(item),
            )
        }

/** Title and body of an inbox item from its snapshot params (§6.8). */
fun LedgerView.inboxText(item: InboxItem): Pair<String, String> {
    val p = item.params
    val currency = p.currency ?: defaultCurrency
    fun money(minor: Long?) = Money.format(minor ?: 0, currency)
    val name = first(p.personId ?: p.actorId ?: "")
    return when (item.type) {
        InboxType.NewExpenseInGroup ->
            "New expense in ${p.groupId?.let { group(it)?.name }.orEmpty()}" to
                "${first(p.actorId ?: "")} added ${p.title}, ${money(p.total)}. Your share is ${money(p.share)}."
        InboxType.PaymentOverdue ->
            "Payment overdue" to
                "$name owes you ${money(p.amount)} for ${p.title}. It was due on ${p.dueDate?.let(Dates::short)}."
        InboxType.PaymentConfirmed ->
            "Payment confirmed" to
                "$name paid you ${money(p.amount)} for ${p.title} by ${p.method?.label}."
        InboxType.PaymentReminder -> {
            val on = localDate(item.createdAt)
            "Payment reminder" to
                "You owe $name ${money(p.amount)} for ${p.title}. ${p.dueDate?.let { Dates.duePhrase(it, on) }.orEmpty()}"
                    .trim()
        }
        InboxType.MonthlySummary -> {
            val month = LocalDate.of(p.year ?: today.year, p.month ?: today.monthValue, 1).month
            val tail =
                when {
                    (p.owed ?: 0) != 0L -> "You’re owed ${Money.format(p.owed!!, defaultCurrency)}."
                    (p.owe ?: 0) != 0L -> "You owe ${Money.format(p.owe!!, defaultCurrency)}."
                    else -> "You’re all square."
                }
            "Monthly summary" to
                "${Dates.monthName(month)}: you spent ${Money.format(p.spent ?: 0, defaultCurrency)} on shared expenses. $tail"
        }
        InboxType.PaymentNotReceived -> "$name hasn’t received it" to p.note.orEmpty()
        InboxType.ExpenseFlagged -> "$name flagged ${p.title}" to p.note.orEmpty()
        InboxType.FlagResolved -> "$name resolved your flag" to p.title.orEmpty()
    }
}

/** The internal link an inbox item (or its notification) opens. */
fun inboxLink(item: InboxItem): String? {
    val p = item.params
    return when (item.type) {
        InboxType.PaymentReminder ->
            "paybak://record-payment?to=${p.personId}&amount=${p.amount}" +
                (p.groupId?.let { "&context=group:$it" } ?: "")
        InboxType.MonthlySummary ->
            "paybak://insights?month=${p.year}-${(p.month ?: 1).toString().padStart(2, '0')}"
        InboxType.PaymentConfirmed,
        InboxType.PaymentNotReceived -> p.paymentId?.let { "paybak://payment/$it" }
        InboxType.PaymentOverdue -> "paybak://remind?person=${p.personId}"
        InboxType.NewExpenseInGroup,
        InboxType.ExpenseFlagged,
        InboxType.FlagResolved -> p.expenseId?.let { "paybak://expense/$it" }
    }
}
