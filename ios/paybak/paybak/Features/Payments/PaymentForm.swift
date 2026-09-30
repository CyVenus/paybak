import Foundation
import Observation

/// Record payment's state (record-lend-group §2.4): who paid whom, the amount as typed, currency,
/// method, what it's for, the date and an optional proof photo.
@Observable
final class PaymentForm {
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
    /// The user typed an amount, so a new "For" doesn't replace it with that context's balance.
    var amountEdited = false

    @ObservationIgnored private var initial: PaymentDraft?

    init(editing: PaymentID? = nil, from: PersonID?, to: PersonID?, amount: Int64?, currency: String, rate: Rate? = nil,
         method: PaymentMethodKind, context: PaymentFor, date: LocalDay, proof: String? = nil) {
        self.editing = editing
        self.from = from
        self.to = to
        amountText = amount.map { MoneyInput.text($0, currency: currency) } ?? ""
        self.currency = currency
        self.rate = rate
        self.method = method
        self.context = context
        self.date = date
        self.proof = proof
        initial = draft
    }

    var amount: Int64 { MoneyInput.minor(amountText, currency: currency) }

    /// The friend on the other side (nil while one side is empty, or for two other people).
    var friend: PersonID? {
        if from == Person.me { return to }
        if to == Person.me { return from }
        return nil
    }

    var canSave: Bool { amount > 0 && from != nil && to != nil && from != to }

    var isDirty: Bool { draft != initial }

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

    /// From: picking the person on the other side swaps the two.
    func setFrom(_ person: PersonID) {
        if person == to { to = from }
        from = person
    }

    /// To: choosing You swaps the direction (someone paid you).
    func setTo(_ person: PersonID) {
        if person == from { from = to }
        to = person
    }
}
