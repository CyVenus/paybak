import Foundation

/// One installment of a loan (domain.md §9).
nonisolated struct Installment: Hashable, Sendable {
    var due: LocalDay?
    var amount: Int64
    /// The date of the repayment that completed it; nil while unpaid.
    var paidOn: LocalDay?
}

nonisolated extension Books {
    /// The installments with their paid dates: confirmed repayments fill them in due order (§9).
    func installments(of loan: Loan, asOf: Date? = nil) -> [Installment] {
        let amounts: [Int64]
        let dues: [LocalDay?]
        if let plan = loan.installments {
            amounts = Splits.installments(loan.amount, count: plan.count)
            dues = (0..<plan.count).map { Self.installmentDue(first: plan.firstDue, frequency: plan.frequency, index: $0) }
        } else {
            amounts = [loan.amount]
            dues = [loan.dueDate]
        }
        var queue = confirmedPayments(asOf: asOf)
            .filter { $0.loanId == loan.id }
            .enumerated()
            .sorted { $0.element.date != $1.element.date ? $0.element.date < $1.element.date : $0.offset < $1.offset }
            .map(\.element)
        var result: [Installment] = []
        var pool: Int64 = 0
        for (need, due) in zip(amounts, dues) {
            var paidOn: LocalDay?
            while pool < need, !queue.isEmpty {
                let repayment = queue.removeFirst()
                pool += repayment.amount
                paidOn = repayment.date
            }
            if pool >= need {
                pool -= need
                result.append(Installment(due: due, amount: need, paidOn: paidOn ?? result.last?.paidOn))
            } else {
                result.append(Installment(due: due, amount: need, paidOn: nil))
            }
        }
        return result
    }

    /// Installment `index` (0-based): weekly +7 d, biweekly +14 d, monthly the same day (clamped).
    static func installmentDue(first: LocalDay, frequency: Loan.Frequency, index: Int) -> LocalDay {
        switch frequency {
        case .weekly: first.adding(days: 7 * index)
        case .biweekly: first.adding(days: 14 * index)
        case .monthly: first.adding(months: index, day: first.day)
        }
    }

    /// Σ confirmed repayments of a loan.
    func loanPaid(_ loan: Loan) -> Int64 {
        confirmedPayments().filter { $0.loanId == loan.id }.reduce(0) { $0 + $1.amount }
    }

    /// The latest reminder sent about a loan.
    func lastReminder(forLoan id: LoanID) -> Reminder? {
        ledger.reminders.filter { $0.loanId == id }.max { $0.sentAt < $1.sentAt }
    }
}
