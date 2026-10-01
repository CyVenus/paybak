package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.addExpense
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderSchedule
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.rupee
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Add & Record logic (screens-add-expense §3, screens-record-lend-group §8). */
class AddRecordTest {
    private val people = listOf(ME, "p-priya", "p-esha", "p-dev")
    private val today = LocalDate.of(2026, 9, 30)

    @Test
    fun amountsAreTypedInMinorUnits() {
        assertEquals(rupee(2800), AmountEntry.minor("2800", "INR"))
        assertEquals(1250L, AmountEntry.minor("12.5", "INR"))
        assertEquals(0L, AmountEntry.minor("", "INR"))
        assertEquals(1200L, AmountEntry.minor("1200", "JPY"))
        assertEquals("2800", AmountEntry.text(rupee(2800), "INR"))
        assertEquals("2800.50", AmountEntry.text(280050, "INR"))
        assertEquals("₹2,800", AmountEntry.display("2800", "INR"))
        assertEquals("₹2,800.5", AmountEntry.display("2800.5", "INR"))
        assertEquals("₹1,00,000", AmountEntry.display("100000", "INR"))
        assertEquals("AED 1,200", AmountEntry.display("1200", "AED"))
        assertEquals("₹0", AmountEntry.placeholder("INR"))
        assertNull(AmountEntry.accept("1234567890"))
        assertFalse(AmountEntry.allowsDecimals("JPY"))
    }

    @Test
    fun equalSplitOfTheOliveGardenBill() {
        val draft = SplitDraft()
        val preview = draft.preview(rupee(2800), people, "INR")
        assertEquals(List(4) { rupee(700) }, people.map { preview.shares[it] })
        assertEquals("₹0 left", preview.left)
        assertEquals("₹2,800 of ₹2,800", preview.detail)
        assertEquals(
            SplitSummary("Equally · ₹700 each"),
            draft.summary(preview, people, rupee(2800), "INR"),
        )
    }

    @Test
    fun exactAmountsThatDontAddUpShowWhatsLeft() {
        val equal = SplitDraft()
        val exact =
            equal.switchTo(
                SplitMode.Exact,
                equal.preview(rupee(2800), people, "INR"),
                people,
                "INR",
            )
        assertEquals("700", exact.value("p-dev"))
        val error = exact.withValue("p-dev", "550").preview(rupee(2800), people, "INR")
        assertFalse(error.balanced)
        assertEquals("₹150 left", error.left)
        assertEquals("₹2,650 of ₹2,800", error.detail)
        assertEquals(
            SplitSummary("Doesn’t add up", error = true),
            exact.summary(error, people, rupee(2800), "INR"),
        )
        assertTrue(exact.withValue("p-dev", "700").preview(rupee(2800), people, "INR").balanced)
        assertEquals(
            "₹50 over",
            exact.withValue("p-dev", "750").preview(rupee(2800), people, "INR").left,
        )
    }

    @Test
    fun percentAndSharesStartFromTheMoneySplit() {
        val equal = SplitDraft()
        val preview = equal.preview(rupee(1000), people.take(3), "INR")
        val percent = equal.switchTo(SplitMode.Percent, preview, people.take(3), "INR")
        assertEquals(listOf("33.34", "33.33", "33.33"), people.take(3).map(percent::value))
        val percentPreview = percent.preview(rupee(1000), people.take(3), "INR")
        assertEquals("0% left", percentPreview.left)
        assertEquals("100% of 100%", percentPreview.detail)
        val shares = equal.switchTo(SplitMode.Shares, preview, people, "INR").withValue(ME, "2")
        val sharesPreview = shares.preview(rupee(1000), people, "INR")
        assertEquals(rupee(400), sharesPreview.shares[ME])
        assertEquals("Shares · 4 people", shares.summary(sharesPreview, people, 1, "INR").text)
    }

    @Test
    fun theLastPersonStaysTickedAndLeftOutPeopleShareNothing() {
        val two = listOf(ME, "p-priya")
        val draft = SplitDraft().toggle("p-priya", two)
        assertEquals(setOf("p-priya"), draft.excluded)
        assertEquals(draft, draft.toggle(ME, two))
        val preview = draft.preview(rupee(900), two, "INR")
        assertEquals(rupee(900), preview.shares[ME])
        assertEquals(0L, preview.shares["p-priya"])
        assertEquals("Equally", draft.summary(preview, listOf(ME), rupee(900), "INR").text)
    }

    @Test
    fun leftoverPaiseRotateFairly() {
        val view = Demo.load()
        val context = ActionContext(view.now, Demo.zone, "INR")
        val trio = listOf(ME, "p-priya", "p-esha")
        val draft = ExpenseDraft.equal(trio).copy(title = "Snacks", amount = rupee(1000))
        val (once, first) = view.ledger.addExpense(draft, context)
        val (twice, second) = once.addExpense(draft, context)
        assertEquals(33334L, once.expense(first)!!.shareOf(ME))
        assertEquals(33334L, twice.expense(second)!!.shareOf("p-priya"))
        assertEquals(33333L, twice.expense(second)!!.shareOf(ME))
    }

