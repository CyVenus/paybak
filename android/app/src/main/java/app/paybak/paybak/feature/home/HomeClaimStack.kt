package app.paybak.paybak.feature.home

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
import app.paybak.paybak.domain.calc.ClaimCard
import app.paybak.paybak.feature.PendingClaimCard
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/** The collapse after Confirm or Not received (home-v2 §3.9): 250 ms, ease out. */
private const val COLLAPSE_MILLIS = 250

/** Between stacked claims; with the gap under the stack they make the 24 dp section gap. */
private val ClaimGap = PbSpace.S12

/**
 * The Confirm cards above the balances, one per pending claim, newest first (home-v2 §3.11). A
 * claim that leaves the ledger's pending list (confirmed anywhere, or marked not received) stays
 * in place and collapses away while the content below moves up; a new claim grows in. Under reduce
 * motion both only fade.
 */
@Composable
internal fun HomeClaimStack(claims: List<ClaimCard>) {
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
    val enter = claimEnter(reduceMotion)
    val exit = claimExit(reduceMotion)
    Column {
        shown.forEach { claim ->
            val id = claim.payment.id
            key(id) {
                val visibility = remember { MutableTransitionState(id in firstIds) }
                visibility.targetState = id in live
                AnimatedVisibility(visibility, enter = enter, exit = exit) {
                    PendingClaimCard(claim, Modifier.padding(bottom = ClaimGap))
                }
                if (visibility.isIdle && !visibility.currentState && id !in live) {
                    LaunchedEffect(Unit) { shown.removeAll { it.payment.id == id } }
                }
            }
        }
        AnimatedVisibility(live.isNotEmpty(), enter = enter, exit = exit) {
            Spacer(Modifier.height(ClaimGap))
        }
    }
}

private fun claimEnter(reduceMotion: Boolean): EnterTransition {
    val fade = fadeIn(tween(COLLAPSE_MILLIS))
    return if (reduceMotion) fade
    else fade + expandVertically(tween(COLLAPSE_MILLIS, easing = PbMotion.EaseOut))
}

private fun claimExit(reduceMotion: Boolean): ExitTransition {
    val fade = fadeOut(tween(COLLAPSE_MILLIS))
    return if (reduceMotion) fade
    else fade + shrinkVertically(tween(COLLAPSE_MILLIS, easing = PbMotion.EaseOut))
}
