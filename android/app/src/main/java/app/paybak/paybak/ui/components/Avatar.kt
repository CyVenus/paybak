package app.paybak.paybak.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `Art / Peep Head / …` line-art heads (Open Peeps, CC0): 120 × 120, transparent, never tinted.
 * The circle behind them comes from the container. They ship as the verified 360 px renders
 * (`avatar-N@3x.png`); their SVG paths are too long to draw efficiently as vectors.
 */
enum class PbPeepHead(@param:DrawableRes val resId: Int) {
    Arjun(R.drawable.avatar_1),
    Priya(R.drawable.avatar_2),
    Rohan(R.drawable.avatar_3),
    Esha(R.drawable.avatar_4),
    Dev(R.drawable.avatar_5),
    Kabir(R.drawable.avatar_6),
    Meera(R.drawable.avatar_7);

    companion object {
        /** The five Setup 1 choices; a saved avatar preset is an index into this list. */
        val Presets = listOf(Arjun, Priya, Rohan, Esha, Dev)
    }
}

/** What an avatar circle shows (`Avatar / Circle` Type, plus the user's own photo). */
sealed interface PbAvatarContent {
    data class Art(val head: PbPeepHead) : PbAvatarContent

    /** A photo the user picked; aspect-filled and clipped like the art. */
    data class Photo(val image: ImageBitmap) : PbAvatarContent

    /** Fallback: the first letters of the first and last name. */
    data class Initials(val text: String) : PbAvatarContent

    /** A category or group icon; archived rows grey it with `icon/tertiary`. */
    data class Symbol(val icon: PbIcon, val tint: Color = PbColors.Icon.Primary) : PbAvatarContent
}

/** `Size` of `Avatar / Circle`, with the initials style and icon size Figma pairs with it. */
enum class PbAvatarSize(val diameter: Dp, val initialsStyle: TextStyle, val iconSize: Dp) {
    Xs(PbSize.AvatarXs, PbTextStyles.Caption2, 14.dp),
    Sm(PbSize.AvatarSm, PbTextStyles.Caption1, PbSize.IconSm),
    Md(PbSize.AvatarMd, PbTextStyles.Headline, PbSize.IconMd),
    Lg(PbSize.AvatarLg, PbTextStyles.Title3, PbSize.IconLg),
}

/**
 * `Avatar / Circle` (`PBAvatar`).
 *
 * @param onCard True inside #F5F5F5 cards: the circle turns white (Colour rule 2), which is also
 *   Figma's "Icon On Card" type.
 */
@Composable
fun PbAvatar(
    content: PbAvatarContent,
    modifier: Modifier = Modifier,
    size: PbAvatarSize = PbAvatarSize.Md,
    onCard: Boolean = false,
    contentDescription: String? = null,
) {
    AvatarCircle(
        content = content,
        diameter = size.diameter,
        fill = if (onCard) PbColors.Bg.Primary else PbColors.Bg.Card,
        initialsStyle = size.initialsStyle,
        iconSize = size.iconSize,
        modifier =
            modifier.then(
                if (contentDescription == null) {
                    Modifier.clearAndSetSemantics {}
                } else {
                    Modifier.semantics { this.contentDescription = contentDescription }
                }
            ),
    )
}

/** A clipped circle showing [content]; shared by [PbAvatar] and [PbAvatarOption]. */
@Composable
internal fun AvatarCircle(
    content: PbAvatarContent,
    diameter: Dp,
    fill: Color,
    initialsStyle: TextStyle,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(diameter).clip(CircleShape).background(fill),
        contentAlignment = Alignment.Center,
    ) {
        when (content) {
            is PbAvatarContent.Art ->
                Image(
                    painter = painterResource(content.head.resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )

            is PbAvatarContent.Photo ->
                Image(
                    bitmap = content.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

            is PbAvatarContent.Initials ->
                Text(
                    text = content.text,
                    style = initialsStyle,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                )

            is PbAvatarContent.Symbol ->
                PbIconImage(
                    content.icon,
                    contentDescription = null,
                    size = iconSize,
                    tint = content.tint,
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatarSize.entries.forEach { PbAvatar(PbAvatarContent.Art(PbPeepHead.Arjun), size = it) }
        PbAvatar(PbAvatarContent.Initials("AM"))
        PbAvatar(PbAvatarContent.Symbol(PbIcon.Groups))
    }
}
