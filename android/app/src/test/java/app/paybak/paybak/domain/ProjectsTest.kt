package app.paybak.paybak.domain

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.recordPayment
import app.paybak.paybak.domain.actions.tick
import app.paybak.paybak.domain.calc.projectReport
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ProjectStatus
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** verify.py `check_projects`. */
class ProjectsTest {
    private val ledger = Demo.load()

    @Test
    fun buildADrone() {
        val report = ledger.projectReport("pj-drone")!!
        assertEquals(
            "₹52,000" to "₹60,000",
            Money.format(report.spent) to Money.format(report.budget!!),
        )
        assertEquals(87, report.percentUsed)
        assertEquals("₹8,000 left", report.budgetLine)
        assertEquals("Planned items bring it to ₹58,000", report.plannedLine)
        assertEquals(
            mapOf(
                "You" to listOf(13000L, 13000L, 0L),
                "Dev" to listOf(25500L, 13000L, 12500L),
                "Priya" to listOf(9000L, 13000L, -4000L),
                "Rohan" to listOf(4500L, 13000L, -8500L),
            ),
            report.fairShare.associate {
                ledger.first(it.personId) to listOf(it.paid / 100, it.share / 100, it.net / 100)
            },
        )
        assertEquals(
            listOf("Dev", "You", "Priya", "Rohan"),
            report.fairShare.map { ledger.first(it.personId) },
        )
        assertEquals(
            mapOf("You" to 51, "Dev" to 100, "Priya" to 35, "Rohan" to 18),
            report.fairShare.associate {
                ledger.first(it.personId) to (it.paidFill * 100).roundToInt()
            },
        )
        assertEquals(
            listOf("Rohan owes Dev ₹8,500", "Priya owes Dev ₹4,000"),
            report.plan.map {
                "${ledger.first(it.debtorId)} owes ${ledger.first(it.creditorId)} ${Money.format(it.amount)}"
            },
        )
        assertEquals(
            listOf(
                "GPS module",
                "Camera",
                "Transmitter",
                "ESCs and propellers",
                "Battery",
                "Flight controller",
                "Motors ×4",
                "Frame",
            ),
            report.parts.map { it.name },
        )
        assertEquals(rupee(2900), ledger.homeTotals().owed)
    }

    @Test
    fun overBudget() {
        val over = Demo.load("devBuysGps")
        val report = over.projectReport("pj-drone")!!
        assertEquals(
            "₹61,500 of ₹60,000 · ₹1,500 over budget",
            "${Money.format(report.spent)} of ${Money.format(report.budget!!)} · ${report.budgetLine}",
        )
        assertEquals(
            mapOf("You" to -2375L, "Dev" to 19625L, "Priya" to -6375L, "Rohan" to -10875L),
            over.groupNets("pj-drone").mapKeys { over.first(it.key) }.mapValues { it.value / 100 },
        )
        // Dev bought it, so the log says so (not "You bought GPS module").
        assertEquals("p-dev", over.ledger.component("c-drone-gps")!!.history.last().by)
        assertTrue(over.timeline().any { it.title == "Dev bought GPS module" })
    }

    @Test
    fun closeAndArchive() {
        val closed = Demo.load("closeDrone")
        assertEquals(ProjectStatus.Closed, closed.group("pj-drone")!!.project!!.status)
        val report = closed.projectReport("pj-drone")!!
        assertEquals(
            listOf("Rohan pays Dev ₹8,500", "Priya pays Dev ₹4,000"),
            report.plan.map {
                "${closed.first(it.debtorId)} pays ${closed.first(it.creditorId)} ${Money.format(it.amount)}"
            },
        )
        assertEquals("₹8,000 under budget", report.budgetLine)
        val ctx = ActionContext(closed.now, Demo.zone, "INR")
        var next = closed.ledger
        for ((id, from, amount) in
            listOf(
                Triple("pay-rohan-dev", "p-rohan", 8500),
                Triple("pay-priya-dev", "p-priya", 4000),
            )) {
            next =
                next
                    .recordPayment(
                        PaymentDraft(
                            from,
                            "p-dev",
                            rupee(amount),
                            method = PaymentMethod.Upi,
                            groupId = "pj-drone",
                            recordedBy = from,
                            id = id,
                        ),
                        ctx,
                    )
                    .first
            next = next.confirmPayment(id, ctx)
        }
        next = next.tick(ctx.copy(at = closed.now.plusSeconds(60)))
        assertEquals(ProjectStatus.Archived, next.group("pj-drone")!!.project!!.status)
    }

    @Test
    fun hackathonKit() {
        val report = ledger.projectReport("pj-hackathon")!!
        assertEquals(
            Triple("₹18,400", 92, "₹1,600 under budget"),
            Triple(Money.format(report.spent), report.percentUsed, report.budgetLine),
        )
        assertEquals(setOf(0L), ledger.groupNets("pj-hackathon").values.toSet())
        assertEquals(
            "30 Aug",
            Dates.short(ledger.localDate(ledger.group("pj-hackathon")!!.project!!.closedAt!!)),
        )
    }
}
