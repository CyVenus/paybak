import Foundation

/// One transfer of a settle plan: `from` pays `to` `amount` (group currency).
nonisolated struct Transfer: Hashable, Sendable {
    var from: PersonID
    var to: PersonID
    var amount: Int64
}

/// Group and project balances and plans (domain.md §5.2–5.4, verify.py §5.2).
nonisolated extension Books {
    /// Each member's net in the group's currency: paid − share + payments sent − received. For a
    /// project, paid and share come from its bought/done components.
    func groupNets(_ groupId: GroupID, asOf: Date? = nil) -> [PersonID: Int64] {
        guard let group = ledger.group(groupId) else { return [:] }
        var nets = Dictionary(uniqueKeysWithValues: group.memberIds.map { ($0, Int64(0)) })
        if group.isProject {
            let (paid, share) = projectPaidShare(groupId)
            for member in group.memberIds {
                nets[member] = paid[member, default: 0] - share[member, default: 0]
            }
        } else {
            for expense in liveExpenses(asOf: asOf) where expense.groupId == groupId {
                for payer in expense.payers {
                    nets[payer.personId, default: 0] += payer.amount
                }
                for row in expense.split.rows {
                    nets[row.personId, default: 0] -= row.share
                }
            }
        }
        for payment in confirmedPayments(asOf: asOf) where payment.groupId == groupId {
            nets[payment.fromId, default: 0] += payment.amount
            nets[payment.toId, default: 0] -= payment.amount
        }
        return nets
    }

    /// What each member paid and their share of the group's live expenses.
    func groupPaidShare(_ groupId: GroupID) -> (paid: [PersonID: Int64], share: [PersonID: Int64]) {
        let members = ledger.group(groupId)?.memberIds ?? []
        var paid = Dictionary(uniqueKeysWithValues: members.map { ($0, Int64(0)) })
        var share = paid
        for expense in liveExpenses() where expense.groupId == groupId {
            for payer in expense.payers {
                paid[payer.personId, default: 0] += payer.amount
            }
            for row in expense.split.rows {
                share[row.personId, default: 0] += row.share
            }
        }
        return (paid, share)
    }

    /// Who pays whom: simplified for projects and when Simplify debts is on, pairwise otherwise.
    func groupPlan(_ groupId: GroupID, asOf: Date? = nil) -> [Transfer] {
        guard let group = ledger.group(groupId) else { return [] }
        if group.simplifyDebts || group.isProject {
            return Self.simplify(groupNets(groupId, asOf: asOf), order: group.memberIds)
        }
        return pairwisePlan(groupId, asOf: asOf)
    }

    /// Simplify off: each participant owes each payer pro rata, netted per pair (§5.3).
    func pairwisePlan(_ groupId: GroupID, asOf: Date? = nil) -> [Transfer] {
        var keys: [[PersonID]] = []
        var pairs: [[PersonID]: Int64] = [:]
        func add(_ debtor: PersonID, _ creditor: PersonID, _ amount: Int64) {
            let key = [debtor, creditor].sorted()
            if pairs[key] == nil { keys.append(key) }
            pairs[key, default: 0] += debtor == key[0] ? amount : -amount
        }
        for expense in liveExpenses(asOf: asOf) where expense.groupId == groupId {
            for row in expense.split.rows {
                for payer in expense.payers where payer.personId != row.personId {
                    add(row.personId, payer.personId, Self.proRata(row.share, payer.amount, expense.amount))
                }
            }
        }
        for payment in confirmedPayments(asOf: asOf) where payment.groupId == groupId {
            add(payment.toId, payment.fromId, payment.amount)
        }
        return keys.compactMap { key in
            let amount = pairs[key, default: 0]
            if amount > 0 { return Transfer(from: key[0], to: key[1], amount: amount) }
            if amount < 0 { return Transfer(from: key[1], to: key[0], amount: -amount) }
            return nil
        }
    }

    /// The saved rate of a foreign-currency group's newest record: converts its open balance into
    /// the default currency (§5.4). nil for a default-currency group.
    func groupRate(_ groupId: GroupID) -> Rate? {
        guard let group = ledger.group(groupId), group.currency != defaultCurrency else { return nil }
        let expenses = liveExpenses().filter { $0.groupId == groupId && $0.rate != nil }.map { ($0.createdAt, $0.rate) }
        let payments = confirmedPayments().filter { $0.groupId == groupId && $0.rate != nil }.map { ($0.createdAt, $0.rate) }
        return (expenses + payments).max { $0.0 < $1.0 }?.1
    }

    /// Fewest transfers: the largest debtor pays the largest creditor, ties by member order (§5.3).
    static func simplify(_ nets: [PersonID: Int64], order: [PersonID]) -> [Transfer] {
        var nets = nets
        var transfers: [Transfer] = []
        while true {
            let debtors = order.filter { nets[$0, default: 0] < 0 }
            let creditors = order.filter { nets[$0, default: 0] > 0 }
            // min(by:) keeps the first of equal elements, so ties go to the earlier member.
            guard let debtor = debtors.min(by: { nets[$0, default: 0] < nets[$1, default: 0] }),
                  let creditor = creditors.min(by: { nets[$0, default: 0] > nets[$1, default: 0] })
            else { return transfers }
            let amount = min(-nets[debtor, default: 0], nets[creditor, default: 0])
            transfers.append(Transfer(from: debtor, to: creditor, amount: amount))
            nets[debtor, default: 0] += amount
            nets[creditor, default: 0] -= amount
        }
    }

    /// What `a` owes `b` on one expense (negative: b owes a); several payers count pro rata.
    static func pairDebt(_ expense: Expense, _ a: PersonID, _ b: PersonID) -> Int64 {
        let aOwes = proRata(expense.share(of: a), expense.paid(by: b), expense.amount)
        let bOwes = proRata(expense.share(of: b), expense.paid(by: a), expense.amount)
        return aOwes - bOwes
    }

    /// ⌊share × paid ÷ total⌋ without overflowing.
    static func proRata(_ share: Int64, _ paid: Int64, _ total: Int64) -> Int64 {
        guard total != 0 else { return 0 }
        return total.dividingFullWidth(share.multipliedFullWidth(by: paid)).quotient
    }
}
