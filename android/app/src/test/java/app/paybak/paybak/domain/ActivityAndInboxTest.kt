package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.homeSummary
import app.paybak.paybak.domain.calc.inboxRows
import app.paybak.paybak.domain.calc.pendingClaims
import app.paybak.paybak.domain.calc.recentlyDeleted
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** verify.py `check_activity_and_inbox`. */
class ActivityAndInboxTest {
    private val ledger = Demo.load("eshaClaimsPayment")

    @Test
    fun confirmCard() {
        assertEquals(
            listOf("Esha says she paid you ₹700" to "Dinner at Olive Garden · UPI · 9:12 pm"),
            ledger.pendingClaims().map { it.title to it.detail },
        )
        assertEquals(rupee(2900), ledger.homeTotals().owed)
    }

    @Test
    fun timeline() {
        val rows =
            ledger
                .timeline()
                .filter { !ledger.localDate(it.at).isBefore(LocalDate.of(2026, 9, 25)) }
                .map {
                    listOf(
                        Dates.dayHeader(ledger.localDate(it.at), ledger.today),
                        it.title,
                        it.subtitle,
                        it.amount,
                    )
                }
        assertEquals(
            listOf(
                listOf(
                    "Today",
                    "Reminder sent to Rohan",
                    "Movie tickets · ₹800 · Sent automatically",
                    null,
                ),
                listOf(
                    "Today",
                    "You added Dinner at Olive Garden",
                    "You paid · 4 people",
                    "₹2,800",
                ),
                listOf(
                    "Yesterday",
                    "Priya paid you",
                    "Weekend groceries · UPI · Confirmed",
                    "₹1,050",
                ),
                listOf(
                    "Mon 28 Sep",
                    "Kabir changed Villa (3 nights)",
                    "Goa Trip · Was ₹17,500",
                    "₹18,000",
                ),
                listOf(
                    "Mon 28 Sep",
                    "Cooking gas draft created",
                    "Flat 302 · Needs an amount",
                    null,
                ),
                listOf(
                    "Sun 27 Sep",
                    "Reminder sent to Rohan",
                    "Movie tickets · ₹800 · Sent automatically",
                    null,
                ),
                listOf(
                    "Sat 26 Sep",
                    "Meera added Electricity bill",
                    "Flat 302 · You owe ₹450",
                    "₹1,350",
                ),
                listOf(
                    "Fri 25 Sep",
                    "Reminder sent to Rohan",
                    "Movie tickets · ₹800 · Sent automatically",
                    null,
                ),
                listOf("Fri 25 Sep", "Dev added Fuel", "Goa Trip · Your share ₹500", "₹2,500"),
            ),
            rows,
        )
    }

    @Test
    fun inbox() {
        val inbox = ledger.inboxRows()
        assertEquals(
            listOf(
                listOf(
                    "Payment reminder",
                    "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.",
                    "9:00 pm",
                    false,
                ),
                listOf(
                    "Monthly summary",
                    "September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900.",
                    "8:00 pm",
                    false,
                ),
            ),
            inbox.filter { it.today }.map { listOf(it.title, it.body, it.time, it.item.read) },
        )
        assertEquals(
            listOf(
                listOf(
                    "Payment confirmed",
                    "Priya paid you ₹1,050 for Weekend groceries by UPI.",
                    true,
                ),
                listOf(
                    "Payment overdue",
                    "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep.",
                    true,
                ),
                listOf(
                    "New expense in Flat 302",
                    "Meera added Electricity bill, ₹1,350. Your share is ₹450.",
                    true,
                ),
            ),
            inbox.filterNot { it.today }.take(3).map { listOf(it.title, it.body, it.item.read) },
        )
        assertEquals(
            "paybak://record-payment?to=p-kabir&amount=140000&context=group:g-goa",
            inbox.first().link,
        )
    }

    @Test
    fun recentlyDeleted() {
        val row = ledger.recentlyDeleted().single()
        assertEquals(
            "₹300 · Goa Trip | Deleted by Priya on 24 Sep · 24 days left",
            "${row.caption} | ${row.detail}",
        )
    }

    @Test
    fun afterConfirm() {
        val confirmed = Demo.load("eshaPaymentConfirmed")
        val totals = confirmed.homeTotals()
        assertEquals(
            "+₹2,200" to "from 3 people",
            Money.format(totals.owed, sign = MoneySign.Signed) to totals.owedCaption,
        )
        val first = confirmed.homeSummary().recent.first()
        assertEquals(
            listOf("Esha paid you", "UPI", "₹700", "Today"),
            listOf(first.title, first.subtitle, first.amount, first.date),
        )
        assertTrue(confirmed.pendingClaims().isEmpty())
    }

    @Test
    fun afterNotReceived() {
        val rejected = Demo.load("eshaPaymentNotReceived")
        assertEquals(rupee(2900), rejected.homeTotals().owed)
        assertTrue(rejected.pendingClaims().isEmpty())
    }
}
