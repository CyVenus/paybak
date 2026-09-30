package app.paybak.paybak.domain

import app.paybak.paybak.domain.settings.ExportRange
import app.paybak.paybak.domain.settings.ExportType
import app.paybak.paybak.domain.settings.NO_GROUP
import app.paybak.paybak.domain.settings.exportCsv
import app.paybak.paybak.domain.settings.exportDefaultSelection
import app.paybak.paybak.domain.settings.exportGroups
import app.paybak.paybak.domain.settings.exportPeriod
import app.paybak.paybak.domain.settings.exportRecords
import app.paybak.paybak.domain.settings.plainAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Export records on the demo, Wed 30 Sep 2026 (screens-settings §9, domain.md §6.5). */
class ExportRecordsTest {
    private val view = Demo.load()

    @Test
    fun theRangesResolveFromToday() {
        assertEquals("1 Sep – 30 Sep 2026", view.exportPeriod(ExportRange.ThisMonth).label)
        assertEquals("1 Jul – 30 Sep 2026", view.exportPeriod(ExportRange.Last3Months).label)
        val all = view.exportPeriod(ExportRange.AllTime)
        assertEquals(view.today, all.end)
        assertTrue(all.start < view.today.minusMonths(6))
    }

    @Test
    fun rowsWithRecordsInSeptemberStartTicked() {
        assertEquals(
            listOf(
                "Goa Trip",
                "Flat 302",
                "College Gang",
                "Dubai Weekend",
                "Build a Drone",
                "Hackathon Kit",
                "Without a group",
            ),
            view.exportGroups().map { it.name },
        )
        assertEquals(
            setOf("g-goa", "g-flat302", "pj-drone", NO_GROUP),
            view.exportDefaultSelection(view.exportPeriod(ExportRange.ThisMonth)),
        )
    }

    @Test
    fun withoutAGroupHasTheDinnerTheMoviesTheGroceriesAndPriyasPayment() {
        val september = view.exportPeriod(ExportRange.ThisMonth)
        val records = view.exportRecords(september, setOf(NO_GROUP))
        val titles = records.map { it.title }
        assertTrue("Dinner at Olive Garden" in titles)
        assertTrue("Movie tickets" in titles)
        assertTrue("Weekend groceries" in titles)
        val priya = records.single { it.type == ExportType.Payment && it.paidBy == "Priya Sharma" }
        assertEquals(rupee(1_050), priya.amount)
        assertEquals("Priya Sharma paid you", priya.title)
        assertTrue(records.all { it.group.id == NO_GROUP })
    }

    @Test
    fun theCsvHasAHeaderAndOneRowPerRecordWithShares() {
        val september = view.exportPeriod(ExportRange.ThisMonth)
        val records = view.exportRecords(september, setOf(NO_GROUP))
        val lines = exportCsv(records, "INR").trimEnd().lines()
        assertEquals(
            "Date,Group,Type,Title,Category,Paid by,Amount,Currency,Rate,Amount (INR),Shares",
            lines.first(),
        )
        assertEquals(records.size + 1, lines.size)
        val dinner = lines.single { "Olive Garden" in it }
        assertTrue(dinner.startsWith("2026-09-30,Without a group,Expense,Dinner at Olive Garden,"))
        assertTrue(",2800.00,INR,," in dinner)
        assertTrue("You ₹700; Priya Sharma ₹700" in dinner)
    }

    @Test
    fun amountsArePlainWithTwoDecimals() {
        assertEquals("700.00", plainAmount(rupee(700), "INR"))
        assertEquals("60.50", plainAmount(6_050, "AED"))
    }
}
