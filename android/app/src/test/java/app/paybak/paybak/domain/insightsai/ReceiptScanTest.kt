package app.paybak.paybak.domain.insightsai

import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.ScanItem
import app.paybak.paybak.domain.model.ScanTax
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.scan.OcrLine
import app.paybak.paybak.domain.scan.ReceiptParser
import app.paybak.paybak.domain.scan.ReceiptSplit
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** Reading the Leopold Cafe receipt and splitting it (insights §4.6, app-architecture §4.1–4.2). */
class ReceiptScanTest {
    /** What ML Kit returns for the bundled receipt: the rupee sign read as T, 7, Z or dropped. */
    private val leopoldOcr =
        listOf(
            line("Chicken biryani", 60, 356, 358, 396),
            line("Paneer tikka", 63, 431, 297, 467),
            line("Leopold Cafe", 266, 84, 631, 151),
            line("Colaba Causeway, Mumbai", 208, 170, 688, 204),
            line("Wed 30 Sep 2026 1:15 pm", 214, 222, 682, 264),
            line("Fish and chips", 61, 507, 335, 552),
            line("Chocolate brownie", 62, 590, 430, 622),
            line("Masala fries", 62, 668, 294, 700),
            line("Fresh lime soda x3", 63, 746, 415, 778),
            line("Subtotal", 62, 878, 214, 906),
            line("GST 5%", 61, 943, 198, 973),
            line("Tip 10%", 60, 1005, 196, 1046),
            line("Total", 60, 1138, 172, 1174),
            line("Thank you. Visit again.", 248, 1250, 640, 1288),
            line("T430", 740, 356, 834, 388),
            line("7370", 743, 431, 835, 466),
            line("T450", 740, 512, 834, 544),
            line("7240", 740, 588, 834, 621),
            line("240", 740, 665, 835, 700),
            line("7270", 742, 743, 834, 777),
            line("72,000", 710, 878, 836, 910),
            line("Z100", 753, 943, 836, 973),
            line("7200", 746, 1006, 836, 1039),
            line("72,300", 669, 1135, 835, 1180),
        )

    private val leopold =
        ReceiptScan(
            merchant = "Leopold Cafe",
            date = LocalDate.of(2026, 9, 30),
            time = "13:15",
            items =
                listOf(
                    ScanItem("Chicken biryani", 43_000),
                    ScanItem("Paneer tikka", 37_000),
                    ScanItem("Fish and chips", 45_000),
                    ScanItem("Chocolate brownie", 24_000),
                    ScanItem("Masala fries", 24_000),
                    ScanItem("Fresh lime soda ×3", 27_000),
                ),
            subtotal = 200_000,
            taxes = listOf(ScanTax("GST 5%", 500, 10_000)),
            tip = 20_000,
            total = 230_000,
        )

    private val drawn =
        listOf(
            listOf("p-dev"),
            listOf("p-esha"),
            listOf(ME),
            listOf(ME),
            listOf(ME, "p-esha", "p-dev"),
            listOf(ME, "p-esha", "p-dev"),
        )
    private val people = listOf(ME, "p-esha", "p-dev")

    @Test
    fun theOcrLinesReadAsTheDesignedReceipt() {
        assertEquals(leopold, ReceiptParser.parse(leopoldOcr))
    }

    @Test
    fun cleanTextReadsTooAndNothingReadableIsNull() {
        val clean =
            listOf(
                line("Corner Store", 0, 0, 300, 40),
                line("Bread ₹60", 0, 60, 300, 90),
                line("Milk Rs 54.50", 0, 100, 300, 130),
                line("Total ₹114.50", 0, 140, 300, 170),
            )
        val scan = ReceiptParser.parse(clean)!!
        assertEquals(listOf(ScanItem("Bread", 6_000), ScanItem("Milk", 5_450)), scan.items)
        assertEquals(11_450L, scan.subtotal)
        assertEquals(11_450L, scan.total)
        assertNull(ReceiptParser.parse(listOf(line("A blurry photo", 0, 0, 100, 20))))
    }

    @Test
    fun taxAndTipFollowEachPersonsItems() {
        assertEquals(230_000L, ReceiptSplit.total(leopold))
        assertEquals("Tip 10%", ReceiptSplit.tipLabel(leopold))
        val totals = ReceiptSplit.totals(leopold, drawn, people)
        assertEquals(mapOf(ME to 98_900L, "p-esha" to 62_100L, "p-dev" to 69_000L), totals.totals)
        assertEquals(0, totals.unassigned)
        assertEquals(8_000L, ReceiptSplit.eachShare(24_000, 3))
        val partial = ReceiptSplit.totals(leopold, drawn.take(2) + List(4) { emptyList() }, people)
        assertEquals(4, partial.unassigned)
        assertEquals(mapOf(ME to 0L, "p-esha" to 42_550L, "p-dev" to 49_450L), partial.totals)
    }

    @Test
    fun theNoteNamesTheReceiptsOwnCharges() {
        assertEquals("Includes GST and tip", ReceiptSplit.chargesNote(leopold))
        assertEquals("Includes GST", ReceiptSplit.chargesNote(leopold.copy(tip = null)))
        val service = leopold.copy(taxes = listOf(ScanTax("Service charge 10%", 1_000, 20_000)))
        assertEquals("Includes service charge and tip", ReceiptSplit.chargesNote(service))
        assertNull(ReceiptSplit.chargesNote(leopold.copy(taxes = emptyList(), tip = null)))
    }

    @Test
    fun theScanEndsInLunchAtLeopoldCafe() {
        val draft = ReceiptSplit.draft(leopold, drawn, people, LocalDate.of(2026, 9, 30))
        assertEquals("Lunch at Leopold Cafe", draft.title)
        assertEquals(230_000L, draft.amount)
        assertEquals(Category.Food.id, draft.category)
        assertEquals(LocalDate.of(2026, 9, 30), draft.date)
        assertEquals(SplitMode.Itemized, draft.split.mode)
        assertEquals(people, draft.personIds)
        assertEquals(drawn, draft.itemized!!.items.map { it.personIds })
        assertEquals(listOf("GST 5%", "Tip 10%"), draft.itemized!!.lines.map { it.label })
        assertEquals(200_000L, draft.itemized!!.subtotal)
    }

    @Test
    fun editsKeepTheTotalAndTheChecksHonest() {
        val misread = leopold.copy(items = leopold.items.dropLast(1))
        assertFalse(ReceiptSplit.itemsMatch(misread))
        val dinner =
            leopold.copy(
                merchant = "Corner Shop",
                time = "20:30",
                items = listOf(ScanItem("Soap", 200_000)),
            )
        assertEquals(Category.Other, ReceiptSplit.category(dinner))
        assertEquals(
            "Corner Shop",
            ReceiptSplit.draft(dinner, listOf(people), people, LocalDate.of(2026, 9, 30)).title,
        )
        val late =
            ReceiptSplit.draft(
                leopold.copy(time = "21:00"),
                drawn,
                people,
                LocalDate.of(2026, 9, 29),
            )
        assertEquals("Dinner at Leopold Cafe", late.title)
        assertEquals(LocalDate.of(2026, 9, 29), late.date)
    }

    private fun line(text: String, left: Int, top: Int, right: Int, bottom: Int) =
        OcrLine(text, left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}
