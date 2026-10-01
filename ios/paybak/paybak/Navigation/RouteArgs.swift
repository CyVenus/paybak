import Foundation

// Route parameters (app-architecture §2.2). They're Codable so a route can be saved and restored,
// and Hashable so routes can sit in a `NavigationStack` path.

/// Add expense: new (optionally prefilled) or editing a saved expense.
struct AddExpenseArgs: Hashable, Codable {
    var editing: ExpenseID?
    var draft: ExpenseDraft?
    /// Focus the amount on appear (a new, empty form).
    var focusAmount = true

    static let new = AddExpenseArgs()
}

/// What a payment is for: the Record payment "For" row.
enum PaymentContext: Hashable, Codable {
    case group(GroupID)
    case expense(ExpenseID)
    case loan(LoanID)
}

/// Record payment: new, prefilled (settle up, reminders, loan repayments) or editing.
struct RecordPaymentArgs: Hashable, Codable {
    var editing: PaymentID?
    var from: PersonID?
    var to: PersonID?
    var amount: Int64?
    var currency: String?
    var method: PaymentMethodKind?
    var context: PaymentContext?

    static let new = RecordPaymentArgs()
}

/// Lend money: new or editing, "I lent" or "I borrowed".
struct LendMoneyArgs: Hashable, Codable {
    enum Direction: String, Hashable, Codable {
        case lent
        case borrowed
    }

    var editing: LoanID?
    var person: PersonID?
    var direction: Direction = .lent

    static let new = LendMoneyArgs()
}

enum NewGroupMode: String, Hashable, Codable {
    case group
    case project
}

/// What a reminder is about.
enum ReminderContext: Hashable, Codable {
    case expense(ExpenseID)
    case group(GroupID)
    case loan(LoanID)
}

/// Which records an activity log lists.
enum ActivityFilter: Hashable, Codable {
    case person(PersonID)
    case group(GroupID)
    case project(GroupID)
    case category(ExpenseCategory, month: YearMonth)
}

/// A photo to show full screen.
enum PhotoRef: Hashable, Codable {
    /// A JPEG in the photos folder.
    case file(String)
    /// A bundled image (demo receipts).
    case asset(String)
}

// MARK: Picker requests (the result comes back through `AppRouter.complete(_:with:)`)

struct PeoplePickRequest: Hashable, Codable {
    enum Mode: String, Hashable, Codable {
        case single
        case multi
    }

    var id = RecordID.make()
    var mode: Mode = .multi
    var selected: [PersonID] = []
    var title = "Split with"
    var allowsGuests = true
    /// Lists you too (Split with, Record payment's From / To); off for Lend money and New group.
    var showsYou = true
}

struct CurrencyPickRequest: Hashable, Codable {
    var id = RecordID.make()
    var selected: String?
    var title = "Currency"
}

struct DatePickRequest: Hashable, Codable {
    enum Kind: String, Hashable, Codable {
        case date
        case dueDate
    }

    var id = RecordID.make()
    var kind: Kind = .date
    var selected: LocalDay?
    /// Shows "No due date".
    var allowsNone = false
    var earliest: LocalDay?
    var latest: LocalDay?
}

struct GroupPickRequest: Hashable, Codable {
    var id = RecordID.make()
    var selected: GroupID?
}

struct RepeatRuleRequest: Hashable, Codable {
    var id = RecordID.make()
    var current: RepeatRule?
    var startDate: LocalDay
}

struct ScanRequest: Hashable, Codable {
    var id = RecordID.make()
    /// The people already on the expense (the Assign step offers them).
    var people: [PersonID] = []
    /// A scanned expense's items: the modal opens straight on Assign items to change who had what.
    var itemized: Itemized?
}

/// What a picker hands back to the screen that asked (§2.7).
enum RouteResult: Hashable, Codable {
    case people([PersonID])
    case person(PersonID)
    case currency(String)
    case day(LocalDay?)
    case group(GroupID?)
    case repeatRule(RepeatRule?)
    case receipt(ReceiptResult)
}

/// A read receipt, as an itemized expense draft and the photo it came from.
struct ReceiptResult: Hashable, Codable {
    var draft: ExpenseDraft
    var photo: String?
}
