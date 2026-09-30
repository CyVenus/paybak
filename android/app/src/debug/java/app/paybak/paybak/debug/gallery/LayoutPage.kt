package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbRadius
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.pbMaterial
import app.paybak.paybak.ui.theme.pbScrim

private val spaces =
    listOf(
        "0" to PbSpace.S0,
        "2" to PbSpace.S2,
        "4" to PbSpace.S4,
        "6" to PbSpace.S6,
        "8" to PbSpace.S8,
        "12" to PbSpace.S12,
        "16" to PbSpace.S16,
        "20" to PbSpace.S20,
        "24" to PbSpace.S24,
        "28" to PbSpace.S28,
        "32" to PbSpace.S32,
        "40" to PbSpace.S40,
        "48" to PbSpace.S48,
        "64" to PbSpace.S64,
        "96" to PbSpace.S96,
    )

private val layouts =
    listOf(
        "screen-margin" to PbLayout.ScreenMargin,
        "card-padding" to PbLayout.CardPadding,
        "section-gap" to PbLayout.SectionGap,
    )

private val radii =
    listOf(
        "xs" to PbRadius.Xs,
        "sm" to PbRadius.Sm,
        "input" to PbRadius.Input,
        "tile" to PbRadius.Tile,
        "card" to PbRadius.Card,
        "sheet" to PbRadius.Sheet,
    )

private val sizes =
    listOf(
        "button-lg" to PbSize.ButtonLg,
        "button-sm" to PbSize.ButtonSm,
        "tap" to PbSize.Tap,
        "icon-sm" to PbSize.IconSm,
        "icon-md" to PbSize.IconMd,
        "icon-lg" to PbSize.IconLg,
        "avatar-xs" to PbSize.AvatarXs,
        "avatar-sm" to PbSize.AvatarSm,
        "avatar-md" to PbSize.AvatarMd,
        "avatar-lg" to PbSize.AvatarLg,
        "tabbar" to PbSize.TabBar,
        "add-button" to PbSize.AddButton,
        "hairline" to PbSize.Hairline,
    )

@Composable
internal fun LayoutPage() {
    GalleryPage {
        GallerySection("space/*") { spaces.forEach { (name, value) -> Bar("space/$name", value) } }
        GallerySection("layout/*") {
            layouts.forEach { (name, value) -> Bar("layout/$name", value) }
        }
        GallerySection("radius/*") { Radii() }
        GallerySection("size/*") { sizes.forEach { (name, value) -> Bar("size/$name", value) } }
        GallerySection("Materials (Android fallback)") { Materials() }
    }
}

@Composable
private fun Bar(name: String, value: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GalleryLabel("$name · ${value.value.toInt()}", Modifier.width(150.dp))
        Box(Modifier.width(value).height(12.dp).background(PbColors.Bg.Inverse))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Radii() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        (radii.map { (name, radius) ->
                "radius/$name · ${radius.value.toInt()}" to RoundedCornerShape(radius)
            } + ("radius/full" to PbShapes.Pill))
            .forEach { (label, shape) ->
                Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                    Box(Modifier.size(104.dp, 64.dp).background(PbColors.Bg.Card, shape))
                    GalleryLabel(label)
                }
            }
    }
}

/** Each material over stripes, so the translucent fill, highlight and shadow show. */
@Composable
private fun Materials() {
    Box(Modifier.fillMaxWidth().height(220.dp).stripes()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaterialChip(
                    "Glass",
                    Modifier.size(150.dp, 62.dp).pbMaterial(PbMaterial.Glass, PbShapes.Pill),
                )
                MaterialChip(
                    "Small",
                    Modifier.size(44.dp).pbMaterial(PbMaterial.GlassSmall, PbShapes.Pill),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
                MaterialChip(
                    "Frosted",
                    Modifier.size(150.dp, 62.dp).pbMaterial(PbMaterial.Frosted, PbShapes.Card),
                )
                MaterialChip("Scrim", Modifier.size(150.dp, 62.dp).pbScrim())
            }
        }
    }
}

@Composable
private fun MaterialChip(label: String, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { GalleryLabel(label) }
}
