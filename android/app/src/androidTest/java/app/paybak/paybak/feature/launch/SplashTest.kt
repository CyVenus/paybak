package app.paybak.paybak.feature.launch

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun freshLaunchShowsSplashThenWelcomeStep1() {
        launchPaybak().use {
            compose.onNodeWithTag("screen.splash").assertExists()
            compose.awaitScreen("welcome1")
            compose.onNodeWithTag("screen.splash").assertDoesNotExist()
        }
    }
}
