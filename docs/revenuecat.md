# RevenueCat in Paybak

Paybak sells one subscription, **Paybak Pro**, through RevenueCat on iOS and Android. This guide covers the dashboard setup, the code on both platforms, and how to move from the Test Store to the real stores.

## 1. Dashboard setup

Project: **Paybak**.

| Step | What we created |
| --- | --- |
| App | A **Test Store** app. It works immediately, with no App Store Connect or Google Play setup. |
| Products | `monthly` (1 month, $9.99) and `yearly` (1 year, $79.99) |
| Entitlement | `paybak_pro` ("Paybak Pro"), with both products attached |
| Offering | `default`, marked **current**, with packages `$rc_monthly` → `monthly` and `$rc_annual` → `yearly` |
| Paywall | Built in the Paywalls editor for the `default` offering: the five Pro features, the two plans, restore, terms and privacy |
| Customer Center | Default configuration (editable under **Customer Center** in the dashboard) |

The app never hard-codes product IDs or prices. It asks for the **current offering** and checks the **entitlement**, so plans, prices and the paywall can change in the dashboard without an app update.

## 2. Install the SDK

**iOS (Swift Package Manager).** The Xcode project depends on `https://github.com/RevenueCat/purchases-ios-spm.git`, pinned to an exact version, and links two products:

- `RevenueCat`: purchases, offerings, customer info
- `RevenueCatUI`: `PaywallView` and Customer Center

**Android (Gradle).** In `android/gradle/libs.versions.toml`:

```toml
revenuecat = "10.24.0"
revenuecat-purchases = { group = "com.revenuecat.purchases", name = "purchases", version.ref = "revenuecat" }
revenuecat-purchases-ui = { group = "com.revenuecat.purchases", name = "purchases-ui", version.ref = "revenuecat" }
```

The SDK's manifest adds the `INTERNET` permission. Google Play Billing comes in as a transitive dependency.

## 3. Configure once at launch

Debug builds use the **Test Store** API key. RevenueCat public API keys are designed to ship inside apps, and Test Store purchases never charge real money, so the key is committed and anyone who clones the repo can try the purchase flow.

The SDK **deliberately crashes a release build** that is configured with a Test Store key. So release builds read a separate store key, which is empty in this repo. When it's empty, the SDK isn't configured and the app simply stays on the free plan.

**iOS**: `Purchases/RevenueCatConfig.swift` holds the keys. `SubscriptionStore.configure()` runs first in `PaybakApp.init()`:

```swift
enum RevenueCatConfig {
    #if DEBUG
    static let apiKey = "test_…"   // RevenueCat Test Store
    #else
    static let apiKey = ""         // your appl_ key for App Store builds
    #endif
    static let entitlementID = "paybak_pro"
}

static func configure() {
    guard !RevenueCatConfig.apiKey.isEmpty else { return }   // Pro stays locked
    #if DEBUG
    Purchases.logLevel = .debug
    #endif
    Purchases.configure(withAPIKey: RevenueCatConfig.apiKey)
}
```

**Android**: `app/build.gradle.kts` sets the key per build type. `RevenueCatConfig.configure()` (in `billing/SubscriptionRepository.kt`) runs first in `PaybakApplication.onCreate()`:

```kotlin
buildTypes {
    debug { buildConfigField("String", "REVENUECAT_API_KEY", "\"test_…\"") }
    // goog_ key from `revenuecat.playKey` in the untracked local.properties
    release { buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProperty("revenuecat.playKey")}\"") }
}

fun configure(context: Context) {
    val apiKey = BuildConfig.REVENUECAT_API_KEY
    if (apiKey.isBlank()) return                     // Pro stays locked
    if (BuildConfig.DEBUG) Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
}
```

**User IDs.** Paybak has no accounts and no server, so it uses RevenueCat's **anonymous app user IDs** and never calls `logIn`. Restoring purchases moves a subscription to the current anonymous user, for example after a reinstall.

## 4. Customer info and entitlement checks

RevenueCat is the single source of truth for Pro. One small observable store per platform keeps the latest `CustomerInfo` and derives `isPro` from the entitlement:

**iOS** (`SubscriptionStore`, an `@Observable` class):

```swift
func observe() async {
    guard Purchases.isConfigured else { return }
    for await info in Purchases.shared.customerInfoStream {
        customerInfo = info
    }
}

var proEntitlement: EntitlementInfo? { customerInfo?.entitlements[RevenueCatConfig.entitlementID] }
var isPro: Bool { proEntitlement?.isActive == true }
```

**Android** (`SubscriptionRepository`, `StateFlow`-based):

```kotlin
val CustomerInfo.activePro: EntitlementInfo?
    get() = entitlements[RevenueCatConfig.ENTITLEMENT_ID]?.takeIf { it.isActive }

init {
    if (Purchases.isConfigured) {
        val purchases = Purchases.sharedInstance
        purchases.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { customerInfoState.value = it }
        scope.launch {
            try { customerInfoState.value = purchases.awaitCustomerInfo() }
            catch (e: PurchasesException) { Log.w(TAG, "Couldn't fetch customer info: ${e.error}") }
        }
    }
}

val proEntitlement = customerInfo.map { it?.activePro }.stateIn(scope, SharingStarted.Eagerly, null)
val isPro = proEntitlement.map { it != null }.stateIn(scope, SharingStarted.Eagerly, false)
```

