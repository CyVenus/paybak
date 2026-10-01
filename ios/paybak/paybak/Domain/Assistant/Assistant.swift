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
    /// The four prompts, for this account's own group and debtor (§3.6.1): who owes you (while
    /// someone does), food this month (once there's some), the soonest-due group you owe in, and the
    /// first person in "People who owe you". A prompt with nothing behind it is left out.
    func suggestedPrompts() -> [AssistantSuggestion] {
        let owers = settleRows().get
        var prompts: [AssistantSuggestion] = []
        if !owers.isEmpty {
            prompts.append(AssistantSuggestion(icon: "people", prompt: Self.whoOwesMePrompt))
        }
        let food = insights(YearMonth(today)).categories.first(where: { $0.category == .food })?.amount ?? 0
        if food > 0 {
            prompts.append(AssistantSuggestion(icon: "food", prompt: "How much did I spend on food this month?"))
        }
        let dueGroup = openItems().filter { $0.kind == .group && !$0.isOwedToMe && $0.due != nil }.min { $0.due! < $1.due! }
        if let dueGroup, let group = ledger.group(dueGroup.ref) {
            prompts.append(AssistantSuggestion(icon: "calendar", prompt: "When is \(group.name) due?"))
        }
        if let debtor = owers.first?.friend {
            prompts.append(AssistantSuggestion(icon: "bell", prompt: "Draft a reminder for \(firstName(debtor))"))
        }
        return prompts
    }

    /// The answer to a prompt. `upi` is your UPI ID for drafted reminders.
    func answer(_ prompt: String, upi: String?) -> AssistantReply {
        switch AssistantParser.intent(of: prompt) {
        case .whoOwesMe: whoOwesReply()
        case .spend(let category, let month, let year): spendReply(category: category, monthName: month, year: year)
        case .due(let group): dueReply(groupName: group)
        case .reminder(let name): reminderReply(name: name, upi: upi)
        case .expense(let phrase): expenseReply(phrase)
        case .unknown: Self.fallback
        }
    }

    static let whoOwesMePrompt = "Who owes me money?"

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

    /// "You spent ₹3,850 on food in September — 17% of your ₹23,300 share."; a word that names no
    /// category gets the fallback.
    func spendReply(category word: String, monthName: String?, year: Int? = nil) -> AssistantReply {
        let category = AssistantParser.category(for: word)
        guard category != .other || word == "other" else { return Self.fallback }
        let month = month(named: monthName, year: year) ?? YearMonth(today)
        let name = category.name.lowercased()
        let report = insights(month)
        let page = insightsPage(month)
        guard let row = page.categories.first(where: { $0.id == category.rawValue }) else {
            return AssistantReply(text: "You didn’t spend anything on \(name) in \(month.name).", chips: [.seeInsights(month)])
        }
        let amount = report.categories.first(where: { $0.category == category })?.amount ?? 0
        return AssistantReply(
            text: "You spent \(Money.format(amount, defaultCurrency)) on \(name) in \(month.name) — \(report.percent(of: category))% of your \(Money.format(report.total, defaultCurrency)) share.",
            card: .category(row), chips: [.seeInsights(month)]
        )
    }

    /// "september" → the latest September up to this month; "september 2025" → that one.
    private func month(named name: String?, year: Int?) -> YearMonth? {
        guard let name, let number = Format.monthNames.firstIndex(where: { $0.lowercased() == name }) else { return nil }
        if let year { return YearMonth(year: year, month: number + 1) }
        let current = YearMonth(today)
        let candidate = YearMonth(year: current.year, month: number + 1)
        return candidate > current ? YearMonth(year: current.year - 1, month: number + 1) : candidate
    }

    // MARK: Due dates

    /// "Your Goa Trip share of ₹1,400 is due Fri 2 Oct." (or "… is open." without a date); a name
    /// that isn't one of your groups gets the fallback.
    func dueReply(groupName name: String) -> AssistantReply {
        guard let group = ledger.groups.first(where: { $0.name.lowercased() == name }) else { return Self.fallback }
        let open: AssistantReply.Chip = .openGroup(group.id, name: group.name, isProject: group.isProject)
        guard let item = openItems().first(where: { $0.kind == .group && $0.ref == group.id && !$0.isOwedToMe }) else {
            return AssistantReply(text: "Nothing is due in \(group.name).", chips: [open])
        }
        let due = item.due.map { " is due \(Format.day($0))" } ?? " is open"
        return AssistantReply(text: "Your \(group.name) share of \(Money.format(item.amount, defaultCurrency))\(due).",
                              chips: [.settleUp(group.id), open])
    }

    // MARK: Reminders

    /// The Remind sheet's Friendly message to a friend who owes you, so the chat and the sheet say the
    /// same thing.
    func reminderReply(name: String, upi: String?) -> AssistantReply {
        guard let person = friend(named: name) else { return AssistantReply(text: Self.notFound(name)) }
        let first = person.firstName
        guard let draft = remindDraft(for: person.id, context: nil, upi: upi) else {
            return AssistantReply(text: "\(first) doesn’t owe you anything right now.")
        }
        return AssistantReply(
            text: "Here’s a reminder for \(first). Nothing is sent until you tap Send.",
            more: [draft.friendly],
            chips: [.remind(person.id, name: first, context: draft.item)]
        )
    }

    /// The Remind sheet's Friendly message: "Hi Rohan! Just a gentle reminder about ₹800 for the movie
    /// tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
    func friendlyReminder(_ item: Obligation, upi: String?) -> String {
        reminderMessage(item, tone: .friendly, upi: upi)
    }

    // MARK: Drafting an expense

    func expenseReply(_ phrase: AssistantParser.ExpensePhrase) -> AssistantReply {
        let group = phrase.group.flatMap { name in ledger.groups.first { $0.name.lowercased() == name.lowercased() } }
        // "in …" that names none of your groups stays part of what it was for.
        var what = phrase.what
        if group == nil, let place = phrase.group {
            what = what.map { "\($0) in \(place)" }
        }
        var people: [PersonID] = []
        var missing = ""
        for name in phrase.names {
            if let person = friend(named: name) {
                if !people.contains(person.id) { people.append(person.id) }
            } else {
                missing += " " + Self.notFound(name)
            }
        }
        guard !people.isEmpty else {
            return AssistantReply(text: "Who is it with? Try “Add ₹600 for a cab, split with Esha and Dev”." + missing)
        }
        let currency = group?.currency ?? defaultCurrency
        let amount = MoneyInput.minor(phrase.amount, currency: currency)
        let category = AssistantParser.guessCategory(what ?? "")
        let title = what.map { $0.prefix(1).uppercased() + $0.dropFirst() } ?? category.name
        let everyone = [Person.me] + people
        let draft = ExpenseDraft(
            groupId: group?.id, title: title, category: category, amount: amount, currency: currency, date: today,
            payers: [Payer(personId: Person.me, amount: amount)], splitMode: .equal, rows: everyone.map { SplitRow(personId: $0) }
        )
        let shares = Splits.equal(amount, among: everyone).shares.values
        let each = Money.format(shares.min() ?? 0, currency)
        let card = AssistantReply.DraftCard(
            draft: draft, title: title, amount: Money.format(amount, currency), icon: category.icon,
            paidLine: "Paid by you · Today",
            splitLine: "Split equally with \(Format.joinedNames(people.map(firstName)))",
            eachLine: Set(shares).count == 1 ? "\(each) each" : "About \(each) each", people: everyone
        )
        return AssistantReply(text: "Here’s what I’ll add. Nothing is saved until you tap Save." + missing, card: .draft(card))
    }

    /// "I couldn’t find Zed."
    private static func notFound(_ name: String) -> String {
        let name = name.trimmingCharacters(in: .whitespaces)
        return "I couldn’t find \(name.prefix(1).uppercased() + name.dropFirst())."
    }

    /// A friend by first name or full name, case-insensitively.
    private func friend(named name: String) -> Person? {
        let name = AssistantParser.normalize(name)
        return ledger.people.first { $0.firstName.lowercased() == name || $0.name.lowercased() == name }
    }
}
