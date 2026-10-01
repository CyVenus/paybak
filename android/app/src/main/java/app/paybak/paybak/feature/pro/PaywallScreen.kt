package app.paybak.paybak.feature.pro

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.billing.activePro
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalSubscriptions
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenterOptions

/** The paywall's purchase → Welcome step (Figma DISSOLVE 300 ms). */
private const val WELCOME_FADE_MILLIS = 300

private const val TAG = "Paywall"

/**
 * The `paywall` route (screens-settings §2–3): the RevenueCat paywall for the current offering
 * (designed in the RevenueCat dashboard), then the Welcome once a purchase or restore unlocks
 * `paybak_pro`. A Pro member opening it sees the Welcome as their plan's status, with Manage
 * subscription opening RevenueCat's Customer Center. Welcome's Done (and system back there)
 * closes the modal and continues to [Route.Paywall]'s `continueTo`, the feature that asked for Pro.
 */
@Composable
fun PaywallScreen(route: Route.Paywall) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val storeEntitlement by LocalSubscriptions.current.proEntitlement.collectAsState()
    val haptics = rememberHaptics()
    var welcome by rememberSaveable { mutableStateOf(snapshot.isPro) }
    var nothingToRestore by rememberSaveable { mutableStateOf(false) }
    var managing by rememberSaveable { mutableStateOf(false) }
    // Pro can also arrive outside the paywall's callbacks (the store's update listener, the debug
    // menu): the paywall moves on to the Welcome either way.
    LaunchedEffect(snapshot.isPro) { if (snapshot.isPro) welcome = true }
    // Back closes the paywall (as its own close button does) or, on the Welcome, finishes it.
    // Customer Center handles back itself.
    BackHandler(enabled = !managing) {
        if (welcome) navigator.finishPaywall() else navigator.dismissModal()
    }

    Box(Modifier.fillMaxSize()) {
        Crossfade(welcome, animationSpec = tween(WELCOME_FADE_MILLIS), label = "Paywall") {
            showWelcome ->
            if (showWelcome) {
                ProWelcome(
                    status =
                        storeEntitlement?.status(snapshot.view.zone)
                            ?: snapshot.ledger.settings.entitlement.status(
                                snapshot.view.today,
                                snapshot.view.zone,
                            ),
                    onDone = navigator::finishPaywall,
                    onManage = if (storeEntitlement != null) ({ managing = true }) else null,
                )
            } else {
                StorePaywall(
                    // RevenueCatUI also asks to close right after a purchase: stay for the Welcome.
                    onDismiss = { if (!welcome) navigator.dismissModal() },
                    onUnlocked = {
                        haptics.perform(HapticKind.Success)
                        welcome = true
                    },
                    onNothingToRestore = { nothingToRestore = true },
                )
            }
        }
        if (managing) {
            CustomerCenter(
                modifier = Modifier.fillMaxSize().testTag("customerCenter"),
                options = remember { CustomerCenterOptions.Builder().build() },
                onDismiss = { managing = false },
            )
        }
    }
    if (nothingToRestore) {
        ProNotice(stringResource(R.string.settings_pro_nothing_to_restore)) {
            nothingToRestore = false
        }
    }
}

/**
 * RevenueCatUI's paywall for the current offering. It buys and restores through the Purchases SDK
 * and shows its own errors; its close button calls [onDismiss]; [onUnlocked] runs once
 * `paybak_pro` is active.
 */
@Composable
private fun StorePaywall(
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit,
    onNothingToRestore: () -> Unit,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    val unlocked by rememberUpdatedState(onUnlocked)
    val nothingToRestore by rememberUpdatedState(onNothingToRestore)
    val options =
        remember {
            PaywallOptions.Builder(dismissRequest = { dismiss() })
                .setShouldDisplayDismissButton(true)
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseCompleted(
                            customerInfo: CustomerInfo,
                            storeTransaction: StoreTransaction,
                        ) {
                            if (customerInfo.activePro != null) unlocked()
                        }

                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            if (customerInfo.activePro != null) unlocked() else nothingToRestore()
                        }

                        override fun onPurchaseError(error: PurchasesError) {
                            Log.w(TAG, "Purchase failed: $error")
                        }

                        override fun onRestoreError(error: PurchasesError) {
                            Log.w(TAG, "Restore failed: $error")
                        }
                    }
                )
                .build()
        }
    Box(Modifier.fillMaxSize().testTag("screen.paywall")) { Paywall(options) }
}
