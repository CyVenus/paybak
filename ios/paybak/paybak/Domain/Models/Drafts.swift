import Foundation

/// What the Add expense form (and the assistant, receipt scan and recurring drafts) hands to
/// `addExpense` / `updateExpense`. Row `value`s follow the split mode (exact minor units, basis
/// points, shares; nil for equal); the action computes the saved shares.
nonisolated struct ExpenseDraft: Codable, Hashable, Sendable {
    var groupId: GroupID?
    var title = ""
    var category: ExpenseCategory = .other
    var amount: Int64 = 0
    var currency: String
    var rate: Rate?
    var date: LocalDay
    var dueDate: LocalDay?
    var payers: [Payer]
    var splitMode: SplitMode = .equal
    /// Everyone on the expense in the order they were added; `share` is only read for itemized.
    var rows: [SplitRow]
    var itemized: Itemized?
    var notes: String?
    var receipt: Receipt?
    /// Set by the Repeat row: saving also creates the recurring rule.
    var repeatRule: RepeatRule?

    init(groupId: GroupID? = nil, title: String = "", category: ExpenseCategory = .other, amount: Int64 = 0,
         currency: String, rate: Rate? = nil, date: LocalDay, dueDate: LocalDay? = nil, payers: [Payer] = [],
         splitMode: SplitMode = .equal, rows: [SplitRow] = [], itemized: Itemized? = nil, notes: String? = nil,
         receipt: Receipt? = nil, repeatRule: RepeatRule? = nil) {
        self.groupId = groupId
        self.title = title
        self.category = category
        self.amount = amount
        self.currency = currency
        self.rate = rate
        self.date = date
        self.dueDate = dueDate
        self.payers = payers
        self.splitMode = splitMode
        self.rows = rows
        self.itemized = itemized
        self.notes = notes
        self.receipt = receipt
        self.repeatRule = repeatRule
    }

    /// The draft that edits a saved expense.
    init(_ expense: Expense) {
        self.init(groupId: expense.groupId, title: expense.title, category: expense.category, amount: expense.amount,
                  currency: expense.currency, rate: expense.rate, date: expense.date, dueDate: expense.dueDate,
                  payers: expense.payers, splitMode: expense.split.mode, rows: expense.split.rows,
                  itemized: expense.itemized, notes: expense.notes, receipt: expense.receipt)
    }

    /// People with an included row.
    var includedIds: [PersonID] { rows.filter(\.included).map(\.personId) }
}

/// A payment to record (Record payment, settle-up prefills, loan repayments). `id` is only set by
/// the demo seed.
nonisolated struct PaymentDraft: Codable, Hashable, Sendable {
    var id: PaymentID?
    var fromId: PersonID
    var toId: PersonID
    var amount: Int64
    var currency: String
    var rate: Rate?
    var method: PaymentMethodKind = .upi
    var date: LocalDay
    var groupId: GroupID?
    var loanId: LoanID?
    var expenseId: ExpenseID?
    var note: String?
    var proof: String?
    var recordedBy: PersonID = Person.me
}

/// A loan to add (Lend money). `id` is only set by the demo seed.
nonisolated struct LoanDraft: Codable, Hashable, Sendable {
    var id: LoanID?
    var lenderId: PersonID
    var borrowerId: PersonID
    var amount: Int64
    var currency: String
    var rate: Rate?
    var reason: String?
    var date: LocalDay
    var installments: Loan.Installments?
    var dueDate: LocalDay?
}

/// A group or project to create (New group). `id` is only set by the demo seed.
nonisolated struct GroupDraft: Codable, Hashable, Sendable {
    var id: GroupID?
    var kind: LedgerGroup.Kind = .group
    var type: LedgerGroup.GroupType?
    var icon: String
    var name: String
    var currency: String
    /// Include `Person.me`.
    var memberIds: [PersonID]
    var simplifyDebts = true
    var settleBy: LocalDay?
    var project: ProjectInfo?
}

/// A project part to add (Add component).
nonisolated struct ComponentDraft: Codable, Hashable, Sendable {
    var projectId: GroupID
    var name: String
    var status: ProjectComponent.Status = .planned
    var estimatedCost: Int64?
    var actualCost: Int64?
    var paidBy: PersonID = Person.me
}

/// Why an action was refused. The message is user-facing copy.
nonisolated enum LedgerError: LocalizedError, Equatable {
    case notFound
    case invalidAmount
    case needsSomeoneElse
    case splitDoesNotAddUp(remaining: Int64)
    case balanceNotSettled(amount: Int64, currency: String)
    case notAllowed

    var errorDescription: String? {
        switch self {
        case .notFound: "That record no longer exists."
        case .invalidAmount: "Enter an amount above zero."
        case .needsSomeoneElse: "Add at least one other person."
        case .splitDoesNotAddUp(let remaining): "The split is off by \(Money.format(abs(remaining)))."
        case .balanceNotSettled(let amount, let currency): "There’s still \(Money.format(abs(amount), currency)) to settle first."
        case .notAllowed: "That can’t be changed."
        }
    }
}
