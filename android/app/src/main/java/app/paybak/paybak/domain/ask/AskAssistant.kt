package app.paybak.paybak.domain.ask

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.calc.categorySpendAnswer
import app.paybak.paybak.domain.calc.groupDueAnswer
import app.paybak.paybak.domain.calc.joinNames
import app.paybak.paybak.domain.calc.monthTotals
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.calc.whoOwesMeAnswer
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.insights.InsightRow
import app.paybak.paybak.domain.insights.insightsPage
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.Person
import java.math.RoundingMode
import java.time.Month
import java.time.YearMonth

/** What a suggested prompt asks about (its icon). */
enum class PromptKind {
    WhoOwesMe,
    Spend,
    Due,
    Reminder,
}

/** A suggested prompt: tapping it sends [text]. */
data class AskPrompt(val text: String, val kind: PromptKind)

/** An action chip under an answer: each opens the app's normal flow. */
sealed interface AskChip {
    data class Remind(val personId: String, val name: String) : AskChip

    data class SeeInsights(val month: YearMonth) : AskChip

    data class SettleUp(val groupId: String) : AskChip

    data class OpenGroup(val groupId: String, val name: String) : AskChip
}

/** A row of the "Who owes me money?" card; [overdue] is its badge ("Overdue 3 days"). */
data class OwedPerson(val personId: String, val amount: Long, val overdue: String?)

/** An expense the assistant drafted; nothing is saved until Save. */
data class DraftCard(
    val draft: ExpenseDraft,
    val amountText: String,
    val categoryIcon: String,
    val paidLine: String,
    val splitLine: String,
    val eachLine: String,
    /** You first, then the people it's split with. */
    val personIds: List<String>,
)

/**
 * One reply of Ask Paybak: its text and what comes with it (a card of people or a share bar, a
 * drafted reminder, a drafted expense, action chips, or the suggestions again).
 */
data class AskAnswer(
    val text: String,
    val people: List<OwedPerson> = emptyList(),
    val share: InsightRow? = null,
    val message: String? = null,
    val draft: DraftCard? = null,
    val chips: List<AskChip> = emptyList(),
    val suggestions: List<AskPrompt> = emptyList(),
)

/**
 * The on-device assistant (insights §3.6, app-architecture §4.3): it answers from the live ledger in
 * [view] and never sends anything anywhere. [reminder] writes the Remind sheet's friendly message to
 * a friend (null when they owe you nothing), so the chat and the sheet say the same thing.
 */
class AskAssistant(private val view: LedgerView, private val reminder: (personId: String) -> String?) {
    private val friends: List<Person> = view.ledger.people.filter { it.id != ME }

    /** The four suggestions, for this account's own group and debtor; empty ones are left out. */
    fun prompts(): List<AskPrompt> {
        val plan = view.settlePlan()
        val dueGroup =
            view.openItems()
                .filter { it.kind == ObligationKind.Group && !it.owedToMe && it.due != null }
                .minByOrNull { it.due!! }
                ?.let { view.group(it.ref)?.name }
        val debtor = plan.get.firstOrNull()?.friendId?.let(view::first)
        val food = view.monthTotals(YearMonth.from(view.today)).categories[Category.Food.id] ?: 0
        return listOfNotNull(
            AskPrompt(WHO_OWES_ME, PromptKind.WhoOwesMe).takeIf { plan.get.isNotEmpty() },
            AskPrompt("How much did I spend on food this month?", PromptKind.Spend).takeIf { food > 0 },
            dueGroup?.let { AskPrompt("When is $it due?", PromptKind.Due) },
            debtor?.let { AskPrompt("Draft a reminder for $it", PromptKind.Reminder) },
        )
    }

    fun answer(question: String): AskAnswer {
        val text = normalize(question)
        return whoOwesMe(text)
            ?: spend(text)
            ?: groupDue(text)
            ?: remind(text)
            ?: draftExpense(question)
            ?: AskAnswer(FALLBACK, suggestions = prompts())
    }

    private fun whoOwesMe(text: String): AskAnswer? {
        if (!whoOwes.matches(text)) return null
        val rows = view.settlePlan().get
        val people =
            rows.map { row ->
                OwedPerson(
                    row.friendId,
                    row.amount,
                    row.due?.takeIf { Dates.isOverdue(it, view.today) }?.let { Dates.dueBadge(it, view.today) },
                )
            }
        return AskAnswer(
            text = view.whoOwesMeAnswer() ?: "No one owes you anything right now.",
            people = people,
            chips =
                people.filter { it.overdue != null }.take(MAX_REMIND_CHIPS).map {
                    AskChip.Remind(it.personId, view.first(it.personId))
                },
        )
    }

    private fun spend(text: String): AskAnswer? {
        val match = spendOn.matchEntire(text) ?: return null
        val category = categoryNamed(match.groupValues[1]) ?: return null
        val month = monthNamed(match.groupValues[2], match.groupValues[3]) ?: YearMonth.from(view.today)
        val share = view.insightsPage(month).categories.firstOrNull { it.key == category.id }
        val monthName = Dates.monthName(month.month)
        return AskAnswer(
            text =
                if (share == null) "You didn’t spend anything on ${category.label.lowercase()} in $monthName."
                else view.categorySpendAnswer(category, month),
            share = share,
            chips = listOf(AskChip.SeeInsights(month)),
        )
    }

