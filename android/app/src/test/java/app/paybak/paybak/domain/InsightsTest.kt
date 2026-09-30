package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.categorySpendAnswer
import app.paybak.paybak.domain.calc.insights
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

/** verify.py `check_insights`. */
class InsightsTest {
    private val ledger = Demo.load()
    private val september = ledger.insights(YearMonth.of(2026, 9))

    @Test
    fun september() {
        assertEquals("₹23,300", Money.format(september.total))
        assertEquals(
            listOf(
                listOf("Rent", 51, "₹12,000", 158.1),
                listOf("Food", 17, "₹3,850", 52.7),
                listOf("Stays", 15, "₹3,600", 46.5),
                listOf("Fun", 8, "₹1,800", 24.8),
                listOf("Travel", 5, "₹1,200", 15.5),
                listOf("Bills", 4, "₹850", 12.4),
            ),
            september.categories.map {
                listOf(
                    it.label,
                    it.percent,
                    Money.format(it.amount),
                    Math.round(310 * it.percent / 100.0 * 10) / 10.0,
                )
            },
        )
        assertEquals(
            listOf(
                Triple("Flat 302", 55, "₹12,850"),
                Triple("Goa Trip", 34, "₹7,900"),
                Triple("Without a group", 11, "₹2,550"),
            ),
            september.groups.map { Triple(it.label, it.percent, Money.format(it.amount)) },
        )
    }

    @Test
    fun chartAndTrend() {
        assertEquals(
            listOf(18400L, 21950L, 19600L, 20600L, 22200L, 23300L),
            september.chart.map { it.total / 100 },
        )
        assertEquals(listOf(95, 113, 101, 106, 114, 120), september.chart.map { it.heightPt })
        assertEquals("Up 5% from August", september.trend)
    }

    @Test
    fun lentAndAsk() {
        assertEquals(
            "₹4,500" to "₹0",
            Money.format(september.lent) to Money.format(september.borrowed),
        )
        assertEquals(
            "You spent ₹3,850 on food in September — 17% of your ₹23,300 share.",
            ledger.categorySpendAnswer(Category.Food, YearMonth.of(2026, 9)),
        )
    }
}
