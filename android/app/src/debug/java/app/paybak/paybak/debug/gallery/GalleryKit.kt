package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** A scrolling gallery page with the screen margins. */
@Composable
internal fun GalleryPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PbLayout.ScreenMargin, vertical = PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbLayout.SectionGap),
        content = content,
    )
}

/** A titled group of samples. */
@Composable
internal fun GallerySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Text(title, style = PbTextStyles.Title3, color = PbColors.Text.Primary)
        content()
    }
}

/** A small grey caption naming a sample. */
@Composable
internal fun GalleryLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary)
}

/** Black diagonal stripes, a busy backdrop that shows translucent materials. */
internal fun Modifier.stripes(): Modifier =
    clipToBounds().drawBehind {
        val step = 12.dp.toPx()
        var x = -size.height
        while (x < size.width) {
            drawLine(
                color = PbColors.Bg.Inverse,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 3.dp.toPx(),
            )
            x += step
        }
    }

/** "#0A0A0A" or "#0A0A0A 6%" for a colour with alpha. */
internal fun Color.hexLabel(): String {
    val argb = toArgb()
    val rgb = "#%06X".format(argb and 0xFFFFFF)
    val alphaPercent = Math.round(alpha * 100)
    return if (alphaPercent == 100) rgb else "$rgb $alphaPercent%"
}

/**
 * An interaction source that always reports [interaction], so the gallery can show a control's
 * Pressed or Focused look without touching it (and without raising the keyboard).
 */
private class StaticInteractionSource(interaction: Interaction) : MutableInteractionSource {
    override val interactions: Flow<Interaction> = flowOf(interaction)

    override suspend fun emit(interaction: Interaction) = Unit

    override fun tryEmit(interaction: Interaction) = true
}

@Composable
internal fun rememberPressedSource(): MutableInteractionSource = remember {
    StaticInteractionSource(PressInteraction.Press(Offset.Zero))
}

@Composable
internal fun rememberFocusedSource(): MutableInteractionSource = remember {
    StaticInteractionSource(FocusInteraction.Focus())
}

/** "2800" → "2,800", "100000" → "1,00,000": Indian digit grouping, for the gallery's samples. */
internal fun groupIndian(digits: String): String {
    val whole = digits.substringBefore('.').ifEmpty { "0" }
    val fraction = digits.substringAfter('.', missingDelimiterValue = "")
    val last3 = whole.takeLast(3)
    val rest = whole.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
    val grouped = if (rest.isEmpty()) last3 else "$rest,$last3"
    return if ('.' in digits) "$grouped.$fraction" else grouped
}
