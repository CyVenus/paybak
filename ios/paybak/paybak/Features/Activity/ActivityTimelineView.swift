import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the timeline and keeps this
// initializer.
/// The Activity timeline under the header: pending claims on top, day groups, the empty state.
struct ActivityTimelineView: View {
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        ScrollView {
            PlaceholderInfo(
                name: "activityTimeline",
                owner: .a,
                spec: "screens-activity §3",
                details: ledgerStore.snapshot.timeline.prefix(4).flatMap { day in
                    day.events.map { "\(day.header) · \($0.title) · \($0.subtitle)" }
                }
            )
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBTabBar.contentInset)
            .phoneContentWidth()
        }
        .accessibilityIdentifier("activity.timeline")
    }
}
