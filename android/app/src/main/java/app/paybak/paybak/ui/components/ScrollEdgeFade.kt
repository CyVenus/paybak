package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace

private val FadeHeight = PbSpace.S24

/**
 * `Scroll edge fade`: a 24 dp gradient from clear to the white background where a scrolling list
 * meets a pinned footer, so rows fade out instead of being cut off. Draw it over the list's bottom
 * edge.
 */
@Composable
fun PbScrollEdgeFade(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(FadeHeight)
            .background(
                Brush.verticalGradient(
                    listOf(PbColors.Bg.Primary.copy(alpha = 0f), PbColors.Bg.Primary)
                )
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun PbScrollEdgeFadePreview() {
    Box(Modifier.size(362.dp, 80.dp), contentAlignment = Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth()) {
            PbCurrencyRow("S$", "Singapore Dollar", "SGD", selected = false, onClick = {})
        }
        PbScrollEdgeFade()
    }
}
