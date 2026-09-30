package app.paybak.paybak.ui.components.groups

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * A pushed page as screens-groups §1.6 builds it: the Push Header pinned over a white band that
 * runs from the top edge to its bottom, and the content scrolling under the band, starting 16 dp
 * below the header. The root is tagged `screen.[id]`; the header's parts "[id].back" and
 * "[id].action". The keyboard lifts the end of the content, so a focused field can scroll into
 * view.
 *
 * @param overlay Drawn over everything, e.g. a local sheet.
 */
@Composable
fun PbPushedPage(
    id: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: PbHeaderAction? = null,
    scrollState: ScrollState = rememberScrollState(),
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.$id"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(Modifier.widthIn(max = PbLayout.MaxContentWidth).fillMaxSize()) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.union(WindowInsets.ime)
                            .only(WindowInsetsSides.Bottom)
                    )
                    .padding(
                        start = PbLayout.ScreenMargin,
                        end = PbLayout.ScreenMargin,
                        top = PbSize.Tap + PbSpace.S16,
                        bottom = PbSpace.S24,
                    ),
                content = content,
            )
            Column(Modifier.fillMaxWidth().background(PbColors.Bg.Primary)) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                PbPushHeader(
                    title,
                    onBack = onBack,
                    modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
                    action = action,
                    testTag = id,
                )
            }
        }
        overlay()
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 400)
@Composable
private fun PbPushedPagePreview() {
    PaybakTheme {
        PbPushedPage(
            id = "group",
            onBack = {},
            action = PbHeaderAction.Icon(PbIcon.Settings, "Group settings", onClick = {}),
        ) {
            Text("Goa Trip", style = PbTextStyles.Title2, color = PbColors.Text.Primary)
        }
    }
}
