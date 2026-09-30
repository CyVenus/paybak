import Foundation

/// What a payment between you and a friend can be filed under (Record payment's "For" row).
nonisolated enum PaymentFor: Hashable, Sendable {
    /// Directly between you, optionally labelled with an expense ("for Weekend groceries").
    case direct(expense: ExpenseID?)
    case group(GroupID)
    case loan(LoanID)
}

nonisolated extension Books {
    /// The ＋ sheet's Record payment prefill (record-lend-group §2.4): the most recent debt you owe,
    /// as friend, amount and context. Nil when you owe nobody.
    func suggestedPayment() -> Obligation? {
        let mine = openItems().filter { $0.debtor == Person.me }
        return mine.enumerated().max { lhs, rhs in
            let (a, b) = (recency(lhs.element), recency(rhs.element))
            return a != b ? a < b : lhs.offset > rhs.offset
        }?.element
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

    /// "Flat 302", "Laptop repair", "Dinner at Olive Garden"; nil for a plain direct payment.
    func paymentForName(_ context: PaymentFor) -> String? {
        switch context {
        case .direct(let expense): expense.flatMap { ledger.expense($0)?.title }
        case .group(let id): ledger.group(id)?.name
        case .loan(let id): ledger.loan(id)?.title
        }
    }

    /// The amount helper: "You owe Meera ₹450 in Flat 302", "Rohan owes you ₹800".
    func openBalanceHelper(with friend: PersonID, for context: PaymentFor) -> String? {
        let balance = openBalance(with: friend, for: context)
        guard balance != 0 else { return nil }
        let amount = Money.format(abs(balance), defaultCurrency)
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
        let forText = paymentForName(context).map { " for \($0)" } ?? ""
        let paid = "\(firstName(from)) paid \(name(to)) \(Money.format(amount, currency))\(how)\(forText)."
        let next = to == Person.me
            ? "It counts as soon as you save. Paybak never moves money."
            : "\(firstName(to)) will be asked to confirm. Paybak never moves money."
        return paid + "\n" + next
    }

    /// When the debt behind an obligation was last added to.
    private func recency(_ item: Obligation) -> Date {
        switch item.kind {
        case .direct: return ledger.expense(item.ref)?.createdAt ?? .distantPast
        case .loan: return ledger.loan(item.ref)?.createdAt ?? .distantPast
        case .group, .project:
            let expenses = liveExpenses().filter { $0.groupId == item.ref }.map(\.createdAt)
            let parts = ledger.components.filter { $0.projectId == item.ref }.map(\.statusChangedAt)
            return (expenses + parts).max() ?? .distantPast
        }
    }
}
