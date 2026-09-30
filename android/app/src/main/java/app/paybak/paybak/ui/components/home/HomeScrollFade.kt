package app.paybak.paybak.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors

/** Home — Active's fade height; the confirm card state starts it lower (home-v2 §3.8). */
val HomeFadeHeight = 150.dp
val HomeFadeHeightWithClaim = 118.dp

/**
 * Home's `Scroll edge (fade)`: white from clear to 85 % at 45 % of its height (about the tab bar's
 * top) to opaque at the bottom edge, over the content and under the tab bar. Not interactive.
 */
@Composable
fun PbHomeScrollFade(modifier: Modifier = Modifier, height: Dp = HomeFadeHeight) {
    val white = PbColors.Bg.Primary
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    0f to white.copy(alpha = 0f),
                    MID_STOP to white.copy(alpha = MID_ALPHA),
                    1f to white,
                )
            )
    )
}

private const val MID_STOP = 0.45f
private const val MID_ALPHA = 0.85f

@Preview(widthDp = 402, heightDp = 200, backgroundColor = 0xFF0A0A0A, showBackground = true)
@Composable
private fun PbHomeScrollFadePreview() {
    Box(Modifier.size(402.dp, 200.dp), contentAlignment = Alignment.BottomCenter) {
        PbHomeScrollFade()
    }
}
