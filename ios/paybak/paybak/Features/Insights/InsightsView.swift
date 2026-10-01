import SwiftUI

/// Activity › Insights (screens-insights-ai §2): the Activity header, the month row and the report
/// scroll together, and the large title collapses into the inline bar once it passes under the
/// status bar. The month lives in `router.insightsMonth` (nil = this month). On the free plan the
/// report stays blurred behind the Pro notice and the column doesn't scroll (§2.5).
struct InsightsView: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var isTitleCollapsed = false
    @State private var scrollPosition = ScrollPosition()

    /// Figma ends the report 144 pt above the screen's bottom edge, so it scrolls exactly 676 pt (§2.4).
    private static let bottomInset: CGFloat = 144

    var body: some View {
        let books = ledgerStore.books
        let page = books.insightsPage(router.insightsMonth ?? YearMonth(books.today))
        let isLocked = !ledgerStore.isPro
        ScrollView {
            VStack(spacing: 0) {
                ActivityHeader()
                InsightsMonthRow(page: page, isEnabled: !isLocked, onSelect: select)
                InsightsReportView(page: page, isLocked: isLocked)
                    .padding(.top, PBSpace.s16)
                    .overlay(alignment: .top) {
                        if isLocked { lock }
                    }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, Self.bottomInset)
            .phoneContentWidth()
        }
        .scrollPosition($scrollPosition)
        .scrollDisabled(isLocked)
        .onScrollGeometryChange(for: Bool.self) { geometry in
            geometry.contentOffset.y + geometry.contentInsets.top > PBSize.tap
        } action: { _, isCollapsed in
            isTitleCollapsed = isCollapsed
        }
        .contentMargins(.bottom, 0, for: .scrollContent)
        .ignoresSafeArea(.container, edges: .bottom)
        .pbCollapsingTitle("Activity", isCollapsed: isTitleCollapsed)
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.insights")
        .onStartScreen([.insightsScrolled]) { _ in
            scrollPosition.scrollTo(y: 676)
        }
    }

    /// The white 60 % veil from the hero card's top to the screen's bottom, and the Pro notice on it.
    private var lock: some View {
        Rectangle()
            .fill(PBColor.bgPrimary.opacity(0.6))
            .padding(.horizontal, -PBLayout.screenMargin)
            .frame(height: 1000, alignment: .top)
            .padding(.top, PBSpace.s16)
            .contentShape(.rect)
            .overlay(alignment: .top) {
                PBNoticeCard(
                    icon: .lock,
                    title: "Insights are part of Paybak Pro",
                    message: "See monthly trends, spending by category, group and friend, and lent vs borrowed.",
                    badge: "Pro",
                    layout: .centered,
                    primary: .init("See Pro", testID: "insights.seePro") { router.open(.paywall(continueTo: nil)) }
                )
                .padding(.top, 148)
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier("insights.locked")
            }
    }

    private func select(_ month: YearMonth) {
        router.insightsMonth = month == YearMonth(ledgerStore.books.today) ? nil : month
        Haptics.selection()
    }
}

/// The month row: glass ‹ and › around "September 2026"; › is dimmed on the current month.
private struct InsightsMonthRow: View {
    let page: InsightsPage
    let isEnabled: Bool
    let onSelect: (YearMonth) -> Void

    var body: some View {
        HStack(spacing: 0) {
            chevron(.chevronLeft, label: "Previous month", testID: "insights.monthPrev",
                    isEnabled: page.canGoBack, to: page.month.adding(months: -1))
            Text(page.title)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .frame(maxWidth: .infinity)
                .contentTransition(.numericText())
                .accessibilityIdentifier("insights.month")
            chevron(.chevronRight, label: "Next month", testID: "insights.monthNext",
                    isEnabled: page.canGoForward, to: page.month.adding(months: 1))
        }
        .frame(height: PBSize.tap)
    }

    private func chevron(_ icon: PBIcon, label: String, testID: String, isEnabled: Bool, to month: YearMonth) -> some View {
        PBIconButton(icon, accessibilityLabel: label, style: .glass) { onSelect(month) }
            .disabled(!isEnabled || !self.isEnabled)
            .opacity(isEnabled ? 1 : 0.3)
            .accessibilityIdentifier(testID)
    }
}

#if DEBUG
#Preview("Insights") {
    GroupsPreview(scenarios: Scenario.pro()) {
        InsightsView()
    }
}

#Preview("Insights · locked") {
    GroupsPreview {
        InsightsView()
    }
}
#endif
