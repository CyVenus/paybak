package app.paybak.paybak.feature.home

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import app.paybak.paybak.pressSystemBack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The Confirmed animation, then the hold before the payment is confirmed (home-v2 §3.9). */
private const val CONFIRMED_MILLIS = 300L
private const val HOLD_MILLIS = 1_000L

/** Home (app-architecture §6.5): its states and where every element leads. */
@RunWith(AndroidJUnit4::class)
class HomeTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun awaitTag(tag: String, timeoutMillis: Long = 15_000) =
        compose.waitUntil(timeoutMillis) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }

    @Test
    fun firstDayOpensAddExpenseAndAddFriend() {
        launchPaybak("homeFirstDay").use { app ->
            compose.awaitScreen("homeFirstDay")
            tag("home.state.firstDay").assertExists()
            tag("home.greeting").assertExists()
            compose.onNodeWithText("Nothing here yet.").assertExists()
            tag("home.firstDay.addExpense").performClick()
            compose.awaitScreen("addExpense")
            app.pressSystemBack()
            compose.awaitScreen("homeFirstDay")
            tag("home.firstDay.invite").performClick()
            compose.awaitScreen("addFriend")
        }
    }

    @Test
    fun activeShowsTheLedgerNumbers() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            compose.onNodeWithText("Good evening, Arjun").assertExists()
            compose.onNodeWithText("+₹2,900").assertExists()
            compose.onNodeWithText("from 4 people").assertExists()
            compose.onNodeWithText("−₹1,850").assertExists()
            compose.onNodeWithText("across 2 groups").assertExists()
            compose.onNodeWithText("Overdue 3 days").assertExists()
            compose.onNodeWithText("Due Fri").assertExists()
            tag("home.activity.row.e-olive").assertExists()
        }
    }

    @Test
    fun confirmingAClaimSettlesItOnHome() {
        launchPaybak("homeConfirmPayment").use {
            compose.awaitScreen("homeConfirmPayment")
            compose.onNodeWithText("Esha says she paid you ₹700").assertExists()
            // The Confirmed card and the toast are timed: step the clock through them.
            compose.mainClock.autoAdvance = false
            tag("claim.pay-esha-olive.confirm").performClick()
            compose.mainClock.advanceTimeBy(CONFIRMED_MILLIS)
            compose.onNodeWithText("Esha paid you ₹700").assertExists()
            compose.mainClock.advanceTimeBy(HOLD_MILLIS)
            compose.onNodeWithText("Payment confirmed").assertExists()
            compose.mainClock.autoAdvance = true
            compose.awaitScreen("homeActive")
            compose.onNodeWithText("+₹2,200").assertExists()
            compose.onNodeWithText("from 3 people").assertExists()
            tag("home.activity.row.pay-esha-olive").performScrollTo().assertExists()
            compose.onNodeWithText("Esha paid you").assertExists()
        }
        val payment = paybakApp.ledger.ledger.value.payments.single { it.id == "pay-esha-olive" }
        assertEquals(PaymentStatus.Confirmed, payment.status)
    }

    @Test
    fun notReceivedOpensItsSheet() {
        launchPaybak("homeConfirmPayment").use {
            compose.awaitScreen("homeConfirmPayment")
            tag("claim.pay-esha-olive.notReceived").performClick()
            awaitTag("screen.notReceived")
        }
    }

    @Test
    fun theHeaderOpensAskPaybakAndNotifications() {
        launchPaybak("homeActive").use { app ->
            compose.awaitScreen("homeActive")
            tag("home.assistant").performClick()
            compose.awaitScreen("paywall")
            app.pressSystemBack()
            compose.awaitScreen("homeActive")
            tag("home.bell").performClick()
            compose.awaitScreen("notifications")
        }
    }

    @Test
    fun theBalancesOpenTheBreakdownsAndSettleUp() {
        launchPaybak("homeActive").use { app ->
            compose.awaitScreen("homeActive")
            tag("home.balance.owed").performClick()
            compose.awaitScreen("owedBreakdown")
            app.pressSystemBack()
            compose.awaitScreen("homeActive")
            tag("home.balance.owe").performClick()
            compose.awaitScreen("oweBreakdown")
            app.pressSystemBack()
            compose.awaitScreen("homeActive")
            tag("home.balance.settleUp").performClick()
            compose.awaitScreen("settleUp")
        }
    }

    @Test
    fun rohansRowRemindsHimOrOpensHisPage() {
        launchPaybak("homeActive").use { app ->
            compose.awaitScreen("homeActive")
            tag("home.due.p-rohan.remind").performClick()
            awaitTag("screen.remind")
            app.pressSystemBack()
            compose.awaitScreen("homeActive")
            tag("home.due.p-rohan").performClick()
            compose.awaitScreen("friend")
        }
    }

    @Test
    fun goaTripSettlesWithKabirInRecordPayment() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            tag("home.due.g-goa.settle").performScrollTo().performClick()
            compose.awaitScreen("recordPayment")
            tag("recordPayment.save").performClick()
            compose.awaitScreen("payment")
        }
        val payment = paybakApp.ledger.ledger.value.payments.last()
        assertEquals(ME, payment.fromId)
        assertEquals("p-kabir", payment.toId)
        assertEquals(140_000L, payment.amount)
        assertEquals("g-goa", payment.groupId)
    }

    @Test
    fun recentActivityOpensTheExpenseAndSeeAllTheTimeline() {
        launchPaybak("homeActive").use { app ->
            compose.awaitScreen("homeActive")
            tag("home.activity.row.e-olive").performScrollTo().performClick()
            compose.awaitScreen("expense")
            app.pressSystemBack()
            compose.awaitScreen("homeActive")
            tag("home.seeAll").performScrollTo().performClick()
            awaitTag("screen.activity")
        }
    }

    @Test
    fun theLogoOpensTheDebugMenu() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            tag("home.logo").performTouchInput { longClick() }
            awaitTag("screen.debugMenu")
            compose.onNodeWithText("Start an empty account").performClick()
            compose.awaitScreen("homeFirstDay")
        }
        assertTrue(paybakApp.ledger.ledger.value.expenses.isEmpty())
    }
}
