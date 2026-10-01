package app.paybak.paybak.ui.components.addrecord

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * A pushed or modal screen with a fixed 44 dp [header] (Push or Modal Header) over a solid white
 * band, and [content] scrolling under it: Figma's "Scroll edge (top)" band and "Header space". The
 * content starts [headerGap] below the header, with the 20 dp margins and [gap] between children;
 * [footer] (pinned buttons, the split total) stays at the bottom and rides the keyboard, and so
 * does [bottomBar], edge to edge (the pinned comment composer).
 *
 * @param testTag The root's tag: `screen.<routeId>` for a route, a page tag for a local page.
 */
@Composable
fun PbPinnedHeaderScreen(
    testTag: String,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    gap: Dp = PbSpace.S16,
    headerGap: Dp = PbSpace.S16,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag(testTag),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = PbLayout.MaxContentWidth)
                .fillMaxSize()
        ) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = PbLayout.ScreenMargin)
                        .padding(top = PbSize.Tap + headerGap, bottom = PbSpace.S24),
                    verticalArrangement = Arrangement.spacedBy(gap),
                    content = content,
                )
                Box(
                    Modifier.fillMaxWidth()
                        .background(PbColors.Bg.Primary)
                        .padding(horizontal = PbLayout.ScreenMargin)
                ) {
                    header()
                }
            }
            if (footer != null) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = PbLayout.ScreenMargin),
                    verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
                    content = footer,
                )
            }
            bottomBar?.invoke()
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 400)
@Composable
private fun PbPinnedHeaderScreenPreview() {
    PaybakTheme {
        PbPinnedHeaderScreen("preview", header = { PbPushHeader("Expense", onBack = {}) }) {
            repeat(12) {
                Text("Row $it", style = PbTextStyles.Body, color = PbColors.Text.Primary)
            }
        }
    }
}
