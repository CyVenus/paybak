package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Card / Payment Preview` (`PBPaymentPreview`), with the plain copy icon: how your name and UPI ID
 * appear to friends ("What friends see"). While [upiId] is empty the card shows [placeholder] in
 * tertiary text and hides the copy button.
 *
 * @param avatar Drawn on a white circle, as avatars on #F5F5F5 cards are.
 * @param copyButtonModifier Applied to the copy button, e.g. its test tag.
 * @param showCaption False where a section header above the card says "What friends see"
 *   (Payment details).
 */
@Composable
fun PbPaymentPreview(
    name: String,
    upiId: String,
    avatar: PbAvatarContent,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    copyButtonModifier: Modifier = Modifier,
    showCaption: Boolean = true,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbLayout.CardPadding),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        if (showCaption) {
            Text(
                text = stringResource(R.string.pb_payment_preview_caption),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Tertiary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = PbSize.Tap),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(avatar, size = PbAvatarSize.Md, onCard = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    text = name,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = upiId.ifEmpty { placeholder },
                    style = PbTextStyles.Subheadline,
                    color =
                        if (upiId.isEmpty()) PbColors.Text.Tertiary else PbColors.Text.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )
            }
            if (upiId.isNotEmpty()) {
                PbIconButton(
                    icon = PbIcon.Copy,
                    contentDescription = stringResource(R.string.pb_copy_upi_id),
                    onClick = onCopy,
                    modifier = copyButtonModifier,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbPaymentPreviewPreview() {
    Column(Modifier.width(362.dp), verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbPaymentPreview(
            name = "Arjun Mehta",
            upiId = "arjun@okaxis",
            avatar = PbAvatarContent.Art(PbPeepHead.Arjun),
            onCopy = {},
        )
        PbPaymentPreview(
            name = "Arjun Mehta",
            upiId = "",
            avatar = PbAvatarContent.Initials("AM"),
            onCopy = {},
            placeholder = "yourname@bank",
        )
    }
}
