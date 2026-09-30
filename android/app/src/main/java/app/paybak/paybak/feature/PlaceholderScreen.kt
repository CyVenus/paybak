package app.paybak.paybak.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * Stand-in for a screen that a later phase builds: it names the flow.md screen [id] and offers the
 * screen's navigation [actions], so the whole flow can be walked end to end. Delete it once the
 * last placeholder is replaced.
 *
 * @param header The screen's real navigation header, when it has one.
 */
@Composable
fun PlaceholderScreen(
    id: String,
    phase: String,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit,
) {
    PbScreen(id = id, modifier = modifier) {
        header()
        Spacer(Modifier.height(PbLayout.SectionGap))
        PbBadge("Placeholder", style = PbBadgeStyle.Muted)
        Spacer(Modifier.height(PbSpace.S12))
        Text(id, style = PbTextStyles.Title1, color = PbColors.Text.Primary)
        Spacer(Modifier.height(PbSpace.S8))
        Text(
            text = "The $phase phase builds this screen.",
            style = PbTextStyles.Body,
            color = PbColors.Text.Secondary,
        )
        Spacer(Modifier.height(PbLayout.SectionGap))
        content()
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = actions,
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PlaceholderScreenPreview() {
    PaybakTheme {
        PlaceholderScreen(id = "signIn", phase = "Sign-in") {
            PbButton("Send code", onClick = {}, Modifier.fillMaxWidth())
        }
    }
}
