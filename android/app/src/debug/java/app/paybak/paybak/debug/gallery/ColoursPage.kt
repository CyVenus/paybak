package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbPalette
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val primitives =
    listOf(
        "gray/0" to PbPalette.Gray0,
        "gray/50" to PbPalette.Gray50,
        "gray/100" to PbPalette.Gray100,
        "gray/200" to PbPalette.Gray200,
        "gray/300" to PbPalette.Gray300,
        "gray/400" to PbPalette.Gray400,
        "gray/600" to PbPalette.Gray600,
        "gray/800" to PbPalette.Gray800,
        "gray/900" to PbPalette.Gray900,
        "red/50" to PbPalette.Red50,
        "red/500" to PbPalette.Red500,
        "red/600" to PbPalette.Red600,
        "black-40" to PbPalette.Black40,
        "black-06" to PbPalette.Black06,
        "white-72" to PbPalette.White72,
        "white-60" to PbPalette.White60,
        "device/black" to PbPalette.DeviceBlack,
    )

private val semantic =
    listOf(
        "bg" to
            listOf(
                "primary" to PbColors.Bg.Primary,
                "card" to PbColors.Bg.Card,
                "card-pressed" to PbColors.Bg.CardPressed,
                "selected" to PbColors.Bg.Selected,
                "inverse" to PbColors.Bg.Inverse,
                "inverse-pressed" to PbColors.Bg.InversePressed,
                "disabled" to PbColors.Bg.Disabled,
                "destructive" to PbColors.Bg.Destructive,
                "destructive-pressed" to PbColors.Bg.DestructivePressed,
                "destructive-subtle" to PbColors.Bg.DestructiveSubtle,
                "scrim" to PbColors.Bg.Scrim,
                "glass" to PbColors.Bg.Glass,
                "indicator" to PbColors.Bg.Indicator,
                "device" to PbColors.Bg.Device,
                "camera" to PbColors.Bg.Camera,
            ),
        "text" to
            listOf(
                "primary" to PbColors.Text.Primary,
                "secondary" to PbColors.Text.Secondary,
                "tertiary" to PbColors.Text.Tertiary,
                "inverse" to PbColors.Text.Inverse,
                "disabled" to PbColors.Text.Disabled,
                "destructive" to PbColors.Text.Destructive,
            ),
        "icon" to
            listOf(
                "primary" to PbColors.Icon.Primary,
                "secondary" to PbColors.Icon.Secondary,
                "tertiary" to PbColors.Icon.Tertiary,
                "inverse" to PbColors.Icon.Inverse,
                "destructive" to PbColors.Icon.Destructive,
            ),
        "border" to
            listOf(
                "subtle" to PbColors.Border.Subtle,
                "strong" to PbColors.Border.Strong,
                "destructive" to PbColors.Border.Destructive,
                "glass-highlight" to PbColors.Border.GlassHighlight,
            ),
        "illustration" to
            listOf(
                "line" to PbColors.Illustration.Line,
                "tint" to PbColors.Illustration.Tint,
                "fill" to PbColors.Illustration.Fill,
            ),
        "chart" to
            listOf(
                "track" to PbColors.Chart.Track,
                "bar" to PbColors.Chart.Bar,
                "fill" to PbColors.Chart.Fill,
                "over" to PbColors.Chart.Over,
            ),
    )

@Composable
internal fun ColoursPage() {
    GalleryPage {
        GallerySection("Primitives") { Swatches(primitives) }
        semantic.forEach { (group, tokens) ->
            GallerySection("color/$group") { Swatches(tokens) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Swatches(tokens: List<Pair<String, Color>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        tokens.forEach { (name, color) -> Swatch(name, color) }
    }
}

/** A chip of [color], outlined so white and translucent tokens stay visible. */
@Composable
private fun Swatch(name: String, color: Color) {
    Column(Modifier.width(84.dp), verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        Box(
            Modifier.size(84.dp, 44.dp)
                .background(PbColors.Bg.Inverse.copy(alpha = 0.04f), PbShapes.Tile)
                .background(color, PbShapes.Tile)
                .border(PbSize.Hairline, PbColors.Border.Subtle, PbShapes.Tile)
        )
        Text(
            name,
            style = PbTextStyles.Caption2,
            color = PbColors.Text.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            color.hexLabel(),
            style = PbTextStyles.Caption2,
            color = PbColors.Text.Secondary,
            maxLines = 1,
        )
    }
}
