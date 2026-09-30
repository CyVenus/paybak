package app.paybak.paybak.feature.launch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.rive.RiveHost
import app.paybak.paybak.ui.theme.PaybakTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Welcome and Get Started on screens shorter than Figma's 874 dp (screens-launch.md §5): the
 * illustration gives way first, and if the content still doesn't fit it scrolls, while the actions
 * stay on screen. Each size is the whole screen: the window's system-bar insets still apply inside
 * it. The illustrations are real Rive views, which must never be laid out empty.
 */
@RunWith(AndroidJUnit4::class)
class ShortScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun welcomeFitsAShortPhone() {
        showOn(ShortPhone) {
            WelcomeScreen(step = 1, onStepChange = {}, onSkip = {}, onGetStarted = {})
        }
        assertFullyVisible("welcome.headline")
        assertFullyVisible("welcome.continue")
    }

    @Test
    fun welcomeKeepsItsHeadlineAndCtaOnScreenInSplitScreen() {
        showOn(SplitScreen) {
            WelcomeScreen(step = 1, onStepChange = {}, onSkip = {}, onGetStarted = {})
        }
        assertFullyVisible("welcome.headline")
        assertFullyVisible("welcome.continue")
    }

    @Test
    fun getStartedFitsAShortPhone() {
        showOn(ShortPhone) { GetStartedScreen({}, {}, {}) }
        assertFullyVisible("getStarted.headline")
        assertFullyVisible("getStarted.email")
    }

    @Test
    fun getStartedScrollsItsContentAboveTheActionsInSplitScreen() {
        showOn(SplitScreen) { GetStartedScreen({}, {}, {}) }
        assertFullyVisible("getStarted.email")
        compose.onNodeWithTag("getStarted.headline").performScrollTo()
        assertFullyVisible("getStarted.headline")
    }

    private fun showOn(size: DpSize, screen: @Composable () -> Unit) {
        compose.setContent {
            PaybakTheme { RiveHost { Box(Modifier.size(size).clipToBounds()) { screen() } } }
        }
    }

    /** Nothing of the node is cut off by the screen's edge, a scroll viewport or the footer. */
    private fun assertFullyVisible(tag: String) {
        val node = compose.onNodeWithTag(tag)
        assertEquals(node.getUnclippedBoundsInRoot(), node.getBoundsInRoot())
    }

    private companion object {
        /** A small phone. */
        val ShortPhone = DpSize(360.dp, 640.dp)

        /** Half of a phone in split-screen. */
        val SplitScreen = DpSize(360.dp, 400.dp)
    }
}
