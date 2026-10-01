package app.paybak.paybak.feature.settle

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.ReminderTone
import app.paybak.paybak.domain.model.ReminderVia
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import app.paybak.paybak.pressSystemBack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settle up (app-architecture §6.3, M5 UI tests) on the demo at Figma parity. */
@RunWith(AndroidJUnit4::class)
class SettleTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    /** Waits for [tag], scrolls it into view when it sits in a scrolling page, and taps it. */
    private fun tap(tag: String) {
        compose.awaitTag(tag)
        runCatching { tag(tag).performScrollTo() }
        tag(tag).performClick()
    }

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    private fun awaitText(text: String) =
        compose.waitUntil(5_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }

    private val ledger
        get() = paybakApp.ledger.ledger.value

    @Test
    fun theOwedBreakdownListsWhoOwesYouAndOpensTheirPage() {
        launchPaybak("settleOwedBreakdown").use {
            compose.awaitScreen("owedBreakdown")
            compose.onNodeWithText("+₹2,900").assertExists()
            compose.onNodeWithText("from 4 people").assertExists()
            compose.onNodeWithText("Overdue 3 days").assertExists()
            listOf("p-rohan", "p-priya", "p-esha", "p-dev").forEach {
                tag("owedBreakdown.row.$it").assertExists()
            }
            tap("owedBreakdown.row.p-rohan")
            compose.awaitScreen("friend")
        }
    }

    @Test
    fun theOweBreakdownExplainsGoaTripAndLeadsToSettleUp() {
        launchPaybak("settleOweBreakdown").use {
            compose.awaitScreen("oweBreakdown")
            compose.onNodeWithText("−₹1,850").assertExists()
            compose.onNodeWithText("across 2 groups").assertExists()
            compose
                .onNodeWithText("Goa Trip uses simplified debts, so you pay Kabir directly.")
                .assertExists()
            tap("oweBreakdown.settleUp")
            compose.awaitScreen("settleUp")
        }
    }

    @Test
    fun rohansReminderIsSentInPaybakAndLogged() {
        launchPaybak("settleRemind").use {
            compose.awaitTag("screen.remind")
            compose.onNodeWithText("Remind Rohan").assertExists()
            compose
                .onNodeWithText(
                    "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on " +
                        "20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
                )
                .assertExists()
            tap("remind.tone.neutral")
            awaitText(
                "Hi Rohan, this is a reminder that ₹800 for movie tickets (20 Sep) is still " +
                    "due. You can pay me on UPI at arjun@okaxis."
            )
            tap("remind.send")
            compose.awaitTag("toast")
            compose.onNodeWithText("Reminder sent to Rohan").assertExists()
            awaitGone("screen.remind")
            val sent = ledger.reminders.last()
            assertEquals("p-rohan", sent.toId)
            assertEquals(ReminderVia.Paybak, sent.via)
            assertEquals(ReminderTone.Neutral, sent.tone)
            val logged =
                paybakApp.ledger.snapshot.value.timeline
                    .flatMap { it.events }
                    .first { it.kind == TimelineKind.ReminderSent }
            assertEquals("Reminder sent to Rohan", logged.title)
            assertEquals("Movie tickets · ₹800 · Sent by you", logged.subtitle)
        }
    }

    @Test
    fun anEditedMessageSurvivesATone() {
        launchPaybak("settleRemind").use {
            compose.awaitTag("remind.message")
            tag("remind.message").performTextReplacement("Rohan, the tickets please!")
            tap("remind.tone.neutral")
            compose.onNodeWithText("Rohan, the tickets please!").assertExists()
            tap("remind.close")
            awaitGone("screen.remind")
            assertTrue(ledger.reminders.none { !it.automatic })
        }
    }

    @Test
    fun notReceivedRemovesTheClaimAndKeepsTheBalance() {
        launchPaybak("settleNotReceived").use {
            compose.awaitTag("screen.notReceived")
            compose.onNodeWithText("Let Esha know you haven’t received ₹700?").assertExists()
            tap("notReceived.send")
            awaitGone("screen.notReceived")
            compose.waitUntil(5_000) {
                ledger.payment("pay-esha-olive")?.status == PaymentStatus.NotReceived
            }
            val payment = ledger.payment("pay-esha-olive")!!
            assertEquals(
                "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you " +
                    "check your UPI app?",
                payment.notReceivedNote,
            )
            awaitGone("claim.pay-esha-olive")
            with(paybakApp.ledger.snapshot.value.home) {
                assertEquals(290_000L, totals.owed)
                assertTrue(pendingClaims.isEmpty())
            }
        }
    }

    @Test
    fun cancelKeepsTheClaimWaiting() {
        launchPaybak("settleNotReceived").use {
            compose.awaitTag("screen.notReceived")
            tap("notReceived.cancel")
            awaitGone("screen.notReceived")
            assertEquals(PaymentStatus.Pending, ledger.payment("pay-esha-olive")?.status)
        }
    }

    @Test
    fun theOwedCardLeadsThroughSettleUpToKabirsPrefilledForm() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            tap("home.balance.owed")
            compose.awaitScreen("owedBreakdown")
            tap("owedBreakdown.settleUp")
            compose.awaitScreen("settleUp")
            compose.onNodeWithText("2 payments to make").assertExists()
            compose.onNodeWithText("4 people owe you").assertExists()
            tap("settleUp.pay.p-kabir")
            compose.awaitScreen("recordPayment")
            compose.onNodeWithText("You owe Kabir ₹1,400 in Goa Trip").assertExists()
            compose.onNodeWithText("kabir@okaxis").assertExists()
        }
    }

    @Test
    fun aRecordedPaymentWaitsAsPendingInThePlan() {
        launchPaybak("settleUp").use { app ->
            tap("settleUp.pay.p-kabir")
            compose.awaitScreen("recordPayment")
            tap("recordPayment.save")
            compose.awaitScreen("payment")
            compose.onNodeWithText("Pending confirmation").assertExists()
            app.pressSystemBack()
            compose.awaitScreen("settleUp")
            compose.onNodeWithText("Pending").assertExists()
            awaitGone("settleUp.pay.p-kabir")
            assertEquals(185_000L, paybakApp.ledger.snapshot.value.home.totals.owe)
            compose.onNodeWithText("Pending").performClick()
            compose.awaitScreen("payment")
        }
    }

    @Test
    fun remindOpensTheSheetOverThePlan() {
        launchPaybak("settleUp").use {
            tap("settleUp.remind.p-rohan")
            compose.awaitTag("screen.remind")
            compose.onNodeWithText("Remind Rohan").assertExists()
            tag("screen.settleUp").assertExists()
            tap("remind.send")
            awaitGone("screen.remind")
            compose.onNodeWithText("Reminder sent to Rohan").assertExists()
        }
    }

    @Test
    fun aConfirmedClaimLeavesTheOwedBreakdown() {
        launchPaybak("homeConfirmPayment").use {
            compose.awaitScreen("homeConfirmPayment")
            tap("claim.pay-esha-olive.confirm")
            compose.awaitScreen("homeActive")
            awaitText("+₹2,200")
            tap("home.balance.owed")
            compose.awaitScreen("owedBreakdown")
            compose.onNodeWithText("from 3 people").assertExists()
            tag("owedBreakdown.row.p-esha").assertDoesNotExist()
        }
    }

    @Test
    fun shareHandsTheMessageToTheSystemShareSheet() {
        launchPaybak("settleRemindShare").use { app ->
            // The share sheet comes up over the app, which stops being the resumed activity.
            val deadline = System.currentTimeMillis() + 10_000
            while (app.state == Lifecycle.State.RESUMED && System.currentTimeMillis() < deadline) {
                Thread.sleep(100)
            }
            assertTrue(app.state != Lifecycle.State.RESUMED)
            // Backing out of it returns to the Remind sheet, still open, with nothing logged.
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand("input keyevent KEYCODE_BACK")
                .close()
            compose.awaitTag("screen.remind")
            assertTrue(ledger.reminders.none { !it.automatic })
        }
    }
}
