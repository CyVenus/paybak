import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with Insights and keeps this
// initializer.
/// Insights under the Activity header: the month (`router.insightsMonth`), hero, chart, categories,
/// who you spent with, lent vs borrowed; locked on the free plan.
struct InsightsView: View {
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let month = YearMonth(ledgerStore.clock.today)
        let report = ledgerStore.books.insights(month)
        ScrollView {
            PlaceholderInfo(
                name: "insights",
                owner: .c,
                spec: "screens-insights-ai §2",
                details: ["\(month.name): \(Money.format(report.total))", ledgerStore.isPro ? "Pro" : "Locked (free plan)"]
            )
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBTabBar.contentInset)
            .phoneContentWidth()
        }
        .accessibilityIdentifier("activity.insights")
    }
}
