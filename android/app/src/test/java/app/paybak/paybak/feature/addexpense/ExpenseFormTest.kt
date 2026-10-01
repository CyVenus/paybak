package app.paybak.paybak.feature.addexpense

import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Rate
import app.paybak.paybak.domain.model.ReceiptResult
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.ScanItem
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.scan.ReceiptSplit
import app.paybak.paybak.navigation.RouteResult
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** The Add expense form's people and receipt rules (add-expense §3.4, §3.12). */
class ExpenseFormTest {
    private val today = LocalDate.of(2026, 9, 30)
    private val now = Instant.parse("2026-09-30T08:00:00Z")
    private val people = listOf(ME, "p-esha", "p-dev")
    private val scan =
        ReceiptScan(
            merchant = "Leopold Cafe",
            date = today,
            items = listOf(ScanItem("Chicken biryani", 43_000), ScanItem("Paneer tikka", 37_000)),
            subtotal = 80_000,
        )

    /** The receipt read in rupees: Dev had the biryani, you and Esha the paneer. */
    private val assigned = listOf(listOf("p-dev"), listOf(ME, "p-esha"))
    private val scanned =
        RouteResult.Receipt(
            ReceiptResult(
                ReceiptSplit.draft(scan, assigned, people, today),
                scan = scan,
                photo = "receipt.jpg",
            )
        )

    @Test
    fun aReceiptsItemsStayOnlyWhileThePeopleDont() {
        val form = ExpenseForm(currency = "INR", date = today).applying(scanned, now, "INR")
        assertEquals(SplitMode.Itemized, form.split.mode)
        assertNotNull(form.itemized)
        val same = form.withPeople(people)
        assertEquals(SplitMode.Itemized, same.split.mode)
        assertEquals(form.itemized, same.itemized)
        val fewer = form.withPeople(listOf(ME, "p-esha"))
        assertEquals(SplitMode.Equal, fewer.split.mode)
        assertNull(fewer.itemized)
    }

    @Test
    fun aScanSwitchesAForeignFormToTheDefaultCurrency() {
        val form =
            ExpenseForm(
                amount = "120",
                currency = "AED",
                rate = Rate("22.85", "INR"),
                date = today,
            )
        val next = form.applying(scanned, now, "INR")
        assertEquals("INR", next.currency)
        assertNull(next.rate)
        assertEquals(80_000L, next.total)
    }
}
