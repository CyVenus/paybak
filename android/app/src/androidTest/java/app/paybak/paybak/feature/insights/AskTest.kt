package app.paybak.paybak.feature.insights

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Ask Paybak (insights §3, app-architecture §6.4 M9 UI tests). */
@RunWith(AndroidJUnit4::class)
class AskTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = InsightsAiRobot(compose)

    @Test
    fun whoOwesMeMoneyAnswersWithTheFigmaCopy() {
        launchPaybak("askStart").use {
            compose.awaitScreen("ask")
            robot.tap("ask.prompt.0")
            robot.await("ask.message.1")
            robot.assertText("ask.message.0", "Who owes me money?")
            robot.assertText(
                "ask.message.1",
                "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and " +
                    "Dev ₹700 each for tonight’s dinner.",
            )
            robot.await("ask.answerCard")
            robot.tap("ask.chip.remind.rohan")
            compose.awaitScreen("remind")
        }
    }

    @Test
    fun aDraftedCabIsSavedInPlaceAndViewOpensIt() {
        launchPaybak("askStart").use {
            compose.awaitScreen("ask")
            val before = paybakApp.ledger.ledger.value.expenses.size
            robot
                .tag("ask.composer")
                .performTextInput("Add ₹600 for a cab, split with Esha and Dev")
            robot.tag("ask.composer").performImeAction()
            robot.tap("ask.draft.save")
            robot.await("ask.draft.view")
            val expenses = paybakApp.ledger.ledger.value.expenses
            assertEquals(before + 1, expenses.size)
            val cab = expenses.last()
            assertEquals("Cab" to 60_000L, cab.title to cab.amount)
            assertEquals(listOf(20_000L, 20_000L, 20_000L), cab.split.rows.map { it.share })
            robot.tap("ask.draft.view")
            compose.awaitScreen("expense")
        }
    }
}
