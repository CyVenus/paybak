package app.paybak.paybak

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.feature.addrecord.AddRecordRobot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The main journey across the modules on a new account: onboarding, a first expense with a new
 * friend, settling it from Settle up, and both records on Activity.
 */
@RunWith(AndroidJUnit4::class)
class JourneyTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = AddRecordRobot(compose)

    private fun awaitText(text: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }

    @Test
    fun aNewAccountSplitsABillSettlesItAndSeesItOnActivity() {
        launchPaybak().use {
            signUp()
            addDinnerWithKabir()
            kabirPaysBack()

            robot.tap("home.tab.activity")
            compose.awaitScreen("activity")
            awaitText("Kabir paid you")
            compose.onNodeWithText("You added Dinner").assertExists()
        }
        val payment = paybakApp.ledger.ledger.value.payments.single()
        assertEquals(60_000L, payment.amount)
        assertEquals(PaymentStatus.Confirmed, payment.status)
    }

    /** Get Started with Google, a name, rupees and no extras, to Home. */
    private fun signUp() {
        compose.awaitScreen("welcome1")
        robot.tap("welcome.skip")
        compose.awaitScreen("getStarted")
        robot.tap("getStarted.google")
        compose.awaitScreen("setup1")
        robot.tag("setup1.name").performTextInput("Arjun Mehta")
        robot.tap("setup1.continue")
        compose.awaitScreen("setup2")
        robot.tag("setup2.search").performTextInput("INR")
        robot.tap("setup2.row.INR")
        robot.tap("setup2.continue")
        compose.awaitScreen("setup3")
        robot.tap("setup.skip")
        compose.awaitScreen("setup4")
        robot.tap("setup.skip")
        compose.awaitScreen("allSet")
        robot.tap("allSet.goHome")
        compose.awaitScreen("homeFirstDay")
    }

    /** ₹1,200 split equally with Kabir, added as a guest from Split with. */
    private fun addDinnerWithKabir() {
        robot.tap("home.firstDay.addExpense")
        compose.awaitScreen("addExpense")
        robot.tag("addExpense.amount").performTextInput("1200")
        robot.tap("addExpense.addPeople")
        compose.awaitScreen("pickPeople")
        robot.tag("splitWith.search").performTextInput("Kabir")
        robot.tap("splitWith.addGuest")
        robot.tap("splitWith.done")
        robot.await("addExpense.form")
        robot.tag("addExpense.title").performTextInput("Dinner")
        robot.tap("addExpense.save")
        compose.awaitScreen("expense")
        compose.onNodeWithText("Expense added").assertExists()
        robot.tap("expense.back")
        compose.awaitScreen("homeActive")
        compose.onNodeWithText("+₹600").assertExists()
    }

    /** Settle up → Kabir's page → Record payment, prefilled with what he owes. */
    private fun kabirPaysBack() {
        robot.tap("home.balance.settleUp")
        compose.awaitScreen("settleUp")
        val kabir = paybakApp.ledger.ledger.value.people.single { it.name == "Kabir" }
        robot.tap("settleUp.row.${kabir.id}")
        compose.awaitScreen("friend")
        robot.tap("friend.recordPayment")
        compose.awaitScreen("recordPayment")
        compose.onNodeWithText("₹600").assertExists()
        robot.tap("recordPayment.save")
        compose.awaitScreen("payment")
        compose.onNodeWithText("Payment recorded").assertExists()
        robot.tap("paymentRecorded.back")
        compose.awaitScreen("friend")
        robot.tap("friend.back")
        compose.awaitScreen("settleUp")
        robot.tap("settleUp.back")
        compose.awaitScreen("homeAllSettled")
    }
}
