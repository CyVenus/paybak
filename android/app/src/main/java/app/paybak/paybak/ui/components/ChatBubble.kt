package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Role` of a [PbChatBubble]. */
enum class PbChatRole {
    /** A black bubble on the right. */
    User,

    /** Plain text on the left, after the sparkles avatar. */
    Assistant,
}

/**
 * `Chat / Bubble` (`PBChatBubble`): one Ask Paybak message across the full width. The user's is a
 * black bubble on the right (at most 280 dp); the assistant's is plain Body text on the left (at
 * most 320 dp with the 24 dp sparkles avatar). The text can be selected and copied.
 */
@Composable
fun PbChatBubble(
    text: String,
    role: PbChatRole,
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
) {
    Box(
        modifier.fillMaxWidth(),
        contentAlignment =
            if (role == PbChatRole.User) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        SelectionContainer {
            when (role) {
                PbChatRole.User ->
                    Text(
                        text = text,
                        modifier =
                            Modifier.widthIn(max = 280.dp)
                                .background(PbColors.Bg.Inverse, PbShapes.Card)
                                .padding(horizontal = PbSpace.S16, vertical = PbSpace.S12),
                        style = PbTextStyles.Body,
                        color = PbColors.Text.Inverse,
                    )
                PbChatRole.Assistant ->
                    Row(
                        modifier = Modifier.widthIn(max = 320.dp),
                        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    ) {
                        if (showAvatar) {
                            PbAvatar(
                                PbAvatarContent.Symbol(PbIcon.Sparkles),
                                size = PbAvatarSize.Xs,
                            )
                        }
                        Text(text, style = PbTextStyles.Body, color = PbColors.Text.Primary)
                    }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbChatBubblePreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbChatBubble("Who owes me money?", PbChatRole.User)
        PbChatBubble("Priya owes you ₹700 and Rohan ₹800.", PbChatRole.Assistant)
    }
}
