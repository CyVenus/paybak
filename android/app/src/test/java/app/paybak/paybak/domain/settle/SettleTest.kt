package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.model.ReminderTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Settle up screens (screens-settle §1–3, §6, §8) from the demo at Figma parity. */
class SettleTest {
    private val demo = Demo.load("eshaClaimsPayment")

    private fun BreakdownRow.cells() = listOf(name, subtitle, amount, dueLabel, overdue)

    private fun SettleUpRow.cells() = listOf(name, row.context, amount, badge, overdue)

    @Test
    fun youAreOwedByFourPeopleOverdueFirst() {
        with(demo.owedBreakdown()) {
            assertEquals("+₹2,900", total)
            assertEquals("from 4 people", caption)
            assertEquals(
                listOf(
                    listOf("Rohan", "Movie tickets", "₹800", null, "Overdue 3 days"),
                    listOf("Priya", "Dinner at Olive Garden", "₹700", "Due Sun 4 Oct", null),
                    listOf("Esha", "Dinner at Olive Garden", "₹700", "Due Sun 4 Oct", null),
                    listOf("Dev", "Dinner at Olive Garden", "₹700", "Due Sun 4 Oct", null),
                ),
                rows.map { it.cells() },
            )
            assertEquals(emptyList<String>(), footnotes)
        }
    }

    @Test
    fun youOweTwoGroupsAndGoaTripExplainsItsSimplifiedDebt() {
        with(demo.oweBreakdown()) {
            assertEquals("−₹1,850", total)
            assertEquals("across 2 groups", caption)
            assertEquals(
                listOf(
                    listOf("Kabir", "Goa Trip", "₹1,400", "Due Fri 2 Oct", null),
                    listOf("Meera", "Flat 302", "₹450", "Due Mon 5 Oct", null),
                ),
                rows.map { it.cells() },
            )
            assertEquals(
                listOf("Goa Trip uses simplified debts, so you pay Kabir directly."),
                footnotes,
            )
        }
    }

    @Test
    fun confirmingEshasPaymentLeavesThreePeople() {
        with(Demo.load("eshaPaymentConfirmed").owedBreakdown()) {
            assertEquals("+₹2,200", total)
            assertEquals("from 3 people", caption)
            assertEquals(listOf("Rohan", "Priya", "Dev"), rows.map { it.name })
        }
        with(Demo.load("eshaPaymentNotReceived").owedBreakdown()) {
            assertEquals("+₹2,900", total)
            assertEquals(4, rows.size)
        }
    }

    @Test
    fun anAllSettledAccountHasEmptyBreakdowns() {
        val view = Demo.load("allSettled")
        with(view.owedBreakdown()) {
            assertEquals("₹0", total)
            assertEquals("Nobody owes you right now", caption)
            assertTrue(isEmpty)
        }
        assertEquals("You don’t owe anyone right now", view.oweBreakdown().caption)
        assertTrue(view.settleUpPage().allSettled)
    }

    @Test
    fun thePlanHasTwoPaymentsToMakeAndFourPeopleWhoOweYou() {
        with(demo.settleUpPage()) {
            assertEquals("2 payments to make", payTitle)
            assertEquals("4 people owe you", getTitle)
            assertEquals(
                listOf(
                    listOf("Kabir", "Goa Trip", "₹1,400", "Due Fri", false),
                    listOf("Meera", "Flat 302", "₹450", "Due Mon", false),
                ),
                pay.map { it.cells() },
            )
            assertEquals(
                listOf(
                    listOf("Rohan", "Movie tickets", "₹800", "Overdue 3 days", true),
                    listOf("Priya", "Dinner at Olive Garden", "₹700", "Due Sun", false),
                    listOf("Esha", "Dinner at Olive Garden", "₹700", "Due Sun", false),
                    listOf("Dev", "Dinner at Olive Garden", "₹700", "Due Sun", false),
                ),
                get.map { it.cells() },
            )
            assertTrue((pay + get).all { it.pendingPaymentId == null })
        }
    }

    @Test
    fun aRecordedPaymentStaysInThePlanAsPending() {
        val view = Demo.load("eshaClaimsPayment", "paymentToKabirPending")
        val kabir = view.settleUpPage().pay.first()
        assertEquals(listOf("Kabir", "Goa Trip", "₹1,400", "Pending", false), kabir.cells())
        assertNotNull(kabir.pendingPaymentId)
        assertEquals("−₹1,850", view.oweBreakdown().total)
    }

    @Test
    fun aGroupPlanListsOnlyItsTransfersWithYou() {
        with(demo.settleUpPage(demo.groupSettlePlan("g-goa"))) {
            assertEquals(listOf("Kabir" to "₹1,400"), pay.map { it.name to it.amount })
            assertEquals(emptyList<SettleUpRow>(), get)
        }
        assertTrue(demo.settleUpPage(demo.groupSettlePlan("g-dubai")).allSettled)
    }

    @Test
    fun rohansReminderUsesTheDesignedFriendlyMessage() {
        val draft = demo.remindDraft("p-rohan", ReminderContext(expenseId = "e-movie"), "arjun@okaxis")!!
        assertEquals("Remind Rohan", draft.title)
        assertEquals(listOf("Movie tickets", "₹800", null, "Overdue 3 days"), draft.cells())
        assertEquals(
            "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You " +
                "can pay me on UPI at arjun@okaxis. Thanks.",
            draft.message(ReminderTone.Friendly),
        )
        assertEquals(
            "Hi Rohan, this is a reminder that ₹800 for movie tickets (20 Sep) is still due. You " +
                "can pay me on UPI at arjun@okaxis.",
            draft.message(ReminderTone.Neutral),
        )
        assertEquals(ReminderContext(expenseId = "e-movie"), draft.context)
    }

    @Test
    fun aReminderWithoutContextOrUpiCoversEverythingTheyOwe() {
        val draft = demo.remindDraft("p-priya", null, "")!!
        assertEquals(listOf("Dinner at Olive Garden", "₹700", "Due Sun 4 Oct", null), draft.cells())
        assertEquals(
            "Hi Priya! Just a gentle reminder about ₹700 for the dinner at Olive Garden on 30 Sep. " +
                "Thanks.",
            draft.message(ReminderTone.Friendly),
        )
        assertNull(demo.remindDraft("p-kabir", null, "arjun@okaxis"))
    }

    @Test
    fun notReceivedWritesTheDesignedNoteToEsha() {
        with(demo.notReceivedDraft("pay-esha-olive")!!) {
            assertEquals("Let Esha know you haven’t received ₹700?", title)
            assertEquals("Dinner at Olive Garden · UPI · 9:12 pm", context)
            assertEquals(
                "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check " +
                    "your UPI app?",
                note,
            )
            assertEquals("Esha still owes you ₹700 until a payment is confirmed.", helper)
        }
        assertNull(Demo.load("eshaPaymentConfirmed").notReceivedDraft("pay-esha-olive"))
    }

    @Test
    fun notReceivedLeavesOutWhatAPlainPaymentWasFor() {
        val ledger = demo.ledger
        val plain =
            ledger.copy(
                payments =
                    ledger.payments.map {
                        if (it.id == "pay-esha-olive") it.copy(expenseId = null) else it
                    }
            )
        val view = LedgerView(plain, demo.defaultCurrency, demo.now, demo.zone)
        assertEquals(
            "Hi Esha, I haven’t received ₹700 yet. Could you check your UPI app?",
            view.notReceivedDraft("pay-esha-olive")!!.note,
        )
    }

    private fun RemindDraft.cells() = listOf(subtitle, amountText, dueLabel, overdue)
}
