package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private class TextStyleSample(val name: String, val style: TextStyle, val sample: String)

private val samples =
    listOf(
        TextStyleSample("Title/1", PbTextStyles.Title1, "Split it. Track it."),
        TextStyleSample("Title/2", PbTextStyles.Title2, "Enter the code"),
        TextStyleSample("Title/3", PbTextStyles.Title3, "Recent activity"),
        TextStyleSample("Amount/Display", PbTextStyles.AmountDisplay, "₹2,450"),
        TextStyleSample("Amount/Large", PbTextStyles.AmountLarge, "+₹1,240"),
        TextStyleSample("Amount/Medium", PbTextStyles.AmountMedium, "−₹380"),
        TextStyleSample("Headline", PbTextStyles.Headline, "Indian Rupee"),
        TextStyleSample(
            "Body",
            PbTextStyles.Body,
            "Totals show in this currency. You can still add expenses in others.",
        ),
        TextStyleSample("Button/Large", PbTextStyles.ButtonLarge, "Continue with Apple"),
        TextStyleSample("Button/Small", PbTextStyles.ButtonSmall, "See all"),
        TextStyleSample("Subheadline", PbTextStyles.Subheadline, "INR · Based on your region"),
        TextStyleSample("Footnote", PbTextStyles.Footnote, "We’ll send a 6-digit code."),
        TextStyleSample("Caption/1", PbTextStyles.Caption1, "DUE FRI"),
        TextStyleSample("Caption/2", PbTextStyles.Caption2, "AM"),
        TextStyleSample("Brand/Wordmark S", PbTextStyles.BrandWordmarkS, "Paybak"),
        TextStyleSample("Brand/Wordmark L", PbTextStyles.BrandWordmarkL, "Paybak"),
    )

/**
 * Every Figma text style with its specs. The tinted band behind each sample is the line box, so the
 * line height can be checked against Figma.
 */
@Composable
internal fun TypePage() {
    GalleryPage {
        samples.forEach { sample ->
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                GalleryLabel("${sample.name} · ${sample.style.specs()}")
                Text(
                    text = sample.sample,
                    modifier = Modifier.background(PbColors.Bg.Selected),
                    style = sample.style,
                    color = PbColors.Text.Primary,
                )
            }
        }
    }
}

private fun TextStyle.specs(): String {
    val weight =
        when (fontWeight) {
            FontWeight.Normal -> "Regular"
            FontWeight.Medium -> "Medium"
            FontWeight.SemiBold -> "SemiBold"
            FontWeight.Bold -> "Bold"
            FontWeight.ExtraBold -> "ExtraBold"
            else -> fontWeight.toString()
        }
    val metrics = "${fontSize.value.toInt()}/${lineHeight.value.toInt()}"
    val tracking = "%+.2f".format(letterSpacing.value * 100)
    return "$weight $metrics · $tracking%"
}
