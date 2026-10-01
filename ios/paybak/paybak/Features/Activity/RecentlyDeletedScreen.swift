import SwiftUI

/// Recently deleted (screens-activity §5): deleted expenses wait here for 30 days, newest deletion
/// first, each with the days it has left and Restore. Restoring counts it in every balance again.
/// Test ids: `recentlyDeleted.row.<expenseId>` (its button is "Restore"), `recentlyDeleted.empty`.
struct RecentlyDeletedScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store

    var body: some View {
        let rows = store.snapshot.recentlyDeleted
        ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                Text("Deleted items are kept for 30 days.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                if rows.isEmpty {
                    Text("Nothing here.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textSecondary)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .accessibilityIdentifier("recentlyDeleted.empty")
                } else {
                    VStack(spacing: 0) {
                        ForEach(rows) { row in
                            PBActivityRow(leading: .icon(row.expense.category.pbIcon), title: row.expense.title,
                                          subtitle: subtitle(row.expense), detail: detail(row),
                                          trailing: .action("Restore") { restore(row.expense) },
                                          surface: .onCard, titleLines: 2, subtitleLines: 3,
                                          showsDivider: row.id != rows.last?.id)
                                .accessibilityIdentifier("recentlyDeleted.row.\(row.id)")
                        }
                    }
                    .padding(.vertical, PBSpace.s8)
                    .padding(.horizontal, PBSpace.s16)
                    .pbCard(padding: 0)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.bottom, PBSpace.s24)
            .pbPushContent()
        }
        .scrollIndicators(.hidden)
        .pbPinnedHeader {
            PBPushHeader("Recently deleted", testIDPrefix: "recentlyDeleted", onBack: router.back)
        }
        .routeTestRoot("recentlyDeleted")
    }

    /// "₹300 · Goa Trip", or just the amount outside a group.
    private func subtitle(_ expense: Expense) -> String {
        let group = expense.groupId.flatMap { store.ledger.group($0)?.name }
        return [Money.format(expense.amount, expense.currency), group].compactMap { $0 }.joined(separator: " · ")
    }

    /// "Deleted by Priya on 24 Sep · 24 days left", with "24 days left" wrapping as one piece.
    private func detail(_ row: DeletedExpenseRow) -> String {
        let left = "\(row.daysLeft) day\(row.daysLeft == 1 ? "" : "s") left"
        return row.detail.replacingOccurrences(of: left, with: left.replacingOccurrences(of: " ", with: "\u{00A0}"))
    }

    private func restore(_ expense: Expense) {
        do {
            try store.restoreExpense(expense.id)
            router.toast("Expense restored")
        } catch {
            Haptics.warning()
            router.toast(error.localizedDescription)
        }
    }
}

#if DEBUG
#Preview("RecentlyDeletedScreen") {
    GroupsPreview {
        RecentlyDeletedScreen()
    }
}
#endif
