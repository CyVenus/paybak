import Foundation
import Observation

/// Record payment's state (record-lend-group §2.4): who paid whom (one side is always you), the
/// amount as typed, currency, method, what it's for, the date and an optional proof photo.
@Observable
final class PaymentForm {
    /// Everything the form holds, to tell whether it changed since it opened (Discard asks first).
    struct Snapshot: Equatable {
        var from: PersonID?
        var to: PersonID?
        var amountText: String
        var amountEdited: Bool
        var currency: String
        var method: PaymentMethodKind
        var context: PaymentFor
        var date: LocalDay
        var proof: String?
    }

    let editing: PaymentID?
    var from: PersonID?
    var to: PersonID?
    var amountText: String
    var currency: String
    var rate: Rate?
    var method: PaymentMethodKind
    var context: PaymentFor
    var date: LocalDay
    var proof: String?
    /// False while the amount is still the prefilled open balance, so a new person or "For" refills
    /// it; true once it was typed (or came prefilled from a Settle up row, a friend or a loan).
    var amountEdited = false

    @ObservationIgnored private var initial: Snapshot?

    init(editing: PaymentID? = nil, from: PersonID?, to: PersonID?, amount: Int64?, currency: String, rate: Rate? = nil,
         method: PaymentMethodKind, context: PaymentFor, date: LocalDay, proof: String? = nil) {
        self.editing = editing
        self.from = from
        self.to = to
        amountText = amount.map { Self.text($0, currency: currency) } ?? ""
        self.currency = currency
        self.rate = rate
        self.method = method
        self.context = context
        self.date = date
        self.proof = proof
        amountEdited = editing != nil
        markUnchanged()
    }

    /// The state Discard compares against: the form as it opened, prefilled.
    func markUnchanged() {
        initial = snapshot
    }

    var snapshot: Snapshot {
        Snapshot(from: from, to: to, amountText: amountText, amountEdited: amountEdited, currency: currency, method: method,
                 context: context, date: date, proof: proof)
    }

    var amount: Int64 { MoneyInput.minor(amountText, currency: currency) }

    /// The friend on the other side (nil while it's still to choose).
    var friend: PersonID? {
        if from == Person.me { return to }
        if to == Person.me { return from }
        return nil
    }

    /// You paid them (else they paid you).
    var youPaid: Bool { from == Person.me }

    var canSave: Bool { amount > 0 && from != nil && to != nil && from != to }

    /// Anything changed since the form opened (an amount typed before choosing someone too).
    var isDirty: Bool { snapshot != initial }

    var draft: PaymentDraft? {
        guard let from, let to else { return nil }
        var draft = PaymentDraft(fromId: from, toId: to, amount: amount, currency: currency, rate: rate, method: method, date: date)
        switch context {
        case .direct(let expense): draft.expenseId = expense
        case .group(let id): draft.groupId = id
        case .loan(let id): draft.loanId = id
        }
        draft.proof = proof
        return draft
    }

    /// The person picked on the side that was tapped. You there puts the friend on the other side;
    /// anyone else becomes the friend, with you on the other side. A different friend starts over
    /// with nothing to file it under.
    func pick(_ person: PersonID, tappedFrom: Bool) {
        if person == Person.me {
            let friend = self.friend
            from = tappedFrom ? Person.me : friend
            to = tappedFrom ? friend : Person.me
        } else {
            if person != friend { context = .direct(expense: nil) }
            from = tappedFrom ? person : Person.me
            to = tappedFrom ? Person.me : person
        }
    }

    /// Follows the context: its currency and, unless the amount was typed, what's open between you
    /// in your direction (nothing when it runs the other way).
    func refill(in store: LedgerStore) {
        guard let friend else { return }
        let books = store.books
        let code = Self.currency(of: context, in: books)
        if code != currency {
            currency = code
            rate = store.todayRate(for: code)
        }
        guard !amountEdited else { return }
        let open = books.contextBalance(with: friend, for: context)
        let owed = youPaid ? -open : open
        amountText = Self.text(owed, currency: code)
    }

    /// The text that edits an amount: 280000 → "2800"; nothing for 0 or less.
    static func text(_ minor: Int64, currency: String) -> String {
        minor > 0 ? MoneyInput.text(minor, currency: currency) : ""
    }

    /// A payment's currency where it's filed: the group's, the loan's, or the default one.
    static func currency(of context: PaymentFor, in books: Books) -> String {
        switch context {
        case .group(let id): books.ledger.group(id)?.currency ?? books.defaultCurrency
        case .loan(let id): books.ledger.loan(id)?.currency ?? books.defaultCurrency
        case .direct: books.defaultCurrency
        }
    }
}
