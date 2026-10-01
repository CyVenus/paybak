package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.Obligation
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.model.ReminderTone

/**
 * What the Remind sheet shows for one friend (screens-settle §6): their row (what they owe you for,
 * the amount, overdue or due) and the pre-written message in each tone. [context] is what the sent
 * reminder is filed under: the earliest-due debt it covers.
 */
data class RemindDraft(
    val personId: String,
    val name: String,
    val subtitle: String,
    val amount: Long,
    val currency: String,
    val dueLabel: String?,
    val overdue: String?,
    val context: ReminderContext,
    private val friendly: String,
    private val neutral: String,
) {
    val amountText: String
        get() = Money.format(amount, currency)

    fun message(tone: ReminderTone): String =
        if (tone == ReminderTone.Neutral) neutral else friendly

    /** The sheet's "Remind Rohan" and the toast's "Reminder sent to Rohan". */
    val title: String
        get() = "Remind $name"
}

/**
 * The reminder to [personId] about [context] (an expense, a group, a loan or one of its
 * installments), or about everything they owe you when [context] is null or no longer open. Null
 * when they owe you nothing. [myUpi] adds "You can pay me on UPI at …" when you have one.
 */
fun LedgerView.remindDraft(personId: String, context: ReminderContext?, myUpi: String?): RemindDraft? {
    val owed = openItems().filter { it.friendId == personId && it.owedToMe }
    val items = context?.let { c -> owed.filter { it.matches(c) } }?.ifEmpty { null } ?: owed
    val lead = items.filter { it.due != null }.minByOrNull { it.due!! } ?: items.firstOrNull()
    lead ?: return null
    val amount = items.sumOf { it.amount }
    val money = Money.format(amount, defaultCurrency)
    val name = first(personId)
    val due = lead.due
    val overdue = due != null && Dates.isOverdue(due, today)
    val more = items.size - 1
    val upi = myUpi?.trim()?.takeIf { it.isNotEmpty() }
    val friendlyWhat = friendlyPhrase(lead) + morePhrase(more)
    val neutralWhat = neutralPhrase(lead) + morePhrase(more)
    return RemindDraft(
        personId = personId,
        name = name,
        subtitle = lead.title,
        amount = amount,
        currency = defaultCurrency,
        dueLabel = due?.takeUnless { overdue }?.let(Dates::dueLabel),
        overdue = due?.takeIf { overdue }?.let { Dates.dueBadge(it, today) },
        context = lead.reminderContext,
        friendly =
            "Hi $name! Just a gentle reminder about $money for $friendlyWhat." +
                (upi?.let { " You can pay me on UPI at $it." } ?: "") +
                " Thanks.",
        neutral =
            "Hi $name, this is a reminder that $money for $neutralWhat is still due." +
                (upi?.let { " You can pay me on UPI at $it." } ?: ""),
    )
}

private fun Obligation.matches(context: ReminderContext): Boolean =
    when (kind) {
        ObligationKind.Direct -> ref == context.expenseId
        ObligationKind.Group,
        ObligationKind.Project -> ref == context.groupId
        ObligationKind.Loan ->
            ref == context.loanId && (context.installment == null || installment == context.installment)
    }

/** "the movie tickets on 20 Sep" · "Goa Trip" · "the laptop repair". */
private fun LedgerView.friendlyPhrase(item: Obligation): String =
    when (item.kind) {
        ObligationKind.Direct -> "the ${lowerFirst(item.title)}${expenseDate(item)?.let { " on $it" } ?: ""}"
        ObligationKind.Group,
        ObligationKind.Project -> item.title
        ObligationKind.Loan -> "the ${lowerFirst(item.title)}"
    }

/** "movie tickets (20 Sep)" · "Goa Trip" · "laptop repair". */
private fun LedgerView.neutralPhrase(item: Obligation): String =
    when (item.kind) {
        ObligationKind.Direct ->
            lowerFirst(item.title) + (expenseDate(item)?.let { " ($it)" } ?: "")
        ObligationKind.Group,
        ObligationKind.Project -> item.title
        ObligationKind.Loan -> lowerFirst(item.title)
    }

private fun LedgerView.expenseDate(item: Obligation): String? =
    expense(item.ref)?.date?.let(Dates::short)

private fun morePhrase(more: Int): String =
    when (more) {
        0 -> ""
        1 -> " and 1 more"
        else -> " and $more more"
    }

/** "Movie tickets" → "movie tickets"; an acronym ("UPI top-up") keeps its capitals. */
private fun lowerFirst(text: String): String =
    if (text.length > 1 && text[1].isUpperCase()) text else text.replaceFirstChar(Char::lowercase)
