package app.paybak.paybak.feature.launch

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.pressSystemBack
import app.paybak.paybak.tapTwiceQuickly
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetStartedTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val profile
        get() = ApplicationProvider.getApplicationContext<PaybakApplication>().profileStore.profile

    @Test
    fun showsTheHeadline() {
        launchPaybak("getStarted").use {
            compose
                .onNodeWithTag("getStarted.headline")
                .assertTextEquals("Shared money,\nkept clear.")
        }
    }

    @Test
    fun appleGoesStraightToSetupAndIsRecorded() {
        launchPaybak("getStarted").use {
            compose.onNodeWithTag("getStarted.apple").performClick()
            compose.awaitScreen("setup1")
            assertEquals(SignInMethod.Apple, profile.value.signInMethod)
        }
    }

    @Test
    fun googleGoesStraightToSetupAndIsRecorded() {
        launchPaybak("getStarted").use {
            compose.onNodeWithTag("getStarted.google").performClick()
            compose.awaitScreen("setup1")
            assertEquals(SignInMethod.Google, profile.value.signInMethod)
        }
    }

    @Test
    fun aQuickSecondTapOnAppleOpensSetupOnce() {
        launchPaybak("getStarted").use { scenario ->
            compose.onNodeWithTag("getStarted.apple").tapTwiceQuickly()
            compose.awaitScreen("setup1")
            scenario.pressSystemBack()
            compose.awaitScreen("getStarted")
        }
    }

    @Test
    fun emailOrPhoneOpensSignIn() {
        launchPaybak("getStarted").use {
            compose.onNodeWithTag("getStarted.email").performClick()
            compose.awaitScreen("signIn")
        }
    }
}
