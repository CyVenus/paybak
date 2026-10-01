package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.projectReport
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.ME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Add / Edit component sheet and the contribution rule (screens-projects §1.5, §5, §6.7). */
class ProjectEditsTest {
    private val view = Demo.load()
    private val ctx = ActionContext(view.now, view.zone, view.defaultCurrency)
    private val members = listOf(ME, "p-dev", "p-priya", "p-rohan")

    private fun after(ledger: app.paybak.paybak.domain.model.Ledger) =
        LedgerView(ledger, view.defaultCurrency, view.now, view.zone)

    @Test
    fun theSheetNeedsANameAndACostOnceBought() {
        assertFalse(ComponentForm().canSave("INR"))
        val named = ComponentForm(name = "Spare propellers")
        assertTrue(named.canSave("INR"))
        assertFalse(named.copy(status = ComponentStatus.Bought).canSave("INR"))
        val bought = named.withActual("2500")
        assertEquals(ComponentStatus.Bought, bought.status)
        assertTrue(bought.canSave("INR"))
        assertEquals(ComponentStatus.Planned, bought.withActual("").status)
        assertEquals(
            ComponentStatus.Done,
            named.copy(status = ComponentStatus.Done).withActual("").status,
        )
    }

    @Test
    fun aPlannedPartMovesOnlyTheProjection() {
        val (ledger, id) =
            view.ledger.addComponent(
                "pj-drone",
                ComponentForm(name = "Spare propellers", estimate = "2000"),
                "INR",
                ctx,
            )
        val report = after(ledger).projectReport("pj-drone")!!
        assertEquals(5_200_000L, report.spent)
        assertEquals("Planned items bring it to ₹60,000", report.plannedLine)
        assertEquals(id, report.parts.first().id)
        assertEquals(ME, report.parts.first().paidBy)
    }

    @Test
    fun aDonePartCountsAtItsActualCost() {
        val (ledger, id) =
            view.ledger.addComponent(
                "pj-drone",
                ComponentForm(name = "Gimbal", actual = "4000", status = ComponentStatus.Done, paidBy = "p-priya"),
                "INR",
                ctx,
            )
        val page = after(ledger).projectPage("pj-drone")!!
        assertEquals("₹56,000", page.budget.spent)
        assertEquals("Equal split · ₹14,000 each so far", page.shareRule)
        val part = ledger.component(id)!!
        assertEquals(ComponentStatus.Done, part.status)
        assertEquals(listOf("added", "bought", "done"), part.history.map { it.kind })
    }

    @Test
    fun editingBuysThePlannedGpsModuleAndDeletingRemovesIt() {
        val gps = view.ledger.component("c-drone-gps")!!
        val form = ComponentForm.of(gps, "INR").copy(paidBy = "p-dev").withActual("9500")
        val bought = view.ledger.editComponent(gps.id, form, "INR", ctx)
        val page = after(bought).projectPage("pj-drone")!!
        assertEquals("₹1,500 over budget", page.budget.left)
        assertEquals(ComponentStatus.Bought, bought.component(gps.id)!!.status)
        val gone = after(bought.deleteComponent(gps.id)).projectPage("pj-drone")!!
        assertEquals("₹52,000", gone.budget.spent)
        assertEquals("All planned items are bought.", gone.budget.planned)
        assertNull(gone.components.firstOrNull { it.component.id == gps.id })
    }

    @Test(expected = LedgerRuleException::class)
    fun aBoughtPartWithoutACostIsRefused() {
        view.ledger.addComponent(
            "pj-drone",
            ComponentForm(name = "Gimbal", status = ComponentStatus.Bought),
            "INR",
            ctx,
        )
    }

    @Test
    fun percentMustMakeAHundred() {
        val start = ContributionEdit.prefill(ContributionRule.Percent, members, 6_000_000, 5_200_000, "INR")
        assertEquals(members.associateWith { "25" }, start.typed)
        assertTrue(start.check(members, 6_000_000, "INR").valid)
        val over = start.copy(typed = start.typed + ("p-dev" to "40"))
        assertEquals(
            ContributionCheck(false, "Set each person’s share. Shares must add up to 100%. 15% over."),
            over.check(members, 6_000_000, "INR"),
        )
        val fixed = over.copy(typed = over.typed + ("p-priya" to "10"))
        assertTrue(fixed.check(members, 6_000_000, "INR").valid)
        assertEquals(4000L, fixed.contribution(members, "INR").values["p-dev"])
        val thirds = ContributionEdit.prefill(ContributionRule.Percent, members.take(3), null, 0, "INR")
        assertEquals(listOf("33.34", "33.33", "33.33"), members.take(3).map { thirds.typed[it] })
    }

    @Test
    fun fixedMustMakeTheBudget() {
        val start = ContributionEdit.prefill(ContributionRule.Fixed, members, 6_000_000, 5_200_000, "INR")
        assertEquals(members.associateWith { "15000" }, start.typed)
        val short = start.copy(typed = start.typed + (ME to "10000"))
        assertEquals(
            "Set a fixed amount for each person. ₹5,000 of the budget left.",
            short.check(members, 6_000_000, "INR").helper,
        )
        val noBudget = ContributionEdit.prefill(ContributionRule.Fixed, members, null, 5_200_000, "INR")
        assertEquals("13000", noBudget.typed["p-dev"])
        assertTrue(noBudget.check(members, null, "INR").valid)
    }

    @Test
    fun aPercentRuleSharesWhatsSpent() {
        val edit = ContributionEdit(
            ContributionRule.Percent,
            mapOf(ME to "40", "p-dev" to "20", "p-priya" to "20", "p-rohan" to "20"),
        )
        val ledger =
            view.ledger.copy(
                groups =
                    view.ledger.groups.map {
                        if (it.id != "pj-drone") it
                        else it.copy(project = it.project!!.copy(contribution = edit.contribution(members, "INR")))
                    }
            )
        val page = after(ledger).projectPage("pj-drone")!!
        assertEquals("Percent split · shares follow each person’s %", page.shareRule)
        assertEquals("You owe ₹7,800 in this project.", page.footnote)
    }
}
