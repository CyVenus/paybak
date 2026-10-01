import SwiftUI

/// The Activity timeline under the header (screens-activity §3): a Confirm card for each payment a
/// friend says they made on top of Today, then every change by day, newest first. The list is clipped
/// 24 below the segments and scrolls under the tab bar. A new account shows the First day card.
struct ActivityTimelineView: View {
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// After Confirm, the list from before stays while the card plays its Confirmed state; then the
    /// card fades and folds away and the new "paid you" row takes its place (§3.6).
    @State private var held: Held?
    /// The latest hold: a second Confirm during a hold restarts it.
    @State private var holdID = UUID()

    private struct Held {
        let claims: [PendingClaim]
        let timeline: [TimelineDay]
    }

    var body: some View {
        let claims = held?.claims ?? ledgerStore.snapshot.home.pendingClaims
        let timeline = held?.timeline ?? ledgerStore.snapshot.timeline
        if claims.isEmpty && timeline.isEmpty {
            empty
        } else {
            list(claims: claims, timeline: timeline)
        }
    }

    private func list(claims: [PendingClaim], timeline: [TimelineDay]) -> some View {
        let books = ledgerStore.books
        return ScrollView {
            ActivityDayList(days: books.activityDays(withToday(timeline, claims: !claims.isEmpty, books: books)),
                            testIDPrefix: "activity") {
                if !claims.isEmpty {
                    VStack(spacing: PBSpace.s8) {
                        ForEach(claims) { claim in
                            PendingClaimCard(claim: claim, testIDPrefix: "activity.claim.\(claim.id)") {
                                hold(claims: claims, timeline: timeline)
                            }
                            .transition(.opacity)
                        }
                    }
                }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBTabBar.contentInset)
            .phoneContentWidth()
        }
        .scrollIndicators(.hidden)
        .padding(.top, PBSpace.s16)
        .ignoresSafeArea(.container, edges: .bottom)
        .accessibilityIdentifier("activity.timeline")
    }

    /// 09-02: no activity yet. The card sits centred between the segments and the tab bar.
    private var empty: some View {
        PBEmptyState(title: "No activity yet.", message: "Expenses, payments and changes will show up here.")
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.bottom, PBTabBar.bottomOffset + PBSize.tabbar + PBSpace.s8)
            .ignoresSafeArea(.container, edges: .bottom)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("activity.empty")
    }

    /// Claims always sit under "Today", even on a day with nothing else yet.
    private func withToday(_ timeline: [TimelineDay], claims: Bool, books: Books) -> [TimelineDay] {
        guard claims, timeline.first?.day != books.today else { return timeline }
        return [TimelineDay(day: books.today, header: Format.dayHeader(books.today, today: books.today), events: [])] + timeline
    }

    private func hold(claims: [PendingClaim], timeline: [TimelineDay]) {
        let id = UUID()
        holdID = id
        held = Held(claims: claims, timeline: timeline)
        Task {
            // The card's 250 ms Confirmed animation, then 0.8 s to read it; none with Reduce Motion.
            try? await Task.sleep(for: .milliseconds(reduceMotion ? 0 : 1050))
            guard holdID == id else { return }
            withAnimation(reduceMotion ? nil : .easeOut(duration: 0.25)) {
                held = nil
            }
        }
    }
}

#if DEBUG
#Preview("ActivityTimelineView") {
    GroupsPreview {
        ActivityTabScreen()
    }
}

#Preview("ActivityTimelineView · empty") {
    GroupsPreview(scenarios: Scenario.empty) {
        ActivityTabScreen()
    }
}
#endif
