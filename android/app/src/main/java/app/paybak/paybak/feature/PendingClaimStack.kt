package app.paybak.paybak.feature

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import app.paybak.paybak.domain.calc.ClaimCard
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbMotion

/** The collapse after Confirm or Not received (home-v2 §3.9, activity §3.6): 250 ms, ease out. */
private const val COLLAPSE_MILLIS = 250

/**
 * The Confirm cards for the pending claims, newest first, each [gap] above the next (Home 12 dp,
 * Activity and the inbox 8 dp). A claim that leaves the ledger's pending list (confirmed anywhere,
 * or marked not received) stays in place while its card finishes, then collapses while the content
 * below moves up; a new claim grows in. Under reduce motion both only fade. With [gapBelow] the
 * stack ends with one more [gap] while it shows any card (Home's section spacing).
 */
@Composable
fun PendingClaimStack(claims: List<ClaimCard>, gap: Dp, gapBelow: Boolean = false) {
    val live = claims.map { it.payment.id }.toSet()
    val firstIds = remember { live }
    val shown = remember { mutableStateListOf(*claims.toTypedArray()) }
    LaunchedEffect(claims) {
        claims.forEachIndexed { index, claim ->
            val at = shown.indexOfFirst { it.payment.id == claim.payment.id }
            if (at >= 0) shown[at] = claim else shown.add(minOf(index, shown.size), claim)
        }
    }
    val reduceMotion = LocalReduceMotion.current
    val size = tween<IntSize>(COLLAPSE_MILLIS, easing = PbMotion.EaseOut)
    val enter =
        fadeIn(tween(COLLAPSE_MILLIS)) +
            if (reduceMotion) EnterTransition.None else expandVertically(size)
    val exit =
        fadeOut(tween(COLLAPSE_MILLIS)) +
            if (reduceMotion) ExitTransition.None else shrinkVertically(size)
    Column {
        shown.forEach { claim ->
            val id = claim.payment.id
            key(id) {
                val visibility = remember { MutableTransitionState(id in firstIds) }
                visibility.targetState = id in live
                AnimatedVisibility(visibility, enter = enter, exit = exit) {
                    PendingClaimCard(claim, Modifier.padding(bottom = gap))
                }
                if (visibility.isIdle && !visibility.currentState && id !in live) {
                    LaunchedEffect(Unit) { shown.removeAll { it.payment.id == id } }
                }
            }
        }
        if (gapBelow) {
            AnimatedVisibility(live.isNotEmpty(), enter = enter, exit = exit) {
                Spacer(Modifier.height(gap))
            }
        }
    }
}
