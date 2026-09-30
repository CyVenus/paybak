import Foundation

/// One Activity timeline row, derived from the records (domain.md §6.6). Nothing is stored.
nonisolated struct TimelineEvent: Hashable, Sendable, Identifiable {
    enum Kind: Hashable, Sendable {
        case expenseAdded(ExpenseID)
        case expenseEdited(ExpenseID)
        case payment(PaymentID)
        case reminderSent(ReminderID)
        case draftCreated(DraftID)
        case loanAdded(LoanID)
    }

    var id: String
    var at: Date
    var kind: Kind
    var title: String
    var subtitle: String
    /// Unsigned; nil for rows without an amount.
    var amount: String?
    /// Black amount (you paid, or money to you); gray otherwise.
    var isPrimary = false
    /// Expense added and payment events also show in Home's Recent activity.
    var showsOnHome = false
    var method: String?
    /// A status pill ("Draft").
    var badge: String?
}

/// A day group of the timeline ("Today", "Mon 28 Sep").
nonisolated struct TimelineDay: Hashable, Sendable, Identifiable {
    var id: LocalDay { day }
    var day: LocalDay
    var header: String
    var events: [TimelineEvent]
}

nonisolated extension Books {
    /// Events for you, newest first (§6.6).
    func timeline() -> [TimelineEvent] {
        var events: [TimelineEvent] = []
        let openItems = openItems()
        for expense in ledger.expenses {
            let group = expense.groupId.flatMap { ledger.group($0) }
            let involved = expense.split.rows.contains { $0.personId == Person.me } || (group?.memberIds.contains(Person.me) ?? false)
            guard involved else { continue }
            let payer = expense.payerId
            let total = Money.format(expense.amount, expense.currency)
            if expense.deletedAt == nil {
                let subtitle: String
                if payer == Person.me {
                    subtitle = group.map { "\($0.name) · You paid" } ?? "You paid · \(expense.split.rows.count) people"
                } else {
                    let word = iOwePayer(expense, openItems: openItems) ? "You owe" : "Your share"
                    subtitle = "\(group?.name ?? firstName(payer)) · \(word) \(Money.format(expense.share(of: Person.me), expense.currency))"
                }
                let actor = expense.createdBy
                events.append(TimelineEvent(
                    id: "expense:\(expense.id)", at: expense.createdAt, kind: .expenseAdded(expense.id),
                    title: actor == Person.me ? "You added \(expense.title)" : "\(firstName(actor)) added \(expense.title)",
                    subtitle: subtitle, amount: total, isPrimary: payer == Person.me, showsOnHome: true
                ))
            }
            for (index, entry) in expense.history.enumerated() where entry.kind == .amountChanged {
                let old = entry.old?.amount.map { Money.format($0, expense.currency) } ?? ""
                events.append(TimelineEvent(
                    id: "edit:\(expense.id):\(index)", at: entry.at, kind: .expenseEdited(expense.id),
                    title: "\(firstName(entry.by)) changed \(expense.title)",
                    subtitle: "\(group?.name ?? "") · Was \(old)", amount: total, isPrimary: payer == Person.me
                ))
            }
        }
        for payment in ledger.payments
        where (payment.fromId == Person.me || payment.toId == Person.me) && payment.status == .confirmed {
            guard let confirmedAt = payment.confirmedAt else { continue }
            let other = payment.otherPartyId
            let toMe = payment.toId == Person.me
            events.append(TimelineEvent(
                id: "payment:\(payment.id)", at: confirmedAt, kind: .payment(payment.id),
                title: toMe ? "\(firstName(other)) paid you" : "You paid \(firstName(other))",
                subtitle: "\(paymentFor(payment)) · \(payment.method.label) · Confirmed",
                amount: Money.format(payment.amount, payment.currency), isPrimary: toMe, showsOnHome: true,
                method: payment.method.label
            ))
        }
        for reminder in ledger.reminders where reminder.fromId == Person.me {
            let what = reminder.expenseId.flatMap { ledger.expense($0)?.title }
                ?? reminder.groupId.flatMap { ledger.group($0)?.name }
                ?? reminder.loanId.flatMap { ledger.loan($0)?.title } ?? ""
            events.append(TimelineEvent(
                id: "reminder:\(reminder.id)", at: reminder.sentAt, kind: .reminderSent(reminder.id),
                title: "Reminder sent to \(firstName(reminder.toId))",
                subtitle: "\(what) · \(Money.format(reminder.amount, reminder.currency)) · \(reminder.automatic ? "Sent automatically" : "Sent by you")"
            ))
        }
        for draft in ledger.drafts {
            guard let rule = ledger.rule(draft.ruleId) else { continue }
            events.append(TimelineEvent(
                id: "draft:\(draft.id)", at: draft.createdAt, kind: .draftCreated(draft.id),
                title: "\(rule.title) draft created", subtitle: "\(groupName(rule.groupId)) · Needs an amount", badge: "Draft"
            ))
        }
        for loan in ledger.loans {
            let lent = loan.lenderId == Person.me
            events.append(TimelineEvent(
                id: "loan:\(loan.id)", at: loan.createdAt, kind: .loanAdded(loan.id),
                title: lent ? "You lent \(firstName(loan.friendId))" : "\(firstName(loan.friendId)) lent you",
                subtitle: loan.title, amount: Money.format(loan.amount, loan.currency), isPrimary: lent
            ))
        }
        return events.enumerated()
            .filter { $0.element.at <= now }
            .sorted { $0.element.at != $1.element.at ? $0.element.at > $1.element.at : $0.offset < $1.offset }
            .map(\.element)
    }

    /// The timeline grouped by day, newest first.
    func timelineDays(_ events: [TimelineEvent]) -> [TimelineDay] {
        var days: [TimelineDay] = []
        for event in events {
            let day = day(of: event.at)
            if days.last?.day == day {
                days[days.count - 1].events.append(event)
            } else {
                days.append(TimelineDay(day: day, header: Format.dayHeader(day, today: today), events: [event]))
            }
        }
        return days
    }

    /// `iOwePayer(_:)` with the open items computed once.
    func iOwePayer(_ expense: Expense, openItems: [Obligation]) -> Bool {
        if let groupId = expense.groupId {
            return groupPlan(groupId).contains { $0.from == Person.me && $0.to == expense.payerId }
        }
        return openItems.contains { $0.debtor == Person.me && $0.ref == expense.id }
    }
}
