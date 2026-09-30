package app.paybak.paybak.feature.addrecord

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick

/** Small waits and taps shared by the Add & Record UI tests. */
class AddRecordRobot(private val compose: ComposeTestRule) {
    fun tag(tag: String): SemanticsNodeInteraction =
        compose.onNodeWithTag(tag, useUnmergedTree = true)

    fun await(tag: String, timeoutMillis: Long = 10_000) =
        compose.waitUntil(timeoutMillis) {
            compose
                .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

    fun awaitGone(tag: String, timeoutMillis: Long = 10_000) =
        compose.waitUntil(timeoutMillis) {
            compose
                .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        }

    /** Waits for [tag], then taps it. */
    fun tap(tag: String) {
        await(tag)
        tag(tag).performClick()
    }

    /** Opens the ＋ sheet and one of its rows. */
    fun openFromAddSheet(row: String) {
        tap("home.tab.add")
        tap("home.addSheet.$row")
    }
}
