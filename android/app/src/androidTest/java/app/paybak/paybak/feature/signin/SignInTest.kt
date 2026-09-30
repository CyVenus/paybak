package app.paybak.paybak.feature.signin

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.pressSystemBack
import app.paybak.paybak.savedProfile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val field
        get() = compose.onNodeWithTag("signIn.field")

    private val sendCode
        get() = compose.onNodeWithTag("signIn.sendCode")

    @Test
    fun sendCodeNeedsAPlausibleEmailOrPhoneNumber() {
        launchPaybak("signIn").use {
            field.performTextClearance()
            sendCode.assertIsNotEnabled()
            field.performTextInput("arjun@example")
            sendCode.assertIsNotEnabled()
            field.performTextInput(".com")
            sendCode.assertIsEnabled()
            field.performTextClearance()
            field.performTextInput("98765")
            sendCode.assertIsNotEnabled()
            field.performTextInput(" 43210")
            sendCode.assertIsEnabled()
        }
    }

    @Test
    fun aPhoneNumberGetsTheCountryCodeAndTheCodeGoesToIt() {
        launchPaybak("signIn").use {
            field.performTextClearance()
            field.performTextInput("98765 43210")
            sendCode.performClick()
            compose.awaitScreen("verify")
            compose.onNodeWithText("Sent to +91 98765 43210 · Change").assertExists()
            assertEquals(SignInMethod.Phone, savedProfile.signInMethod)
            assertEquals("+91 98765 43210", savedProfile.contact)
        }
    }

    @Test
    fun backReturnsToGetStarted() {
        launchPaybak("signIn").use { scenario ->
            scenario.pressSystemBack()
            compose.awaitScreen("getStarted")
        }
    }

    @Test
    fun changeOnVerifyReturnsWithTheContactInTheField() {
        launchPaybak("signIn").use {
            field.performTextClearance()
            field.performTextInput("priya@example.com")
            sendCode.performClick()
            compose.awaitScreen("verify")
            compose.onNodeWithTag("verify.change").performClick()
            compose.awaitScreen("signIn")
            field.assertTextEquals("priya@example.com")
        }
    }
}
