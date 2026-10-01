import SwiftUI

/// Paybak Pro (screens-settings §2–3): the paywall, then Welcome in the same modal once the mock
/// purchase flips the local entitlement. Yearly starts the 7-day trial, Monthly subscribes. Opened
/// while already Pro (the Profile row), it shows Welcome as the plan's status. Welcome's Done
/// continues to the feature that asked (`continueTo`), or just closes.
struct PaywallScreen: View {
    let continueTo: Route?

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    @State private var plan = ProPlan.yearly
    @State private var showsWelcome: Bool?
    @State private var isNothingToRestorePresented = false

    var body: some View {
        ZStack {
            if showsWelcome ?? ledgerStore.isPro {
                ProWelcomeView(statusLine: statusLine, onDone: router.finishPaywall)
                    .transition(.opacity)
            } else {
                paywall
                    .transition(.opacity)
            }
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.paywall")
        .proNotice("No purchases to restore.", isPresented: $isNothingToRestorePresented)
    }

    /// The pitch and the five features scroll under the ✕; the plans, the CTA, its small print and the
    /// legal links stay at the bottom, at least 24 pt below them.
    private var paywall: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 0) {
                    PBModalHeader(testIDPrefix: "paywall", onClose: router.dismissModal)
                    hero
                        .padding(.top, PBSpace.s4)
                    features
                        .padding(.top, PBSpace.s20)
                }
                .padding(.horizontal, PBLayout.screenMargin)
            }
            .scrollBounceBehavior(.basedOnSize)
            purchaseBlock
                .padding(.top, PBSpace.s24)
                .padding(.horizontal, PBLayout.screenMargin)
        }
    }

    private var hero: some View {
        VStack(spacing: PBSpace.s16) {
            PBAppMark(size: 96)
            VStack(spacing: PBSpace.s4) {
                Text("Paybak Pro")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                Text("Smart tools on top of your free ledger.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .multilineTextAlignment(.center)
        }
    }

    private var features: some View {
        VStack(spacing: 0) {
            ForEach(ProFeature.all) { feature in
                HStack(spacing: PBSpace.s12) {
                    PBIconView(feature.icon)
                        .foregroundStyle(PBColor.iconPrimary)
                        .frame(width: PBSize.tap, height: PBSize.tap)
                        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.tile))
                    VStack(alignment: .leading, spacing: PBSpace.s2) {
                        Text(feature.title)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                        Text(feature.subtitle)
                            .textStyle(.subheadline)
                            .foregroundStyle(PBColor.textSecondary)
                    }
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .frame(height: 56)
                .accessibilityElement(children: .combine)
            }
        }
    }

    private var purchaseBlock: some View {
        VStack(spacing: PBSpace.s16) {
            HStack(spacing: PBSpace.s20) {
                ForEach(ProPlan.allCases, id: \.self) { option in
                    PBPlanCard(period: option.name, price: option.price, detail: option.detail, badge: option.badge,
                               isSelected: plan == option) { plan = option }
                        .accessibilityIdentifier("paywall.plan.\(option == .yearly ? "yearly" : "monthly")")
                }
            }
            VStack(spacing: 10) {
                PBButton(plan.callToAction, fillsWidth: true, action: buy)
                    .accessibilityIdentifier("paywall.cta")
                VStack(spacing: 6) {
                    Text(plan.smallPrint)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .multilineTextAlignment(.center)
                        .accessibilityIdentifier("paywall.smallPrint")
                    HStack(spacing: PBSpace.s24) {
                        legalLink("Restore purchases", id: "paywall.restore", action: restore)
                        // No URLs yet (same as Get Started): tappable, nothing opens.
                        legalLink("Terms", id: "paywall.terms") {}
                        legalLink("Privacy", id: "paywall.privacy") {}
                    }
                }
            }
        }
        .padding(.bottom, PBSpace.s24)
    }

    /// Footnote links in `text/tertiary`, 18 pt tall with a 44 pt hit area; half opacity while pressed.
    private func legalLink(_ title: String, id: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textTertiary)
                .lineLimit(1)
                .frame(height: 18)
                .padding(.vertical, 13)
                .contentShape(.rect)
        }
        .buttonStyle(LegalLinkStyle())
        .padding(.vertical, -13)
        .accessibilityIdentifier(id)
    }

    /// The plan's status under the Welcome title; nil on the free plan.
    private var statusLine: String? {
        ledgerStore.ledger.settings.entitlement.statusLine(today: ledgerStore.clock.today, calendar: ledgerStore.clock.calendar)
    }

    /// The mock purchase: Yearly starts the trial, Monthly subscribes; a success tick, then Welcome
    /// cross-fades in.
    private func buy() {
        switch plan {
        case .yearly: ledgerStore.startTrial(.yearly)
        case .monthly: ledgerStore.subscribe(.monthly)
        }
        Haptics.success()
        showWelcome()
    }

    /// The purchase is simulated, so a saved entitlement is already on the device.
    private func restore() {
        ledgerStore.restorePurchases()
        if ledgerStore.isPro {
            showWelcome()
        } else {
            isNothingToRestorePresented = true
        }
    }

    private func showWelcome() {
        withAnimation(reduceMotion ? nil : .easeOut(duration: 0.3)) {
            showsWelcome = true
        }
    }
}

/// Text links fade to half while pressed (README rule 11).
private struct LegalLinkStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(configuration.isPressed ? 0.5 : 1)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
    }
}

#Preview("PaywallScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-paywall")!)
    PaywallScreen(continueTo: nil)
        .environment(AppRouter())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-paywall.json")), profileStore: profileStore))
}
