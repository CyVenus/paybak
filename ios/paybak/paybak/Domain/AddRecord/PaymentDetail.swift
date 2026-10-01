import Foundation

/// The payment detail (record-lend-group §3, settle §5): hero, status notice, detail rows and what
/// the viewer may do, per status and side.
nonisolated struct PaymentDetail: Sendable {
    enum Notice: Hashable, Sendable {
        /// You paid; waiting for them to confirm.
        case awaitingThem(name: String)
        /// They paid you; you confirm once the money has arrived, or say Not received.
        case awaitingYou(name: String)
        case notReceived(name: String, note: String?)
        case confirmed(text: String)
        case cancelled
    }

    struct Row: Hashable, Sendable {
        let title: String
        let value: String
    }

    let payment: Payment
    /// "You paid Meera".
    let title: String
    let amount: String
    /// "Cash · Today · Flat 302".
    let meta: String
    let notice: Notice
    let rows: [Row]
    /// "Your balance updates once Meera confirms."
    let footnote: String?
    /// Edit and Cancel payment: your own payment, recorded by you, while it waits for its
    /// confirmation (Android: pending && mine).
    let canChange: Bool
}

nonisolated extension Books {
    func paymentDetail(_ id: PaymentID, myUPI: String?) -> PaymentDetail? {
        guard let payment = ledger.payment(id) else { return nil }
        let from = firstName(payment.fromId)
        let to = firstName(payment.toId)
        let title = payment.toId == Person.me ? "\(from) paid you" : "\(from) paid \(name(payment.toId))"
        let context = paymentContextName(payment)
        let other = firstName(payment.otherPartyId)
        let notice: PaymentDetail.Notice = switch payment.status {
        case .pending: payment.fromId == Person.me ? .awaitingThem(name: other) : .awaitingYou(name: other)
        case .notReceived: .notReceived(name: other, note: payment.notReceivedNote)
        case .confirmed:
            .confirmed(text: "\(payment.toId == Person.me ? "You" : to) confirmed on \(Format.day(day(of: payment.confirmedAt ?? payment.createdAt)))")
        case .cancelled: .cancelled
        }
        var rows = [
            PaymentDetail.Row(title: "From", value: from),
            PaymentDetail.Row(title: "To", value: to),
            PaymentDetail.Row(title: "Method", value: payment.method.label),
        ]
        // "Paid to": the friend's UPI ID when you paid them by UPI.
        if payment.method == .upi, payment.fromId == Person.me, let paidTo = ledger.person(payment.toId)?.upi, !paidTo.isEmpty {
            rows.append(PaymentDetail.Row(title: "Paid to", value: paidTo))
        }
        rows.append(PaymentDetail.Row(title: "Date", value: Format.day(payment.date)))
        if let context { rows.append(PaymentDetail.Row(title: "For", value: context)) }
        rows.append(PaymentDetail.Row(title: "Proof", value: payment.proof == nil ? "None" : "1 photo"))
        return PaymentDetail(
            payment: payment,
            title: title,
            amount: Money.format(payment.amount, payment.currency),
            meta: ([payment.method.label, Format.rowDate(payment.date, today: today)] + [context].compactMap(\.self)).joined(separator: " · "),
            notice: notice,
            rows: rows,
            footnote: payment.status == .pending && payment.fromId == Person.me ? "Your balance updates once \(other) confirms." : nil,
            canChange: payment.status == .pending && payment.recordedBy == Person.me && payment.fromId == Person.me
        )
    }

    /// The group ("Flat 302"), loan ("Loan · Laptop repair") or expense a payment is filed under; nil
    /// for a plain direct payment.
    func paymentContextName(_ payment: Payment) -> String? {
        if let id = payment.groupId, let group = ledger.group(id) { return group.name }
        if let id = payment.loanId, let loan = ledger.loan(id) { return "Loan · \(loan.title)" }
        if let id = payment.expenseId, let expense = ledger.expense(id) { return expense.title }
        return nil
    }

    /// The Cancel payment alert's message: "Meera won’t be asked to confirm. You’ll still owe her ₹450."
    /// What you'd still owe is the open balance with the receiver where the payment is filed.
    func cancelPaymentMessage(_ payment: Payment) -> String {
        let person = ledger.person(payment.toId)
        let name = person?.firstName ?? "They"
        let first = "\(name) won’t be asked to confirm."
        let context: PaymentFor = payment.groupId.map { .group($0) } ?? payment.loanId.map { .loan($0) } ?? .direct(expense: nil)
        let owe = -contextBalance(with: payment.toId, for: context)
        guard owe > 0 else { return first }
        let object = switch person?.pronoun {
        case .she: "her"
        case .he: "him"
        default: name
        }
        return "\(first) You’ll still owe \(object) \(Money.format(owe, payment.currency))."
    }
}
