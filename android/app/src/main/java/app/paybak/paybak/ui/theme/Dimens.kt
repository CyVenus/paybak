package app.paybak.paybak.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing tokens: [S12] is `space/12`. */
object PbSpace {
    val S0 = 0.dp
    val S2 = 2.dp
    val S4 = 4.dp
    val S6 = 6.dp
    val S8 = 8.dp
    val S12 = 12.dp
    val S16 = 16.dp
    val S20 = 20.dp
    val S24 = 24.dp
    val S28 = 28.dp
    val S32 = 32.dp
    val S40 = 40.dp
    val S48 = 48.dp
    val S64 = 64.dp
    val S96 = 96.dp
}

/**
 * Layout tokens (`layout/screen-margin` …). Figma's `layout/status-bar` (62) and
 * `layout/home-indicator` (34) are the iPhone frame's insets; the app lays out from the real window
 * insets instead.
 */
object PbLayout {
    val ScreenMargin = PbSpace.S20
    val CardPadding = PbSpace.S16
    val SectionGap = PbSpace.S24

    /** Phone layouts stay centred at this width on wider screens (flow.md, resolved decisions). */
    val MaxContentWidth = 430.dp
}

/** Radius tokens (`radius/xs` … `radius/sheet`). `radius/full` is [PbShapes.Pill]. */
object PbRadius {
    val Xs = 6.dp
    val Sm = 10.dp
    val Input = 14.dp
    val Tile = 14.dp
    val Card = 20.dp
    val Sheet = 40.dp
}

/** Shapes built from the radius tokens. */
object PbShapes {
    /** `radius/full`: pills, circles, capsules. */
    val Pill = RoundedCornerShape(percent = 50)
    val Input = RoundedCornerShape(PbRadius.Input)
    val Tile = RoundedCornerShape(PbRadius.Tile)
    val Card = RoundedCornerShape(PbRadius.Card)
    val Sheet = RoundedCornerShape(PbRadius.Sheet)
}

/** Size tokens (`size/button-lg` …) and `stroke/hairline`. */
object PbSize {
    val ButtonLg = 52.dp
    val ButtonSm = 36.dp
    val Tap = 44.dp
    val IconSm = 16.dp
    val IconMd = 20.dp
    val IconLg = 24.dp
    val AvatarXs = 24.dp
    val AvatarSm = 32.dp
    val AvatarMd = 40.dp
    val AvatarLg = 56.dp
    val TabBar = 62.dp
    val AddButton = 52.dp

    /** `stroke/hairline`: 1 dp (3 px on a 3× screen), never a 1-px line. */
    val Hairline = 1.dp
}
