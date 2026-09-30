package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * A screen's left-aligned Title/1 headline with its Body text 12 dp below, as on the sign-in and
 * setup screens. The headline is a heading for TalkBack.
 */
@Composable
fun PbTitleBlock(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = PbTextStyles.Title1,
            color = PbColors.Text.Primary,
        )
        Text(text = body, style = PbTextStyles.Body, color = PbColors.Text.Secondary)
    }
}

@Preview(showBackground = true)
@Composable
private fun PbTitleBlockPreview() {
    PbTitleBlock(
        title = "What’s your name?",
        body = "Friends see your name and picture on shared expenses.",
        modifier = Modifier.width(362.dp),
    )
}
