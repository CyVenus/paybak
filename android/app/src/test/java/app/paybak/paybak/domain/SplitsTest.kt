package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.Splits
import org.junit.Assert.assertEquals
import org.junit.Test

/** verify.py `check_splits`. */
class SplitsTest {
    @Test
    fun equalSplitsRotateTheLeftoverPaisa() {
        assertEquals(
            setOf(rupee(700)),
            Splits.equal(rupee(2800), listOf("me", "priya", "esha", "dev")).shares.values.toSet(),
        )
        val first = Splits.equal(rupee(1000), listOf("me", "priya", "esha"), 0)
        assertEquals(mapOf("me" to 33334L, "priya" to 33333L, "esha" to 33333L), first.shares)
        val next = Splits.equal(rupee(1000), listOf("me", "priya", "esha"), first.counter)
        assertEquals(mapOf("me" to 33333L, "priya" to 33334L, "esha" to 33333L), next.shares)
    }

    @Test
    fun exactFooter() {
        val status =
            Splits.exactStatus(rupee(2800), listOf(rupee(700), rupee(700), rupee(700), rupee(550)))
        assertEquals(rupee(150), status.remaining)
        assertEquals("₹150 left", status.left)
        assertEquals("₹2,650 of ₹2,800", status.detail)
        assertEquals("₹50 over", Splits.exactStatus(rupee(100), listOf(rupee(150))).left)
    }

    @Test
    fun weightedSplits() {
        val percent =
            Splits.weighted(
                rupee(1000),
                mapOf("a" to 3333L, "b" to 3333L, "c" to 3334L),
                listOf("a", "b", "c"),
            )
        assertEquals(rupee(1000), percent.shares.values.sum())
        val shares =
            Splits.weighted(
                rupee(2800),
                mapOf("a" to 2L, "b" to 1L, "c" to 1L),
                listOf("a", "b", "c"),
            )
        assertEquals(mapOf("a" to rupee(1400), "b" to rupee(700), "c" to rupee(700)), shares.shares)
    }

    @Test
    fun receiptItemsWithTaxAndTip() {
        val items =
            listOf(
                rupee(430) to listOf("dev"),
                rupee(370) to listOf("esha"),
                rupee(450) to listOf("me"),
                rupee(240) to listOf("me"),
                rupee(240) to listOf("me", "esha", "dev"),
                rupee(270) to listOf("me", "esha", "dev"),
            )
        val (totals, subtotals) = Splits.itemized(items, rupee(2300), listOf("me", "esha", "dev"))
        assertEquals(
            mapOf("me" to rupee(860), "esha" to rupee(540), "dev" to rupee(600)),
            subtotals,
        )
        assertEquals(
            mapOf("me" to rupee(989), "esha" to rupee(621), "dev" to rupee(690)),
            totals.shares,
        )
    }

    @Test
    fun installmentsGiveTheLeftoverToTheEarliest() {
        assertEquals(
            listOf(rupee(2000), rupee(2000), rupee(2000)),
            Splits.installments(rupee(6000), 3),
        )
        assertEquals(listOf(334L, 333L, 333L), Splits.installments(1000, 3))
    }

    @Test
    fun largestRemainderAddsUpTo100() {
        val percent =
            Splits.largestRemainderPercent(
                mapOf(
                    "rent" to 12000L,
                    "food" to 3850L,
                    "stays" to 3600L,
                    "fun" to 1800L,
                    "travel" to 1200L,
                    "bills" to 850L,
                )
            )
        assertEquals(
            mapOf(
                "rent" to 51,
                "food" to 17,
                "stays" to 15,
                "fun" to 8,
                "travel" to 5,
                "bills" to 4,
            ),
            percent,
        )
    }
}
