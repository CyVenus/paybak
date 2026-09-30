import Foundation

/// A reminder Paybak sent to a friend (domain.md §1.9): automatic from the schedule, or manual from
/// the Remind sheet. Reminders change no balance.
nonisolated struct Reminder: Codable, Hashable, Identifiable, Sendable {
    enum Tone: String, Codable, Sendable {
        case friendly
        case neutral
        case firm
    }

    enum Via: String, Codable, Sendable {
        case paybak
        case share
    }

    var id: ReminderID
    var toId: PersonID
    var fromId: PersonID = Person.me
    var amount: Int64
    var currency: String
    var expenseId: ExpenseID?
    var groupId: GroupID?
    var loanId: LoanID?
    var installment: Int?
    var sentAt: Date
    var automatic: Bool
    var message: String?
    var tone: Tone?
    var via: Via?
}

/// Something that happened to you: a row of the Notifications screen (domain.md §1.10, §6.8).
/// `params` is a snapshot taken when it was created, so its text never changes later.
nonisolated struct InboxItem: Codable, Hashable, Identifiable, Sendable {
    enum Kind: String, Codable, Sendable {
        case paymentReminder
        case monthlySummary
        case paymentConfirmed
        case paymentOverdue
        case newExpenseInGroup
        case paymentNotReceived
        case expenseFlagged
        case flagResolved
    }

    var id: InboxItemID
    var type: Kind
    var createdAt: Date
    var read: Bool
    var params: InboxParams
}

/// The union of every inbox type's parameters (domain.md §6.8); each type uses its own subset.
nonisolated struct InboxParams: Codable, Hashable, Sendable {
    var personId: PersonID?
    var actorId: PersonID?
    var expenseId: ExpenseID?
    var groupId: GroupID?
    var loanId: LoanID?
    var paymentId: PaymentID?
    var title: String?
    var amount: Int64?
    var total: Int64?
    var share: Int64?
    var currency: String?
    var method: PaymentMethodKind?
    var dueDate: LocalDay?
    var year: Int?
    var month: Int?
    var spent: Int64?
    var owed: Int64?
    var owe: Int64?
    var note: String?
}
