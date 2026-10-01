package app.paybak.paybak.domain.ask

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money

/**
 * The Remind sheet's Friendly message to [personId] (domain.md §6.9): "Hi Rohan! Just a gentle
 * reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis.
 * Thanks." About their earliest-due debt, with everything they owe you. Null when they owe you
 * nothing.
 */
fun LedgerView.friendlyReminder(personId: String, myUpi: String?): String? {
    val row = settlePlan().get.firstOrNull { it.friendId == personId } ?: return null
    val lead = row.lead
    val what =
        when (lead?.kind) {
            ObligationKind.Direct ->
                "the ${lowerFirst(lead.title)}" +
                    (expense(lead.ref)?.date?.let { " on ${Dates.short(it)}" } ?: "")
            ObligationKind.Loan -> "the ${lowerFirst(lead.title)}"
            ObligationKind.Group,
            ObligationKind.Project -> lead.title
            null -> "what you owe me"
        }
    val more = row.items.size - 1
    val others =
        when (more) {
            0 -> ""
            1 -> " and 1 more"
            else -> " and $more more"
        }
    val upi = myUpi?.trim()?.takeIf { it.isNotEmpty() }?.let { " You can pay me on UPI at $it." }
    return "Hi ${first(personId)}! Just a gentle reminder about " +
        "${Money.format(row.amount, defaultCurrency)} for $what$others.${upi.orEmpty()} Thanks."
}

/** "Movie tickets" → "movie tickets"; an acronym ("UPI top-up") keeps its capitals. */
private fun lowerFirst(text: String): String =
    if (text.length > 1 && text[1].isUpperCase()) text else text.replaceFirstChar(Char::lowercase)
