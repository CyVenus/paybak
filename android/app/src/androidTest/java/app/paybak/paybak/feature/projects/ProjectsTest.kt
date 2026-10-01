package app.paybak.paybak.feature.projects

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Projects (app-architecture §6.3 M7 UI tests, screens-projects §11) on the demo. */
@RunWith(AndroidJUnit4::class)
class ProjectsTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun text(text: String) = compose.onNodeWithText(text)

    /** Waits for [tag], scrolls it into view when it sits in a scrolling page, and taps it. */
    private fun tap(tag: String) {
        compose.awaitTag(tag)
        runCatching { tag(tag).performScrollTo() }
        tag(tag).performClick()
    }

    private fun awaitText(text: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    private fun drone() = paybakApp.ledger.ledger.value

    @Test
    fun theDashboardShowsBuildADrone() {
        launchPaybak("groupsList").use {
            tap("groups.row.pj-drone")
            compose.awaitScreen("project")
            tag("project.state.active").assertExists()
            text("Project · 4 members · Active since 10 Aug").assertExists()
            text("₹52,000").assertExists()
            text("87% used").assertExists()
            text("₹8,000 left").assertExists()
            text("Planned items bring it to ₹58,000").assertExists()
            listOf("gps", "camera", "transmitter", "escs", "battery", "fc", "motors", "frame")
                .forEach { tag("project.component.c-drone-$it").assertExists() }
            text("Equal split · ₹13,000 each so far").assertExists()
            tag("project.transfer.0").assertExists()
            text("Rohan owes Dev").assertExists()
            text("₹8,500").assertExists()
            text("You’re settled in this project.").assertExists()
        }
    }

    @Test
    fun aNewPartStartsPlannedAndOnlyMovesTheProjection() {
        launchPaybak("projectDrone").use {
            compose.awaitScreen("project")
            tap("project.addComponent")
            compose.awaitTag("addComponent.add")
            tag("addComponent.add").assertIsNotEnabled()
            tag("addComponent.name").performTextReplacement("Spare propellers")
            tag("addComponent.estimate").performTextReplacement("2000")
            tag("addComponent.add").assertIsEnabled().performClick()
            awaitGone("addComponent.add")
            awaitText("Planned items bring it to ₹60,000")
            text("₹52,000").assertExists()
            val added = drone().components.last()
            assertEquals("Spare propellers" to ComponentStatus.Planned, added.name to added.status)
            tag("project.component.${added.id}").assertExists()
        }
    }

    @Test
    fun anActualCostBuysThePartAndMovesTheShares() {
        launchPaybak("projectAddComponent").use {
            compose.awaitTag("addComponent.add")
            tag("addComponent.name").performTextReplacement("Spare propellers")
            tag("addComponent.actual").performTextReplacement("4000")
            tag("addComponent.add").assertIsEnabled().performClick()
            awaitText("Equal split · ₹14,000 each so far")
            text("₹56,000").assertExists()
            assertEquals(ComponentStatus.Bought, drone().components.last().status)
        }
    }

    @Test
    fun editingAPartCanDeleteIt() {
        launchPaybak("projectDrone").use {
            tap("project.component.c-drone-gps")
            compose.awaitTag("addComponent.delete")
            text("Edit component").assertExists()
            tap("addComponent.delete")
            compose.awaitTag("project.deleteAlert")
            tag("project.deleteAlert.action").performClick()
            awaitText("All planned items are bought.")
            awaitGone("project.component.c-drone-gps")
        }
    }

    @Test
    fun percentAppliesOnceItAddsUp() {
        launchPaybak("projectSettings").use {
            compose.awaitScreen("projectSettings")
            tap("projectSettings.rule.percent")
            awaitText("Set each person’s share. Shares must add up to 100%.")
            field("p-dev").performTextReplacement("40")
            awaitText("Set each person’s share. Shares must add up to 100%. 15% over.")
            field("p-priya").performTextReplacement("10")
            awaitText("Set each person’s share. Shares must add up to 100%.")
            val contribution = drone().group("pj-drone")!!.project!!.contribution
            assertEquals(ContributionRule.Percent, contribution.rule)
            assertEquals(4000L, contribution.values["p-dev"])
        }
    }

    @Test
    fun closingShowsTheFinalPlanThenArchives() {
        launchPaybak("projectDrone").use {
            tap("project.settings")
            compose.awaitScreen("projectSettings")
            tap("projectSettings.close")
            compose.awaitTag("projectSettings.closeAlert")
            text("Close Build a Drone?").assertExists()
            tag("projectSettings.closeAlert.action").performClick()
            compose.awaitTag("project.state.closed")
            text("Closed · Read-only").assertExists()
            text("Rohan pays Dev").assertExists()
            text("₹8,000 under budget").assertExists()
            tag("project.addComponent").assertDoesNotExist()

            compose.runOnIdle {
                listOf("p-rohan" to 850_000L, "p-priya" to 400_000L).forEach { (from, amount) ->
                    val id =
                        paybakApp.ledger.recordPayment(
                            PaymentDraft(from, "p-dev", amount, groupId = "pj-drone", recordedBy = from)
                        )
                    paybakApp.ledger.confirmPayment(id)
                }
            }
            compose.awaitTag("project.state.archived")
            text("Everyone is settled").assertExists()
            tag("project.members.p-dev").assertExists()
        }
    }

    /** The editable share of [personId] on Project settings. */
    private fun field(personId: String) =
        compose.onNode(
            hasSetTextAction() and
                hasAnyAncestor(hasTestTag("projectSettings.member.$personId.value"))
        )
}
