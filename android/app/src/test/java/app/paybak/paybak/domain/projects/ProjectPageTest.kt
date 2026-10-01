package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.recordPayment
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The project dashboard's read model (screens-projects §3–§8) from the demo at Figma parity. */
class ProjectPageTest {
    private val view = Demo.load("eshaClaimsPayment")

    private fun LedgerView.with(change: (Ledger, ActionContext) -> Ledger): LedgerView {
        val ctx = ActionContext(now, zone, defaultCurrency)
        return LedgerView(change(ledger, ctx), defaultCurrency, now, zone)
    }

    @Test
    fun buildADroneOnTrack() {
        val page = view.projectPage("pj-drone")!!
        assertEquals(ProjectState.Active, page.state)
        assertEquals("Project · 4 members · Active since 10 Aug", page.subtitle)
        assertNull(page.notice)
        with(page.budget) {
            assertEquals(listOf("₹52,000", "of ₹60,000", "87% used", "₹8,000 left"),
                listOf(spent, budget, percent, left))
            assertEquals("Planned items bring it to ₹58,000", planned)
            assertEquals(52f / 60f, progress, 1e-4f)
            assertEquals(58f / 60f, projected!!, 1e-4f)
        }
        assertEquals(
            listOf(
                listOf("GPS module", "Est. ₹6,000", "—"),
                listOf("Camera", "Dev · Unplanned", "₹7,500"),
                listOf("Transmitter", "You · Est. ₹5,000", "₹5,000"),
                listOf("ESCs and propellers", "You · Est. ₹8,000", "₹8,000"),
                listOf("Battery", "Rohan · Est. ₹5,000", "₹4,500"),
                listOf("Flight controller", "Priya · Est. ₹9,000", "₹9,000"),
                listOf("Motors ×4", "Dev · Est. ₹12,000", "₹12,000"),
                listOf("Frame", "Dev · Est. ₹6,000", "₹6,000"),
            ),
            page.components.map { listOf(it.component.name, it.subtitle, it.amount) },
        )
        assertNull(page.components.first().payerId)
        assertEquals("Equal split · ₹13,000 each so far", page.shareRule)
        assertEquals(
            listOf(
                listOf("Dev", "Paid ₹25,500", "₹12,500", Standing.Owed),
                listOf("You", "Paid ₹13,000", "Settled", Standing.Settled),
                listOf("Priya", "Paid ₹9,000", "₹4,000", Standing.Owe),
                listOf("Rohan", "Paid ₹4,500", "₹8,500", Standing.Owe),
            ),
            page.shares.map { listOf(it.name, it.caption, it.value, it.standing) },
        )
        assertEquals(
            listOf("Rohan owes Dev ₹8,500", "Priya owes Dev ₹4,000"),
            page.plan.map { "${it.title} ${it.amount}" },
        )
        assertTrue(page.plan.all { it.role == TransferRole.Others })
        assertEquals("You’re settled in this project.", page.footnote)
        assertTrue(page.editable)
    }

    @Test
    fun overBudget() {
        val page = Demo.load("devBuysGps").projectPage("pj-drone")!!
        assertEquals(ProjectState.OverBudget, page.state)
        with(page.budget) {
            assertTrue(over)
            assertEquals("₹1,500 over budget", left)
            assertEquals("All planned items are bought.", planned)
            assertEquals(60f / 61.5f, progress, 1e-4f)
            assertNull(projected)
        }
        assertEquals("Dev · Est. ₹6,000" to "₹9,500", page.components.first().let { it.subtitle to it.amount })
        assertEquals(
            listOf("₹19,625", "₹2,375", "₹6,375", "₹10,875"),
            page.shares.map { it.value },
        )
        assertEquals("You owe Dev", page.plan.first { it.role == TransferRole.YouPay }.title)
        assertEquals("You owe ₹2,375 in this project.", page.footnote)
    }

    @Test
    fun closedShowsTheFinalPlan() {
        val page = Demo.load("closeDrone").projectPage("pj-drone")!!
        assertEquals(ProjectState.Closed, page.state)
        assertEquals("Project · 4 members", page.subtitle)
        assertEquals("Closed · Read-only", page.notice?.title)
        assertEquals("₹8,000 under budget", page.budget.left)
        assertNull(page.budget.planned)
        assertNull(page.budget.projected)
        assertNull(page.shareRule)
        assertEquals(
            listOf("Rohan pays Dev", "Priya pays Dev"),
            page.plan.map { it.title },
        )
        assertEquals(
            "You’re settled. It becomes a permanent record once everyone has paid.",
            page.footnote,
        )
        assertTrue(!page.editable && !page.readyToArchive)
    }

    @Test
    fun aPendingPaymentIsNotedUntilConfirmed() {
        val pending =
            Demo.load("closeDrone").with { ledger, ctx ->
                ledger
                    .recordPayment(
                        PaymentDraft("p-rohan", "p-dev", 850_000, groupId = "pj-drone", recordedBy = "p-rohan"),
                        ctx,
                    )
                    .first
            }
        assertEquals(
            "You’re settled. It becomes a permanent record once everyone has paid. " +
                "1 payment is waiting for confirmation.",
            pending.projectPage("pj-drone")!!.footnote,
        )
    }

    @Test
    fun aClosedProjectWhosePlanIsPaidIsReadyToArchive() {
        val paid =
            Demo.load("closeDrone").with { ledger, ctx ->
                listOf("p-rohan" to 850_000L, "p-priya" to 400_000L).fold(ledger) { acc, (from, amount) ->
                    val (next, id) =
                        acc.recordPayment(
                            PaymentDraft(from, "p-dev", amount, groupId = "pj-drone", recordedBy = from),
                            ctx,
                        )
                    next.confirmPayment(id, ctx)
                }
            }
        val page = paid.projectPage("pj-drone")!!
        assertTrue(page.readyToArchive)
        assertEquals("Everyone is settled", page.planNotice?.title)
        assertNull(page.footnote)
        val archived = paid.with { ledger, ctx -> ledger.archiveIfSettled("pj-drone", ctx) }
        with(archived.projectPage("pj-drone")!!) {
            assertEquals(ProjectState.Archived, state)
            assertEquals("Project · 4 members · Closed 30 Sep", subtitle)
        }
    }

    @Test
    fun hackathonKitIsArchived() {
        val page = view.projectPage("pj-hackathon")!!
        assertEquals(ProjectState.Archived, page.state)
        assertEquals("Project · 4 members · Closed 30 Aug", page.subtitle)
        assertEquals("Read-only" to "Nothing here can be edited.", page.notice?.let { it.title to it.body })
        with(page.budget) {
            assertEquals(listOf("₹18,400", "of ₹20,000", "92% used", "₹1,600 under budget"),
                listOf(spent, budget, percent, left))
        }
        assertEquals("Everyone is settled", page.planNotice?.title)
        assertEquals(
            listOf("You" to "Settled", "Esha" to "Settled", "Dev" to "Settled", "Kabir" to "Settled"),
            page.members.map { it.name to it.status },
        )
        assertEquals(listOf(ME, "p-esha", "p-dev", "p-kabir"), page.members.map { it.personId })
    }
}
