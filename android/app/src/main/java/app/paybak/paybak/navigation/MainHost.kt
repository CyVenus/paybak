package app.paybak.paybak.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbToastHost
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize

/** Modals slide up in 300 ms, ease-out (app-architecture §7.2). */
private const val MODAL_MILLIS = 300

/** Toasts sit 16 dp above the tab bar on tab roots and 50 dp above the bottom elsewhere. */
private val ToastAboveTabBar = 16.dp
private val ToastAboveBottom = 50.dp

/**
 * The main root (app-architecture §2.8): the top screen of [navigator]'s stack with its push or
 * modal transition, the route sheet over it and the app's toast. Every entry and tab root keeps its
 * saved state while it's covered; system back follows §2.5.
 */
@Composable
fun MainHost(navigator: MainNavigator) {
    val stateHolder = rememberSaveableStateHolder()
    val reduceMotion = LocalReduceMotion.current
    BackHandler(enabled = navigator.handlesBack) { navigator.back() }
    ForgetClosedEntries(navigator) { stateHolder.removeState(it) }

    Box(Modifier.fillMaxSize().background(PbColors.Bg.Primary)) {
        AnimatedContent(
            targetState = navigator.screen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { transitionFor(navigator.motion, reduceMotion) },
            contentKey = { it.key },
            label = "MainHost",
        ) { entry ->
            stateHolder.SaveableStateProvider(entry.key) {
                if (entry.route == Route.Tabs) TabShell(navigator, stateHolder)
                else RouteContent(entry.route)
            }
        }
        navigator.sheet?.let { sheet ->
            stateHolder.SaveableStateProvider(sheet.key) { RouteContent(sheet.route) }
        }
        val bottom =
            if (navigator.onTabRoot) tabBarBottomOffset() + PbSize.TabBar + ToastAboveTabBar
            else ToastAboveBottom
        PbToastHost(
            navigator.toasts,
            Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(bottom = bottom),
        )
    }
}

/** Drops the saved state of entries that left the stack for good. */
@Composable
private fun ForgetClosedEntries(navigator: MainNavigator, forget: (String) -> Unit) {
    val known = remember { mutableSetOf<String>() }
    val keys = navigator.entries.map { it.key }.toSet()
    LaunchedEffect(keys) {
        (known - keys).forEach(forget)
        known.clear()
        known += keys
    }
}

private fun transitionFor(motion: NavMotion, reduceMotion: Boolean): ContentTransform {
    if (reduceMotion) {
        return fadeIn(tween(PbMotion.SWAP_MILLIS)) togetherWith fadeOut(tween(PbMotion.SWAP_MILLIS))
    }
    val slide = tween<IntOffset>(MODAL_MILLIS, easing = PbMotion.EaseOut)
    return when (motion) {
        NavMotion.Push -> pushTransition()
        NavMotion.Pop -> popTransition()
        // The screen below stays in place (fully visible) while the modal covers it.
        NavMotion.ModalUp ->
            slideInVertically(slide) { it } togetherWith
                fadeOut(tween(MODAL_MILLIS), targetAlpha = 1f)
        NavMotion.ModalDown ->
            (EnterTransition.None togetherWith slideOutVertically(slide) { it }).apply {
                targetContentZIndex = -1f
            }
        NavMotion.None -> EnterTransition.None togetherWith ExitTransition.None
    }
}
