package app.paybak.paybak.feature.payments

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The payer's payment-approved scene: when a friend confirms a payment you made,
 * `paybak-payment.riv` takes over the screen, whatever is open, until it ends or you tap it.
 */
@RunWith(AndroidJUnit4::class)
class PaymentApprovedTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun sceneShows() =
        compose.onAllNodes(hasTestTag(SCENE)).fetchSemanticsNodes().isNotEmpty()

    /** Taps the scene away: it fades out on the test clock. */
    private fun closeScene() {
        compose.mainClock.autoAdvance = false
        tag(SCENE).performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.mainClock.autoAdvance = true
        compose.waitUntil(5_000) { !sceneShows() }
    }

    @Test
    fun friendConfirmingYourPaymentPlaysTheScene() {
        launchPaybak("homeActive", scenarios = listOf("paymentToMeeraPending")).use {
            compose.awaitScreen("homeActive")
            compose.runOnIdle { paybakApp.ledger.confirmPayment("pay-me-meera") }
            compose.awaitTag(SCENE)
            tag(SCENE).assertContentDescriptionEquals("Meera confirmed ₹450")
            closeScene()
            compose.awaitScreen("homeActive")
        }
        val payment = paybakApp.ledger.ledger.value.payment("pay-me-meera")!!
        assertEquals(PaymentStatus.Confirmed, payment.status)
    }

    @Test
    fun autoApprovedPaymentCoversAnOpenAlert() {
        launchPaybak("homeActive", autoApprove = true).use {
            compose.awaitScreen("homeActive")
            tag("home.due.g-goa.settle").performScrollTo().performClick()
            compose.awaitScreen("recordPayment")
            tag("recordPayment.save").performClick()
            compose.awaitScreen("payment")
            tag("paymentRecorded.cancel").performScrollTo().performClick()
            compose.awaitTag("paymentRecorded.alert")

            // Kabir confirms 5 s after the save, over the alert.
            compose.awaitTag(SCENE, timeoutMillis = 10_000)
            tag(SCENE).assertContentDescriptionEquals("Kabir confirmed ₹1,400")
            closeScene()
            tag("paymentRecorded.alert").assertExists()
        }
        val payment = paybakApp.ledger.ledger.value.payments.last()
        assertEquals("p-kabir", payment.toId)
        assertEquals(PaymentStatus.Confirmed, payment.status)
    }

    private companion object {
        const val SCENE = "paymentApproved"
    }
}
