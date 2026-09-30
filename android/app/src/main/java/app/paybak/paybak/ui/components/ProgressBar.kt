package app.paybak.paybak.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/** Bars grow to new values over this long (a suggestion; Figma has no motion here). */
private const val GROW_MILLIS = 300
private val MarkWidth = 2.dp
private val MarkOverhang = 3.dp
private val MarkOutline = 1.dp

/** `Size` of `Control / Progress Bar`: Small inside rows, Large in cards. */
enum class PbProgressBarSize(internal val height: Dp) {
    Small(6.dp),
    Large(12.dp),
}

/**
 * `Control / Progress Bar` (`PBProgressBar`): a full-width capsule track in chart greys with the
 * black fill up to [progress] (0…1). Changes grow over 0.3 s.
 *
 * @param projected Projected: a lighter `chart/bar` segment up to this point (planned spending).
 * @param over Over: the fill stops square at [progress] (the budget point) and red runs to the end.
 *   The only red in charts.
 * @param mark A 2 dp tick overhanging the track by 3 dp, with a white outline: a fair share or the
 *   budget point.
 */
@Composable
fun PbProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    size: PbProgressBarSize = PbProgressBarSize.Small,
    projected: Float? = null,
    over: Boolean = false,
    mark: Float? = null,
) {
    val fill by
        animateFloatAsState(
            progress.coerceIn(0f, 1f),
            tween(GROW_MILLIS, easing = PbMotion.EaseOut),
            label = "Progress fill",
        )
    val projection by
        animateFloatAsState(
            (projected ?: 0f).coerceIn(0f, 1f),
            tween(GROW_MILLIS, easing = PbMotion.EaseOut),
            label = "Progress projection",
        )
    Spacer(
        modifier
            .fillMaxWidth()
            .height(size.height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
            .drawBehind {
                drawBar(fill, projection.takeIf { projected != null }, over)
                if (mark != null) drawMark(mark.coerceIn(0f, 1f))
            }
    )
}

private fun DrawScope.drawBar(fill: Float, projected: Float?, over: Boolean) {
    val radius = CornerRadius(size.height / 2)
    val track = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, radius)) }
    val fillEnd = size.width * fill
    clipPath(track) {
        drawRect(PbColors.Chart.Track)
        if (projected != null && projected > 0f) {
            drawRoundRect(
                PbColors.Chart.Bar,
                size = Size(size.width * projected, size.height),
                cornerRadius = radius,
            )
        }
        if (over) {
            drawRect(
                PbColors.Chart.Over,
                topLeft = Offset(fillEnd, 0f),
                size = Size(size.width - fillEnd, size.height),
            )
        }
        if (fill > 0f) {
            val segment =
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = fillEnd,
                    bottom = size.height,
                    topLeftCornerRadius = radius,
                    bottomLeftCornerRadius = radius,
                    topRightCornerRadius = if (over) CornerRadius.Zero else radius,
                    bottomRightCornerRadius = if (over) CornerRadius.Zero else radius,
                )
            drawPath(Path().apply { addRoundRect(segment) }, PbColors.Chart.Fill)
        }
    }
}

/** The tick at [at], overhanging the track, drawn over a 1 dp white outline. */
private fun DrawScope.drawMark(at: Float) {
    val width = MarkWidth.toPx()
    val overhang = MarkOverhang.toPx()
    val outline = MarkOutline.toPx()
    val left = size.width * at - width / 2
    drawRect(
        PbColors.Bg.Primary,
        topLeft = Offset(left - outline, -overhang - outline),
        size = Size(width + 2 * outline, size.height + 2 * (overhang + outline)),
    )
    drawRect(
        PbColors.Chart.Fill,
        topLeft = Offset(left, -overhang),
        size = Size(width, size.height + 2 * overhang),
    )
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbProgressBarPreview() {
    Column(Modifier.padding(PbSpace.S8), verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbProgressBar(0.6f, mark = 0.51f)
        PbProgressBar(0.87f, projected = 0.97f)
        PbProgressBar(0.976f, size = PbProgressBarSize.Large, over = true, mark = 0.976f)
    }
}
