package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val DotSize = 8.dp
private val ActiveDotWidth = 24.dp

/**
 * `Control / Page Dots` (`PBPageDots`): the onboarding pager. The active dot is a 24 × 8 black
 * pill; changing [active] grows it and shrinks the previous one. Not interactive.
 *
 * @param active 1-based index of the current page.
 */
@Composable
fun PbPageDots(active: Int, modifier: Modifier = Modifier, count: Int = 3) {
    val description = stringResource(R.string.pb_page_of, active, count)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val isActive = index + 1 == active
            val width by
                animateDpAsState(
                    targetValue = if (isActive) ActiveDotWidth else DotSize,
                    animationSpec = tween(PbMotion.PAGE_DOTS_MILLIS, easing = PbMotion.EaseInOut),
                    label = "Page dot width",
                )
            val color by
                animateColorAsState(
                    targetValue = if (isActive) PbColors.Bg.Inverse else PbColors.Bg.Indicator,
                    animationSpec = tween(PbMotion.PAGE_DOTS_MILLIS, easing = PbMotion.EaseInOut),
                    label = "Page dot colour",
                )
            Box(Modifier.size(width, DotSize).background(color, PbShapes.Pill))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbPageDotsPreview() {
    PbPageDots(active = 2)
}
