package app.paybak.paybak.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour primitives from Figma "01 Foundations": the gray, red, alpha and device scales. UI code
 * uses the semantic [PbColors] instead; these exist so the semantic tokens can alias them.
 */
object PbPalette {
    val Gray0 = Color(0xFFFFFFFF)
    val Gray50 = Color(0xFFF5F5F5)
    val Gray100 = Color(0xFFEBEBEB)
    val Gray200 = Color(0xFFE0E0E0)
    val Gray300 = Color(0xFFD1D1D1)
    val Gray400 = Color(0xFFA3A3A3)
    val Gray600 = Color(0xFF6B6B6B)
    val Gray800 = Color(0xFF2B2B2B)
    val Gray900 = Color(0xFF0A0A0A)
    val Red50 = Color(0xFFFBEBEB)
    val Red500 = Color(0xFFC93636)
    val Red600 = Color(0xFFA92E2E)

    /** `alpha/black-40`: #0A0A0A at 40 %. */
    val Black40 = Gray900.copy(alpha = 0.40f)

    /** `alpha/black-06`: #0A0A0A at 6 %. */
    val Black06 = Gray900.copy(alpha = 0.06f)

    /** `alpha/white-72`: #FFFFFF at 72 %. */
    val White72 = Gray0.copy(alpha = 0.72f)

    /** `alpha/white-60`: #FFFFFF at 60 %. */
    val White60 = Gray0.copy(alpha = 0.60f)

    /** `device/black`. */
    val DeviceBlack = Color(0xFF000000)
}

/**
 * Semantic colour tokens (Figma mode "Light", the only theme). Named after the Figma variables:
 * `color/bg/card` is [PbColors.Bg.Card], `color/text/secondary` is [PbColors.Text.Secondary].
 */
object PbColors {
    object Bg {
        val Primary = PbPalette.Gray0
        val Card = PbPalette.Gray50
        val CardPressed = PbPalette.Gray100
        val Selected = PbPalette.Black06
        val Inverse = PbPalette.Gray900
        val InversePressed = PbPalette.Gray800
        val Disabled = PbPalette.Gray200
        val Destructive = PbPalette.Red500
        val DestructivePressed = PbPalette.Red600
        val DestructiveSubtle = PbPalette.Red50
        val Scrim = PbPalette.Black40
        val Glass = PbPalette.White72
        val Indicator = PbPalette.Gray300
        val Device = PbPalette.DeviceBlack
        val Camera = PbPalette.Gray800
    }

    object Text {
        val Primary = PbPalette.Gray900
        val Secondary = PbPalette.Gray600
        val Tertiary = PbPalette.Gray400
        val Inverse = PbPalette.Gray0
        val Disabled = PbPalette.Gray400
        val Destructive = PbPalette.Red500
    }

    object Icon {
        val Primary = PbPalette.Gray900
        val Secondary = PbPalette.Gray600
        val Tertiary = PbPalette.Gray400
        val Inverse = PbPalette.Gray0
        val Destructive = PbPalette.Red500
    }

    object Border {
        val Subtle = PbPalette.Gray100
        val Strong = PbPalette.Gray900
        val Destructive = PbPalette.Red500
        val GlassHighlight = PbPalette.White60
    }

    object Illustration {
        val Line = PbPalette.Gray900
        val Tint = PbPalette.Gray100
        val Fill = PbPalette.Gray0
    }

    object Chart {
        val Track = PbPalette.Gray100
        val Bar = PbPalette.Gray300
        val Fill = PbPalette.Gray900
        val Over = PbPalette.Red500
    }
}
