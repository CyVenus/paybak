import Foundation
import os
import RevenueCat

/// Paybak Pro from the store: the anonymous customer's RevenueCat `CustomerInfo`, kept current by
/// `customerInfoStream`. Pro is the active `paybak_pro` entitlement; the app mirrors it into
/// `LedgerStore.isPro`, which every gate reads.
@Observable
final class SubscriptionStore {
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Purchases")

    private(set) var customerInfo: CustomerInfo?

    var proEntitlement: EntitlementInfo? {
        customerInfo?.entitlements[RevenueCatConfig.entitlementID]
    }

    var isPro: Bool {
        proEntitlement?.isActive == true
    }

    /// Configures the SDK once, at launch, before anything reads it. Skipped without a key.
    static func configure() {
        guard !RevenueCatConfig.apiKey.isEmpty else {
            log.error("No RevenueCat API key for this build; Pro stays locked.")
            return
        }
        #if DEBUG
        Purchases.logLevel = .debug
        #endif
        Purchases.configure(withAPIKey: RevenueCatConfig.apiKey)
    }

    /// Follows the entitlements for the app's lifetime: the stream yields the cached info first, then
    /// every change (a purchase, a restore, a renewal, an expiry).
    func observe() async {
        guard Purchases.isConfigured else { return }
        for await info in Purchases.shared.customerInfoStream {
            customerInfo = info
        }
    }
}