Both platforms feed this value into the ledger store (`LedgerStore.hasStoreEntitlement` on iOS, the `storePro` flow on Android). As a result, every existing screen that reads `isPro` updates live.

The stream and the listener fire with the SDK's cached value right away, and again after every purchase, restore, renewal or expiry. The UI never polls, and Pro survives relaunches offline because the SDK caches `CustomerInfo`.

Every Pro feature goes through one gate, `requirePro(route)`. It opens the feature when `isPro` is true, and otherwise opens the paywall with the feature as the place to continue to afterwards.

Debug builds also keep a local "Toggle Pro" switch, the `-pro` launch argument and demo data, so screenshots and UI tests can show Pro screens without buying. This override is compiled out of release builds.

## 5. The paywall

The paywall is designed in the RevenueCat dashboard and rendered natively by RevenueCatUI. That's why it isn't built in app code: its copy, layout and plans can change without a release.

**iOS:**

```swift
PaywallView()
    .onPurchaseCompleted { info in
        if info.entitlements[RevenueCatConfig.entitlementID]?.isActive == true { showWelcome() }
    }
    .onRestoreCompleted { info in
        if info.entitlements[RevenueCatConfig.entitlementID]?.isActive == true { showWelcome() }
        else { isNothingToRestorePresented = true }
    }
    // The paywall's ✕, and the SDK's own dismissal right after a purchase (Welcome replaces that)
    .onRequestedDismissal { if showsWelcome != true { router.dismissModal() } }
```

**Android:**

```kotlin
Paywall(
    // RevenueCatUI also asks to close right after a purchase: stay for the Welcome
    PaywallOptions.Builder(dismissRequest = { if (!welcome) navigator.dismissModal() })
        .setShouldDisplayDismissButton(true)
        .setListener(object : PaywallListener {
            override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) { … }
            override fun onRestoreCompleted(customerInfo: CustomerInfo) { … }
        })
        .build()
)
```

After a successful purchase, Paybak shows its own welcome screen. "Done" then continues to the feature that opened the paywall.

## 6. Customer Center

Pro members who open **Profile › Paybak Pro** see their plan status, which comes from `EntitlementInfo`: `periodType`, `expirationDate` and `willRenew`. They also get a **Manage subscription** button:

- iOS: `.presentCustomerCenter(isPresented: $showsCustomerCenter)`
- Android: the `CustomerCenter(options = CustomerCenterOptions.Builder().build(), onDismiss = …)` composable

Customer Center handles cancellation, plan changes, refunds (on iOS) and restore, all configured in the dashboard.

## 7. Error handling

- **Purchase and restore errors** show inside the RevenueCat paywall, and are also logged. A **cancelled** purchase is not an error and shows nothing.
- **Restore with nothing to restore** shows Paybak's "No purchases to restore." notice.
- **Offline or not configured.** Every `Purchases.shared` / `Purchases.sharedInstance` access is guarded by `Purchases.isConfigured`. Without the SDK the app stays on the free plan and keeps working, because the core ledger never needs the network.
- **Debug logging** (`Purchases.logLevel = .debug`) is enabled only in debug builds.

## 8. Testing with the Test Store

1. Run a **debug** build on iOS or Android.
2. Open any Pro feature, or **Profile › Paybak Pro**.
3. Pick a plan. Instead of the App Store or Play sheet, the Test Store shows a dialog with the product details and three outcomes: a successful purchase, a failed purchase, or cancel. Try all three.
4. After a successful purchase the feature unlocks. The purchase appears in the RevenueCat dashboard under **Customers** as sandbox data.

## 9. Going to production

1. In the RevenueCat project, add an **App Store** app (bundle ID `app.paybak.paybak`) and a **Play Store** app (package `app.paybak.paybak`), with their store credentials.
2. Create the `monthly` and `yearly` subscriptions in App Store Connect and Google Play Console, import them into RevenueCat, attach them to `paybak_pro`, and add them to the `$rc_monthly` / `$rc_annual` packages of the `default` offering.
3. Put the platform public keys into the **release** configuration:
   - iOS: the `#else` branch of `RevenueCatConfig.apiKey` (`appl_…`)
   - Android: `revenuecat.playKey=goog_…` in `android/local.properties` (untracked), which the `release` build reads
4. Keep the Test Store key in debug builds for day-to-day testing. The SDK refuses to run a Test Store key in a release build, so it can't reach the stores by accident.

## Best practices we followed

- Check **entitlements**, never product IDs, so plans can change without code changes.
- Use the **current offering** and a dashboard paywall, so prices and copy come from the store and can be tested remotely.
- React to **`CustomerInfo` updates** (stream or listener) instead of polling, and treat RevenueCat as the source of truth rather than storing a local "is Pro" flag.
- **Configure once, early**, and guard every SDK access.
- Keep **Test Store keys out of release builds**, and use per-build-type keys.
- Always give users a **restore** path and a way to **manage** their subscription (Customer Center).
