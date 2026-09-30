package app.paybak.paybak.feature.signin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifyTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val code
        get() = compose.onNodeWithTag("verify.code")

    @Test
    fun showsWhereTheCodeWentAndLocksResend() {
        launchPaybak("verify").use {
            compose.onNodeWithText("Sent to arjun@example.com · Change").assertIsDisplayed()
            compose.onNodeWithTag("verify.countdown").assertTextEquals("Resend code in 0:30")
            compose.onNodeWithTag("verify.resend").assertDoesNotExist()
        }
    }

    @Test
    fun theRightCodeOpensSetup() {
        launchPaybak("verify").use {
            code.performTextInput("000000")
            compose.awaitScreen("setup1")
        }
    }

    @Test
    fun aWrongCodeShowsTheErrorUntilADigitIsEdited() {
        launchPaybak("verify").use {
            code.performTextInput("123456")
            compose.awaitScreen("verifyWrong")
            compose.onNodeWithTag("verify.error").assertIsDisplayed()
            code.performTextInput("0")
            compose.awaitScreen("verify")
            compose.onNodeWithTag("verify.error").assertDoesNotExist()
            code.performTextInput("00000")
            compose.awaitScreen("setup1")
        }
    }

    @Test
    fun resendAfterAWrongCodeStartsOver() {
        launchPaybak("verifyWrong").use {
            compose.onNodeWithTag("verify.error").assertIsDisplayed()
            compose.onNodeWithTag("verify.resend").performClick()
            compose.awaitScreen("verify")
            compose.onNodeWithTag("verify.countdown").assertTextEquals("Resend code in 0:30")
            code.assertTextEquals("")
        }
    }

    @Test
    fun changeReturnsToSignIn() {
        launchPaybak("verify").use {
            compose.onNodeWithTag("verify.change").performClick()
            compose.awaitScreen("signIn")
            compose.onNodeWithTag("signIn.field").assertTextEquals("arjun@example.com")
        }
    }
}
