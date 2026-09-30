package app.paybak.paybak.feature.launch

import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun continueWalksTheStepsAndGetStartedOpensGetStarted() {
        launchPaybak("welcome1").use {
            assertStep(1)
            continueButton().assertTextEquals("Continue").performClick()
            assertStep(2)
            continueButton().performClick()
            assertStep(3)
            continueButton().assertTextEquals("Get started").performClick()
            compose.awaitScreen("getStarted")
        }
    }

    @Test
    fun skipOpensGetStarted() {
        launchPaybak("welcome1").use {
            compose.onNodeWithTag("welcome.skip").performClick()
            compose.awaitScreen("getStarted")
        }
    }

    @Test
    fun skipIsHiddenOnTheLastStep() {
        launchPaybak("welcome3").use {
            assertStep(3)
            compose.onNodeWithTag("welcome.skip").assertDoesNotExist()
        }
    }

    @Test
    fun backFromGetStartedReturnsToTheStepLeftFrom() {
        launchPaybak("welcome1").use {
            continueButton().performClick()
            compose.onNodeWithTag("welcome.skip").performClick()
            compose.awaitScreen("getStarted")
            Espresso.pressBack()
            assertStep(2)
        }
        launchPaybak("welcome3").use {
            continueButton().performClick()
            compose.awaitScreen("getStarted")
            Espresso.pressBack()
            assertStep(3)
        }
    }

    @Test
    fun systemBackGoesToThePreviousStep() {
        launchPaybak("welcome2").use {
            assertStep(2)
            Espresso.pressBack()
            assertStep(1)
        }
    }

    @Test
    fun swipesChangeTheStepBothWays() {
        launchPaybak("welcome1").use {
            swipeOn(step = 1) { swipeLeft() }
            assertStep(2)
            swipeOn(step = 2) { swipeRight() }
            assertStep(1)
        }
    }

    @Test
    fun swipingForwardOnTheLastStepDoesNothing() {
        launchPaybak("welcome3").use {
            swipeOn(step = 3) { swipeLeft() }
            assertStep(3)
        }
    }

    private fun continueButton() = compose.onNodeWithTag("welcome.continue")

    /** Swipes across the whole Welcome screen, currently on [step]. */
    private fun swipeOn(step: Int, gesture: TouchInjectionScope.() -> Unit) {
        compose.onNodeWithTag("screen.welcome$step").performTouchInput(gesture)
    }

    /** The screen shows [step] with its headline. */
    private fun assertStep(step: Int) {
        compose.awaitScreen("welcome$step")
        compose.onNodeWithTag("welcome.headline").assertTextEquals(headlines[step - 1])
    }

    private companion object {
        val headlines =
            listOf(
                "Split any bill in seconds.",
                "Know who owes what, and by when.",
                "Settle up without the awkward chat.",
            )
    }
}
