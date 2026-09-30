package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace

/** The least space between a screen's content and its footer (Figma's `Spacer(minLength: 24)`). */
private val MinFooterGap = PbSpace.S24

/**
 * A white full-screen page laid out like the Figma frames: [content] from the top of the safe area
 * and [footer] at the bottom, at least 24 dp apart, with the 20 dp screen margins, centred at
 * [PbLayout.MaxContentWidth] on wide screens. The root is tagged `screen.<id>` for UI tests.
 *
 * When the screen is short, the content gives way first: a child weighted with `fill = false`, such
 * as an illustration, gets only the height left over. If the content still doesn't fit, it scrolls,
 * and the footer's actions stay on screen.
 *
 * @param id The flow.md screen id.
 * @param modifier Applied to the whole screen, e.g. for gestures anywhere on it.
 */
@Composable
fun PbScreen(
    id: String,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.$id"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier.safeDrawingPadding()
                    .widthIn(max = PbLayout.MaxContentWidth)
                    .fillMaxSize()
                    .padding(horizontal = PbLayout.ScreenMargin)
        ) {
            val scroll = rememberScrollState()
            // At least the height above the footer, which the content's weighted children share.
            // Scrolling is off while everything fits, so there is no overscroll stretch.
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scroll, enabled = scroll.maxValue > 0),
                content = content,
            )
            Spacer(Modifier.height(MinFooterGap))
            footer()
        }
    }
}
