import SwiftUI

/// The notifications inbox from the Home bell (screens-activity §6): Today (with a Confirm card for
/// each payment waiting for you) and Earlier, newest first, unread dots, and Mark all read. Opening a
/// row marks it read and goes where it's about; an overdue alert carries the red "Overdue" pill in
/// place of its time. Reminders Paybak sends for you stay on the timeline. With nothing at all it
/// reads "You’re all caught up."
/// Test ids: `notifications.action` (Mark all read), `notifications.row.<itemId>`,
/// `notifications.claim.<paymentId>.confirm` / `.notReceived`, `notifications.empty`.
struct NotificationsScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// After Confirm, the inbox from before stays while the card plays its Confirmed state.
    @State private var held: (claims: [PendingClaim], rows: [InboxRow])?
    @State private var holdID = UUID()

    var body: some View {
        let claims = held?.claims ?? store.snapshot.home.pendingClaims
        let rows = held?.rows ?? store.snapshot.inbox
        let today = rows.filter(\.isToday)
        let earlier = rows.filter { !$0.isToday }
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                if !claims.isEmpty || !today.isEmpty {
                    section("Today", rows: today) {
                        ForEach(claims) { claim in
                            PendingClaimCard(claim: claim, testIDPrefix: "notifications.claim.\(claim.id)") {
                                hold(claims: claims, rows: rows)
                            }
                            .transition(.opacity)
                        }
                    }
                }
                if !earlier.isEmpty {
                    section("Earlier", rows: earlier) { EmptyView() }
                }
                if claims.isEmpty && rows.isEmpty {
                    Text("You’re all caught up.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textSecondary)
                        .accessibilityIdentifier("notifications.empty")
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.bottom, PBSpace.s24)
            .pbPushContent()
        }
        .scrollIndicators(.hidden)
        .pbPinnedHeader {
            PBPushHeader("Notifications", trailing: .wideText("Mark all read") { store.markAllInboxRead() },
                         isTrailingEnabled: store.snapshot.unreadCount > 0, testIDPrefix: "notifications", onBack: router.back)
        }
        .routeTestRoot("notifications")
    }

    private func section(_ title: String, rows: [InboxRow], @ViewBuilder pinned: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader(title)
            pinned()
            VStack(spacing: 0) {
                ForEach(rows) { row in
                    Button { open(row) } label: {
                        PBActivityRow(leading: leading(row), title: row.title, subtitle: row.body,
                                      trailing: trailing(row), titleLines: 2, subtitleLines: 3,
                                      isUnread: !row.item.read)
                    }
                    .buttonStyle(PBRowButtonStyle())
                    .accessibilityIdentifier("notifications.row.\(row.id)")
                }
            }
        }
    }

    // MARK: Rows

    private func leading(_ row: InboxRow) -> PBActivityRow.Leading {
        let params = row.item.params
        switch row.item.type {
        case .paymentReminder: return .icon(.calendar)
        case .monthlySummary: return .icon(.chart)
        case .paymentConfirmed, .paymentOverdue, .paymentNotReceived:
            return .avatar(params.personId.flatMap { store.ledger.person($0)?.avatarContent } ?? .icon(.profile))
        case .newExpenseInGroup:
            return .icon((params.expenseId.flatMap { store.ledger.expense($0)?.category } ?? .other).pbIcon)
        case .expenseFlagged, .flagResolved: return .icon(.flag)
        }
    }

    /// The time or date; an overdue alert shows the red "Overdue" pill instead.
    private func trailing(_ row: InboxRow) -> PBActivityRow.Trailing {
        row.item.type == .paymentOverdue ? .badge("Overdue", style: .overdue) : .date(row.time)
    }

    private func open(_ row: InboxRow) {
        if !row.item.read {
            store.markInboxRead(row.id)
        }
        let params = row.item.params
        switch row.item.type {
        case .paymentReminder:
            guard let payee = params.personId else { return }
            router.open(Route.recordPayment(RecordPaymentArgs(
                from: Person.me, to: payee, amount: params.amount, currency: params.currency,
                method: store.ledger.reminderMethod(paying: payee),
                context: params.groupId.map { .group($0) }
            )))
        case .monthlySummary:
            guard let year = params.year, let month = params.month else { return }
            router.open(DeepLink.insights(YearMonth(year: year, month: month)))
        case .paymentConfirmed, .paymentNotReceived:
            if let id = params.paymentId { router.open(Route.payment(id)) }
        case .paymentOverdue:
            guard let person = params.personId else { return }
            let context: ReminderContext? = params.expenseId.map { .expense($0) }
                ?? params.groupId.map { .group($0) } ?? params.loanId.map { .loan($0) }
            router.open(Route.remind(personId: person, context: context))
        case .newExpenseInGroup, .expenseFlagged, .flagResolved:
            if let id = params.expenseId { router.open(Route.expense(id)) }
        }
    }

    private func hold(claims: [PendingClaim], rows: [InboxRow]) {
        let id = UUID()
        holdID = id
        held = (claims, rows)
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
#Preview("NotificationsScreen") {
    GroupsPreview {
        NotificationsScreen()
    }
}
#endif
