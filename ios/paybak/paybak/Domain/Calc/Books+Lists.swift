import Foundation

/// One friend's balance for the Friends list and the friend page (domain.md §5.5, §6.5).
nonisolated struct FriendBalance: Hashable, Sendable, Identifiable {
    var id: PersonID { person.id }
    var person: Person
    /// > 0: they owe you (default currency).
    var net: Int64
    /// Their open items (in the net's direction).
    var items: [Obligation]
    /// The earliest due date among the open items.
    var due: LocalDay?
    /// They owe you and the earliest due date has passed.
    var isOverdue: Bool
}

/// One row of the Groups list (§6.5).
nonisolated struct GroupSummary: Hashable, Sendable, Identifiable {
    var id: GroupID { group.id }
    var group: LedgerGroup
    /// Your net in the group's currency (> 0: you're owed).
    var myNet: Int64
    /// Your open items in this group (default currency).
    var items: [Obligation]
    var due: LocalDay?
    /// Someone still owes someone in the group, even if you're square.
    var isOpen: Bool
}

/// A Recently deleted row (§6.5).
nonisolated struct DeletedExpenseRow: Hashable, Sendable, Identifiable {
    var id: ExpenseID { expense.id }
    var expense: Expense
    /// "Deleted by Priya on 24 Sep · 24 days left".
    var detail: String
    var daysLeft: Int
}

nonisolated extension Books {
    /// Friends in list order (§6.5): owed and overdue (most overdue first), owed by due date, you owe
    /// by due date, then no balance; amount descending, then the order they were added.
    func friendBalances(contexts: [BalanceContext]? = nil) -> [FriendBalance] {
        let contexts = contexts ?? self.contexts()
        let nets = friendNets(contexts: contexts)
        let items = openItems(contexts: contexts)
        let far = LocalDay(year: 9999, month: 12, day: 31)
        let rows = ledger.people.map { person in
            let net = nets[person.id, default: 0]
            let mine = items.filter { $0.friend == person.id }
            let due = mine.compactMap(\.due).min()
            return FriendBalance(person: person, net: net, items: mine, due: due, isOverdue: net > 0 && (due.map { $0 < today } ?? false))
        }
        func key(_ row: FriendBalance, index: Int) -> (Int, LocalDay, Int64, Int) {
            let due = row.due ?? far
            let bucket = row.net > 0 && due < today ? 0 : row.net > 0 ? 1 : row.net < 0 ? 2 : 3
            return (bucket, bucket < 3 ? due : far, -abs(row.net), index)
        }
        return rows.enumerated().sorted { key($0.element, index: $0.offset) < key($1.element, index: $1.offset) }.map(\.element)
    }

    /// Groups and projects you're in, in list order (§6.5): open ones by due date, then the rest
    /// alphabetically, archived projects last.
    func groupSummaries(contexts: [BalanceContext]? = nil) -> [GroupSummary] {
        let items = openItems(contexts: contexts ?? self.contexts())
        let far = LocalDay(year: 9999, month: 12, day: 31)
        let rows = ledger.groups.filter { $0.memberIds.contains(Person.me) }.map { group in
            let mine = items.filter { $0.ref == group.id }
            let nets = groupNets(group.id)
            return GroupSummary(group: group, myNet: nets[Person.me, default: 0], items: mine, due: mine.first?.due,
                                isOpen: nets.values.contains { $0 != 0 })
        }
        return rows.sorted { lhs, rhs in
            (lhs.group.isArchived ? 1 : 0, lhs.items.isEmpty ? 1 : 0, lhs.due ?? far, lhs.group.name)
                < (rhs.group.isArchived ? 1 : 0, rhs.items.isEmpty ? 1 : 0, rhs.due ?? far, rhs.group.name)
        }
    }

    /// Groups where you pay someone other than who paid for what you owe since your group balance was
    /// last zero: the Owe breakdown footnote ("Goa Trip uses simplified debts, …", §6.2).
    func simplifiedFootnoteGroups() -> [LedgerGroup] {
        ledger.groups.filter { group in
            guard group.simplifyDebts, group.kind == .group else { return false }
            let payees = Set(groupPlan(group.id).filter { $0.from == Person.me }.map(\.to))
            guard !payees.isEmpty else { return false }
            enum Event { case expense(Expense), payment(Payment) }
            var events: [(Date, Int, Event)] = []
            for expense in liveExpenses() where expense.groupId == group.id {
                events.append((expense.createdAt, events.count, .expense(expense)))
            }
            for payment in confirmedPayments() where payment.groupId == group.id {
                events.append((payment.confirmedAt ?? payment.createdAt, events.count, .payment(payment)))
            }
            var running: Int64 = 0
            var sinceZero: [PersonID] = []
            for (_, _, event) in events.sorted(by: { ($0.0, $0.1) < ($1.0, $1.1) }) {
                switch event {
                case .expense(let expense):
                    running += expense.paid(by: Person.me) - expense.share(of: Person.me)
                    if expense.payerId != Person.me, expense.share(of: Person.me) != 0 {
                        sinceZero.append(expense.payerId)
                    }
                case .payment(let payment):
                    running += payment.fromId == Person.me ? payment.amount : payment.toId == Person.me ? -payment.amount : 0
                }
                if running == 0 { sinceZero = [] }
            }
            return Set(sinceZero) != payees
        }
    }

    /// Deleted expenses, newest deletion first, with their retention (§6.5).
    func recentlyDeleted() -> [DeletedExpenseRow] {
        ledger.expenses.compactMap { expense -> DeletedExpenseRow? in
            guard let deletedAt = expense.deletedAt else { return nil }
            let deletedOn = day(of: deletedAt)
            let left = max(0, today.days(to: deletedOn.adding(days: Ledger.deletedRetentionDays)))
            // "Deleted by Priya", or "Deleted by you".
            let who = expense.deletedBy.map(name) ?? "you"
            return DeletedExpenseRow(expense: expense,
                                     detail: "Deleted by \(who) on \(Format.short(deletedOn)) · \(Format.daysLeft(deletedOn: deletedOn, today: today))",
                                     daysLeft: left)
        }
        .sorted { $0.expense.deletedAt! > $1.expense.deletedAt! }
    }

    /// The effective due date of an expense: its own, else the group's settle-by date.
    func effectiveDue(_ expense: Expense) -> LocalDay? {
        expense.dueDate ?? expense.groupId.flatMap { ledger.group($0)?.settleBy }
    }

    /// The latest reminder sent to a friend.
    func lastReminder(to person: PersonID) -> Reminder? {
        ledger.reminders.filter { $0.toId == person }.max { $0.sentAt < $1.sentAt }
    }

    /// The currency picker's Recent section after the default currency: the other currencies of your
    /// latest expenses (newest first), then of your latest payments, without duplicates.
    func recentCurrencies(limit: Int = 3) -> [String] {
        let expenses = ledger.expenses.enumerated()
            .sorted { $0.element.createdAt != $1.element.createdAt ? $0.element.createdAt > $1.element.createdAt : $0.offset < $1.offset }
            .map(\.element.currency)
        let payments = ledger.payments.enumerated()
            .sorted { $0.element.createdAt != $1.element.createdAt ? $0.element.createdAt > $1.element.createdAt : $0.offset < $1.offset }
            .map(\.element.currency)
        var seen: [String] = []
        for code in expenses + payments where code != defaultCurrency && !seen.contains(code) {
            seen.append(code)
            if seen.count == limit { break }
        }
        return seen
    }
}
