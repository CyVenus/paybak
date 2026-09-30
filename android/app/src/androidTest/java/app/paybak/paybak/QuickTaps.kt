package app.paybak.paybak

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput

/** About as fast as a person taps twice. */
private const val SECOND_TAP_DELAY_MILLIS = 150L

/**
 * Taps the node twice in quick succession, like an impatient double tap. The test clock runs
 * between the taps, so the second one lands while the transition started by the first is under way
 * and the outgoing screen is still on screen.
 */
fun SemanticsNodeInteraction.tapTwiceQuickly() {
    performTouchInput {
        click()
        advanceEventTime(SECOND_TAP_DELAY_MILLIS)
        click()
    }
}
