import Foundation

/// The live result of a split while it's being edited (add-expense §3.5): each person's share as
/// `addExpense` will save it (same order and rotation counter), and what's left to assign.
nonisolated struct SplitPreview: Hashable, Sendable {
    let mode: SplitMode
    let amount: Int64
    let currency: String
    /// Every row's share in minor units (0 for excluded people).
    let shares: [PersonID: Int64]
    /// Exact: minor units still to assign; Percent: basis points; 0 when it adds up.
    let remaining: Int64
    /// Exact: Σ entered minor units; Percent: Σ basis points.
    let entered: Int64
    let includedCount: Int

    var isBalanced: Bool { remaining == 0 }

    /// Card / Split Total: "₹150 left" · "₹2,650 of ₹2,800"; "5% left" · "95% of 100%".
    var footer: (left: String, detail: String) {
        switch mode {
        case .exact:
            let status = Splits.exactStatus(total: amount, amounts: [entered], currency: currency)
            return (status.left, status.detail)
        case .percent:
            let left = remaining >= 0 ? "\(MoneyInput.percentText(remaining))% left" : "\(MoneyInput.percentText(-remaining))% over"
            return (left, "\(MoneyInput.percentText(entered))% of 100%")
        case .equal, .shares, .itemized:
            let total = Money.format(amount, currency)
            return ("\(Money.format(0, currency)) left", "\(total) of \(total)")
        }
    }

    /// The form's Split row value: "Equally · ₹700 each", "Equally · 3 people", "Exact · 4 people",
    /// or "Doesn't add up" (red) when an Exact or Percent split no longer matches the total.
    var formValue: (text: String, isError: Bool) {
        let people = "\(includedCount) \(includedCount == 1 ? "person" : "people")"
        switch mode {
        case .equal:
            // "Equally" alone until there's an amount and someone to split with.
            guard amount > 0, shares.count > 1 else { return ("Equally", false) }
            let (each, leftover) = amount.quotientAndRemainder(dividingBy: Int64(max(includedCount, 1)))
            return (leftover == 0 ? "Equally · \(Money.format(each, currency)) each" : "Equally · \(people)", false)
        case .exact, .percent:
            guard isBalanced else { return ("Doesn’t add up", true) }
            return ("\(mode == .exact ? "Exact" : "Percent") · \(people)", false)
        case .shares:
            return ("Shares · \(people)", false)
        case .itemized:
            return ("Itemized · \(people)", false)
        }
    }
}

nonisolated extension Books {
    /// The order `addExpense` shares in: the group's member order, else you first, then the order
    /// people were added (domain.md §4.1).
    func splitOrder(included: [PersonID], groupId: GroupID?) -> [PersonID] {
        if let groupId, let group = ledger.group(groupId) {
            return group.memberIds.filter(included.contains) + included.filter { !group.memberIds.contains($0) }
        }
        return included.contains(Person.me) ? [Person.me] + included.filter { $0 != Person.me } : included
    }

    /// The shares a draft's split gives right now, with the context's current rotation counter, so the
    /// preview matches what Save stores.
    func previewSplit(_ draft: ExpenseDraft) -> SplitPreview {
        let order = splitOrder(included: draft.includedIds, groupId: draft.groupId)
        let counter = ledger.rotation[draft.groupId ?? Self.rotationKey(draft.rows.map(\.personId)), default: 0]
        let values = Dictionary(draft.rows.map { ($0.personId, $0.value ?? 0) }, uniquingKeysWith: { first, _ in first })
        let total = draft.amount
        var shares: [PersonID: Int64] = [:]
        var remaining: Int64 = 0
        var entered: Int64 = 0
        switch draft.splitMode {
        case .equal:
            shares = Splits.equal(total, among: order, counter: counter).shares
        case .exact:
            entered = order.reduce(0) { $0 + values[$1, default: 0] }
            remaining = total - entered
            for person in order { shares[person] = values[person, default: 0] }
        case .percent:
            entered = order.reduce(0) { $0 + values[$1, default: 0] }
            remaining = 10_000 - entered
            shares = Splits.weighted(total, weights: values, order: order, counter: counter).shares
        case .shares:
            shares = Splits.weighted(total, weights: values, order: order, counter: counter).shares
        case .itemized:
            for row in draft.rows where row.included { shares[row.personId] = row.share }
        }
        for row in draft.rows where !row.included { shares[row.personId] = 0 }
        return SplitPreview(mode: draft.splitMode, amount: total, currency: draft.currency, shares: shares,
                            remaining: remaining, entered: entered, includedCount: order.count)
    }
}
