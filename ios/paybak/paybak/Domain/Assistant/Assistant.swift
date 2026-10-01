import Foundation

/// A suggested prompt on Ask Paybak's start screen.
nonisolated struct AssistantSuggestion: Hashable, Sendable {
    /// A `PBIcon` raw value.
    var icon: String
    var prompt: String
}

/// Ask Paybak's answer to one prompt (screens-insights-ai §3.6): the reply, any further messages,
/// one card and the action chips. Everything is computed from the ledger when it's asked.
nonisolated struct AssistantReply: Hashable, Sendable {
    struct PersonLine: Hashable, Sendable, Identifiable {
        var id: PersonID
        var name: String
        var amount: String
        /// "Overdue 3 days"
        var overdue: String?
    }

    /// The drafted expense and its card copy.
    struct DraftCard: Hashable, Sendable {
        var draft: ExpenseDraft
        var title: String
        var amount: String
        /// A `PBIcon` raw value.
        var icon: String
        /// "Paid by you · Today"
        var paidLine: String
        /// "Split equally with Esha and Dev"
        var splitLine: String
        /// "₹200 each" or "About ₹267 each".
        var eachLine: String
        /// You first, then the people as named.
        var people: [PersonID]
    }

    enum Card: Hashable, Sendable {
        case people([PersonLine])
        case category(InsightsPage.BarRow)
        case draft(DraftCard)
        /// The suggested prompts again (the fallback).
        case suggestions
    }

    enum Chip: Hashable, Sendable {
        case remind(PersonID, name: String, context: Obligation)
        case seeInsights(YearMonth)
        case settleUp(GroupID)
        case openGroup(GroupID, name: String, isProject: Bool)
    }

    var text: String
    /// Messages after the reply (a drafted reminder, names it couldn't find).
    var more: [String] = []
    var card: Card?
    var chips: [Chip] = []
}

