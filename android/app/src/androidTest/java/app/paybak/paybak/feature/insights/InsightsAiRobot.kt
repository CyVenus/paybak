package app.paybak.paybak.feature.insights

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

/** Waits and taps for the Insights & AI UI tests. */
class InsightsAiRobot(private val compose: ComposeTestRule) {
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

    fun tap(tag: String) {
        await(tag)
        tag(tag).performClick()
    }

    /** Scrolls [tag] into view first: rows below the fold. */
    fun scrollTap(tag: String) {
        await(tag)
        tag(tag).performScrollTo().performClick()
    }

    /** [tag] holds [text] (the text may sit in a child, as in a chat bubble or a badge). */
    fun assertText(tag: String, text: String) {
        compose
            .onNode(
                hasText(text) and (hasTestTag(tag) or hasAnyAncestor(hasTestTag(tag))),
                useUnmergedTree = true,
            )
            .assertExists()
    }
}
