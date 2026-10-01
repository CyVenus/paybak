package app.paybak.paybak.feature.insights

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Recurring expenses (insights §5, app-architecture §6.4 M9 UI tests). */
@RunWith(AndroidJUnit4::class)
class RecurringTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = InsightsAiRobot(compose)

    @Test
    fun theCookingGasAmountTurnsTheDraftIntoAnExpense() {
        launchPaybak("recurringFlat302").use {
            compose.awaitScreen("recurring")
            robot.tap("recurring.draft.r-gas.enterAmount")
            compose.awaitScreen("enterDraftAmount")
            robot.tag("enterAmount.action").assertIsNotEnabled()
            robot.tag("enterAmount.amount").performTextInput("1350")
            robot.tag("enterAmount.action").assertIsEnabled()
            robot.tap("enterAmount.action")
            compose.awaitScreen("recurring")
            robot.awaitGone("recurring.draft.r-gas")
            robot.await("toast")
            val gas = paybakApp.ledger.ledger.value.expenses.last()
            assertEquals("Cooking gas", gas.title)
            assertEquals("g-flat302", gas.groupId)
            assertEquals(135_000L, gas.amount)
            assertEquals(LocalDate.of(2026, 9, 28), gas.date)
            assertEquals(listOf(45_000L, 45_000L, 45_000L), gas.split.rows.map { it.share })
        }
    }

    @Test
    fun aRuleSetToNeverStopsRepeating() {
        launchPaybak("recurringFlat302").use {
            compose.awaitScreen("recurring")
            robot.tap("recurring.rule.r-wifi")
            robot.await("repeatSheet")
            compose
                .onNodeWithText("Paybak adds this expense on the 5th of every month.")
                .assertExists()
            robot.tap("repeatSheet.freq.never")
            robot.tap("repeatSheet.done")
            robot.awaitGone("recurring.rule.r-wifi")
            compose.onNodeWithText("Wi-Fi won’t repeat").assertExists()
            assertFalse(
                paybakApp.ledger.ledger.value.recurringRules.first { it.id == "r-wifi" }.active
            )
        }
    }
}
