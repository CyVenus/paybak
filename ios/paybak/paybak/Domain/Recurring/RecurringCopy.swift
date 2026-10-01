import Foundation

/// A group's Recurring screen (screens-insights-ai §5.2): the drafts waiting for an amount and the
/// active rules with their schedules and next dates.
nonisolated struct RecurringPage: Hashable, Sendable {
    struct DraftRow: Hashable, Sendable, Identifiable {
        var id: DraftID
        var title: String
        /// "September draft · 28 Sep"
        var subtitle: String
        /// A `PBIcon` raw value.
        var icon: String
    }

    struct RuleRow: Hashable, Sendable, Identifiable {
        var id: RuleID
        var title: String
        var icon: String
        /// "Monthly on the 1st\nPaid by you"
        var subtitle: String
        /// "Next Thu 1 Oct"
        var detail: String
        /// "₹36,000" or "Varies".
        var amount: String
    }

    /// "Paybak adds these to Flat 302 on schedule."
    var intro: String
    var drafts: [DraftRow]
    var rules: [RuleRow]
}

/// Enter amount for a variable rule's draft (§5.4).
nonisolated struct DraftAmountPage: Hashable, Sendable {
    var title: String
    /// "Flat 302 · September"
    var meta: String
    var currency: String
    var paidBy: String
    /// "Equally · 3 people"
    var split: String
    /// "Mon 28 Sep"
    var date: String
    /// Already turned into an expense.
    var isDone: Bool
}

nonisolated extension Books {
    func recurringPage(_ groupId: GroupID) -> RecurringPage {
        // The active rules; drafts still waiting for an amount, in the order they came.
        let rules = ledger.recurringRules.filter { $0.groupId == groupId && $0.active }
        let drafts = ledger.drafts
            .filter { draft in draft.expenseId == nil && rules.contains { $0.id == draft.ruleId } }
            .compactMap { draft -> RecurringPage.DraftRow? in
                guard let rule = ledger.rule(draft.ruleId) else { return nil }
                return RecurringPage.DraftRow(
                    id: draft.id, title: rule.title,
                    subtitle: "\(Format.month(draft.occurrenceDate.month)) draft · \(Format.short(draft.occurrenceDate))",
                    icon: Self.ruleIcon(rule)
                )
            }
        return RecurringPage(
            intro: "Paybak adds these to \(groupName(groupId)) on schedule.",
            drafts: drafts,
            rules: rules.map { rule in
                RecurringPage.RuleRow(
                    id: rule.id, title: rule.title, icon: Self.ruleIcon(rule),
                    subtitle: "\(rule.repeatRule.schedule)\nPaid by \(rule.payerId == Person.me ? "you" : firstName(rule.payerId))",
                    detail: "Next \(Format.day(nextOccurrence(of: rule)))",
                    amount: rule.variable ? "Varies" : rule.amount.map { Money.format($0, rule.currency) } ?? "Varies"
                )
            }
        )
    }

    func draftAmountPage(_ id: DraftID) -> DraftAmountPage? {
        guard let draft = ledger.drafts.first(where: { $0.id == id }), let rule = ledger.rule(draft.ruleId) else { return nil }
        return DraftAmountPage(
            title: rule.title,
            meta: [rule.groupId.map(groupName), Format.month(draft.occurrenceDate.month)].compactMap(\.self).joined(separator: " · "),
            currency: rule.currency,
            paidBy: rule.payerId == Person.me ? "You" : firstName(rule.payerId),
            split: "Equally · \(rule.split.personIds.count) people",
            date: Format.day(draft.occurrenceDate),
            isDone: draft.expenseId != nil
        )
    }

    /// The rule's next occurrence after today (or after its last one, when that's later).
    func nextOccurrence(of rule: RecurringRule) -> LocalDay {
        Self.nextOccurrence(rule, after: max(today, rule.lastOccurrence ?? today))
    }

    /// A rule's tile: Flame for gas and Wi-Fi for internet (Figma's Cooking gas and Wi-Fi rows),
    /// otherwise its category's icon. Whole words only ("Gas bill", not "Vegas").
    static func ruleIcon(_ rule: RecurringRule) -> String {
        let words = rule.title.lowercased().split { !(($0 >= "a" && $0 <= "z") || $0 == "-") }.map(String.init)
        if words.contains("gas") { return "flame" }
        if words.contains(where: ["wi-fi", "wifi", "internet", "broadband"].contains) { return "wi-fi" }
        return rule.category.icon
    }
}

nonisolated extension RecurringRule {
    var repeatRule: RepeatRule {
        RepeatRule(frequency: frequency, anchorDate: anchorDate, variable: variable)
    }
}

/// The Repeat sheet's and the rule rows' copy (§5.3, §5.5).
nonisolated extension RepeatRule {
    /// "Monthly on the 28th" · "Weekly on Mondays" · "Every 2 weeks on Mondays" · "Yearly on 28 Sep".
    var schedule: String {
        switch frequency {
        case .weekly: "Weekly on \(Format.weekday(anchorDate))s"
        case .biweekly: "Every 2 weeks on \(Format.weekday(anchorDate))s"
        case .monthly: "Monthly on the \(dayOfMonth)"
        case .yearly: "Yearly on \(Format.short(anchorDate))"
        }
    }

    /// The Day of month row's value: "28th", "31st" (a short month uses its last day).
    var dayOfMonthValue: String {
        Format.ordinal(anchorDate.day)
    }

    /// "Paybak adds a draft on the 28th and asks you for the amount."
    var helper: String {
        let when = switch frequency {
        case .weekly: "every \(Format.weekday(anchorDate))"
        case .biweekly: "every other \(Format.weekday(anchorDate))"
        case .monthly: "on the \(dayOfMonth)"
        case .yearly: "on \(Format.short(anchorDate)) each year"
        }
        if variable { return "Paybak adds a draft \(when) and asks you for the amount." }
        return switch frequency {
        case .weekly: "Paybak adds this expense every \(Format.weekday(anchorDate))."
        case .biweekly: "Paybak adds this expense every other \(Format.weekday(anchorDate))."
        case .monthly: "Paybak adds this expense on the \(dayOfMonth) of every month."
        case .yearly: "Paybak adds this expense on \(Format.short(anchorDate)) every year."
        }
    }

    /// "Next draft: Wed 28 Oct" (variable) or "Next: Wed 28 Oct".
    func nextLine(after start: LocalDay) -> String {
        "\(variable ? "Next draft" : "Next"): \(Format.day(nextOccurrence(after: start)))"
    }

    /// The first occurrence after `day`, by the scheduler's own rule.
    func nextOccurrence(after day: LocalDay) -> LocalDay {
        let schedule = RecurringRule(
            id: "", groupId: nil, title: "", category: .other, amount: nil, currency: "", variable: variable,
            frequency: frequency, anchorDate: anchorDate, startDate: day, lastOccurrence: day, payerId: Person.me,
            split: .init(personIds: []), createdAt: .distantPast, createdBy: Person.me, active: true
        )
        return Books.nextOccurrence(schedule, after: day)
    }

    private var dayOfMonth: String {
        Format.ordinal(anchorDate.day)
    }
}
