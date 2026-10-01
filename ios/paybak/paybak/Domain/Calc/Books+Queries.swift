import Foundation

/// A group's detail (domain.md §5.2–5.4, §6.5).
nonisolated struct GroupSheet: Sendable {
    var group: LedgerGroup
    /// Live expenses, newest date first; on one date, the newest added first.
    var expenses: [Expense]
    var paid: [PersonID: Int64]
    var share: [PersonID: Int64]
    /// Group currency.
    var nets: [PersonID: Int64]
    var plan: [Transfer]
    /// Σ paid (group currency).
    var spent: Int64
    /// "21–25 Sep" (range of expense dates), nil without expenses.
    var dateRange: String?
    /// "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly."
    var simplifyFootnote: String?
    /// For a foreign-currency group: Σ of each expense converted at its own saved rate.
    var spentInDefault: Int64?

    var myNet: Int64 { nets[Person.me, default: 0] }
    var isSettled: Bool { nets.values.allSatisfy { $0 == 0 } }
}

/// What a friend page lists besides the balance (§6.5).
nonisolated struct FriendPage: Sendable {
    enum HistoryItem: Hashable, Sendable {
        case expense(Expense)
        case payment(Payment)
        case loan(Loan)

        var date: LocalDay {
            switch self {
            case .expense(let expense): expense.date
            case .payment(let payment): payment.date
            case .loan(let loan): loan.date
            }
        }
    }

    var balance: FriendBalance
    /// Non-project expenses you share, payments and loans between you, newest first.
    var history: [HistoryItem]
    /// Groups and projects with both of you.
    var groupsTogether: [LedgerGroup]
    var lastReminder: Reminder?
}

nonisolated extension Books {
    func groupSheet(_ id: GroupID) -> GroupSheet? {
        guard let group = ledger.group(id) else { return nil }
        let expenses = liveExpenses().filter { $0.groupId == id }
        let (paid, share) = group.isProject ? projectPaidShare(id) : groupPaidShare(id)
        let plan = groupPlan(id)
        let dates = expenses.map(\.date)
        let foreign = group.currency != defaultCurrency
        return GroupSheet(
            group: group,
            expenses: expenses.enumerated()
                .sorted { lhs, rhs in
                    let (a, b) = (lhs.element, rhs.element)
                    if a.date != b.date { return a.date > b.date }
                    if a.createdAt != b.createdAt { return a.createdAt > b.createdAt }
                    return lhs.offset < rhs.offset
                }
                .map(\.element),
            paid: paid,
            share: share,
            nets: groupNets(id),
            plan: plan,
            spent: paid.values.reduce(0, +),
            dateRange: dates.min().flatMap { first in dates.max().map { Format.dateRange(first, $0) } },
            simplifyFootnote: simplifyFootnote(group, plan: plan),
            spentInDefault: foreign
                ? expenses.reduce(0) { $0 + toDefault($1.amount, currency: $1.currency, rate: $1.rate) }
                : nil
        )
    }

    /// "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly." (one creditor), or
    /// "Simplify debts is on. Everyone settles in {n} payments." (several).
    func simplifyFootnote(_ group: LedgerGroup, plan: [Transfer]) -> String? {
        guard group.simplifyDebts, !plan.isEmpty else { return nil }
        let creditors = Set(plan.map(\.to))
        guard creditors.count == 1, let creditor = creditors.first else {
            return "Simplify debts is on. Everyone settles in \(plan.count) payments."
        }
        let debtors = group.memberIds.filter { member in plan.contains { $0.from == member } }.map(firstName)
        let each = debtors.count > 1 ? " each" : ""
        return "Simplify debts is on. \(Format.joinedNames(debtors))\(each) pay\(debtors == ["You"] || debtors.count > 1 ? "" : "s") \(firstName(creditor)) directly."
    }

    func friendPage(_ id: PersonID) -> FriendPage? {
        guard let balance = friendBalances().first(where: { $0.id == id }) else { return nil }
        var history: [(item: FriendPage.HistoryItem, at: Date)] = []
        func isProject(_ groupId: GroupID?) -> Bool {
            groupId.flatMap { ledger.group($0)?.isProject } ?? false
        }
        // Every live non-project expense they're on, and the payments (not project ones) and loans
        // between you.
        for expense in liveExpenses() where expense.participantIds.contains(id) && !isProject(expense.groupId) {
            history.append((.expense(expense), expense.createdAt))
        }
        for payment in ledger.payments
        where Set([payment.fromId, payment.toId]) == Set([Person.me, id]) && payment.status != .cancelled && !isProject(payment.groupId) {
            history.append((.payment(payment), payment.createdAt))
        }
        for loan in ledger.loans where loan.friendId == id {
            history.append((.loan(loan), loan.createdAt))
        }
        return FriendPage(
            balance: balance,
            // Newest date first; on one date, the newest added first.
            history: history.enumerated()
                .sorted { lhs, rhs in
                    let (a, b) = (lhs.element, rhs.element)
                    if a.item.date != b.item.date { return a.item.date > b.item.date }
                    if a.at != b.at { return a.at > b.at }
                    return lhs.offset < rhs.offset
                }
                .map(\.element.item),
            groupsTogether: ledger.groups.filter { $0.memberIds.contains(id) && $0.memberIds.contains(Person.me) },
            lastReminder: lastReminder(to: id)
        )
    }

    /// The friend page's "Last reminder sent today" · "yesterday" · "on Mon 2 Nov" (§6.5).
    func lastReminderText(_ reminder: Reminder) -> String {
        let day = day(of: reminder.sentAt)
        return switch today.days(to: day) {
        case 0: "Last reminder sent today"
        case -1: "Last reminder sent yesterday"
        default: "Last reminder sent on \(Format.day(day))"
        }
    }

    /// The loan footnote's "Last reminder sent Mon 2 Nov" (§9).
    func loanReminderText(_ reminder: Reminder) -> String {
        "Last reminder sent \(Format.day(day(of: reminder.sentAt)))"
    }
}