    @Test
    fun dueChipsAndDateCopy() {
        assertEquals(LocalDate.of(2026, 10, 1), DueChip.Tomorrow.date(today))
        assertEquals(LocalDate.of(2026, 10, 4), DueChip.ThisWeekend.date(today))
        assertEquals(LocalDate.of(2026, 10, 7), DueChip.NextWeek.date(today))
        assertEquals(DueChip.ThisWeekend, DueChip.matching(LocalDate.of(2026, 10, 4), today))
        assertEquals(
            "Sun 4 Oct · in 4 days",
            DateCopy.summary(LocalDate.of(2026, 10, 4), today, true),
        )
        assertEquals("Mon 28 Sep · 2 days ago", DateCopy.summary(today.minusDays(2), today, false))
        assertEquals(
            "Wed 15 Dec 2027 · in 441 days",
            DateCopy.summary(LocalDate.of(2027, 12, 15), today, true),
        )
        assertEquals("Today", DateCopy.label(today, today))
        assertEquals(
            "Paybak reminds them 2 days before, on the day, and every 3 days if it’s overdue.",
            DateCopy.reminderHint(ReminderSchedule()),
        )
    }

    @Test
    fun recordPaymentStartsFromTheLatestDebt() {
        val view = Demo.load()
        val prefill = view.recordPaymentPrefill()!!
        assertEquals("p-meera", prefill.toId)
        assertEquals(rupee(450), prefill.amount)
        assertEquals(PaymentContext(groupId = "g-flat302"), prefill.context)
        assertEquals(
            "You owe Meera ₹450 in Flat 302",
            PaymentCopy.helper(
                "Meera",
                view.openBalance("p-meera", prefill.context),
                "INR",
                "Flat 302",
                false,
            ),
        )
        assertEquals(
            "You paid Meera ₹450 in cash for Flat 302.\n" +
                "Meera will be asked to confirm. Paybak never moves money.",
            PaymentCopy.summary("Meera", true, "₹450", PaymentMethod.Cash, "Flat 302"),
        )
        assertEquals(
            "You paid Kabir ₹1,400 by UPI for Goa Trip.\n" +
                "Kabir will be asked to confirm. Paybak never moves money.",
            PaymentCopy.summary("Kabir", true, "₹1,400", PaymentMethod.Upi, "Goa Trip"),
        )
    }

    @Test
    fun thePendingPaymentDetail() {
        val view = Demo.load("paymentToMeeraPending")
        val detail = view.paymentDetail("pay-me-meera")!!
        assertEquals("You paid Meera", detail.title)
        assertEquals("Cash · Today · Flat 302", detail.meta)
        assertEquals("Pending confirmation", detail.statusTitle)
        assertEquals("Waiting for Meera to confirm", detail.statusBody)
        assertEquals("Your balance updates once Meera confirms.", detail.footnote)
        assertNull(detail.paidTo)
        assertEquals(
            "Meera won’t be asked to confirm. You’ll still owe her ₹450.",
            view.cancelPaymentMessage(detail.payment, rupee(450)),
        )
        val kabir = Demo.load("paymentToKabirPending").paymentDetail("pay-me-kabir")!!
        assertEquals("kabir@okaxis", kabir.paidTo)
        assertEquals("UPI · Today · Goa Trip", kabir.meta)
    }

    @Test
    fun loanSchedulePreview() {
        val first = LoanSchedule.defaultFirstDue(today, Frequency.Monthly)
        assertEquals(LocalDate.of(2026, 10, 30), first)
        assertEquals(
            "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec",
            LoanSchedule.preview(rupee(6000), "INR", 3, Frequency.Monthly, first),
        )
        assertEquals(
            "6 × ₹1,000 · monthly from Fri 30 Oct to Tue 30 Mar",
            LoanSchedule.preview(rupee(6000), "INR", 6, Frequency.Monthly, first),
        )
        assertEquals("3 monthly installments", LoanSchedule.header(3, Frequency.Monthly))
    }

    @Test
    fun theOliveGardenDetail() {
        val detail = Demo.load().expenseDetail("e-olive")!!
        assertEquals("₹2,800", detail.amount)
        assertEquals("Paid by you · Today", detail.meta)
        assertEquals(listOf("Food"), detail.tags)
        assertEquals("₹700", detail.yourShare)
        assertEquals("Sun 4 Oct", detail.due)
        assertNull(detail.groupBalance)
        assertEquals("Split equally · 4 people", detail.splitHeader)
        assertEquals(listOf("You", "Priya", "Esha", "Dev"), detail.split.map { it.name })
        assertEquals("Paid ₹2,800", detail.split.first().paid)
        assertEquals(listOf("You added this"), detail.history.map { it.text })
        assertFalse(detail.canFlag)
    }

    @Test
    fun theVillaDetail() {
        val detail = Demo.load().expenseDetail("e-goa-villa")!!
        assertEquals("Paid by Kabir · 21 Sep", detail.meta)
        assertEquals(listOf("Goa Trip", "Stays"), detail.tags)
        assertEquals("Fri 2 Oct", detail.due)
        assertEquals("−₹1,400", detail.groupBalance!!.value)
        assertEquals(listOf("Kabir", "You", "Priya", "Esha", "Dev"), detail.split.map { it.name })
        assertEquals(
            listOf("Kabir changed the amount from ₹17,500 to ₹18,000", "Kabir added this"),
            detail.history.map { it.text },
        )
        assertEquals("Added by Kabir · 21 Sep", detail.receiptLine)
    }

    @Test
    fun equalContributions() {
        assertEquals("25%", ContributionDraft.equalShare(4))
        assertEquals("33.3%", ContributionDraft.equalShare(3))
    }
}
