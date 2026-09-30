import Foundation

/// One open debt between the user and a friend, in the default currency (domain.md §5.5).
nonisolated struct Obligation: Hashable, Sendable {
    enum Kind: String, Sendable {
        case direct
        case group
        case project
        case loan
    }

    var debtor: PersonID
    var creditor: PersonID
    /// Default-currency minor units, > 0.
    var amount: Int64
    var due: LocalDay?
    /// The expense title, group name or loan reason.
    var title: String
    var kind: Kind
    /// The expense id (direct), group id or loan id.
    var ref: String
    /// 1-based installment number for loans.
    var installment: Int?

    var friend: PersonID { debtor == Person.me ? creditor : debtor }
    var isOwedToMe: Bool { creditor == Person.me }
}

/// Everything between the user and one friend in one place: a group, a project, a loan or the direct
/// (no group) ledger. `amount > 0`: the friend owes the user.
nonisolated struct BalanceContext: Hashable, Sendable {
    var friend: PersonID
    var kind: Obligation.Kind
    var ref: String
    var title: String
    var amount: Int64
    var items: [Obligation]
}

nonisolated extension Books {
    /// Every context between the user and each friend (§5.5): group and project plans, the direct
    /// ledger per friend, then loans.
    func contexts(asOf: Date? = nil) -> [BalanceContext] {
        var result: [BalanceContext] = []
        for group in ledger.groups where group.memberIds.contains(Person.me) {
            let rate = groupRate(group.id)
            for transfer in groupPlan(group.id, asOf: asOf) where transfer.from == Person.me || transfer.to == Person.me {
                let value = Money.toDefault(transfer.amount, currency: group.currency, rate: rate, defaultCurrency: defaultCurrency)
                let friend = transfer.from == Person.me ? transfer.to : transfer.from
                let kind: Obligation.Kind = group.isProject ? .project : .group
                let obligation = Obligation(debtor: transfer.from, creditor: transfer.to, amount: value,
                                            due: group.isProject ? nil : group.settleBy, title: group.name, kind: kind, ref: group.id)
                result.append(BalanceContext(friend: friend, kind: kind, ref: group.id, title: group.name,
                                             amount: transfer.to == Person.me ? value : -value, items: [obligation]))
            }
        }
        for person in ledger.people {
            if let direct = directContext(person.id, asOf: asOf) {
                result.append(direct)
            }
        }
        for loan in ledger.loans {
            if let context = loanContext(loan, asOf: asOf) {
                result.append(context)
            }
        }
        return result
    }

    /// Expenses outside any group plus direct payments. Payments pay the oldest debts first, so the
    /// open items are the newest ones (§5.5).
    func directContext(_ friend: PersonID, asOf: Date? = nil) -> BalanceContext? {
        var debts: [Obligation] = []
        var net: Int64 = 0
        let expenses = liveExpenses(asOf: asOf).enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (lhs.element, rhs.element)
                if a.date != b.date { return a.date < b.date }
                if a.createdAt != b.createdAt { return a.createdAt < b.createdAt }
                return lhs.offset < rhs.offset
            }
            .map(\.element)
        for expense in expenses where expense.groupId == nil {
            let amount = Self.pairDebt(expense, friend, Person.me)  // > 0: the friend owes me
            guard amount != 0 else { continue }
            let value = toDefault(abs(amount), currency: expense.currency, rate: expense.rate)
            let (debtor, creditor) = amount > 0 ? (friend, Person.me) : (Person.me, friend)
            debts.append(Obligation(debtor: debtor, creditor: creditor, amount: value, due: expense.dueDate,
                                    title: expense.title, kind: .direct, ref: expense.id))
            net += amount > 0 ? value : -value
        }
        for payment in confirmedPayments(asOf: asOf)
        where payment.groupId == nil && payment.loanId == nil && Set([payment.fromId, payment.toId]) == Set([Person.me, friend]) {
            let value = toDefault(payment.amount, currency: payment.currency, rate: payment.rate)
            net += payment.fromId == friend ? -value : value
        }
        if debts.isEmpty && net == 0 { return nil }
        var items: [Obligation] = []
        var left = abs(net)
        for debt in debts.reversed() {  // newest first
            if left == 0 { break }
            if debt.isOwedToMe == (net > 0) {
                let take = min(left, debt.amount)
                var item = debt
                item.amount = take
                items.append(item)
                left -= take
            }
        }
        return BalanceContext(friend: friend, kind: .direct, ref: friend, title: "", amount: net, items: items)
    }

    /// A loan's remaining amount and its unpaid installments.
    func loanContext(_ loan: Loan, asOf: Date? = nil) -> BalanceContext? {
        if (asOf ?? now) < loan.createdAt { return nil }
        let paid = confirmedPayments(asOf: asOf).filter { $0.loanId == loan.id }.reduce(0) { $0 + $1.amount }
        let remaining = loan.amount - paid
        let items = installments(of: loan, asOf: asOf).enumerated().compactMap { index, installment -> Obligation? in
            guard installment.paidOn == nil else { return nil }
            return Obligation(debtor: loan.borrowerId, creditor: loan.lenderId, amount: installment.amount,
                              due: installment.due, title: loan.title, kind: .loan, ref: loan.id, installment: index + 1)
        }
        let sign: Int64 = loan.lenderId == Person.me ? 1 : -1
        return BalanceContext(friend: loan.friendId, kind: .loan, ref: loan.id, title: loan.title, amount: sign * remaining, items: items)
    }

    /// One net per friend (people order): the sum over their contexts.
    func friendNets(asOf: Date? = nil) -> [PersonID: Int64] {
        friendNets(contexts: contexts(asOf: asOf))
    }

    func friendNets(contexts: [BalanceContext]) -> [PersonID: Int64] {
        var nets = Dictionary(uniqueKeysWithValues: ledger.people.map { ($0.id, Int64(0)) })
        for context in contexts {
            nets[context.friend, default: 0] += context.amount
        }
        return nets
    }

    /// Obligations in the direction of each friend's overall net (§5.6): they drive due dates,
    /// overdue, reminders and row subtitles.
    func openItems(asOf: Date? = nil) -> [Obligation] {
        openItems(contexts: contexts(asOf: asOf))
    }

    func openItems(contexts: [BalanceContext]) -> [Obligation] {
        let nets = friendNets(contexts: contexts)
        return contexts.flatMap { context in
            context.items.filter { item in
                let net = nets[context.friend, default: 0]
                return (net > 0 && item.isOwedToMe) || (net < 0 && !item.isOwedToMe)
            }
        }
    }
}
