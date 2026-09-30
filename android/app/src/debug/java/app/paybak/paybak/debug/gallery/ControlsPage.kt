package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbAvatarStack
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbDivider
import app.paybak.paybak.ui.components.PbDividerInset
import app.paybak.paybak.ui.components.PbPageDots
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val segmentOptions =
    listOf(
        listOf("Groups", "Friends") to 240.dp,
        listOf("All", "Upcoming", "Overdue") to 330.dp,
        listOf("Equally", "Exact", "%", "Shares") to 362.dp,
    )

@Composable
internal fun ControlsPage() {
    GalleryPage {
        GallerySection("Badge / Pill") {
            PbBadgeStyle.entries.forEach { style ->
                val row =
                    @Composable {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PbBadge("Due Fri", style = style)
                            PbBadge("Due Fri", style = style, icon = PbIcon.Calendar)
                            GalleryLabel(style.name)
                        }
                    }
                if (style == PbBadgeStyle.OnCard) OnCard(row) else row()
            }
        }
        GallerySection("Avatar / Circle: Art · Initials · Icon at 24 · 32 · 40 · 56") {
            PbAvatarSize.entries.forEach { size ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PbAvatar(PbAvatarContent.Art(PbPeepHead.Arjun), size = size)
                    PbAvatar(PbAvatarContent.Initials("AM"), size = size)
                    PbAvatar(PbAvatarContent.Symbol(PbIcon.Groups), size = size)
                    GalleryLabel("${size.diameter.value.toInt()}")
                }
            }
            OnCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PbAvatar(PbAvatarContent.Art(PbPeepHead.Rohan), onCard = true)
                    PbAvatar(PbAvatarContent.Initials("AM"), onCard = true)
                    PbAvatar(PbAvatarContent.Symbol(PbIcon.Groups), onCard = true)
                    GalleryLabel("On card (Icon On Card)")
                }
            }
        }
        GallerySection("Avatar / Stack: 2 · 3 · 4") {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
                (2..4).forEach { count -> PbAvatarStack(PbPeepHead.Presets.take(count)) }
            }
        }
        GallerySection("Control / Page Dots") {
            (1..3).forEach { active -> PbPageDots(active = active) }
            var active by remember { mutableIntStateOf(1) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbPageDots(active = active)
                PbButton(
                    "Next dot",
                    onClick = { active = active % 3 + 1 },
                    style = PbButtonStyle.Secondary,
                    size = PbButtonSize.Small,
                )
            }
        }
        GallerySection("Control / Segmented (tap to select)") {
            segmentOptions.forEach { (options, width) ->
                var selected by remember { mutableIntStateOf(0) }
                PbSegmentedControl(
                    options,
                    selected,
                    onSelect = { selected = it },
                    Modifier.width(width),
                )
            }
        }
        GallerySection("Divider / Line: None · Leading") {
            PbDivider()
            PbDivider(inset = PbDividerInset.Leading)
        }
    }
}

/** A #F5F5F5 card, for the On Card variants. */
@Composable
internal fun OnCard(content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        content()
    }
}
