package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Control / Payment Parties` (`PBPaymentParties`): who paid whom on Record payment, a 96
 * dp #F5F5F5 card with a From tile, an arrow and a To tile. Each tile opens a person picker. Parts
 * are tagged "[testTag].from" and "[testTag].to".
 */
@Composable
fun PbPaymentParties(
    fromName: String,
    fromAvatar: PbAvatarContent,
    toName: String,
    toAvatar: PbAvatarContent,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(96.dp)
                .partTag(testTag)
                .clip(PbShapes.Card)
                .background(PbColors.Bg.Card)
                .padding(horizontal = PbSpace.S16),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PartyTile(
            label = stringResource(R.string.pb_from),
            name = fromName,
            avatar = fromAvatar,
            onClick = onFromClick,
            modifier = Modifier.weight(1f).partTag(testTag, "from"),
        )
        PbIconImage(
            PbIcon.ArrowRight,
            contentDescription = null,
            size = PbSize.IconMd,
            tint = PbColors.Icon.Tertiary,
        )
        PartyTile(
            label = stringResource(R.string.pb_to),
            name = toName,
            avatar = toAvatar,
            onClick = onToClick,
            modifier = Modifier.weight(1f).partTag(testTag, "to"),
        )
    }
}

@Composable
private fun PartyTile(
    label: String,
    name: String,
    avatar: PbAvatarContent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState(interactionSource = null)
    Row(
        modifier =
            modifier
                .clip(PbShapes.Tile)
                .background(rowPressColor(press.isPressed, onCard = true))
                .pressable(press, enabled = true, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(avatar, size = PbAvatarSize.Lg, onCard = true)
        Column {
            Text(label, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary)
            Text(
                text = name,
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbPaymentPartiesPreview() {
    PbPaymentParties(
        fromName = "You",
        fromAvatar = PbAvatarContent.Art(PbPeepHead.Arjun),
        toName = "Meera",
        toAvatar = PbAvatarContent.Art(PbPeepHead.Meera),
        onFromClick = {},
        onToClick = {},
    )
}