nonisolated extension Books {
    /// The four prompts: Figma's for the demo; another account gets its soonest-due group and its
    /// most overdue debtor, and a prompt with nothing behind it is left out (§3.6.1).
    func suggestedPrompts() -> [AssistantSuggestion] {
        var prompts = [
            AssistantSuggestion(icon: "people", prompt: "Who owes me money?"),
            AssistantSuggestion(icon: "food", prompt: "How much did I spend on food this month?"),
        ]
        let items = openItems()
        if let group = items.filter({ $0.debtor == Person.me && $0.kind == .group && $0.due != nil }).min(by: { $0.due! < $1.due! }) {
            prompts.append(AssistantSuggestion(icon: "calendar", prompt: "When is \(groupName(group.ref)) due?"))
        }
        let overdue = items.filter { $0.isOwedToMe && ($0.due.map { $0 < today } ?? false) }
        if let debtor = overdue.min(by: { $0.due! < $1.due! }) {
            prompts.append(AssistantSuggestion(icon: "bell", prompt: "Draft a reminder for \(firstName(debtor.friend))"))
        }
        return prompts
    }

    /// The answer to a prompt. `upi` is your UPI ID for drafted reminders.
    func answer(_ prompt: String, upi: String?) -> AssistantReply {
        switch AssistantParser.intent(of: prompt) {
        case .whoOwesMe: whoOwesReply()
        case .spend(let category, let month): spendReply(category: category, monthName: month)
        case .due(let group): dueReply(groupName: group)
        case .reminder(let name): reminderReply(name: name, upi: upi)
        case .expense(let phrase): expenseReply(phrase)
        case .unknown: Self.fallback
        }
    }

    static let fallback = AssistantReply(
        text: "I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”",
        card: .suggestions
    )

    // MARK: Who owes me

    func whoOwesReply() -> AssistantReply {
        let rows = settleRows().get
        guard !rows.isEmpty else { return AssistantReply(text: whoOwesAnswer()) }
        let lines = rows.map { row in
            AssistantReply.PersonLine(
                id: row.friend, name: firstName(row.friend), amount: Money.format(row.amount, defaultCurrency),
                overdue: row.due.flatMap { $0 < today ? Format.dueBadge($0, today: today) : nil }
            )
        }
        let chips = rows.filter { $0.due.map { $0 < today } ?? false }.prefix(2).compactMap { row in
            row.lead.map { AssistantReply.Chip.remind(row.friend, name: firstName(row.friend), context: $0) }
        }
        return AssistantReply(text: whoOwesAnswer(), card: .people(lines), chips: chips)
    }

    // MARK: Spending

    /// "You spent ₹3,850 on food in September — 17% of your ₹23,300 share."
    func spendReply(category word: String, monthName: String?) -> AssistantReply {
        let category = AssistantParser.category(for: word)
        guard category != .other || word == "other" else { return Self.fallback }
        guard let month = month(named: monthName) else { return Self.fallback }
        let page = insightsPage(month)
        guard let row = page.categories.first(where: { $0.id == category.rawValue }) else {
            return AssistantReply(text: "You didn’t spend anything on \(word) in \(month.name).", chips: [.seeInsights(month)])
        }
        return AssistantReply(
            text: "You spent \(row.amount) on \(word) in \(month.name) — \(row.caption) of your \(page.total) share.",
            card: .category(row), chips: [.seeInsights(month)]
        )
    }

    /// This month, or the latest month with that name up to this one.
    private func month(named name: String?) -> YearMonth? {
        let current = YearMonth(today)
        guard let name else { return current }
        guard let number = Format.monthNames.firstIndex(where: { $0.lowercased() == name || $0.lowercased().hasPrefix(name) && name.count >= 3 })
        else { return nil }
        let candidate = YearMonth(year: current.year, month: number + 1)
        return candidate > current ? YearMonth(year: current.year - 1, month: number + 1) : candidate
    }

    // MARK: Due dates

    /// "Your Goa Trip share of ₹1,400 is due Fri 2 Oct."
    func dueReply(groupName name: String) -> AssistantReply {
        guard let group = ledger.groups.first(where: { $0.name.lowercased() == name }) else {
            return AssistantReply(text: "I couldn’t find a group called \(name.capitalized).", card: .suggestions)
        }
        let chips: [AssistantReply.Chip] = [.settleUp(group.id), .openGroup(group.id, name: group.name, isProject: group.isProject)]
        let mine = openItems().filter { $0.debtor == Person.me && $0.ref == group.id }
        guard !mine.isEmpty else { return AssistantReply(text: "Nothing is due in \(group.name).", chips: [chips[1]]) }
        let amount = Money.format(mine.reduce(0) { $0 + $1.amount }, defaultCurrency)
        guard let due = mine.compactMap(\.due).min() else {
            return AssistantReply(text: "Your \(group.name) share of \(amount) has no due date.", chips: chips)
        }
        let when = due < today ? "was due \(Format.day(due))" : "is due \(Format.day(due))"
        return AssistantReply(text: "Your \(group.name) share of \(amount) \(when).", chips: chips)
    }

    // MARK: Reminders

    func reminderReply(name: String, upi: String?) -> AssistantReply {
        guard let person = friend(named: name) else { return AssistantReply(text: "I couldn’t find \(name.capitalized).") }
        let first = person.firstName
        let items = openItems().filter { $0.friend == person.id && $0.isOwedToMe }
        // The earliest due first; items without a due date last.
        let item = items.min { lhs, rhs in
            switch (lhs.due, rhs.due) {
            case let (left?, right?): left < right
            case (.some, .none): true
            default: false
            }
        }
        guard let item else {
            return AssistantReply(text: "\(first) doesn’t owe you anything right now.")
        }
        return AssistantReply(
            text: "Here’s a reminder for \(first). Nothing is sent until you tap Send.",
            more: [friendlyReminder(item, upi: upi)],
            chips: [.remind(person.id, name: first, context: item)]
        )
    }

    /// The Remind sheet's Friendly message: "Hi Rohan! Just a gentle reminder about ₹800 for the movie
    /// tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
    func friendlyReminder(_ item: Obligation, upi: String?) -> String {
        let subject: String = switch item.kind {
        case .direct:
            if let expense = ledger.expense(item.ref) {
                "the \(expense.title.prefix(1).lowercased() + expense.title.dropFirst()) on \(Format.short(expense.date))"
            } else {
                item.title
            }
        case .group, .project: groupName(item.ref)
        case .loan: "the loan" + (item.title.isEmpty ? "" : " for \(item.title.lowercased())")
        }
        let payMe = upi.map { " You can pay me on UPI at \($0)." } ?? ""
        return "Hi \(firstName(item.friend))! Just a gentle reminder about \(Money.format(item.amount, defaultCurrency)) for \(subject).\(payMe) Thanks."
    }

    // MARK: Drafting an expense

    func expenseReply(_ phrase: AssistantParser.ExpensePhrase) -> AssistantReply {
        let group = phrase.group.flatMap { name in ledger.groups.first { $0.name.lowercased() == name } }
        var people: [PersonID] = []
        var missing: [String] = []
        for name in phrase.names {
            if let person = friend(named: name) {
                if !people.contains(person.id) { people.append(person.id) }
            } else {
                missing.append(name.capitalized)
            }
        }
        if people.isEmpty, let group {
            people = group.memberIds.filter { $0 != Person.me }
        }
        let more = missing.map { "I couldn’t find \($0)." }
        guard !people.isEmpty else {
            return AssistantReply(text: "Who should I split it with? Try “Add ₹600 for a cab, split with Esha and Dev”.", more: more)
        }
        let currency = defaultCurrency
        let amount = MoneyInput.minor(phrase.amount, currency: currency)
        let title = phrase.what.map { $0.prefix(1).uppercased() + $0.dropFirst() } ?? "Expense"
        let category = phrase.what.map(AssistantParser.category(for:)) ?? .other
        let everyone = [Person.me] + people
        let draft = ExpenseDraft(
            groupId: group?.id, title: title, category: category, amount: amount, currency: currency, date: today,
            payers: [Payer(personId: Person.me, amount: amount)], splitMode: .equal, rows: everyone.map { SplitRow(personId: $0) }
        )
        let shares = Set(previewSplit(draft).shares.values)
        let each = shares.count == 1
            ? "\(Money.format(shares.first!, currency)) each"
            : "About \(Money.format(Money.roundedToWholeUnits(amount / Int64(everyone.count), currency), currency)) each"
        let card = AssistantReply.DraftCard(
            draft: draft, title: title, amount: Money.format(amount, currency), icon: category.icon,
            paidLine: "Paid by you · Today",
            splitLine: "Split equally with \(Format.joinedNames(people.map(firstName)))" + (group.map { " in \($0.name)" } ?? ""),
            eachLine: each, people: everyone
        )
        return AssistantReply(text: "Here’s what I’ll add. Nothing is saved until you tap Save.", more: more, card: .draft(card))
    }

    /// A friend by first name (or full name), case-insensitively.
    private func friend(named name: String) -> Person? {
        let name = name.lowercased()
        return ledger.people.first { $0.firstName.lowercased() == name } ?? ledger.people.first { $0.name.lowercased() == name }
    }
}
