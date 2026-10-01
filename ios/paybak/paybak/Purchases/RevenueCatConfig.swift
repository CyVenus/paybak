import Foundation

/// RevenueCat project settings. Purchases are anonymous (no account, no `logIn`): RevenueCat keys the
/// customer to an app user ID it generates on the device.
enum RevenueCatConfig {
    #if DEBUG
    /// The Test Store key: debug builds buy from RevenueCat's Test Store, no App Store account needed.
    static let apiKey = "test_ZodzFFhityYkLvLvsuepQmsAeqo"
    #else
    /// Put the project's App Store key (`appl_…`) here for App Store builds. Test Store keys crash
    /// release builds on purpose; while this is empty the SDK stays off and Pro stays locked.
    static let apiKey = ""
    #endif

    /// The entitlement both Pro products (`monthly`, `yearly`) unlock.
    static let entitlementID = "paybak_pro"
}
