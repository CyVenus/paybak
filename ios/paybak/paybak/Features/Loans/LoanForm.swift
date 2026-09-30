import Foundation
import Observation

/// Lend money's state (record-lend-group §4.4): direction, amount, the other person, reason, date,
/// and either installments (count, frequency, first due) or a single due date.
@Observable
final class LoanForm {
    static let countRange = 2...24

    let editing: LoanID?
    var direction: LendMoneyArgs.Direction
    var amountText: String
    var currency: String
    var rate: Rate?
    var person: PersonID?
    var reason: String
    var date: LocalDay
    var hasInstallments: Bool
    var count: Int
    var frequency: Loan.Frequency
    /// Picked by the user; otherwise one period after the loan date.
    var pickedFirstDue: LocalDay?
    var dueDate: LocalDay?

    @ObservationIgnored private var initial: LoanDraft?

    init(editing: LoanID? = nil, direction: LendMoneyArgs.Direction, amount: Int64?, currency: String, rate: Rate?, person: PersonID?,
         reason: String, date: LocalDay, installments: Loan.Installments?, dueDate: LocalDay?) {
        self.editing = editing
        self.direction = direction
        amountText = amount.map { MoneyInput.text($0, currency: currency) } ?? ""
        self.currency = currency
        self.rate = rate
        self.person = person
        self.reason = reason
        self.date = date
        hasInstallments = installments != nil || dueDate == nil
        count = installments?.count ?? 3
        frequency = installments?.frequency ?? .monthly
        pickedFirstDue = installments?.firstDue
        self.dueDate = dueDate
        initial = draft
    }

    var amount: Int64 { MoneyInput.minor(amountText, currency: currency) }

    var isLent: Bool { direction == .lent }

    /// Default first due = the loan date + one period (Wed 30 Sep → Fri 30 Oct).
    var firstDue: LocalDay {
        pickedFirstDue ?? Books.installmentDue(first: date, frequency: frequency, index: 1)
    }

    var canSave: Bool { amount > 0 && person != nil }

    var isDirty: Bool { draft != initial }

    var draft: LoanDraft? {
        guard let person else { return nil }
        return LoanDraft(
            lenderId: isLent ? Person.me : person, borrowerId: isLent ? person : Person.me, amount: amount, currency: currency,
            rate: rate, reason: reason.isEmpty ? nil : reason, date: date,
            installments: hasInstallments ? Loan.Installments(count: count, frequency: frequency, firstDue: firstDue) : nil,
            dueDate: hasInstallments ? nil : dueDate
        )
    }

    /// "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec".
    var schedulePreview: String? {
        guard hasInstallments, amount > 0 else { return nil }
        return Books.schedulePreview(amount: amount, currency: currency, count: count, frequency: frequency, firstDue: firstDue)
    }
}

extension Loan.Frequency {
    /// The Repeats row and sheet.
    var title: String {
        switch self {
        case .weekly: "Weekly"
        case .biweekly: "Every 2 weeks"
        case .monthly: "Monthly"
        }
    }
}
