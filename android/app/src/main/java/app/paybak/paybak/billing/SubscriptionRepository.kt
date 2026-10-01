package app.paybak.paybak.billing

import android.content.Context
import android.util.Log
import app.paybak.paybak.BuildConfig
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How Paybak talks to RevenueCat. */
object RevenueCatConfig {
    /** The entitlement both Pro plans (monthly, yearly) unlock in the RevenueCat dashboard. */
    const val ENTITLEMENT_ID = "paybak_pro"

    /**
     * Configures the Purchases SDK once, at launch, with an anonymous app user id (Paybak has no
     * accounts). Debug builds use the RevenueCat Test Store; a release build without a Play key
     * skips it, and Pro stays locked.
     */
    fun configure(context: Context) {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) return
        if (BuildConfig.DEBUG) Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
    }
}

/** The `paybak_pro` entitlement while it is active (a trial counts); null otherwise. */
val CustomerInfo.activePro: EntitlementInfo?
    get() = entitlements[RevenueCatConfig.ENTITLEMENT_ID]?.takeIf { it.isActive }

/**
 * Paybak Pro as the store sees it: RevenueCat's [CustomerInfo], kept current by the SDK's update
 * listener (purchases, restores, renewals, expiries), and the `paybak_pro` entitlement while it is
 * active. Everything stays empty when Purchases isn't configured.
 */
class SubscriptionRepository(scope: CoroutineScope) {
    private val customerInfoState = MutableStateFlow<CustomerInfo?>(null)

    /** The latest customer info, null until the first fetch (or without RevenueCat). */
    val customerInfo: StateFlow<CustomerInfo?> = customerInfoState.asStateFlow()

    /** The active Pro entitlement: its period type, renewal and expiry. Null when not Pro. */
    val proEntitlement: StateFlow<EntitlementInfo?> =
        customerInfo.map { it?.activePro }.stateIn(scope, SharingStarted.Eagerly, null)

    /** The store unlocks Pro (a trial counts). */
    val isPro: StateFlow<Boolean> =
        proEntitlement.map { it != null }.stateIn(scope, SharingStarted.Eagerly, false)

    init {
        if (Purchases.isConfigured) {
            val purchases = Purchases.sharedInstance
            purchases.updatedCustomerInfoListener =
                UpdatedCustomerInfoListener { customerInfoState.value = it }
            scope.launch {
                try {
                    customerInfoState.value = purchases.awaitCustomerInfo()
                } catch (e: PurchasesException) {
                    Log.w(TAG, "Couldn't fetch customer info: ${e.error}")
                }
            }
        }
    }

    private companion object {
        const val TAG = "Subscriptions"
    }
}
