import Foundation

/// What a payment between you and a friend can be filed under (Record payment's "For" row).
nonisolated enum PaymentFor: Hashable, Sendable {
    /// Directly between you, optionally labelled with an expense ("for Weekend groceries").
    case direct(expense: ExpenseID?)
    case group(GroupID)
    case loan(LoanID)
}

nonisolated extension Books {
    /// The ＋ sheet's Record payment prefill (record-lend-group §2.4): of the people you pay in Settle
    /// up, the one whose debt you took on most recently, as friend, the open amount where it's filed,
    /// and that context. Nil when you owe nobody.
    func suggestedPayment() -> Obligation? {
        var best: (item: Obligation, since: Date)?
        for row in settleRows().pay {
            guard let lead = row.lead else { continue }
            let context: PaymentFor = switch lead.kind {
            case .group, .project: .group(lead.ref)
            case .loan: .loan(lead.ref)
            case .direct: .direct(expense: nil)
            }
            let amount = -openBalance(with: row.friend, for: context)
            guard amount > 0 else { continue }
            let since = recency(lead)
            if best.map({ since > $0.since }) ?? true {
                var item = lead
                item.amount = amount
                best = (item, since)
            }
        }
        return best?.item
    }

    /// What you and a friend owe each other in one context, from the friend's side: > 0 they owe you.
    func openBalance(with friend: PersonID, for context: PaymentFor) -> Int64 {
        let match: (BalanceContext) -> Bool = switch context {
        case .direct: { $0.kind == .direct }
        case .group(let id): { $0.ref == id && ($0.kind == .group || $0.kind == .project) }
        case .loan(let id): { $0.ref == id && $0.kind == .loan }
        }
        return contexts().filter { $0.friend == friend && match($0) }.reduce(0) { $0 + $1.amount }
    }

    /// The contexts a payment with `friend` can be filed under: shared groups and projects that
    /// aren't archived, loans between you, then direct.
    func paymentContexts(with friend: PersonID) -> [PaymentFor] {
        let groups = ledger.groups
            .filter { $0.memberIds.contains(Person.me) && $0.memberIds.contains(friend) && !$0.isArchived }
            .map { PaymentFor.group($0.id) }
        let loans = ledger.loans.filter { $0.friendId == friend }.map { PaymentFor.loan($0.id) }
        return groups + loans + [.direct(expense: nil)]
    }

    /// "Flat 302", "Laptop repair" (the helper's name for a group or loan); nil for what's directly
    /// between you.
    func paymentForName(_ context: PaymentFor) -> String? {
        switch context {
        case .direct: nil
        case .group(let id): ledger.group(id)?.name
        case .loan(let id): ledger.loan(id)?.title
        }
    }

    /// The For row and the summary's "for …": "Flat 302", "Loan · Laptop repair"; nil for what's
    /// directly between you.
    func paymentForLabel(_ context: PaymentFor) -> String? {
        switch context {
        case .direct: nil
        case .group(let id): ledger.group(id)?.name
        case .loan(let id): ledger.loan(id).map { "Loan · \($0.title)" }
        }
    }

    /// The open balance with `friend` where a payment is filed, in that context's own currency
    /// (Android's `openBalance`): a group's plan after Simplify debts, a loan's remainder, or what's
    /// directly between you. > 0: they owe you.
    func contextBalance(with friend: PersonID, for context: PaymentFor) -> Int64 {
        switch context {
        case .group(let id):
            return groupPlan(id).reduce(0) { sum, transfer in
                if transfer.from == friend && transfer.to == Person.me { return sum + transfer.amount }
                if transfer.from == Person.me && transfer.to == friend { return sum - transfer.amount }
                return sum
            }
        case .loan(let id):
            return ledger.loan(id).flatMap { loanContext($0)?.amount } ?? 0
        case .direct:
            return directContext(friend)?.amount ?? 0
        }
    }

    /// The amount helper: "You owe Meera ₹450 in Flat 302", "Rohan owes you ₹800", in the currency of
    /// what it's for.
    func openBalanceHelper(with friend: PersonID, for context: PaymentFor) -> String? {
        let balance = contextBalance(with: friend, for: context)
        guard balance != 0 else { return nil }
        let currency = switch context {
        case .group(let id): ledger.group(id)?.currency ?? defaultCurrency
        case .loan(let id): ledger.loan(id)?.currency ?? defaultCurrency
        case .direct: defaultCurrency
        }
        let amount = Money.format(abs(balance), currency)
        let phrase = balance < 0 ? "You owe \(firstName(friend)) \(amount)" : "\(firstName(friend)) owes you \(amount)"
        switch context {
        case .direct: return phrase
        case .group: return paymentForName(context).map { "\(phrase) in \($0)" } ?? phrase
        case .loan: return paymentForName(context).map { "\(phrase) for \($0)" } ?? phrase
        }
    }

    /// The Record payment summary: "You paid Meera ₹450 in cash for Flat 302.\nMeera will be asked
    /// to confirm. Paybak never moves money."
    func paymentSummary(from: PersonID, to: PersonID, amount: Int64, currency: String, method: PaymentMethodKind,
                        context: PaymentFor) -> String {
        let how = switch method {
        case .cash: " in cash"
        case .upi: " by UPI"
        case .bank: " by bank transfer"
        case .card: " by card"
        case .other: ""
        }
        let forText = paymentForLabel(context).map { " for \($0)" } ?? ""
        let paid = "\(firstName(from)) paid \(name(to)) \(Money.format(amount, currency))\(how)\(forText)."
        let next = to == Person.me
            ? "It counts once you save. Paybak never moves money."
            : "\(firstName(to)) will be asked to confirm. Paybak never moves money."
        return paid + "\n" + next
    }

    /// When you took on the debt behind an obligation: the expense or loan, or the newest expense
    /// someone else paid in its group.
    private func recency(_ item: Obligation) -> Date {
        switch item.kind {
        case .direct: return ledger.expense(item.ref)?.createdAt ?? .distantPast
        case .loan: return ledger.loan(item.ref)?.createdAt ?? .distantPast
        case .group, .project:
            return liveExpenses().filter { $0.groupId == item.ref && $0.payerId != Person.me }.map(\.createdAt).max() ?? .distantPast
        }
    }
}
