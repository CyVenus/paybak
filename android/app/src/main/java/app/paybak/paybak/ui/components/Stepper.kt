package app.paybak.paybak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes

/** The kit stepper's fill: iOS tertiary fill, #767680 at 12 %. */
private val StepperFill = Color(0x1F767680)

/** The hairline between the halves: iOS separator, #3C3C43 at 30 %. */
private val StepperDivider = Color(0x4D3C3C43)
private val GlyphSize = 14.dp
private val GlyphStroke = 2.dp

/**
 * The iOS kit `Stepper` drawn for Android (components-app.md §1.2, §2.6): a 92 × 32
 * capsule, #767680 at 12 %, with − and + halves split by a hairline. Disabled draws it at 40 % (an
 * excluded split person); a half that can't step further dims its glyph.
 */
@Composable
fun PbStepper(
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
) {
    Box(
        modifier.size(92.dp, 32.dp).alpha(if (enabled) 1f else 0.4f).clip(PbShapes.Pill),
        contentAlignment = Alignment.Center,
    ) {
        Row(Modifier.matchParentSize().background(StepperFill)) {
            StepperHalf(
                plus = false,
                enabled = enabled && canDecrement,
                onClick = onDecrement,
                description = stringResource(R.string.pb_decrease),
            )
            StepperHalf(
                plus = true,
                enabled = enabled && canIncrement,
                onClick = onIncrement,
                description = stringResource(R.string.pb_increase),
            )
        }
        Box(Modifier.size(1.dp, 18.dp).background(StepperDivider))
    }
}

@Composable
private fun RowScope.StepperHalf(
    plus: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    description: String,
) {
    val press = rememberPressState(interactionSource = null)
    val glyph = if (enabled) PbColors.Text.Primary else PbColors.Text.Tertiary
    Box(
        modifier =
            Modifier.weight(1f)
                .fillMaxHeight()
                .background(rowPressColor(press.isPressed, onCard = true))
                .pressable(press, enabled, onClick = onClick)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(GlyphSize)) {
            val stroke = GlyphStroke.toPx()
            val mid = size.height / 2
            drawLine(glyph, Offset(0f, mid), Offset(size.width, mid), stroke, StrokeCap.Round)
            if (plus) {
                val centre = size.width / 2
                drawLine(
                    glyph,
                    Offset(centre, 0f),
                    Offset(centre, size.height),
                    stroke,
                    StrokeCap.Round,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbStepperPreview() {
    PbStepper(onDecrement = {}, onIncrement = {})
}
