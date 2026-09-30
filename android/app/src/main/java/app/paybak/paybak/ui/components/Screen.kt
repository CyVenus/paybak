package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout

/**
 * A white full-screen page whose content column sits in the safe area with the 20 dp screen
 * margins, centred at [PbLayout.MaxContentWidth] on wide screens. The root is tagged `screen.<id>`
 * for UI tests.
 *
 * @param id The flow.md screen id.
 * @param modifier Applied to the whole screen, e.g. for gestures anywhere on it.
 */
@Composable
fun PbScreen(
    id: String,
    modifier: Modifier = Modifier,
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
                    .padding(horizontal = PbLayout.ScreenMargin),
            content = content,
        )
    }
}
