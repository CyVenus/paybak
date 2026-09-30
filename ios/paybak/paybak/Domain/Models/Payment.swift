import Foundation

/// A settlement recorded in Paybak; the money moved elsewhere (domain.md §1.5). Only `confirmed`
/// payments change balances.
nonisolated struct Payment: Codable, Hashable, Identifiable, Sendable {
    enum Status: String, Codable, Sendable {
        case pending
        case confirmed
        case notReceived
        case cancelled
    }

    var id: PaymentID
    var fromId: PersonID
    var toId: PersonID
    var amount: Int64
    var currency: String
    var rate: Rate?
    var method: PaymentMethodKind
    var date: LocalDay
    /// Counts in that group or project.
    var groupId: GroupID?
    /// A loan repayment.
    var loanId: LoanID?
    /// Only the "for" label ("for Weekend groceries").
    var expenseId: ExpenseID?
    var note: String?
    /// A photo file name.
    var proof: String?
    var status: Status
    var recordedBy: PersonID
    var createdAt: Date
    var confirmedAt: Date?
    var notReceivedNote: String?

    // lane fields: add optional fields below with a default.

    /// The friend on the other side of a payment you're part of.
    var otherPartyId: PersonID { fromId == Person.me ? toId : fromId }
}

nonisolated enum PaymentMethodKind: String, Codable, Sendable, CaseIterable {
    case cash
    case upi
    case bank
    case card
    case other

    var label: String {
        switch self {
        case .cash: "Cash"
        case .upi: "UPI"
        case .bank: "Bank"
        case .card: "Card"
        case .other: "Other"
        }
    }
}

/// An IOU (domain.md §1.6). One of lender and borrower is `Person.me`; repayments are payments with
/// `loanId`.
nonisolated struct Loan: Codable, Hashable, Identifiable, Sendable {
    struct Installments: Codable, Hashable, Sendable {
        var count: Int
        var frequency: Frequency
        var firstDue: LocalDay
    }

    enum Frequency: String, Codable, Sendable, CaseIterable {
        case weekly
        case biweekly
        case monthly
    }

    var id: LoanID
    var lenderId: PersonID
    var borrowerId: PersonID
    var amount: Int64
    var currency: String
    var rate: Rate?
    var reason: String?
    var date: LocalDay
    var installments: Installments?
    var dueDate: LocalDay?
    var createdAt: Date
    var createdBy: PersonID

    // lane fields: add optional fields below with a default.

    var friendId: PersonID { lenderId == Person.me ? borrowerId : lenderId }
    var title: String { reason ?? "Loan" }
}
