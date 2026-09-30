import Foundation

/// An expense (domain.md §1.4). Amounts are minor units of `currency`.
nonisolated struct Expense: Codable, Hashable, Identifiable, Sendable {
    var id: ExpenseID
    /// nil = direct, between the people on it. Never a project.
    var groupId: GroupID?
    var title: String
    var category: ExpenseCategory
    /// The total, > 0.
    var amount: Int64
    var currency: String
    /// Required when `currency` isn't the default currency; saved once.
    var rate: Rate?
    var date: LocalDay
    /// When the others should pay back. Inside a group the effective due date is `dueDate ?? settleBy`.
    var dueDate: LocalDay?
    var payers: [Payer]
    var split: Split
    var itemized: Itemized?
    var notes: String?
    var receipt: Receipt?
    var recurringRuleId: RuleID?
    var occurrenceDate: LocalDay?
    var createdAt: Date
    var createdBy: PersonID
    /// Oldest first.
    var history: [HistoryEntry]
    /// Oldest first.
    var comments: [Comment]
    /// "Disputed". A flagged expense still counts everywhere.
    var flag: Flag?
    /// Soft delete: excluded from every calculation, purged 30 days later.
    var deletedAt: Date?
    var deletedBy: PersonID?

    // lane fields: add optional fields below with a default.

    /// The first payer (one payer in every designed case).
    var payerId: PersonID { payers.first?.personId ?? Person.me }

    /// A person's saved share (0 when they're not on it).
    func share(of person: PersonID) -> Int64 {
        split.rows.first { $0.personId == person }?.share ?? 0
    }

    func paid(by person: PersonID) -> Int64 {
        payers.filter { $0.personId == person }.reduce(0) { $0 + $1.amount }
    }

    /// Everyone with a row in the split (included or not).
    var participantIds: [PersonID] { split.rows.map(\.personId) }

    var isDeleted: Bool { deletedAt != nil }
}

nonisolated struct Payer: Codable, Hashable, Sendable {
    var personId: PersonID
    var amount: Int64
}

nonisolated enum SplitMode: String, Codable, Sendable, CaseIterable {
    case equal
    case exact
    case percent
    case shares
    case itemized
}

nonisolated struct Split: Codable, Hashable, Sendable {
    var mode: SplitMode
    var rows: [SplitRow]
}

/// One person's line of a split. `value` is what they entered (exact minor units, basis points or
/// shares; nil for equal); `share` is the saved result in minor units.
nonisolated struct SplitRow: Codable, Hashable, Sendable {
    var personId: PersonID
    var included = true
    var value: Int64?
    var share: Int64 = 0
}

/// A receipt split (domain.md §4.2 itemized).
nonisolated struct Itemized: Codable, Hashable, Sendable {
    struct Item: Codable, Hashable, Sendable {
        var label: String
        var amount: Int64
        var personIds: [PersonID]
    }

    struct Line: Codable, Hashable, Sendable {
        var label: String
        var amount: Int64
    }

    var items: [Item]
    /// Tax, service and tip lines.
    var lines: [Line]
    var subtotal: Int64
}

nonisolated struct Receipt: Codable, Hashable, Sendable {
    /// A JPEG file name in the photos folder.
    var photo: String?
    /// A bundled image name (demo only).
    var asset: String?
    var addedBy: PersonID
    var addedAt: Date
}

/// One entry of an expense's history (domain.md §1.4). `old`/`new` carry the changed value where the
/// kind has one (an amount for `amountChanged`, text for `titleChanged`).
nonisolated struct HistoryEntry: Codable, Hashable, Sendable {
    enum Kind: String, Codable, Sendable {
        case created
        case amountChanged
        case titleChanged
        case dateChanged
        case splitChanged
        case payersChanged
        case categoryChanged
        case receiptAdded
        case flagged
        case flagRemoved
        case flagResolved
        case deleted
        case restored
    }

    var kind: Kind
    var at: Date
    var by: PersonID
    var old: HistoryValue?
    var new: HistoryValue?
}

/// A changed value in a history entry: an amount (minor units) or text.
nonisolated enum HistoryValue: Codable, Hashable, Sendable {
    case amount(Int64)
    case text(String)

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let amount = try? container.decode(Int64.self) {
            self = .amount(amount)
        } else {
            self = .text(try container.decode(String.self))
        }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .amount(let amount): try container.encode(amount)
        case .text(let text): try container.encode(text)
        }
    }

    var amount: Int64? {
        if case .amount(let amount) = self { return amount }
        return nil
    }
}

nonisolated struct Comment: Codable, Hashable, Identifiable, Sendable {
    var id: String
    var by: PersonID
    var at: Date
    var text: String
}

nonisolated struct Flag: Codable, Hashable, Sendable {
    var by: PersonID
    var note: String
    var at: Date
}

/// The fixed category list, in picker order (domain.md §1.12).
nonisolated enum ExpenseCategory: String, Codable, Sendable, CaseIterable {
    case food
    case travel
    case stays
    case fun
    case rent
    case bills
    case shopping
    case other

    var name: String {
        switch self {
        case .food: "Food"
        case .travel: "Travel"
        case .stays: "Stays"
        case .fun: "Fun"
        case .rent: "Rent"
        case .bills: "Bills"
        case .shopping: "Shopping"
        case .other: "Other"
        }
    }

    /// The icon key (`PBIcon` raw value).
    var icon: String {
        switch self {
        case .food: "food"
        case .travel: "car"
        case .stays: "bed"
        case .fun: "ticket"
        case .rent: "home"
        case .bills: "bolt"
        case .shopping: "shopping-bag"
        case .other: "tag"
        }
    }
}
