package app.paybak.paybak.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The sizes of `Brand / App Mark` in Figma. */
object PbAppMarkSize {
    val Cover = 160.dp
    val Splash = 96.dp
    val Medium = 40.dp
    val Header = 28.dp
}

/**
 * `Brand / App Mark`: the white "P" on a black squircle (22.37 % radius, 60 % corner smoothing).
 * Drawn from `assets/brand/app-mark.svg`, which keeps Figma's smoothing, so it scales cleanly.
 */
@Composable
fun PbAppMark(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.brand_app_mark),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

enum class PbLogoLayout {
    Horizontal,
    Stacked,
}

/**
 * `Brand / Logo`: mark + live "Paybak" wordmark. Horizontal (104 × 28) for Get Started and the Home
 * header; Stacked (135 × 156) for Splash. TalkBack reads it as "Paybak".
 */
@Composable
fun PbLogo(layout: PbLogoLayout, modifier: Modifier = Modifier) {
    when (layout) {
        PbLogoLayout.Horizontal ->
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbAppMark(PbAppMarkSize.Header)
                Wordmark(large = false)
            }

        PbLogoLayout.Stacked ->
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbAppMark(PbAppMarkSize.Splash)
                Wordmark(large = true)
            }
    }
}

@Composable
private fun Wordmark(large: Boolean) {
    Text(
        text = stringResource(R.string.app_name),
        style = if (large) PbTextStyles.BrandWordmarkL else PbTextStyles.BrandWordmarkS,
        color = PbColors.Text.Primary,
        maxLines = 1,
    )
}

@Preview(showBackground = true)
@Composable
private fun PbLogoPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        PbLogo(PbLogoLayout.Horizontal)
        PbLogo(PbLogoLayout.Stacked)
    }
}
