package app.paybak.paybak.feature.settings

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import app.paybak.paybak.savedProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settings and Pro (screens-settings, app-architecture §6.4 M8 UI tests). */
@RunWith(AndroidJUnit4::class)
class SettingsTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    @Test
    fun exportOpensTheStorePaywallForAFreeMember() {
        launchPaybak("privacyData", pro = false).use {
            compose.awaitScreen("privacyData")
            tag("privacyData.export").performClick()
            // RevenueCat's paywall (Test Store in debug builds); purchases go through its dialog.
            compose.awaitScreen("paywall")
            assertFalse(paybakApp.ledger.snapshot.value.isPro)
        }
    }

    @Test
    fun aProMemberSeesTheWelcomeAndDoneContinuesToExport() {
        launchPaybak("proWelcome").use {
            compose.awaitTag("screen.proWelcome")
            tag("proWelcome.body").assertExists()
            tag("proWelcome.done").performClick()
            compose.awaitScreen("privacyExport")
            assertTrue(paybakApp.ledger.snapshot.value.isPro)
        }
    }

    @Test
    fun aUpiIdWithoutAnAtShowsTheErrorAndAValidOneIsAdded() {
        launchPaybak("paymentDetails").use {
            compose.awaitScreen("paymentDetails")
            tag("paymentDetails.add").performClick()
            compose.awaitTag("paymentAddUpi.field")
            tag("paymentAddUpi.field").assertTextContains("arjun@okhdfcbank")
            tag("paymentAddUpi.field").performTextReplacement("arjunokhdfcbank")
            tag("paymentAddUpi.save").performClick()
            tag("paymentAddUpi.error").assertExists()
            tag("paymentAddUpi.field").performTextReplacement("arjun@okhdfcbank")
            awaitGone("paymentAddUpi.error")
            tag("paymentAddUpi.save").performClick()
            awaitGone("paymentAddUpi.field")
            tag("paymentDetails.method.2").assertTextContains("arjun@okhdfcbank")
            assertEquals(3, savedProfile.paymentMethods.size)
        }
    }

    @Test
    fun aPaymentMethodCanBeMadePrimaryAndRemoved() {
        launchPaybak("paymentDetails").use {
            compose.awaitScreen("paymentDetails")
            tag("paymentDetails.method.1").performClick()
            compose.awaitTag("paymentDetails.actions.primary")
            tag("paymentDetails.actions.primary").performClick()
            compose.waitUntil { savedProfile.primaryMethod?.id == "pm-hdfc" }
            compose
                .onNode(
                    hasTestTag("paymentDetails.method.1") and hasText("Bank transfer · Primary")
                )
                .assertExists()
            tag("paymentDetails.method.0").performClick()
            compose.awaitTag("paymentDetails.actions.remove")
            tag("paymentDetails.actions.remove").performClick()
            compose.awaitTag("paymentDetails.removeAlert.action")
            tag("paymentDetails.removeAlert.action").performClick()
            compose.waitUntil { savedProfile.paymentMethods.size == 1 }
            assertEquals("", savedProfile.upiId)
        }
    }

    @Test
    fun deletingTheAccountIsBlockedWhileBalancesAreOpen() {
        launchPaybak("privacyData").use {
            compose.awaitScreen("privacyData")
            tag("privacyData.deleteAccount").performClick()
            compose.awaitTag("privacyDeleteBlocked")
            compose
                .onNodeWithText(
                    "You still owe ₹1,850 and are owed ₹2,900. Settle every balance before " +
                        "deleting your account."
                )
                .assertExists()
            tag("privacyDeleteBlocked.action").performClick()
            compose.awaitScreen("settleUp")
        }
    }

    @Test
    fun notificationSettingsSurviveARelaunch() {
        launchPaybak("settingsNotifications").use {
            compose.awaitScreen("settingsNotifications")
            tag("settingsNotifications.push.reminders").assertIsOn().performClick()
            tag("settingsNotifications.schedule.onDueDate").performClick()
            tag("settingsNotifications.push.reminders").assertIsOff()
        }
        launchPaybak(resetOnboarding = false).use {
            compose.awaitTag("home.tab.profile", timeoutMillis = 20_000)
            tag("home.tab.profile").performClick()
            compose.awaitScreen("profile")
            tag("profile.row.notifications").performClick()
            compose.awaitScreen("settingsNotifications")
            tag("settingsNotifications.push.reminders").assertIsOff()
            tag("settingsNotifications.push.monthlySummary").assertIsOn()
            val schedule = paybakApp.ledger.snapshot.value.ledger.settings.reminderSchedule
            assertEquals(false, schedule.onDueDate)
            assertEquals(true, schedule.twoDaysBefore)
        }
    }

    @Test
    fun theCurrencyScreenShowsTheDefaultAndKeepsTheSwitch() {
        launchPaybak("settingsCurrency").use {
            compose.awaitScreen("settingsCurrency")
            tag("settingsCurrency.default").assertTextContains("INR")
            tag("settingsCurrency.perCurrency").assertIsOff().performClick().assertIsOn()
            assertTrue(paybakApp.ledger.snapshot.value.ledger.settings.keepBalancesPerCurrency)
        }
    }
}
