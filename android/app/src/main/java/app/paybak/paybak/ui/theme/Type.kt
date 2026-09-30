package app.paybak.paybak.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.paybak.paybak.R

/** Manrope, bundled in the five weights the design uses. */
val Manrope =
    FontFamily(
        Font(R.font.manrope_regular, FontWeight.Normal),
        Font(R.font.manrope_medium, FontWeight.Medium),
        Font(R.font.manrope_semibold, FontWeight.SemiBold),
        Font(R.font.manrope_bold, FontWeight.Bold),
        Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    )

/**
 * The Figma text styles. Line height and tracking match Figma exactly: every line box is the
 * style's line height, the leading is split evenly above and below the glyphs (Figma's behaviour),
 * and tracking is a percentage of the size.
 */
object PbTextStyles {
    val Title1 = manrope(FontWeight.ExtraBold, size = 32, lineHeight = 38, trackingPercent = -2f)
    val Title2 = manrope(FontWeight.Bold, size = 24, lineHeight = 30, trackingPercent = -1.5f)
    val Title3 = manrope(FontWeight.Bold, size = 20, lineHeight = 26, trackingPercent = -1f)
    val AmountDisplay =
        amount(FontWeight.ExtraBold, size = 56, lineHeight = 64, trackingPercent = -2f)
    val AmountLarge =
        amount(FontWeight.ExtraBold, size = 26, lineHeight = 32, trackingPercent = -2f)
    val AmountMedium = amount(FontWeight.Bold, size = 17, lineHeight = 22, trackingPercent = -0.5f)
    val Headline =
        manrope(FontWeight.SemiBold, size = 16, lineHeight = 22, trackingPercent = -0.25f)
    val Body = manrope(FontWeight.Normal, size = 16, lineHeight = 24, trackingPercent = 0f)
    val ButtonLarge =
        manrope(FontWeight.SemiBold, size = 17, lineHeight = 22, trackingPercent = -0.5f)
    val ButtonSmall =
        manrope(FontWeight.SemiBold, size = 15, lineHeight = 20, trackingPercent = -0.25f)
    val Subheadline = manrope(FontWeight.Medium, size = 14, lineHeight = 20, trackingPercent = 0f)
    val Footnote = manrope(FontWeight.Medium, size = 13, lineHeight = 18, trackingPercent = 0f)
    val Caption1 = manrope(FontWeight.Bold, size = 12, lineHeight = 16, trackingPercent = 1f)
    val Caption2 = manrope(FontWeight.SemiBold, size = 11, lineHeight = 13, trackingPercent = 1f)
    val BrandWordmarkS =
        manrope(FontWeight.ExtraBold, size = 20, lineHeight = 24, trackingPercent = -3f)
    val BrandWordmarkL =
        manrope(FontWeight.ExtraBold, size = 40, lineHeight = 44, trackingPercent = -3f)
}

/** Manrope's ascent + descent: (2132 + 600) / 2000 em. */
private const val MANROPE_CONTENT_HEIGHT_EM = 1.366f

/**
 * Figma makes every line box exactly the line height and centres the glyphs in it, letting them
 * overhang when the font is taller than the line (Title/1: 43.7 dp of font in a 38 dp line).
 * Compose would pad the first and last lines in that case, so those styles trim them back to the
 * line height (`Mode.Tight` + `Trim.Both`). Styles whose line is taller than the font keep their
 * half-leading above and below (`Trim.None`).
 */
private fun figmaLineHeight(size: Int, lineHeight: Int) =
    LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim =
            if (lineHeight < size * MANROPE_CONTENT_HEIGHT_EM) {
                LineHeightStyle.Trim.Both
            } else {
                LineHeightStyle.Trim.None
            },
        mode = LineHeightStyle.Mode.Tight,
    )

private fun manrope(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    trackingPercent: Float,
) =
    TextStyle(
        fontFamily = Manrope,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = (trackingPercent / 100f).em,
        lineHeightStyle = figmaLineHeight(size, lineHeight),
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

/** Amount styles use tabular figures so numbers line up (Typography rule 2). */
private fun amount(weight: FontWeight, size: Int, lineHeight: Int, trackingPercent: Float) =
    manrope(weight, size, lineHeight, trackingPercent).copy(fontFeatureSettings = "tnum")
