import SwiftUI

/// Settle up (settle §3): the fewest-payments plan from your side. Payments you make (Settle opens
/// Record payment prefilled), then people who owe you (Remind opens the reminder sheet over this
/// list). A payment you recorded stays "Pending" until its receiver confirms. With a `groupId` it is
/// that group's plan in the group's currency.
struct SettleUpScreen: View {
    let groupId: GroupID?

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let snapshot = ledgerStore.snapshot
        let plan = ledgerStore.books.settlePlan(groupId: groupId, pay: snapshot.settlePay, get: snapshot.settleGet)
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                PBNoticeCard(icon: .shuffle, message: "Paybak simplifies balances into the fewest payments. Everyone ends up in the same place.")
                if plan.pay.isEmpty && plan.get.isEmpty {
                    PBEmptyState.allSettled
                        .accessibilityIdentifier("settleUp.allSettled")
                }
                section(Books.paymentsHeader(plan.pay.count), rows: plan.pay)
                section(Books.owersHeader(plan.get.count), rows: plan.get)
            }
            .padding(.top, PBSpace.s8)
            .pbPushContent()
        }
        .pbPinnedHeader {
            PBPushHeader("Settle up", testIDPrefix: "settleUp", onBack: router.back)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.settleUp")
    }

    @ViewBuilder
    private func section(_ title: String, rows: [PlanRowCopy]) -> some View {
        if !rows.isEmpty {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader(title)
                VStack(spacing: PBSpace.s12) {
                    ForEach(rows) { row in
                        planRow(row)
                    }
                }
            }
        }
    }

    private func planRow(_ row: PlanRowCopy) -> some View {
        let person = ledgerStore.ledger.person(row.friend)
        let action: SettlePlanRow.Action? = switch (row.pays, row.pendingPayment) {
        case (true, nil): .init(title: "Settle", testID: "settleUp.pay.\(row.friend)") { settle(row, payeeHasUPI: person?.upi != nil) }
        case (true, _): nil
        case (false, _): .init(title: "Remind", testID: "settleUp.remind.\(row.friend)") {
            router.open(.remind(personId: row.friend, context: row.item.map(reminderContext)))
        }
        }
        return SettlePlanRow(avatar: person?.avatarContent ?? .icon(.profile), title: row.name, detail: row.context, amount: row.amount,
                             badge: row.badge, isOverdue: row.isOverdue, action: action, testID: "settleUp.row.\(row.friend)") {
            router.open(row.pendingPayment.map(Route.payment) ?? .friend(row.friend))
        }
    }

    /// Record payment prefilled from the suggestion: you pay them the amount, by UPI when they have an
    /// ID, for the group or expense (settle §4.2).
    private func settle(_ row: PlanRowCopy, payeeHasUPI: Bool) {
        router.open(.recordPayment(RecordPaymentArgs(from: Person.me, to: row.friend, amount: row.amountMinor, currency: row.currency,
                                                     method: payeeHasUPI ? .upi : .cash, context: row.item.map(paymentContext))))
    }

    private func reminderContext(_ item: Obligation) -> ReminderContext {
        switch item.kind {
        case .direct: .expense(item.ref)
        case .group, .project: .group(item.ref)
        case .loan: .loan(item.ref)
        }
    }

    private func paymentContext(_ item: Obligation) -> PaymentContext {
        switch item.kind {
        case .direct: .expense(item.ref)
        case .group, .project: .group(item.ref)
        case .loan: .loan(item.ref)
        }
    }
}

#if DEBUG
#Preview("SettleUpScreen") {
    GroupsPreview {
        SettleUpScreen(groupId: nil)
    }
}

#Preview("SettleUpScreen · Kabir pending") {
    GroupsPreview(scenarios: ["paymentToKabirPending"]) {
        SettleUpScreen(groupId: nil)
    }
}
#endif
