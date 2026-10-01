package app.paybak.paybak.feature.insights

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Scan receipt with the simulated camera (insights §4, app-architecture §6.4 M9 UI tests). */
@RunWith(AndroidJUnit4::class)
class ScanReceiptTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = InsightsAiRobot(compose)

    @Test
    fun theReceiptIsReadAssignedAndFillsTheForm() {
        launchPaybak("addExpenseEmpty", pro = true).use {
            compose.awaitScreen("addExpense")
            robot.scrollTap("addExpense.row.receipt")
            compose.awaitScreen("scanCamera")
            robot.tap("scan.shutter")
            compose.awaitScreen("scanReview", timeoutMillis = 30_000)
            robot.assertText("scanReview.found", "We found 6 items.")
            robot.tap("scanReview.confirm")
            compose.awaitScreen("scanAssign")
            robot.tap("scanAssign.addPeople")
            compose.awaitScreen("pickPeople")
            robot.tap("splitWith.friend.p-esha")
            robot.tap("splitWith.friend.p-dev")
            robot.tap("splitWith.done")
            compose.awaitScreen("scanAssign")
            robot.tag("scanAssign.continue").assertIsNotEnabled()
            val drawn = listOf(listOf("dev"), listOf("esha"), listOf("you"), listOf("you"))
            val everyone = listOf("you", "esha", "dev")
            (drawn + listOf(everyone, everyone)).forEachIndexed { item, people ->
                people.forEach { robot.scrollTap("scanAssign.item.$item.person.$it") }
            }
            compose.onNodeWithText("All items assigned").assertExists()
            listOf("₹989", "₹621", "₹690").forEach { compose.onNodeWithText(it).assertExists() }
            robot.tag("scanAssign.continue").assertIsEnabled()
            robot.tap("scanAssign.continue")
            compose.awaitScreen("addExpense")
            robot.await("addExpense.form")
            compose.onNodeWithText("₹2,300").assertExists()
            compose.onNodeWithText("Itemized · 3 people").assertExists()
            compose.onNodeWithText("Lunch at Leopold Cafe").assertExists()
        }
    }
}
