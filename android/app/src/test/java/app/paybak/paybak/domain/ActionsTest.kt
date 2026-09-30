package app.paybak.paybak.domain

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.actions.addExpense
import app.paybak.paybak.domain.actions.addGuest
import app.paybak.paybak.domain.actions.clearRecords
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.deleteExpense
import app.paybak.paybak.domain.actions.enterDraftAmount
import app.paybak.paybak.domain.actions.leaveGroup
import app.paybak.paybak.domain.actions.recordPayment
import app.paybak.paybak.domain.actions.restoreExpense
import app.paybak.paybak.domain.actions.startTrial
import app.paybak.paybak.domain.actions.updateExpense
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.HistoryKind
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.Plan
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Each store action's side effects (domain.md §11). */
class ActionsTest {
    private val demo = Demo.load()
    private val ctx = ActionContext(demo.now, Demo.zone, "INR")

    private fun view(ledger: Ledger) = LedgerView(ledger, "INR", demo.now, Demo.zone)

    @Test
    fun addingTheOliveGardenDinnerAgainAddsItsShares() {
        val draft =
            ExpenseDraft.equal(listOf(ME, "p-priya", "p-esha", "p-dev"))
                .copy(title = "Dinner", amount = rupee(2800))
        val (ledger, id) = demo.ledger.addExpense(draft, ctx)
        val expense = ledger.expense(id)!!
        assertEquals(setOf(rupee(700)), expense.split.rows.map { it.share }.toSet())
        assertEquals(listOf(HistoryKind.Created), expense.history.map { it.kind })
        assertEquals(demo.today, expense.date)
        assertEquals(rupee(2900 + 2100), view(ledger).homeTotals().owed)
    }

    @Test
    fun leftoverPaiseRotateWithinTheSameContext() {
        val draft = ExpenseDraft.equal(listOf(ME, "p-priya", "p-esha")).copy(amount = rupee(1000))
        val (first, a) = demo.ledger.addExpense(draft, ctx)
        val (second, b) = first.addExpense(draft, ctx)
        assertEquals(
            listOf(33334L, 33333L, 33333L),
            second.expense(a)!!.split.rows.map { it.share },
        )
        assertEquals(
            listOf(33333L, 33334L, 33333L),
            second.expense(b)!!.split.rows.map { it.share },
        )
        assertEquals("Other", second.expense(a)!!.title)
    }

    @Test
    fun anExpenseNeedsAnAmountAndSomeoneElse() {
        assertThrows(LedgerRuleException::class.java) {
            demo.ledger.addExpense(ExpenseDraft.equal(listOf(ME, "p-priya")), ctx)
        }
        assertThrows(LedgerRuleException::class.java) {
            demo.ledger.addExpense(ExpenseDraft.equal(listOf(ME)).copy(amount = 100), ctx)
        }
    }

    @Test
    fun editsLogHistoryAndClearTheFlag() {
        val villa = demo.ledger.expense("e-goa-villa")!!
        val draft =
            ExpenseDraft(
                title = villa.title,
                amount = rupee(19000),
                category = villa.category,
                split = villa.split,
                dueDate = villa.dueDate,
            )
        val edited = demo.ledger.updateExpense("e-goa-villa", draft, ctx).expense("e-goa-villa")!!
        assertEquals(
            HistoryKind.AmountChanged,
            edited.history.last().kind,
        )
        assertEquals(setOf(rupee(3800)), edited.split.rows.map { it.share }.toSet())
        assertEquals("p-kabir", edited.payerId)
        assertNull(edited.flag)
    }

    @Test
    fun deleteAndRestore() {
        val deleted = demo.ledger.deleteExpense("e-goa-villa", ctx)
        // Without the Villa (₹3,600 each), your −₹1,400 in Goa Trip becomes +₹2,200.
        assertEquals(rupee(2200), view(deleted).groupNets("g-goa")[ME])
        assertEquals(1, deleted.expenses.count { it.id == "e-goa-villa" })
        val restored = deleted.restoreExpense("e-goa-villa", ctx)
        assertEquals(-rupee(1400), view(restored).groupNets("g-goa")[ME])
        assertEquals(HistoryKind.Restored, restored.expense("e-goa-villa")!!.history.last().kind)
    }

    @Test
    fun paymentsArePendingUntilTheReceiverConfirms() {
        val (pending, id) =
            demo.ledger.recordPayment(
                PaymentDraft(ME, "p-meera", rupee(450), groupId = "g-flat302"),
                ctx,
            )
        assertEquals(PaymentStatus.Pending, pending.payment(id)!!.status)
        assertEquals(rupee(1850), view(pending).homeTotals().owe)
        val (received, rohan) =
            demo.ledger.recordPayment(PaymentDraft("p-rohan", ME, rupee(800), recordedBy = ME), ctx)
        assertEquals(PaymentStatus.Confirmed, received.payment(rohan)!!.status)
        assertEquals(rupee(2100), view(received).homeTotals().owed)
    }

    @Test
    fun confirmingAPaymentToYouAddsAReadInboxItem() {
        val (claimed, id) =
            demo.ledger.recordPayment(
                PaymentDraft(
                    "p-esha",
                    ME,
                    rupee(700),
                    expenseId = "e-olive",
                    recordedBy = "p-esha",
                ),
                ctx,
            )
        val confirmed = claimed.confirmPayment(id, ctx)
        val item = confirmed.inbox.last()
        assertEquals(InboxType.PaymentConfirmed, item.type)
        assertTrue(item.read)
        assertEquals("Dinner at Olive Garden", item.params.title)
    }

    @Test
    fun leavingNeedsAZeroBalance() {
        val error =
            assertThrows(LedgerRuleException::class.java) { demo.ledger.leaveGroup("g-goa", ctx) }
        assertEquals(
            "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave.",
            error.message,
        )
        val left = demo.ledger.leaveGroup("g-college", ctx)
        assertTrue(ME !in left.group("g-college")!!.memberIds)
    }

    @Test
    fun enteringADraftAmountCreatesItsExpense() {
        val (ledger, id) = demo.ledger.enterDraftAmount("d-gas-09", rupee(900), ctx)
        val expense = ledger.expense(id)!!
        assertEquals(LocalDate.of(2026, 9, 28), expense.date)
        assertEquals("g-flat302", expense.groupId)
        assertEquals(id, ledger.draft("d-gas-09")!!.expenseId)
    }

    @Test
    fun guestsAndTrialsAndClearing() {
        val (withGuest, id) = demo.ledger.addGuest("Tara Nair", "+91 90000 00000", ctx)
        assertTrue(withGuest.person(id)!!.isGuest)
        assertEquals(id, withGuest.addGuest("Tara", "+91 90000 00000", ctx).second)
        val trial = demo.ledger.startTrial(ctx).settings.entitlement
        assertEquals(Plan.Pro to LocalDate.of(2026, 10, 7), trial.plan to trial.trialEndsAt)
        assertTrue(demo.ledger.clearRecords().isEmpty)
    }
}
