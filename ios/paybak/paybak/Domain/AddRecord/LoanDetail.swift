import Foundation

/// The loan detail (record-lend-group §5, domain.md §9): hero, progress card, the installments with
/// their paid / due / overdue lines, and what the lender can do next.
nonisolated struct LoanDetail: Sendable {
    struct Line: Hashable, Sendable, Identifiable {
        var id: Int { number }
        let number: Int
        /// "Installment 1" (or the loan's title for a loan without installments).
        let title: String
        /// "Due Fri 30 Oct" · "Paid 10 Jul" · "Paid 14 Sep · 2 days late" · "No due date".
        let subtitle: String
        let amount: String
        let isPaid: Bool
        /// "Overdue 4 days": the only red on the screen.
        let overdue: String?
    }

    let loan: Loan
    /// "You lent Dev" / "You borrowed from Dev".
    let title: String
    let amount: String
    /// "Laptop repair · Today".
    let meta: String
    let original: String
    let paid: String
    let remaining: String
    /// Paid ÷ original, 0…1.
    let progress: Double
    /// "0% paid back" / "Paid back on 14 Sep".
    let caption: String
    let isPaidBack: Bool
    /// "3 monthly installments" / "Due".
    let sectionTitle: String
    let lines: [Line]
    /// "Last reminder sent Mon 2 Nov", shown while something is overdue.
    let lastReminder: String?
    let isOverdue: Bool
    /// The next unpaid installment (the Record repayment prefill).
    let nextAmount: Int64?
}

nonisolated extension Books {
    func loanDetail(_ id: LoanID) -> LoanDetail? {
        guard let loan = ledger.loan(id) else { return nil }
        let lent = loan.lenderId == Person.me
        let paid = loanPaid(loan)
        let remaining = max(0, loan.amount - paid)
        let installments = installments(of: loan)
        let lastPaidOn = confirmedPayments().filter { $0.loanId == loan.id }.map(\.date).max()
        let lines = installments.enumerated().map { index, installment in
            loanLine(installment, number: index + 1, title: loan.installments == nil ? loan.title : nil, currency: loan.currency)
        }
        let isOverdue = lines.contains { $0.overdue != nil }
        let meta = "\(loan.title) · \(Format.loanMetaDate(loan.date, today: today))"
        return LoanDetail(
            loan: loan,
            title: lent ? "You lent \(firstName(loan.borrowerId))" : "You borrowed from \(firstName(loan.lenderId))",
            amount: Money.format(loan.amount, loan.currency),
            meta: meta,
            original: Money.format(loan.amount, loan.currency),
            paid: Money.format(paid, loan.currency),
            remaining: Money.format(remaining, loan.currency),
            progress: loan.amount > 0 ? min(1, Double(paid) / Double(loan.amount)) : 0,
            caption: remaining == 0
                ? "Paid back on \(Format.short(installments.last?.paidOn ?? lastPaidOn ?? loan.date))"
                : "\(loan.amount > 0 ? min(100, Int((Double(paid) * 100 / Double(loan.amount)).rounded())) : 0)% paid back",
            isPaidBack: remaining == 0,
            sectionTitle: loan.installments.map { Self.installmentsHeader(count: $0.count, frequency: $0.frequency) } ?? "Due",
            lines: lines,
            lastReminder: isOverdue ? lastReminder(forLoan: loan.id).map(loanReminderText) : nil,
            isOverdue: isOverdue,
            nextAmount: installments.first { $0.paidOn == nil }?.amount
        )
    }

    /// "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec"; more than 3 installments:
    /// "6 × ₹1,000 · monthly from Fri 30 Oct to Wed 31 Mar".
    static func schedulePreview(amount: Int64, currency: String, count: Int, frequency: Loan.Frequency, firstDue: LocalDay) -> String {
        guard count > 0 else { return "" }
        let amounts = Splits.installments(amount, count: count)
        let dues = (0..<count).map { installmentDue(first: firstDue, frequency: frequency, index: $0) }
        // Uneven amounts give the first ones the extra unit: "about" the smaller, later ones.
        let each = Set(amounts).count == 1 ? Money.format(amounts[0], currency) : "about \(Money.format(amounts[count - 1], currency))"
        let when = count <= 3
            ? Format.joinedNames(dues.map(Format.day))
            : "\(frequencyAdverb(frequency)) from \(Format.day(dues[0])) to \(Format.day(dues[count - 1]))"
        return "\(count) × \(each) · \(when)"
    }

    /// The loan detail's section header: "3 monthly installments", "1 weekly installment".
    static func installmentsHeader(count: Int, frequency: Loan.Frequency) -> String {
        "\(count) \(frequencyAdjective(frequency)) installment\(count == 1 ? "" : "s")"
    }

    static func frequencyAdjective(_ frequency: Loan.Frequency) -> String {
        switch frequency {
        case .weekly: "weekly"
        case .biweekly: "fortnightly"
        case .monthly: "monthly"
        }
    }

    /// The preview's "monthly from …": the same words as the header ("fortnightly" every 2 weeks).
    static func frequencyAdverb(_ frequency: Loan.Frequency) -> String {
        frequencyAdjective(frequency)
    }

    /// `title` names the single repayment of a loan without installments (its title); nil numbers them.
    private func loanLine(_ installment: Installment, number: Int, title: String?, currency: String) -> LoanDetail.Line {
        let subtitle: String
        var overdue: String?
        if let paidOn = installment.paidOn {
            let late = installment.due.map { $0.days(to: paidOn) } ?? 0
            subtitle = "Paid \(Format.short(paidOn))" + (late > 0 ? " · \(late) day\(late == 1 ? "" : "s") late" : "")
        } else if let due = installment.due {
            subtitle = "Due \(Format.day(due))"
            let days = due.days(to: today)
            if days > 0 { overdue = "Overdue \(days) day\(days == 1 ? "" : "s")" }
        } else {
            subtitle = "No due date"
        }
        return LoanDetail.Line(number: number, title: title ?? "Installment \(number)", subtitle: subtitle,
                               amount: Money.format(installment.amount, currency), isPaid: installment.paidOn != nil,
                               overdue: overdue)
    }
}
