import Foundation

/// The payment detail (record-lend-group §3, settle §5): hero, status notice, detail rows and what
/// the viewer may do, per status and side.
nonisolated struct PaymentDetail: Sendable {
    enum Notice: Hashable, Sendable {
        /// You recorded it; waiting for them.
        case awaitingThem(name: String)
        /// They say they paid you; you confirm or say Not received.
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
    /// Edit and Cancel payment: the recorder, while it isn't confirmed or cancelled.
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
        case .pending: payment.recordedBy == Person.me ? .awaitingThem(name: other) : .awaitingYou(name: other)
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
        let paidTo = payment.toId == Person.me ? myUPI : ledger.person(payment.toId)?.upi
        if payment.method == .upi, let paidTo, !paidTo.isEmpty {
            rows.append(PaymentDetail.Row(title: "Paid to", value: paidTo))
        }
        rows.append(PaymentDetail.Row(title: "Date", value: Format.dayWithYear(payment.date, today: today)))
        rows.append(PaymentDetail.Row(title: "For", value: context ?? "No group"))
        rows.append(PaymentDetail.Row(title: "Proof", value: payment.proof == nil ? "None" : "1 photo"))
        let isOpen = payment.status == .pending || payment.status == .notReceived
        return PaymentDetail(
            payment: payment,
            title: title,
            amount: Money.format(payment.amount, payment.currency),
            meta: ([payment.method.label, Format.rowDate(payment.date, today: today)] + [context].compactMap(\.self)).joined(separator: " · "),
            notice: notice,
            rows: rows,
            footnote: isOpen && payment.recordedBy == Person.me ? "Your balance updates once \(other) confirms." : nil,
            canChange: isOpen && payment.recordedBy == Person.me
        )
    }

    /// The group, expense or loan a payment is filed under; nil for a plain direct payment.
    func paymentContextName(_ payment: Payment) -> String? {
        if let id = payment.groupId, let group = ledger.group(id) { return group.name }
        if let id = payment.loanId, let loan = ledger.loan(id) { return loan.title }
        if let id = payment.expenseId, let expense = ledger.expense(id) { return expense.title }
        return nil
    }

    /// The Cancel payment alert's message: "Meera won’t be asked to confirm. You’ll still owe her ₹450."
    func cancelPaymentMessage(_ payment: Payment) -> String {
        let other = payment.otherPartyId
        let first = "\(firstName(other)) won’t be asked to confirm."
        let owe = -friendNets()[other, default: 0]
        guard payment.fromId == Person.me, owe > 0 else { return first }
        let object = switch ledger.person(other)?.pronoun {
        case .she: "her"
        case .he: "him"
        default: firstName(other)
        }
        return "\(first) You’ll still owe \(object) \(Money.format(owe, defaultCurrency))."
    }
}
