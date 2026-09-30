import SwiftUI

/// Home, the first tab (screens-home, screens-home-v2): the header and greeting, then what the ledger
/// calls for. First day (a new account), Active (balances, Due soon, Recent activity, with a Confirm
/// card on top for each payment a friend says they made), or All settled. Content scrolls under the
/// glass tab bar and its fade.
struct HomeScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// After Confirm, Home keeps showing the summary from before the confirm while the card plays
    /// its Confirmed state, then collapses into the new numbers (home-v2 §3.9).
    @State private var heldSummary: HomeSummary?
    /// The latest hold: a second Confirm during a hold restarts it.
    @State private var holdID = UUID()

    /// The last row scrolls fully above the tab bar: its top (21 + 62 from the bottom) plus 24.
    private static let bottomInset = PBTabBar.bottomOffset + PBSize.tabbar + PBLayout.sectionGap

    var body: some View {
        let home = heldSummary ?? ledgerStore.snapshot.home
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                header(home)
                content(home)
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, Self.bottomInset)
            .phoneContentWidth()
        }
        .scrollIndicators(.hidden)
        .ignoresSafeArea(.container, edges: .bottom)
        .overlay(alignment: .bottom) {
            if home.state == .active {
                PBScrollEdgeFade(height: home.pendingClaims.isEmpty ? 150 : 118)
                    .ignoresSafeArea(.container, edges: .bottom)
            }
        }
        .overlay(alignment: .top) {
            // Scrolled rows pass under a solid status bar, not the clock.
            Color.clear
                .frame(height: 0)
                .background(PBColor.bgPrimary, ignoresSafeAreaEdges: .top)
        }
        .background(PBColor.bgPrimary)
        .screenIdentifier(home.screen)
        .overlay(alignment: .topLeading) {
            Color.clear
                .frame(width: 1, height: 1)
                .accessibilityElement()
                .accessibilityLabel(home.stateID)
                .accessibilityIdentifier("home.state.\(home.stateID)")
        }
    }

    private func header(_ home: HomeSummary) -> some View {
        TimelineView(.everyMinute) { _ in
            PBHomeHeader(
                greeting: Greeting.text(firstName: profileStore.profile.firstName, at: ledgerStore.clock.now,
                                        calendar: ledgerStore.clock.calendar),
                hasUnread: home.hasUnread,
                onAssistant: { router.requirePro(.ask) },
                onBell: { router.open(.notifications) },
                onLogoLongPress: debugMenu
            )
        }
    }

    @ViewBuilder
    private func content(_ home: HomeSummary) -> some View {
        switch home.state {
        case .firstDay:
            PBEmptyState(
                primary: .init(title: "Add expense", icon: .plus, testID: "home.firstDay.addExpense") {
                    router.open(.addExpense(.new))
                },
                secondary: .init(title: "Invite friends", icon: .userAdd, testID: "home.firstDay.invite") {
                    router.open(.addFriend)
                }
            )
        case .allSettled:
            PBEmptyState.allSettled
        case .active:
            HomeActiveContent(home: home) {
                hold(home)
            }
        }
    }

    /// Holds `before` (the summary the card was confirmed on) through the Confirmed state and a short
    /// read, then lets the card collapse and the new balances and activity row in.
    private func hold(_ before: HomeSummary) {
        let id = UUID()
        holdID = id
        heldSummary = before
        Task {
            // The card's 250 ms Confirmed animation, then 0.8 s to read it.
            try? await Task.sleep(for: .milliseconds(reduceMotion ? 800 : 1050))
            guard holdID == id else { return }
            var transaction = Transaction(animation: reduceMotion ? nil : .easeOut(duration: 0.25))
            transaction.disablesAnimations = reduceMotion
            withTransaction(transaction) {
                heldSummary = nil
            }
        }
    }

    /// Long-pressing the logo opens the debug menu in debug builds.
    private var debugMenu: (() -> Void)? {
        #if DEBUG
        { router.open(.debugMenu) }
        #else
        nil
        #endif
    }
}
