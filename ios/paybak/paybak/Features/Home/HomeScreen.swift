import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane D replaces this file with the real Home and keeps this
// initializer. It already derives its state from the ledger, so onboarding's UI tests find
// `screen.homeFirstDay` after All set.
/// Home: First day, Active, All settled, and pending claims above the balances (screens-home-v2).
struct HomeScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        let home = ledgerStore.snapshot.home
        RoutePlaceholder(
            route: .home,
            title: Greeting.text(firstName: profileStore.profile.firstName, at: ledgerStore.clock.now, calendar: ledgerStore.clock.calendar),
            owner: .d,
            spec: "screens-home-v2 §2–3, screens-home §2–4",
            screenID: "screen.\(screenID)",
            details: [
                "You’re owed \(Money.format(home.totals.owed, sign: .signed)) \(home.totals.owedCaption)",
                "You owe \(Money.format(-home.totals.owe, sign: .signed)) \(home.totals.oweCaption)",
                "Due soon: " + home.dueSoon.map { "\($0.title) \(Money.format($0.amount)) \($0.badge)" }.joined(separator: " · "),
                "Recent: " + home.recent.map(\.title).joined(separator: " · "),
                "Claims: " + home.pendingClaims.map(\.title).joined(separator: " · "),
            ],
            accessory: AnyView(logo)
        )
        .overlay(alignment: .topLeading) {
            Color.clear
                .frame(width: 1, height: 1)
                .accessibilityElement()
                .accessibilityLabel(stateID)
                .accessibilityIdentifier("home.state.\(stateID)")
        }
    }

    /// Long-press the logo for the debug menu (debug builds).
    private var logo: some View {
        PBLogo()
            .frame(height: PBSize.tap)
            .accessibilityIdentifier("home.logo")
            .onLongPressGesture {
                #if DEBUG
                router.open(.debugMenu)
                #endif
            }
    }

    private var stateID: String {
        let home = ledgerStore.snapshot.home
        if !home.pendingClaims.isEmpty { return "confirmPayment" }
        return home.state.rawValue
    }

    private var screenID: String {
        switch stateID {
        case "firstDay": "homeFirstDay"
        case "allSettled": "homeAllSettled"
        case "confirmPayment": "homeConfirmPayment"
        default: "homeActive"
        }
    }
}
