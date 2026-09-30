package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import java.time.YearMonth

/**
 * Building blocks for Ask Paybak's answers (§6.9), from the same read models as Home and Insights.
 * The prompt parser is lane C's.
 */

/**
 * "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each
 * for tonight’s dinner." Null when nobody owes you.
 */
fun LedgerView.whoOwesMeAnswer(): String? {
    val nets = friendNets()
    val items = openItems()
    val parts = mutableListOf<String>()
    val grouped = LinkedHashMap<Pair<String, Long>, MutableList<String>>()
    for (row in settlePlan().get) {
        val mine = items.filter { it.friendId == row.friendId }
        val overdue = mine.filter { it.due != null && it.due.isBefore(today) }
        when {
            overdue.isNotEmpty() ->
                parts +=
                    "${first(row.friendId)} ${Money.format(row.amount, defaultCurrency)} " +
                        "(overdue since ${Dates.short(overdue.first().due!!)})"
            mine.size == 1 && mine.single().kind == ObligationKind.Direct ->
                grouped.getOrPut(mine.single().ref to row.amount) { mutableListOf() } +=
                    first(row.friendId)
            else -> parts += "${first(row.friendId)} ${Money.format(row.amount, defaultCurrency)}"
        }
    }
    for ((key, names) in grouped) {
        val expense = expense(key.first)!!
        val what =
            when {
                expense.date == today &&
                    expense.category == Category.Food.id &&
                    expense.title.startsWith("Dinner") -> "tonight’s dinner"
                expense.date == today -> "today’s ${expense.title.lowercase()}"
                else -> expense.title
            }
        val each = if (names.size > 1) " each" else ""
        parts += "${joinNames(names)} ${Money.format(key.second, defaultCurrency)}$each for $what"
    }
    val owed = nets.values.filter { it > 0 }
    if (owed.isEmpty()) return null
    val joined =
        if (parts.size == 1) parts.single()
        else parts.dropLast(1).joinToString(", ") + ", and " + parts.last()
    val count = owed.size
    return "$count ${if (count == 1) "person owes" else "people owe"} you ${Money.format(owed.sum(), defaultCurrency)}: $joined."
}

/** "You spent ₹3,850 on food in September — 17% of your ₹23,300 share." */
fun LedgerView.categorySpendAnswer(category: Category, month: YearMonth): String {
    val totals = monthTotals(month)
    val amount = totals.categories[category.id] ?: 0
    val percent = Splits.largestRemainderPercent(totals.categories)[category.id] ?: 0
    return "You spent ${Money.format(amount, defaultCurrency)} on ${category.label.lowercase()} in " +
        "${Dates.monthName(month.month)} — $percent% of your ${Money.format(totals.total, defaultCurrency)} share."
}

/** "Your Goa Trip share of ₹1,400 is due Fri 2 Oct." Null when you owe nothing there. */
fun LedgerView.groupDueAnswer(groupId: String): String? {
    val item =
        openItems().firstOrNull {
            it.kind == ObligationKind.Group && it.ref == groupId && !it.owedToMe
        } ?: return null
    val name = group(groupId)?.name ?: return null
    val due = item.due?.let { " is due ${Dates.day(it)}" } ?: " is open"
    return "Your $name share of ${Money.format(item.amount, defaultCurrency)}$due."
}
