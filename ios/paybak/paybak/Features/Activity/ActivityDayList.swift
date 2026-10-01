import SwiftUI

/// Day groups of activity rows (screens-activity §3.4): a Title/3 header, then `pinned` content (the
/// timeline's claim cards, Today only), then the rows with no dividers. 24 between groups, 8 inside.
/// A row opens its item; rows with nowhere to go aren't buttons. Test ids: `<prefix>.day.<yyyy-MM-dd>`
/// on the headers and `<prefix>.row.<event id>` on the rows.
struct ActivityDayList<Pinned: View>: View {
    let days: [ActivityDay]
    let testIDPrefix: String
    @ViewBuilder var pinned: () -> Pinned

    @Environment(AppRouter.self) private var router

    init(days: [ActivityDay], testIDPrefix: String, @ViewBuilder pinned: @escaping () -> Pinned = { EmptyView() }) {
        self.days = days
        self.testIDPrefix = testIDPrefix
        self.pinned = pinned
    }

    var body: some View {
        LazyVStack(alignment: .leading, spacing: PBLayout.sectionGap) {
            ForEach(Array(days.enumerated()), id: \.element.id) { index, day in
                VStack(alignment: .leading, spacing: PBSpace.s8) {
                    PBSectionHeader(day.header)
                        .accessibilityIdentifier("\(testIDPrefix).day.\(day.day)")
                    if index == 0 {
                        pinned()
                    }
                    VStack(spacing: 0) {
                        ForEach(day.items) { item in
                            row(item)
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func row(_ item: ActivityItem) -> some View {
        let content = PBActivityRow(leading: item.leading, title: item.title, subtitle: item.subtitle,
                                    trailing: item.trailing, titleLines: 2, subtitleLines: 3)
        if let route = item.route {
            Button { router.open(route) } label: { content }
                .buttonStyle(PBRowButtonStyle())
                .accessibilityIdentifier("\(testIDPrefix).row.\(item.id)")
        } else {
            content
                .accessibilityIdentifier("\(testIDPrefix).row.\(item.id)")
        }
    }
}

#Preview("ActivityDayList") {
    let day = LocalDay(year: 2026, month: 9, day: 30)
    let items = [
        ActivityItem(id: "1", at: .now, leading: .icon(.bell), title: "Reminder sent to Rohan",
                     subtitle: "Movie tickets · ₹800 · Sent automatically", trailing: .none, route: nil),
        ActivityItem(id: "2", at: .now, leading: .icon(.food), title: "You added Dinner at Olive Garden",
                     subtitle: "You paid · 4 people", trailing: .amount("₹2,800", date: nil, isIncoming: true), route: .home),
    ]
    ScrollView {
        ActivityDayList(days: [ActivityDay(day: day, header: "Today", items: items)], testIDPrefix: "preview")
            .padding(PBLayout.screenMargin)
    }
    .environment(AppRouter())
}
