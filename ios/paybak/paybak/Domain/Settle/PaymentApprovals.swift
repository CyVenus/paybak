import Foundation

/// The payer's side of a confirm: payments you made that the friend has just confirmed, found by
/// comparing the ledger before and after a change, so any source counts (the friend's confirm, a
/// sync). The payment-approved scene celebrates them.
nonisolated enum PaymentApprovals {
    /// Your payments to a friend that were pending in `old` and are confirmed in `new`, in `new`'s
    /// order. Claims a friend made to you are confirmed by you, so they never count.
    static func approved(from old: Ledger, to new: Ledger) -> [Payment] {
        let wasPending = Set(old.payments.lazy.filter { $0.status == .pending }.map(\.id))
        return new.payments.filter { payment in
            isYoursToAFriend(payment) && payment.status == .confirmed && wasPending.contains(payment.id)
        }
    }

    /// Payments `new` adds that you recorded paying a friend, waiting for their confirm.
    static func recorded(from old: Ledger, to new: Ledger) -> [Payment] {
        let known = Set(old.payments.map(\.id))
        return new.payments.filter { payment in
            !known.contains(payment.id) && isYoursToAFriend(payment)
                && payment.recordedBy == Person.me && payment.status == .pending
        }
    }

    /// The scene's line. One friend in one currency: "Meera confirmed ₹450" (several payments add
    /// up). Otherwise "Meera and Kabir confirmed your payments".
    static func headline(for payments: [Payment], in ledger: Ledger) -> String {
        var friends: [PersonID] = []
        for payment in payments where !friends.contains(payment.toId) {
            friends.append(payment.toId)
        }
        let names = friends.map { ledger.person($0)?.firstName ?? "Someone" }
        let currencies = Set(payments.map(\.currency))
        if names.count == 1, currencies.count == 1, let currency = currencies.first {
            let total = payments.reduce(Int64(0)) { $0 + $1.amount }
            return "\(names[0]) confirmed \(Money.format(total, currency))"
        }
        return "\(Format.joinedNames(names)) confirmed your payments"
    }

    private static func isYoursToAFriend(_ payment: Payment) -> Bool {
        payment.fromId == Person.me && payment.toId != Person.me
    }
}
