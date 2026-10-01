import Foundation

/// A Notifications row: an inbox item with its copy (domain.md §6.8).
nonisolated struct InboxRow: Hashable, Sendable, Identifiable {
    var id: InboxItemID { item.id }
    var item: InboxItem
    var title: String
    var body: String
    /// "9:00 pm" today, else the row date.
    var time: String
    var isToday: Bool
}

nonisolated extension Books {
    /// Newest first.
    func inbox() -> [InboxRow] {
        ledger.inbox.enumerated()
            .sorted { $0.element.createdAt != $1.element.createdAt ? $0.element.createdAt > $1.element.createdAt : $0.offset < $1.offset }
            .map { _, item in
                let (title, body) = inboxText(item)
                return InboxRow(item: item, title: title, body: body,
                                time: Format.inboxTime(item.createdAt, today: today, calendar: calendar),
                                isToday: day(of: item.createdAt) == today)
            }
    }

    /// The title and body of an inbox item, from its snapshot params (§6.8).
    func inboxText(_ item: InboxItem) -> (title: String, body: String) {
        let p = item.params
        let currency = p.currency ?? defaultCurrency
        // The person, else whoever acted; "Someone" when neither is known.
        let person = firstName(p.personId ?? p.actorId ?? "")
        let amount = Money.format(p.amount ?? 0, currency)
        let title = p.title ?? ""
        switch item.type {
        case .newExpenseInGroup:
            return ("New expense in \(groupName(p.groupId))",
                    "\(firstName(p.actorId ?? "")) added \(title), \(Money.format(p.total ?? 0, currency)). Your share is \(Money.format(p.share ?? 0, currency)).")
        case .paymentOverdue:
            return ("Payment overdue", "\(person) owes you \(amount) for \(title). It was due on \(p.dueDate.map(Format.short) ?? "").")
        case .paymentConfirmed:
            return ("Payment confirmed", "\(person) paid you \(amount) for \(title) by \(p.method?.label ?? "").")
        case .paymentReminder:
            let body = "You owe \(person) \(amount) for \(title). \(reminderWhen(due: p.dueDate, createdAt: item.createdAt))"
            return ("Payment reminder", body.trimmingCharacters(in: .whitespaces))
        case .monthlySummary:
            let tail = if let owed = p.owed, owed > 0 {
                "You’re owed \(Money.format(owed, defaultCurrency))."
            } else if let owe = p.owe, owe > 0 {
                "You owe \(Money.format(owe, defaultCurrency))."
            } else {
                "You’re all square."
            }
            return ("Monthly summary",
                    "\(Format.month(p.month ?? today.month)): you spent \(Money.format(p.spent ?? 0, defaultCurrency)) on shared expenses. \(tail)")
        case .paymentNotReceived:
            return ("\(person) hasn’t received it", p.note ?? "")
        case .expenseFlagged:
            return ("\(person) flagged \(title)", p.note ?? "")
        case .flagResolved:
            return ("\(person) resolved your flag", title)
        }
    }

    /// "It’s due today." · "It’s due tomorrow." · "It’s due Friday." · "It’s due on 12 Oct." · "It was due on 27 Sep."
    private func reminderWhen(due: LocalDay?, createdAt: Date) -> String {
        guard let due else { return "" }
        let days = day(of: createdAt).days(to: due)
        switch days {
        case 0: return "It’s due today."
        case 1: return "It’s due tomorrow."
        case 2...6: return "It’s due \(Format.weekday(due))."
        case 7...: return "It’s due on \(Format.short(due))."
        default: return "It was due on \(Format.short(due))."
        }
    }
}
