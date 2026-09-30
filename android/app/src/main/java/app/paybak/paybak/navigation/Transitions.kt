package app.paybak.paybak.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import app.paybak.paybak.ui.theme.PbMotion

/**
 * Figma's PUSH LEFT (350 ms, ease-in-out): the new content slides in from the right edge over the
 * old, which drifts a third of the way left.
 */
fun pushTransition(): ContentTransform {
    val slide = tween<IntOffset>(PbMotion.PUSH_MILLIS, easing = PbMotion.EaseInOut)
    return slideInHorizontally(slide) { it } togetherWith slideOutHorizontally(slide) { -it / 3 }
}

/** The reverse of [pushTransition]: the old content slides off to the right, uncovering the new. */
fun popTransition(): ContentTransform {
    val slide = tween<IntOffset>(PbMotion.PUSH_MILLIS, easing = PbMotion.EaseInOut)
    return (slideInHorizontally(slide) { -it / 3 } togetherWith slideOutHorizontally(slide) { it })
        .apply { targetContentZIndex = -1f }
}
