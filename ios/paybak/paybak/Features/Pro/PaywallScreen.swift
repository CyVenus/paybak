import RevenueCat
import RevenueCatUI
import SwiftUI

/// Paybak Pro (screens-settings §2–3): RevenueCat's paywall for the current offering (its plans,
/// prices, trial and restore come from the dashboard), then Welcome in the same modal once a purchase
/// or restore unlocks `paybak_pro`. Opened while already Pro (the Profile row), it shows Welcome as
/// the plan's status. Welcome's Done continues to the feature that asked (`continueTo`), or just closes.
struct PaywallScreen: View {
    let continueTo: Route?

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(SubscriptionStore.self) private var subscriptionStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    @State private var showsWelcome: Bool?
    @State private var isNothingToRestorePresented = false

    var body: some View {
        ZStack {
            if showsWelcome ?? ledgerStore.isPro {
                ProWelcomeView(statusLine: statusLine, managesSubscription: subscriptionStore.isPro,
                               onDone: router.finishPaywall)
                    .transition(.opacity)
                    .phoneContentWidth()
            } else {
                paywall
                    .transition(.opacity)
            }
        }
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.paywall")
        .proNotice("No purchases to restore.", isPresented: $isNothingToRestorePresented)
    }

    @ViewBuilder
    private var paywall: some View {
        if Purchases.isConfigured {
            PaywallView()
                .onPurchaseCompleted { customerInfo in
                    guard unlocksPro(customerInfo) else { return }
                    Haptics.success()
                    showWelcome()
                }
                .onRestoreCompleted { customerInfo in
                    if unlocksPro(customerInfo) {
                        showWelcome()
                    } else {
                        isNothingToRestorePresented = true
                    }
                }
                // The paywall's ✕, and the SDK's own dismissal after a purchase (Welcome replaces that).
                .onRequestedDismissal {
                    if showsWelcome != true { router.dismissModal() }
                }
        } else {
            VStack(spacing: 0) {
                PBModalHeader(testIDPrefix: "paywall", onClose: router.dismissModal)
                Spacer()
                Text("Paybak Pro isn’t available in this build.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                Spacer()
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
        }
    }

    private func unlocksPro(_ customerInfo: CustomerInfo) -> Bool {
        customerInfo.entitlements[RevenueCatConfig.entitlementID]?.isActive == true
    }

    /// The plan's status under the Welcome title: the store subscription's, else (debug builds) the
    /// mock entitlement's; nil when neither has one.
    private var statusLine: String? {
        if subscriptionStore.isPro {
            return subscriptionStore.proEntitlement?.statusLine(calendar: ledgerStore.clock.calendar)
        }
        #if DEBUG
        return ledgerStore.ledger.settings.entitlement.statusLine(today: ledgerStore.clock.today,
                                                                   calendar: ledgerStore.clock.calendar)
        #else
        return nil
        #endif
    }

    private func showWelcome() {
        withAnimation(reduceMotion ? nil : .easeOut(duration: 0.3)) {
            showsWelcome = true
        }
    }
}

#Preview("PaywallScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-paywall")!)
    PaywallScreen(continueTo: nil)
        .environment(AppRouter())
        .environment(SubscriptionStore())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-paywall.json")), profileStore: profileStore))
}
