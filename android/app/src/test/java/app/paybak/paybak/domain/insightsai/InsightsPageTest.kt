package app.paybak.paybak.domain.insightsai

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.insights.LoanBadge
import app.paybak.paybak.domain.insights.insightsPage
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Insights report beyond verify.py's checks (insights §2.6): rows, friends, loans, months. */
class InsightsPageTest {
    private val page = Demo.load().insightsPage(YearMonth.of(2026, 9))

    @Test
    fun theHeroAndCategoriesMatchFigma() {
        assertEquals("September 2026", page.monthLabel)
        assertEquals(2_330_000L, page.total)
        assertEquals("Up 5% from August", page.trend)
        assertEquals(listOf(95, 113, 101, 106, 114, 120), page.chart.map { it.heightPt })
        assertEquals(
            listOf("home", "food", "bed", "ticket", "car", "bolt"),
            page.categories.map { it.icon },
        )
        assertTrue(page.chartDescription.startsWith("Your share by month: April ₹18,400, May"))
    }

    @Test
    fun groupsCarryTheirIcons() {
        assertEquals(
            listOf(
                listOf("Flat 302", 55, 1_285_000L, "home", "g-flat302"),
                listOf("Goa Trip", 34, 790_000L, "plane", "g-goa"),
                listOf("Without a group", 11, 255_000L, "people", null),
            ),
            page.groups.map { listOf(it.label, it.percent, it.amount, it.icon, it.groupId) },
        )
    }

    @Test
    fun friendsShareEveryExpenseAndAddUpToTheTotal() {
        assertEquals(page.total, page.friends.sumOf { it.amount })
        assertEquals(100, page.friends.sumOf { it.percent })
        assertEquals(page.friends.sortedByDescending { it.amount }, page.friends)
        assertTrue(page.friends.all { it.personId != null })
    }

    @Test
    fun lentVersusBorrowedSinceApril() {
        assertEquals("Lent vs borrowed since April", page.lentTitle)
        assertEquals(450_000L to 0L, page.lent to page.borrowed)
        assertEquals(1, page.loans.size)
        val loan = page.loans.single()
        assertEquals("Kabir · Bike service", loan.label)
        assertEquals(LoanBadge.PaidBack, loan.badge)
    }

    @Test
    fun monthsGoBackToTheFirstExpenseAndNotPastToday() {
        assertTrue(page.hasPrevious)
        assertFalse(page.hasNext)
        val august = Demo.load().insightsPage(YearMonth.of(2026, 8))
        assertTrue(august.hasNext)
        assertEquals(2_220_000L, august.total)
        assertEquals("Up 8% from July", august.trend)
    }
}
