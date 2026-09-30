import SwiftUI

/// The loan detail (record-lend-group §5): hero, Original · Paid · Remaining with the bar, the
/// installments (paid, due, or red only when overdue now), the last reminder, and Remind /
/// Record repayment pinned at the bottom. A loan paid back shows its "Paid back" state, read-only.
struct LoanScreen: View {
    let loanId: LoanID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    /// "Loan added" sits 12 pt above the pinned buttons here, so this screen shows the app toast
    /// itself while it's on top.
    @State private var toast: PBToastMessage?

    var body: some View {
        let detail = store.books.loanDetail(loanId)
        Group {
            if let detail {
                content(detail)
            } else {
                Text("This loan is no longer here.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .pinnedHeader {
            PBPushHeader("Loan", trailing: detail.map { !$0.isPaidBack } == true ? .text("Edit", action: edit) : .none,
                         testIDPrefix: "loan", onBack: router.back)
        }
        .background(PBColor.bgPrimary)
        .onChange(of: router.toast, initial: true) { _, message in
            guard let message, isOnTop else { return }
            toast = message
            router.toast = nil
        }
        .routeTestRoot("loan")
    }

    private var isOnTop: Bool {
        let route = Route.loan(loanId)
        if let layer = router.modals.last {
            return layer.path.last == route || layer.path.isEmpty && layer.root == route
        }
        return router.mainPath.last == route
    }

    private func content(_ detail: LoanDetail) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                PBAmountHero(leading: .avatar(store.ledger.person(detail.loan.friendId)?.avatarContent ?? .icon(.profile)),
                             title: detail.title, amount: detail.amount, meta: detail.meta,
                             status: detail.isPaidBack ? "Paid back" : nil)
                    .accessibilityIdentifier("loan.hero")
                PBLoanProgressCard(original: detail.original, paid: detail.paid, remaining: detail.remaining,
                                   progress: detail.progress, caption: detail.caption, isPaidBack: detail.isPaidBack)
                    .accessibilityIdentifier("loan.progress")
                VStack(alignment: .leading, spacing: PBSpace.s12) {
                    PBSectionHeader(detail.sectionTitle)
                    VStack(alignment: .leading, spacing: PBSpace.s8) {
                        VStack(spacing: 0) {
                            ForEach(detail.lines) { line in
                                InstallmentRow(line: line, showsDivider: line.id != detail.lines.last?.id)
                                    .accessibilityIdentifier("loan.installment.\(line.number)")
                            }
                        }
                        .padding(.horizontal, PBLayout.cardPadding)
                        .pbCard(padding: 0)
                        if let reminder = detail.lastReminder {
                            Text(reminder)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textSecondary)
                                .accessibilityIdentifier("loan.lastReminder")
                        }
                    }
                }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s16)
            .phoneContentWidth()
        }
        .pbToast($toast, bottomPadding: PBSpace.s12)
        .safeAreaInset(edge: .bottom) {
            if !detail.isPaidBack {
                VStack(spacing: PBSpace.s12) {
                    if detail.isOverdue, detail.loan.lenderId == Person.me {
                        PBButton("Remind \(store.books.firstName(detail.loan.friendId))", style: .secondary, fillsWidth: true) {
                            router.open(.remind(personId: detail.loan.friendId, context: .loan(loanId)))
                        }
                        .accessibilityIdentifier("loan.remind")
                    }
                    PBButton("Record repayment", fillsWidth: true) { recordRepayment(detail) }
                        .accessibilityIdentifier("loan.recordRepayment")
                }
                .padding(.horizontal, PBLayout.screenMargin)
                .phoneContentWidth()
                .background(PBColor.bgPrimary)
            }
        }
    }

    private func edit() {
        router.open(.lendMoney(LendMoneyArgs(editing: loanId)))
    }

    /// Record payment from the borrower to the lender for the next installment (§5.5).
    private func recordRepayment(_ detail: LoanDetail) {
        let loan = detail.loan
        router.open(.recordPayment(RecordPaymentArgs(from: loan.borrowerId, to: loan.lenderId,
                                                     amount: detail.nextAmount ?? loan.amount, currency: loan.currency,
                                                     context: .loan(loanId))))
    }
}

/// An installment: a calendar (or check once paid) in a white circle, "Installment 1" over its
/// due or paid line, and the amount with the red Overdue pill under it when it's overdue now.
private struct InstallmentRow: View {
    let line: LoanDetail.Line
    let showsDivider: Bool

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            PBIconView(line.isPaid ? .checkCircle : .calendar, size: PBSize.iconMd)
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: PBSize.avatarMd, height: PBSize.avatarMd)
                .background(PBColor.bgPrimary, in: .circle)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(line.title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                Text(line.subtitle)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .lineLimit(1)
            .frame(maxWidth: .infinity, alignment: .leading)
            VStack(alignment: .trailing, spacing: PBSpace.s2) {
                Text(line.amount)
                    .textStyle(.amountMedium)
                    .foregroundStyle(PBColor.textPrimary)
                if let overdue = line.overdue {
                    PBBadge(overdue, style: .overdue)
                        .accessibilityIdentifier("loan.installment.\(line.number).badge")
                }
            }
            .fixedSize()
        }
        .padding(.vertical, PBSpace.s8)
        .frame(minHeight: 64)
        .overlay(alignment: .bottom) {
            if showsDivider { PBDivider().padding(.leading, 52) }
        }
        .accessibilityElement(children: .combine)
    }
}
