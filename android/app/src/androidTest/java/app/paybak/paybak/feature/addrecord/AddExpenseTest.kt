package app.paybak.paybak.feature.addrecord

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import app.paybak.paybak.pressSystemBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Add expense (screens-add-expense §13): the form, the split editor and the discard alert. */
@RunWith(AndroidJUnit4::class)
class AddExpenseTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = AddRecordRobot(compose)

    @Test
    fun addingTheOliveGardenBillLandsOnItsDetail() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            val before = paybakApp.ledger.ledger.value.expenses.size
            robot.openFromAddSheet("expense")
            compose.awaitScreen("addExpense")
            robot.tag("addExpense.save").assertIsNotEnabled()
            robot.tag("addExpense.amount").performTextInput("2800")
            robot.tap("addExpense.addPeople")
            compose.awaitScreen("pickPeople")
            listOf("p-priya", "p-esha", "p-dev").forEach { robot.tap("splitWith.friend.$it") }
            robot.tap("splitWith.done")
            robot.await("addExpense.form")
            robot.tag("addExpense.save").assertIsEnabled()
            compose.onNodeWithText("Equally · ₹700 each").assertExists()
            robot.tag("addExpense.title").performTextInput("Dinner at Olive Garden")
            robot.tap("addExpense.row.category")
            robot.tap("category.row.food")
            robot.awaitGone("category.sheet")
            robot.tap("addExpense.due.weekend")
            compose.onNodeWithText("Sun 4 Oct").assertExists()
            robot.tap("addExpense.save")

            compose.awaitScreen("expense")
            robot.await("toast")
            compose.onNodeWithText("Expense added").assertExists()
            compose.onNodeWithText("Split equally · 4 people").assertExists()
            listOf("me", "p-priya", "p-esha", "p-dev").forEach {
                compose
                    .onNode(hasText("₹700") and hasAnyAncestor(hasTestTag("expense.split.$it")))
                    .assertExists()
            }
            val expenses = paybakApp.ledger.ledger.value.expenses
            assertEquals(before + 1, expenses.size)
            val added = expenses.last()
            assertEquals("food", added.category)
            assertEquals(
                listOf(70_000L, 70_000L, 70_000L, 70_000L),
                added.split.rows.map { it.share },
            )
        }
    }

    @Test
    fun exactAmountsMustAddUpBeforeDone() {
        launchPaybak("addExpenseSplitExactError").use {
            robot.await("split.page")
            compose.onNodeWithText("₹150 left").assertExists()
            compose.onNodeWithText("₹2,650 of ₹2,800").assertExists()
            robot.tag("split.done").assertIsNotEnabled()
            compose
                .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("split.row.p-dev")))
                .performTextReplacement("700")
            compose.onNodeWithText("₹0 left").assertExists()
            robot.tag("split.done").assertIsEnabled()
            robot.tap("split.done")
            robot.await("addExpense.form")
            compose.onNodeWithText("Exact · 4 people").assertExists()
        }
    }

    @Test
    fun closingWithChangesAsksFirst() {
        launchPaybak("addExpenseFilled").use { scenario ->
            robot.await("addExpense.form")
            robot.tag("addExpense.title").performTextReplacement("Dinner")
            robot.tap("addExpense.close")
            robot.await("addExpense.discardAlert")
            compose.onNodeWithText("Discard this expense?").assertExists()
            robot.tap("addExpense.discardAlert.cancel")
            robot.awaitGone("addExpense.discardAlert")
            robot.tag("addExpense.form").assertExists()
            scenario.pressSystemBack()
            robot.tap("addExpense.discardAlert.action")
            robot.awaitGone("screen.addExpense")
        }
    }
}
