import Foundation

/// A recurring expense rule (domain.md §1.8). The schedule's day comes from `anchorDate`.
nonisolated struct RecurringRule: Codable, Hashable, Identifiable, Sendable {
    struct RuleSplit: Codable, Hashable, Sendable {
        var mode: SplitMode = .equal
        var personIds: [PersonID]
    }

    var id: RuleID
    var groupId: GroupID?
    var title: String
    var category: ExpenseCategory
    /// nil when the amount is variable.
    var amount: Int64?
    var currency: String
    var variable: Bool
    var frequency: RecurrenceFrequency
    var anchorDate: LocalDay
    var startDate: LocalDay
    var lastOccurrence: LocalDay?
    var payerId: PersonID
    var split: RuleSplit
    var createdAt: Date
    var createdBy: PersonID
    var active: Bool

    // lane fields: add optional fields below with a default.
}

nonisolated enum RecurrenceFrequency: String, Codable, Sendable, CaseIterable {
    case weekly
    /// Every other week on the anchor's weekday (the Repeat sheet's Custom).
    case biweekly
    case monthly
    case yearly
}

/// A variable rule's occurrence waiting for its amount. Drafts never count anywhere.
nonisolated struct RecurringDraft: Codable, Hashable, Identifiable, Sendable {
    var id: DraftID
    var ruleId: RuleID
    var occurrenceDate: LocalDay
    var createdAt: Date
    /// Set once Enter amount created the expense.
    var expenseId: ExpenseID?
}

/// What the Repeat sheet returns to the Add expense form (a rule is created when the expense saves).
nonisolated struct RepeatRule: Codable, Hashable, Sendable {
    var frequency: RecurrenceFrequency
    /// The day the schedule follows (day of month, weekday or day and month).
    var anchorDate: LocalDay
    /// "Amount changes each time": creates drafts instead of expenses.
    var variable = false
}
