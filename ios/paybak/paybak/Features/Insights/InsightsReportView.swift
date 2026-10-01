import SwiftUI

/// The month's report under the month row (screens-insights-ai §2.2–§2.4): the hero card with the
/// trend and the six-month chart, By category, Who you spent with (Groups | Friends) and Lent vs
/// borrowed. Locked, the summary, chart and category rows are blurred (§2.5).
struct InsightsReportView: View {
    let page: InsightsPage
    let isLocked: Bool

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var who = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            hero
            if page.isEmpty {
                Text(page.emptyLine)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .padding(.top, PBSpace.s24)
            } else {
                byCategory
                    .padding(.top, PBSpace.s24)
                    .lockedBlur(isLocked)
                whoYouSpentWith
                    .padding(.top, PBSpace.s24)
                    .lockedBlur(isLocked)
            }
            lentVsBorrowed
                .padding(.top, PBSpace.s24)
                .lockedBlur(isLocked)
        }
        .animation(reduceMotion ? nil : .easeOut(duration: 0.3), value: page)
    }

    // MARK: Hero

    private var hero: some View {
        VStack(alignment: .leading, spacing: 0) {
            VStack(alignment: .leading, spacing: PBSpace.s4) {
                Text("Your share of shared expenses")
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                HStack(spacing: PBSpace.s8) {
                    Text(page.total)
                        .textStyle(.title1)
                        .foregroundStyle(PBColor.textPrimary)
                        .contentTransition(.numericText())
                        .accessibilityIdentifier("insights.total")
                    if let trend = page.trend {
                        PBBadge(trend, style: .onCard)
                            .accessibilityIdentifier("insights.trend")
                    }
                }
            }
            .lockedBlur(isLocked)
            Spacer(minLength: PBSpace.s16)
            PBMonthlyBarChart(months: page.chart.map { PBMonthlyBarChart.Month(label: $0.label, value: Double($0.total)) })
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(page.chartLabel)
                .accessibilityIdentifier("insights.chart")
                .lockedBlur(isLocked)
        }
        .padding(PBSpace.s20)
        .frame(height: 280)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }

    // MARK: Lists

    private var byCategory: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBSectionHeader("By category")
            rows(page.categories, prefix: "insights.category")
        }
    }

    private var whoYouSpentWith: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Who you spent with")
            PBSegmentedControl(options: ["Groups", "Friends"], selection: $who, testIDPrefix: "insights.who")
            if who == 0 {
                rows(page.groups, prefix: "insights.group")
            } else {
                rows(page.friends, prefix: "insights.friend")
            }
        }
    }

    private func rows(_ rows: [InsightsPage.BarRow], prefix: String) -> some View {
        VStack(spacing: 0) {
            ForEach(rows) { row in
                barRow(row)
                    .accessibilityIdentifier("\(prefix).\(row.id)")
            }
        }
    }

    @ViewBuilder
    private func barRow(_ row: InsightsPage.BarRow) -> some View {
        let content = PBBarRow(leading: leading(row.leading), title: row.title, caption: row.caption, amount: row.amount,
                               progress: row.progress)
        if let target = row.target {
            Button { open(target) } label: { content }
                .buttonStyle(PBRowButtonStyle())
        } else {
            content
        }
    }

    /// A row lists its records (proposal, insights §2.3).
    private func open(_ target: InsightsPage.BarRow.Target) {
        switch target {
        case .category(let category, let month):
            router.open(.activityLog(.category(category, month: month)))
        case .group(let id):
            let isProject = ledgerStore.ledger.group(id)?.isProject ?? false
            router.open(.activityLog(isProject ? .project(id) : .group(id)))
        case .person(let id):
            router.open(.activityLog(.person(id)))
        }
    }

    private func leading(_ leading: InsightsPage.BarRow.Leading) -> PBAvatar.Content {
        switch leading {
        case .icon(let name): .icon(PBIcon(rawValue: name) ?? .tag)
        case .person(let id): ledgerStore.ledger.person(id)?.avatarContent ?? .icon(.profile)
        }
    }

    // MARK: Lent vs borrowed

    private var lentVsBorrowed: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader(page.loansTitle)
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                VStack(alignment: .leading, spacing: PBSpace.s12) {
                    HStack(alignment: .top, spacing: PBSpace.s16) {
                        total("Lent", page.lent, testID: "insights.lent")
                        total("Borrowed", page.borrowed, testID: "insights.borrowed")
                    }
                    ForEach(page.loans) { loan in
                        loanRow(loan)
                    }
                }
                .pbCard()
                Text(InsightsPage.footnote)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }

    private func total(_ label: String, _ amount: String, testID: String) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s2) {
            Text(label)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
            Text(amount)
                .textStyle(.amountLarge)
                .foregroundStyle(PBColor.textPrimary)
                .accessibilityIdentifier(testID)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }

    private func loanRow(_ loan: InsightsPage.LoanRow) -> some View {
        Button { router.open(.loan(loan.id)) } label: {
            HStack(spacing: PBSpace.s8) {
                PBAvatar(ledgerStore.ledger.person(loan.personId)?.avatarContent ?? .icon(.profile),
                         diameter: PBSize.avatarXs, isOnCard: true)
                Text(loan.text)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if let badge = loan.badge {
                    PBBadge(badge, style: badgeStyle(loan.status))
                }
            }
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("insights.loan.\(loan.id)")
    }

    private func badgeStyle(_ status: InsightsPage.LoanRow.Status) -> PBBadge.Style {
        switch status {
        case .paidBack: .inverse
        case .overdue: .overdue
        case .due, .open: .onCard
        }
    }
}

private extension View {
    /// The locked report: unreadable but still there (Figma: opacity 0.4 + layer blur 16).
    func lockedBlur(_ isLocked: Bool) -> some View {
        blur(radius: isLocked ? 8 : 0)
            .opacity(isLocked ? 0.4 : 1)
            .allowsHitTesting(!isLocked)
            .accessibilityHidden(isLocked)
    }
}
