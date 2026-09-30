package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/** `Size` of `Avatar / Pair`: 32 for transfer rows, 56 for the amount hero. */
enum class PbAvatarPairSize(
    internal val avatar: PbAvatarSize,
    internal val arrow: Dp,
    internal val gap: Dp,
) {
    Small(PbAvatarSize.Sm, PbSize.IconSm, PbSpace.S4),
    Large(PbAvatarSize.Lg, PbSize.IconMd, PbSpace.S8),
}

/**
 * `Avatar / Pair` (`PBAvatarPair`): "from → to" for transfers and payments, two avatars with a grey
 * arrow between them. Decorative: the row or hero around it names the people.
 *
 * @param onCard White circles, for #F5F5F5 cards.
 */
@Composable
fun PbAvatarPair(
    from: PbAvatarContent,
    to: PbAvatarContent,
    modifier: Modifier = Modifier,
    size: PbAvatarPairSize = PbAvatarPairSize.Small,
    onCard: Boolean = false,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(size.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(from, size = size.avatar, onCard = onCard)
        PbIconImage(
            PbIcon.ArrowRight,
            contentDescription = null,
            size = size.arrow,
            tint = PbColors.Icon.Tertiary,
        )
        PbAvatar(to, size = size.avatar, onCard = onCard)
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarPairPreview() {
    val arjun = PbAvatarContent.Art(PbPeepHead.Arjun)
    val kabir = PbAvatarContent.Art(PbPeepHead.Kabir)
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbAvatarPair(arjun, kabir)
        PbAvatarPair(arjun, kabir, size = PbAvatarPairSize.Large)
    }
}
