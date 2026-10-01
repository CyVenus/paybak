package app.paybak.paybak.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import app.paybak.paybak.billing.SubscriptionRepository
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.AppClock

/** The main navigator, for every screen inside the main root. */
val LocalMainNavigator =
    staticCompositionLocalOf<MainNavigator> { error("No MainNavigator provided") }

/** The ledger store. */
val LocalLedger =
    staticCompositionLocalOf<LedgerRepository> { error("No LedgerRepository provided") }

/** Paybak Pro from the store (RevenueCat). */
val LocalSubscriptions =
    staticCompositionLocalOf<SubscriptionRepository> { error("No SubscriptionRepository provided") }

/** The app clock (real or pinned). */
val LocalAppClock = staticCompositionLocalOf<AppClock> { error("No AppClock provided") }

/** The profile store (identity, default currency, payment methods). */
val LocalProfileStore = staticCompositionLocalOf<ProfileStore> { error("No ProfileStore provided") }

/**
 * The bottom padding tab roots give their scrolling content, so it clears the floating tab bar (tab
 * bar + its offset + 24; app-architecture §2.3).
 */
val LocalTabBarPadding = compositionLocalOf { PaddingValues() }

/**
 * The debug start-screen id (`--es startScreen <id>`) for the screen that owns its in-screen state
 * (a prefilled form, an open local sheet). Null in release builds.
 */
val LocalDebugStartScreen = staticCompositionLocalOf<DebugStartScreen?> { null }

/** Hands the start id to the first screen that owns it, once. */
class DebugStartScreen(val id: String) {
    private var consumed = false

    fun consume(ids: Set<String>): String? =
        if (!consumed && id in ids) {
            consumed = true
            id
        } else {
            null
        }
}

/**
 * The debug start id if it is one of [ids] (this screen's designed states), only the first time the
 * screen appears; null otherwise and in release builds. Apply the state it names once.
 */
@Composable
fun rememberDebugStartScreen(vararg ids: String): String? {
    val start = LocalDebugStartScreen.current
    return rememberSaveable { mutableStateOf(start?.consume(ids.toSet())) }.value
}