    private fun groupDue(text: String): AskAnswer? {
        val name = (dueQuestion.matchEntire(text) ?: payQuestion.matchEntire(text))?.groupValues?.get(1)
        val group = name?.let(::groupNamed) ?: return null
        val due = view.groupDueAnswer(group.id)
        return AskAnswer(
            text = due ?: "Nothing is due in ${group.name}.",
            chips =
                listOfNotNull(
                    due?.let { AskChip.SettleUp(group.id) },
                    AskChip.OpenGroup(group.id, group.name),
                ),
        )
    }

    private fun remind(text: String): AskAnswer? {
        val name = remindQuestion.matchEntire(text)?.groupValues?.get(1) ?: return null
        val person = personNamed(name) ?: return AskAnswer(notFound(name))
        val first = view.first(person.id)
        val message =
            reminder(person.id) ?: return AskAnswer("$first doesn’t owe you anything right now.")
        return AskAnswer(
            text = "Here’s a reminder for $first. Nothing is sent until you tap Send.",
            message = message,
            chips = listOf(AskChip.Remind(person.id, first)),
        )
    }

    private fun draftExpense(question: String): AskAnswer? {
        val phrase = ExpensePhrase.parse(question) { groupNamed(it) != null } ?: return null
        val group = phrase.group?.let(::groupNamed)
        val people = phrase.names.map { it to personNamed(it) }
        val found = people.mapNotNull { it.second }.distinctBy { it.id }
        val missing = people.filter { it.second == null }.joinToString("") { " " + notFound(it.first) }
        if (found.isEmpty()) return AskAnswer(NEEDS_PEOPLE + missing)
        val currency = group?.currency ?: view.defaultCurrency
        val amount =
            phrase.amount
                .movePointRight(Money.currency(currency).exponent)
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
        val everyone = listOf(ME) + found.map { it.id }
        val category = phrase.category
        val shares = Splits.equal(amount, everyone).shares.values
        val each = Money.format(shares.min(), currency)
        return AskAnswer(
            text = DRAFT_REPLY + missing,
            draft =
                DraftCard(
                    draft =
                        ExpenseDraft.equal(everyone)
                            .copy(
                                title = phrase.title ?: category.label,
                                amount = amount,
                                currency = currency,
                                groupId = group?.id,
                                category = category.id,
                                date = view.today,
                                payers = listOf(Payer(ME, amount)),
                            ),
                    amountText = Money.format(amount, currency),
                    categoryIcon = category.icon,
                    paidLine = "Paid by you · Today",
                    splitLine = "Split equally with ${joinNames(found.map { view.first(it.id) })}",
                    eachLine = if (shares.distinct().size == 1) "$each each" else "About $each each",
                    personIds = everyone,
                ),
        )
    }

    private fun personNamed(name: String): Person? {
        val wanted = normalize(name)
        return friends.firstOrNull { it.firstName.lowercase() == wanted || it.name.lowercase() == wanted }
    }

    private fun groupNamed(name: String) =
        view.ledger.groups.firstOrNull { it.name.lowercase() == normalize(name) }

    private fun categoryNamed(name: String): Category? =
        Category.entries.firstOrNull { it.label.lowercase() == name || it.id == name }
            ?: ExpensePhrase.guessCategory(name).takeIf { it != Category.Other }

    /** "september" / "september 2025" → that month, the latest one up to now when no year. */
    private fun monthNamed(name: String, year: String): YearMonth? {
        if (name.isEmpty()) return null
        val month = Month.entries.firstOrNull { it.name.lowercase() == name } ?: return null
        val current = YearMonth.from(view.today)
        year.toIntOrNull()?.let { return YearMonth.of(it, month) }
        val thisYear = YearMonth.of(current.year, month)
        return if (thisYear > current) thisYear.minusYears(1) else thisYear
    }

    private fun notFound(name: String) = "I couldn’t find ${name.trim().replaceFirstChar(Char::uppercaseChar)}."

    companion object {
        const val WHO_OWES_ME = "Who owes me money?"
        const val DRAFT_REPLY = "Here’s what I’ll add. Nothing is saved until you tap Save."
        const val FALLBACK =
            "I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”"
        const val NEEDS_PEOPLE =
            "Who is it with? Try “Add ₹600 for a cab, split with Esha and Dev”."
        private const val MAX_REMIND_CHIPS = 2

        private val whoOwes = Regex("""who owes me(?: money)?|what am i owed|how much am i owed""")
        private val spendOn =
            Regex("""how much (?:did|have) i spen[dt] on (.+?)(?: this month| in ([a-z]+)(?: (\d{4}))?)?""")
        private val dueQuestion = Regex("""when(?: is|'s) (.+?) due""")
        private val payQuestion = Regex("""when do i pay (.+)""")
        private val remindQuestion = Regex("""(?:draft a reminder (?:for|to)|remind) (.+)""")

        /** Lower case, single spaces, straight quotes, no closing "?" or ".". */
        fun normalize(text: String): String =
            text.lowercase()
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace(Regex("""\s+"""), " ")
                .trim()
                .trimEnd('?', '.', '!')
                .trim()
    }
}
