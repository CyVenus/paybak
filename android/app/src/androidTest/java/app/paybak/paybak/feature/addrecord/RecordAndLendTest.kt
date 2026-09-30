package app.paybak.paybak.feature.addrecord

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import app.paybak.paybak.pressSystemBack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Record payment, Lend money and New group (screens-record-lend-group, app-architecture §6.2). */
@RunWith(AndroidJUnit4::class)
class RecordAndLendTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = AddRecordRobot(compose)

    @Test
    fun aPaymentToMeeraWaitsForHerAndCanBeCancelled() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            robot.openFromAddSheet("payment")
            compose.awaitScreen("recordPayment")
            compose.onNodeWithText("You owe Meera ₹450 in Flat 302").assertExists()
            compose
                .onNodeWithText(
                    "You paid Meera ₹450 in cash for Flat 302.\n" +
                        "Meera will be asked to confirm. Paybak never moves money."
                )
                .assertExists()
            robot.tap("recordPayment.save")

            compose.awaitScreen("payment")
            compose.onNodeWithText("Payment recorded").assertExists()
            compose.onNodeWithText("Pending confirmation").assertExists()
            compose.onNodeWithText("Waiting for Meera to confirm").assertExists()
            val recorded = paybakApp.ledger.ledger.value.payments.last()
            assertEquals(PaymentStatus.Pending, recorded.status)
            assertEquals(45_000L, recorded.amount)
            assertEquals("g-flat302", recorded.groupId)

            robot.tap("paymentRecorded.cancel")
            robot.await("paymentRecorded.alert")
            compose
                .onNodeWithText("Meera won’t be asked to confirm. You’ll still owe her ₹450.")
                .assertExists()
            robot.tap("paymentRecorded.alert.action")
            robot.awaitGone("screen.payment")
            assertEquals(
                PaymentStatus.Cancelled,
                paybakApp.ledger.ledger.value.payment(recorded.id)!!.status,
            )
        }
    }

    @Test
    fun lendingInThreeInstallmentsShowsTheSchedule() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            robot.openFromAddSheet("lend")
            compose.awaitScreen("lendMoney")
            robot.tag("lendMoney.amount").performTextInput("6000")
            robot.tap("lendMoney.person")
            compose.awaitScreen("pickPeople")
            robot.tap("pickPerson.friend.p-dev")
            compose.awaitScreen("lendMoney")
            compose
                .onNodeWithText("3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec")
                .assertExists()
            robot.tap("lendMoney.save")

            compose.awaitScreen("loan")
            compose.onNodeWithText("Loan added").assertExists()
            compose.onNodeWithText("You lent Dev").assertExists()
            compose.onNodeWithText("3 monthly installments").assertExists()
            listOf("Due Fri 30 Oct", "Due Mon 30 Nov", "Due Wed 30 Dec").forEach {
                compose.onNodeWithText(it).assertExists()
            }
            val loan = paybakApp.ledger.ledger.value.loans.last()
            assertEquals("p-dev", loan.borrowerId)
            assertEquals(3, loan.installments?.count)
        }
    }

    @Test
    fun creatingAGroupOpensItOnTheGroupsTab() {
        launchPaybak("homeActive").use { scenario ->
            compose.awaitScreen("homeActive")
            robot.openFromAddSheet("group")
            compose.awaitScreen("newGroup")
            robot.tag("newGroup.name").performTextInput("Weekend Trek")
            robot.tap("newGroup.type.trip")
            robot.tap("newGroup.addPeople")
            compose.awaitScreen("pickPeople")
            listOf("p-esha", "p-dev", "p-kabir").forEach { robot.tap("splitWith.friend.$it") }
            robot.tap("splitWith.done")
            compose.awaitScreen("newGroup")
            compose.onNodeWithText("Kabir Singh").assertExists()
            robot.tap("newGroup.create")

            compose.awaitScreen("group")
            compose.onNodeWithText("Group created").assertExists()
            val group = paybakApp.ledger.ledger.value.groups.last()
            assertEquals("Weekend Trek", group.name)
            assertEquals(listOf("me", "p-esha", "p-dev", "p-kabir"), group.memberIds)
            assertTrue(group.simplifyDebts)
            scenario.pressSystemBack()
            compose.awaitScreen("groups")
        }
    }
}
