package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbAppMark
import app.paybak.paybak.ui.components.PbAppMarkSize
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val iconTints =
    listOf(
        "primary" to PbColors.Icon.Primary,
        "secondary" to PbColors.Icon.Secondary,
        "tertiary" to PbColors.Icon.Tertiary,
        "destructive" to PbColors.Icon.Destructive,
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AssetsPage() {
    GalleryPage {
        GallerySection("Icons (${PbIcon.entries.size})") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            ) {
                PbIcon.entries.forEach { icon ->
                    Column(
                        Modifier.width(56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PbIconImage(icon, contentDescription = icon.name)
                        Text(
                            icon.name,
                            style = PbTextStyles.Caption2,
                            color = PbColors.Text.Secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        GallerySection("Icon sizes (stroke scales with the icon)") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(PbSize.IconLg, PbSize.IconMd, PbSize.IconSm, 14.dp).forEach { size ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PbIconImage(PbIcon.Calendar, contentDescription = null, size = size)
                        GalleryLabel("${size.value.toInt()}")
                    }
                }
            }
        }
        GallerySection("Icon tints") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                iconTints.forEach { (name, tint) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PbIconImage(PbIcon.Bell, contentDescription = null, tint = tint)
                        GalleryLabel(name)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PbIconImage(
                        PbIcon.Bell,
                        contentDescription = null,
                        modifier = Modifier.background(PbColors.Bg.Inverse),
                        tint = PbColors.Icon.Inverse,
                    )
                    GalleryLabel("inverse")
                }
            }
        }
        GallerySection("Brand logos (never recoloured)") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbIconImage(PbIcon.Apple, contentDescription = "Apple")
                PbIconImage(
                    PbIcon.Apple,
                    contentDescription = "Apple on black",
                    modifier =
                        Modifier.background(PbColors.Bg.Inverse, CircleShape).padding(PbSpace.S8),
                    tint = Color.White,
                )
                PbIconImage(
                    PbIcon.Google,
                    contentDescription = "Google",
                    tint = PbColors.Icon.Destructive,
                )
                PbIconImage(PbIcon.WhatsApp, contentDescription = "WhatsApp")
            }
        }
        GallerySection("Brand / App Mark: 160 · 96 · 40 · 28") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.Bottom,
            ) {
                listOf(
                        PbAppMarkSize.Cover,
                        PbAppMarkSize.Splash,
                        PbAppMarkSize.Medium,
                        PbAppMarkSize.Header,
                    )
                    .forEach { PbAppMark(it) }
            }
        }
        GallerySection("Brand / Logo") {
            PbLogo(PbLogoLayout.Horizontal)
            PbLogo(PbLogoLayout.Stacked)
        }
        GallerySection("Art / Peep Head (avatar-1…7)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            ) {
                PbPeepHead.entries.forEach { head ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PbAvatar(PbAvatarContent.Art(head), size = PbAvatarSize.Lg)
                        GalleryLabel(head.name)
                    }
                }
            }
        }
    }
}
