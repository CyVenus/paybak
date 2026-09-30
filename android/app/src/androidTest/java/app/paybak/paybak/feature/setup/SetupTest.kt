package app.paybak.paybak.feature.setup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.savedProfile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    @Test
    fun continueNeedsANameAndSavesTheAvatar() {
        launchPaybak("setup1").use {
            tag("setup1.avatar.0").assertIsSelected()
            tag("setup1.name").performTextClearance()
            tag("setup1.continue").assertIsNotEnabled()
            tag("setup1.name").performTextInput("Priya Shah")
            tag("setup1.avatar.1").performClick()
            tag("setup1.continue").assertIsEnabled().performClick()
            compose.awaitScreen("setup2")
            assertEquals("Priya Shah", savedProfile.name)
            assertEquals(AvatarChoice.Preset(1), savedProfile.avatar)
        }
    }

    @Test
    fun searchFindsAnyCurrencyAndKeepsItListed() {
        launchPaybak("setup2").use {
            tag("setup2.row.INR").assertIsSelected()
            tag("setup2.search").performTextInput("yen")
            compose.awaitTag("setup2.row.JPY")
            tag("setup2.row.JPY").performClick()
            tag("setup2.search").performTextClearance()
            tag("setup2.row.JPY").assertIsSelected()
            tag("setup2.continue").performClick()
            compose.awaitScreen("setup3")
            assertEquals("JPY", savedProfile.currencyCode)
        }
    }

    @Test
    fun aSearchWithNoMatchSaysSo() {
        launchPaybak("setup2").use {
            tag("setup2.search").performTextInput("zzz")
            compose.onNodeWithText("No currencies match “zzz”").assertIsDisplayed()
        }
    }

    @Test
    fun anInvalidUpiIdIsNotSaved() {
        launchPaybak("setup3").use {
            tag("setup3.upi").performTextClearance()
            tag("setup3.upi").performTextInput("arjun")
            tag("setup3.continue").performClick()
            compose.onNodeWithText("Enter a UPI ID like name@bank.").assertIsDisplayed()
            compose.awaitScreen("setup3")
            tag("setup3.upi").performTextInput("@ybl")
            tag("setup3.continue").performClick()
            compose.awaitScreen("setup4")
            assertEquals("arjun@ybl", savedProfile.upiId)
        }
    }

    @Test
    fun copyConfirmsWithAToast() {
        launchPaybak("setup3").use {
            tag("setup3.copy").performClick()
            compose.onNodeWithText("UPI ID copied").assertIsDisplayed()
        }
    }

    @Test
    fun skipKeepsTheSavedUpiId() {
        launchPaybak("setup3").use {
            tag("setup3.upi").performTextClearance()
            tag("setup3.upi").performTextInput("new@ybl")
            tag("setup.skip").performClick()
            compose.awaitScreen("setup4")
            assertEquals("arjun@okaxis", savedProfile.upiId)
        }
    }

    @Test
    fun backStepsThroughSetupToVerify() {
        launchPaybak("setup3").use {
            tag("setup.back").performClick()
            compose.awaitScreen("setup2")
            tag("setup.back").performClick()
            compose.awaitScreen("setup1")
            tag("setup.back").performClick()
            compose.awaitScreen("verify")
        }
    }

    @Test
    fun notNowFinishesSetup() {
        launchPaybak("setup4").use {
            tag("setup4.notNow").performClick()
            compose.awaitScreen("allSet")
            compose.onNodeWithText("You’re all set, Arjun.").assertIsDisplayed()
        }
    }
}
