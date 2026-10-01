package app.paybak.paybak.domain.insightsai

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.ask.AskAssistant
import app.paybak.paybak.domain.ask.AskChip
import app.paybak.paybak.domain.ask.ExpensePhrase
import app.paybak.paybak.domain.ask.PromptKind
import app.paybak.paybak.domain.ask.friendlyReminder
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.SplitMode
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ask Paybak's on-device assistant (insights §3.6, app-architecture §4.3) on the demo. */
class AskAssistantTest {
    private val view = Demo.load("eshaClaimsPayment")
    private val assistant = AskAssistant(view) { view.friendlyReminder(it, "arjun@okaxis") }

    @Test
    fun theFourSuggestionsMatchFigma() {
        assertEquals(
            listOf(
                "Who owes me money?",
                "How much did I spend on food this month?",
                "When is Goa Trip due?",
                "Draft a reminder for Rohan",
            ),
            assistant.prompts().map { it.text },
        )
        assertEquals(PromptKind.entries, assistant.prompts().map { it.kind })
    }

    @Test
    fun whoOwesMeIsTheFigmaAnswer() {
        val answer = assistant.answer("Who owes me money?")
        assertEquals(
            "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev " +
                "₹700 each for tonight’s dinner.",
            answer.text,
        )
        assertEquals(
            listOf(
                Triple("p-rohan", 80_000L, "Overdue 3 days"),
                Triple("p-priya", 70_000L, null),
                Triple("p-esha", 70_000L, null),
                Triple("p-dev", 70_000L, null),
            ),
            answer.people.map { Triple(it.personId, it.amount, it.overdue) },
        )
        assertEquals(listOf(AskChip.Remind("p-rohan", "Rohan")), answer.chips)
        assertEquals(answer.text, assistant.answer("  who owes me  ").text)
    }

    @Test
    fun foodThisMonthComesFromInsights() {
        val answer = assistant.answer("How much did I spend on food this month?")
        assertEquals(
            "You spent ₹3,850 on food in September — 17% of your ₹23,300 share.",
            answer.text,
        )
        assertEquals(17, answer.share?.percent)
        assertEquals(listOf(AskChip.SeeInsights(YearMonth.of(2026, 9))), answer.chips)
        assertEquals(
            "You didn’t spend anything on shopping in September.",
            assistant.answer("how much did I spend on shopping?").text,
        )
    }

    @Test
    fun goaTripIsDueOnFriday() {
        val answer = assistant.answer("When is Goa Trip due?")
        assertEquals("Your Goa Trip share of ₹1,400 is due Fri 2 Oct.", answer.text)
        assertEquals(
            listOf(AskChip.SettleUp("g-goa"), AskChip.OpenGroup("g-goa", "Goa Trip")),
            answer.chips,
        )
    }

    @Test
    fun theReminderIsTheRemindSheetsFriendlyMessage() {
        val answer = assistant.answer("Draft a reminder for Rohan")
        assertEquals(
            "Here’s a reminder for Rohan. Nothing is sent until you tap Send.",
            answer.text,
        )
        assertEquals(
            "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can " +
                "pay me on UPI at arjun@okaxis. Thanks.",
            answer.message,
        )
        assertEquals(listOf(AskChip.Remind("p-rohan", "Rohan")), answer.chips)
        assertEquals("I couldn’t find Zara.", assistant.answer("remind zara").text)
        assertEquals(
            "Kabir doesn’t owe you anything right now.",
            assistant.answer("Draft a reminder for Kabir").text,
        )
    }

    @Test
    fun aCabIsDraftedForThreeAndNothingIsSaved() {
        val answer = assistant.answer("Add ₹600 for a cab, split with Esha and Dev")
        assertEquals(AskAssistant.DRAFT_REPLY, answer.text)
        val card = answer.draft!!
        assertEquals("Cab", card.draft.title)
        assertEquals(60_000L, card.draft.amount)
        assertEquals(Category.Travel.id, card.draft.category)
        assertEquals(LocalDate.of(2026, 9, 30), card.draft.date)
        assertEquals(SplitMode.Equal, card.draft.split.mode)
        assertEquals(listOf(ME, "p-esha", "p-dev"), card.personIds)
        assertEquals("₹600", card.amountText)
        assertEquals("Paid by you · Today", card.paidLine)
        assertEquals("Split equally with Esha and Dev", card.splitLine)
        assertEquals("₹200 each", card.eachLine)
    }

    @Test
    fun unevenSplitsUnknownNamesAndGroups() {
        val uneven =
            assistant.answer("log rs 1,000 for dinner with Priya & Rohan and Zed in Goa Trip")
        val card = uneven.draft!!
        assertEquals("About ₹333.33 each", card.eachLine)
        assertEquals("g-goa", card.draft.groupId)
        assertEquals(Category.Food.id, card.draft.category)
        assertEquals("Dinner", card.draft.title)
        assertEquals(AskAssistant.DRAFT_REPLY + " I couldn’t find Zed.", uneven.text)
        assertEquals(
            AskAssistant.NEEDS_PEOPLE,
            assistant.answer("Add 450 for pizza").text,
        )
    }

    @Test
    fun anythingElseGetsTheSuggestionsAgain() {
        val answer = assistant.answer("What’s the weather?")
        assertEquals(AskAssistant.FALLBACK, answer.text)
        assertEquals(4, answer.suggestions.size)
        assertNull(answer.draft)
    }

    @Test
    fun phrasesReadTheirAmountWhatPeopleAndGroup() {
        val phrase =
            ExpensePhrase.parse("Split 99.50 rupees for the groceries with Esha") { false }!!
        assertEquals("99.50", phrase.amount.toPlainString())
        assertEquals("Groceries", phrase.title)
        assertEquals(Category.Food, phrase.category)
        assertEquals(listOf("Esha"), phrase.names)
        assertNull(ExpensePhrase.parse("who owes me money") { false })
        assertTrue(ExpensePhrase.guessCategory("movie tickets") == Category.Fun)
        assertEquals(Category.Other, ExpensePhrase.guessCategory("plants"))
    }
}
