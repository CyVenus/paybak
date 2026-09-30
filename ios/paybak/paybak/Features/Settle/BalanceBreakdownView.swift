import SwiftUI

/// The screen behind a Home balance card (settle §1–§2): the total as a hero, every person in that
/// direction (overdue first) in one card, why you pay someone directly, and Settle up pinned at the
/// bottom. Money you're owed is black with "+", money you owe gray with "−"; only overdue is red.
struct BalanceBreakdownView: View {
    enum Direction {
        case owed
        case owe
    }

    let direction: Direction

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    private var routeID: String { direction == .owed ? "owedBreakdown" : "oweBreakdown" }

    var body: some View {
        let snapshot = ledgerStore.snapshot
        let books = ledgerStore.books
        let rows = (direction == .owed ? snapshot.owedBreakdown : snapshot.oweBreakdown).map(books.breakdownRow)
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                hero(snapshot.home.totals, isEmpty: rows.isEmpty)
                if !rows.isEmpty {
                    people(rows, footnotes: direction == .owe ? books.simplifiedDebtsFootnotes(snapshot.simplifiedGroups) : [])
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, PBSpace.s8)
            .padding(.bottom, PBSpace.s24)
            .pbPushContent()
        }
        .pbPinnedHeader {
            PBPushHeader(direction == .owed ? "You’re owed" : "You owe", testIDPrefix: routeID, onBack: router.back)
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            PBButton("Settle up", fillsWidth: true) {
                router.open(.settleUp(groupId: nil))
            }
            .accessibilityIdentifier("\(routeID).settleUp")
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.top, PBSpace.s12)
            .phoneContentWidth()
            .background(PBColor.bgPrimary)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.\(routeID)")
    }

    // MARK: Hero

    /// "+₹2,900 / from 4 people" or "−₹1,850 / across 2 groups": the same numbers as the Home cards.
    /// Nobody in this direction: a tertiary "₹0" and a calm line instead.
    private func hero(_ totals: HomeTotals, isEmpty: Bool) -> some View {
        let (amount, caption): (String, String) = switch direction {
        case .owed: (Money.format(totals.owed, currency, sign: .signed), totals.owedCaption)
        case .owe: (Money.format(-totals.owe, currency, sign: .debit), totals.oweCaption)
        }
        return VStack(alignment: .leading, spacing: PBSpace.s4) {
            Text(isEmpty ? Money.format(0, currency) : amount)
                .textStyle(.title1)
                .foregroundStyle(isEmpty ? PBColor.textTertiary : direction == .owed ? PBColor.textPrimary : PBColor.textSecondary)
                .lineLimit(1)
                .minimumScaleFactor(0.6)
                .accessibilityIdentifier("\(routeID).total")
            Text(isEmpty ? emptyCaption : caption)
                .textStyle(.body)
                .foregroundStyle(PBColor.textSecondary)
        }
        .accessibilityElement(children: .combine)
    }

    private var emptyCaption: String {
        direction == .owed ? "Nobody owes you right now" : "You don’t owe anyone right now"
    }

    private var currency: String { ledgerStore.books.defaultCurrency }

    // MARK: People

    private func people(_ rows: [BreakdownRowCopy], footnotes: [String]) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader(direction == .owed ? "Who owes you" : "Who you owe")
            VStack(spacing: 0) {
                ForEach(rows) { row in
                    personRow(row, showsDivider: row.id != rows.last?.id)
                }
            }
            .pbCard(padding: 0)
            ForEach(footnotes, id: \.self) { footnote in
                Text(footnote)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .accessibilityIdentifier("\(routeID).footnote")
            }
        }
    }

    private func personRow(_ row: BreakdownRowCopy, showsDivider: Bool) -> some View {
        PBPersonRow(
            name: row.name,
            avatar: ledgerStore.ledger.person(row.id)?.avatarContent ?? .icon(.profile),
            subtitle: row.subtitle,
            isOnCard: true,
            trailing: .amount(row.amount, direction: direction == .owed ? .owed : .owe, label: row.dueLabel, overdue: row.overdue),
            showsDivider: showsDivider
        ) {
            router.open(.friend(row.id))
        }
        .accessibilityIdentifier("\(routeID).row.\(row.id)")
    }
}

#if DEBUG
#Preview("BalanceBreakdownView · owed") {
    GroupsPreview {
        BalanceBreakdownView(direction: .owed)
    }
}

#Preview("BalanceBreakdownView · owe") {
    GroupsPreview {
        BalanceBreakdownView(direction: .owe)
    }
}
#endif
