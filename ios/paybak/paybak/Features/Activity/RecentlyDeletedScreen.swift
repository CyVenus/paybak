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
                        .frame(maxWidth: .infinity)
                        .padding(.top, PBSpace.s24)
                        .accessibilityIdentifier("recentlyDeleted.empty")
                } else {
                    VStack(spacing: 0) {
                        ForEach(rows) { row in
                            PBActivityRow(leading: .icon(row.expense.category.pbIcon), title: row.expense.title,
                                          subtitle: subtitle(row.expense), detail: row.detail,
                                          trailing: .action("Restore") { restore(row.expense) },
                                          surface: .onCard, showsDivider: row.id != rows.last?.id)
                                .accessibilityIdentifier("recentlyDeleted.row.\(row.id)")
                                .transition(.opacity)
                        }
                    }
                    .padding(.vertical, PBSpace.s8)
                    .padding(.horizontal, PBSpace.s16)
                    .pbCard(padding: 0)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .pbPushContent()
        }
        .pbPinnedHeader {
            PBPushHeader("Recently deleted", testIDPrefix: "recentlyDeleted", onBack: router.back)
        }
        .routeTestRoot("recentlyDeleted")
    }

    /// "₹300 · Goa Trip"; outside a group, the first other person on it.
    private func subtitle(_ expense: Expense) -> String {
        let books = store.books
        let place = expense.groupId.map(books.groupName)
            ?? expense.participantIds.first { $0 != Person.me }.map(books.firstName) ?? ""
        return [Money.format(expense.amount, expense.currency), place].filter { !$0.isEmpty }.joined(separator: " · ")
    }

    private func restore(_ expense: Expense) {
        do {
            try withAnimation(.easeOut(duration: 0.25)) {
                try store.restoreExpense(expense.id)
            }
            Haptics.success()
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
