package app.paybak.paybak

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.data.SignInMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The whole first run, from a fresh install to Home. */
@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    /** The email path, with a wrong code first: the error shows and typing starts over. */
    @Test
    fun emailSignInThroughSetupToHome() {
        launchPaybak().use {
            compose.awaitScreen("welcome1")
            tag("welcome.skip").performClick()
            compose.awaitScreen("getStarted")
            tag("getStarted.email").performClick()
            compose.awaitScreen("signIn")
            tag("signIn.field").performTextInput("priya@example.com")
            tag("signIn.sendCode").performClick()
            compose.awaitScreen("verify")
            tag("verify.code").performTextInput("123456")
            compose.awaitScreen("verifyWrong")
            tag("verify.error").assertIsDisplayed()
            tag("verify.code").performTextInput("000000")
            compose.awaitScreen("setup1")
            tag("setup1.name").performTextInput("Priya Shah")
            tag("setup1.avatar.1").performClick()
            tag("setup1.continue").performClick()
            compose.awaitScreen("setup2")
            tag("setup2.continue").performClick()
            compose.awaitScreen("setup3")
            tag("setup3.upi").performTextInput("priya@okicici")
            tag("setup3.continue").performClick()
            compose.awaitScreen("setup4")
            tag("setup4.notNow").performClick()
            compose.awaitScreen("allSet")
            compose.onNodeWithText("You’re all set, Priya.").assertIsDisplayed()
            tag("allSet.goHome").performClick()
            compose.awaitScreen("homeFirstDay")
        }
        with(savedProfile) {
            assertEquals("Priya Shah", name)
            assertEquals(AvatarChoice.Preset(1), avatar)
            assertEquals(Currencies.suggested().currency.code, currencyCode)
            assertEquals("priya@okicici", upiId)
            assertEquals(SignInMethod.Email, signInMethod)
            assertEquals("priya@example.com", contact)
            assertTrue(onboardingComplete)
        }
    }

    @Test
    fun appleSkipsTheCodeAndGoesStraightToSetup() {
        launchPaybak("getStarted").use {
            tag("getStarted.apple").performClick()
            compose.awaitScreen("setup1")
            tag("setup1.continue").assertIsNotEnabled()
            tag("setup.back").performClick()
            compose.awaitScreen("getStarted")
            tag("getStarted.apple").performClick()
            compose.awaitScreen("setup1")
            tag("setup1.name").performTextInput("Dev")
            tag("setup1.continue").performClick()
            compose.awaitScreen("setup2")
            tag("setup2.continue").performClick()
            compose.awaitScreen("setup3")
            tag("setup.skip").performClick()
            compose.awaitScreen("setup4")
            tag("setup.skip").performClick()
            compose.awaitScreen("allSet")
            tag("allSet.goHome").performClick()
            compose.awaitScreen("homeFirstDay")
        }
        with(savedProfile) {
            assertEquals(SignInMethod.Apple, signInMethod)
            assertEquals("", contact)
            assertEquals("Dev", name)
            assertEquals(AvatarChoice.None, avatar)
            assertEquals("", upiId)
            assertTrue(onboardingComplete)
        }
    }
}
