import Foundation

// The Settle up copy (screens-settle §1–§8) built from the read models, so every string the refs show
// comes out of the records.

/// A person row of the You’re owed / You owe breakdowns (settle §1–§2).
nonisolated struct BreakdownRowCopy: Hashable, Sendable, Identifiable {
    var id: PersonID
    var name: String
    /// The lead item's expense or group ("Movie tickets", "Goa Trip"), or "{n} expenses".
    var subtitle: String
    /// Unsigned: the row adds "+" or "−".
    var amount: String
    /// "Due Sun 4 Oct" under the amount when nothing is overdue.
    var dueLabel: String?
    /// The red "Overdue 3 days" pill.
    var overdue: String?
}

/// A row of the Settle up plan (settle §3): a payment you make or one a friend owes you.
nonisolated struct PlanRowCopy: Hashable, Sendable, Identifiable {
    var id: PersonID { friend }
    var friend: PersonID
    var name: String
    /// The group or expense it's for.
    var context: String
    /// No sign: the section says the direction.
    var amount: String
    var amountMinor: Int64
    var currency: String
    /// "Due Fri", "Overdue 3 days" or "Pending".
    var badge: String?
    var isOverdue: Bool
    /// You pay them (else they owe you).
    var pays: Bool
    /// The open item the row settles or reminds about.
    var item: Obligation?
    /// A payment you recorded to them that waits for their Confirm: the row shows "Pending", hides
    /// Settle and opens the payment.
    var pendingPayment: PaymentID?
}

/// The Not received sheet's copy (settle §8.3).
nonisolated struct NotReceivedCopy: Hashable, Sendable {
    /// "Let Esha know you haven’t received ₹700?"
    var title: String
    /// "Dinner at Olive Garden · UPI · 9:12 pm"
    var context: String
    /// The editable note.
    var note: String
    /// "Esha still owes you ₹700 until a payment is confirmed."
    var helper: String
}

nonisolated extension Books {
    // MARK: Breakdowns

    func breakdownRow(_ row: SettleRow) -> BreakdownRowCopy {
        let overdue = row.due.flatMap { $0 < today ? Format.dueBadge($0, today: today) : nil }
        return BreakdownRowCopy(
            id: row.friend,
            name: firstName(row.friend),
            subtitle: row.itemCount > 1 ? "\(row.itemCount) expenses" : row.context,
            amount: Money.format(row.amount, defaultCurrency),
            dueLabel: overdue == nil ? row.due.map(Format.dueLabel) : nil,
            overdue: overdue
        )
    }

    /// "Goa Trip uses simplified debts, so you pay Kabir directly." for each group whose plan
    /// re-routes what you owe (settle §2.2, domain.md §6.2).
    func simplifiedDebtsFootnotes(_ groups: [LedgerGroup]) -> [String] {
        groups.map { group in
            let payees = groupPlan(group.id).filter { $0.from == Person.me }.map { firstName($0.to) }
            return "\(group.name) uses simplified debts, so you pay \(Format.joinedNames(payees)) directly."
        }
    }

    // MARK: Settle up

    /// Your side of the fewest-payments plan (settle §3.1): everyone, or one group's plan in its
    /// currency. Each list keeps the plan's order.
    func settlePlan(groupId: GroupID? = nil, pay: [SettleRow], get: [SettleRow]) -> (pay: [PlanRowCopy], get: [PlanRowCopy]) {
        let rows: [PlanRowCopy]
        if let groupId, let group = ledger.group(groupId) {
            rows = groupPlanRows(group)
        } else {
            rows = (pay + get).map(planRow)
        }
        return (rows.filter(\.pays), rows.filter { !$0.pays })
    }

    /// "2 payments to make" · "1 payment to make".
    static func paymentsHeader(_ count: Int) -> String {
        "\(count) \(count == 1 ? "payment" : "payments") to make"
    }

    /// "4 people owe you" · "1 person owes you".
    static func owersHeader(_ count: Int) -> String {
        count == 1 ? "1 person owes you" : "\(count) people owe you"
    }

    private func planRow(_ row: SettleRow) -> PlanRowCopy {
        let pending = row.pay ? pendingPayment(to: row.friend, groupId: nil) : nil
        return PlanRowCopy(
            friend: row.friend,
            name: firstName(row.friend),
            context: row.context,
            amount: Money.format(row.amount, defaultCurrency),
            amountMinor: row.amount,
            currency: defaultCurrency,
            badge: pending == nil ? row.due.map { Format.dueBadge($0, today: today) } : "Pending",
            isOverdue: pending == nil && (row.due.map { $0 < today } ?? false),
            pays: row.pay,
            item: row.lead,
            pendingPayment: pending
        )
    }

    /// One group's transfers that involve you, in the group's currency.
    private func groupPlanRows(_ group: LedgerGroup) -> [PlanRowCopy] {
        let due = group.isProject ? nil : group.settleBy
        return groupPlan(group.id).compactMap { transfer -> PlanRowCopy? in
            guard transfer.from == Person.me || transfer.to == Person.me else { return nil }
            let pays = transfer.from == Person.me
            let friend = pays ? transfer.to : transfer.from
            let pending = pays ? pendingPayment(to: friend, groupId: group.id) : nil
            let item = Obligation(debtor: transfer.from, creditor: transfer.to, amount: transfer.amount, due: due, title: group.name,
                                  kind: group.isProject ? .project : .group, ref: group.id)
            return PlanRowCopy(
                friend: friend,
                name: firstName(friend),
                context: group.name,
                amount: Money.format(transfer.amount, group.currency),
                amountMinor: transfer.amount,
                currency: group.currency,
                badge: pending == nil ? due.map { Format.dueBadge($0, today: today) } : "Pending",
                isOverdue: pending == nil && (due.map { $0 < today } ?? false),
                pays: pays,
                item: item,
                pendingPayment: pending
            )
        }
    }

    /// Your newest payment to `friend` that still waits for their Confirm (in one group, or anywhere).
    func pendingPayment(to friend: PersonID, groupId: GroupID?) -> PaymentID? {
        ledger.payments.last { payment in
            payment.fromId == Person.me && payment.toId == friend && payment.status == .pending
                && (groupId == nil || payment.groupId == groupId)
        }?.id
    }

    // MARK: Remind

    /// The open item a reminder to `friend` is about: the one `context` names, else their lead item.
    func reminderItem(for friend: PersonID, context: ReminderContext?) -> Obligation? {
        let contexts = contexts()
        if let context, let match = openItems(contexts: contexts).first(where: { $0.friend == friend && $0.isOwedToMe && $0.matches(context) }) {
            return match
        }
        return settleRows(contexts: contexts).get.first { $0.friend == friend }?.lead
    }

    /// The Remind sheet's pre-written message (settle §6.3). Friendly is Figma's copy; Neutral is the
    /// spec's proposal. The UPI sentence goes when you have no UPI ID.
    func reminderMessage(_ item: Obligation, tone: Reminder.Tone, upi: String?) -> String {
        let first = firstName(item.friend)
        let amount = Money.format(item.amount, defaultCurrency)
        let payMe = upi.flatMap { $0.isEmpty ? nil : " You can pay me on UPI at \($0)." } ?? ""
        switch tone {
        case .friendly, .firm:
            return "Hi \(first)! Just a gentle reminder about \(amount) for \(reminderSubject(item, neutral: false)).\(payMe) Thanks."
        case .neutral:
            return "Hi \(first), this is a reminder that \(amount) for \(reminderSubject(item, neutral: true)) is still due.\(payMe)"
        }
    }

    /// "the movie tickets on 20 Sep" (Neutral: "movie tickets (20 Sep)"), a group's name, or "the bike
    /// service loan".
    private func reminderSubject(_ item: Obligation, neutral: Bool) -> String {
        switch item.kind {
        case .direct:
            let title = Self.lowerFirst(item.title)
            guard let expense = ledger.expense(item.ref) else { return neutral ? title : "the \(title)" }
            let date = Format.short(expense.date)
            return neutral ? "\(title) (\(date))" : "the \(title) on \(date)"
        case .group, .project:
            return item.title
        case .loan:
            let reason = ledger.loan(item.ref)?.reason.map { Self.lowerFirst($0) + " " } ?? ""
            return "the \(reason)loan"
        }
    }

    /// "Movie tickets" → "movie tickets" (keeps acronyms such as "UPI").
    static func lowerFirst(_ text: String) -> String {
        guard let first = text.first, !(text.dropFirst().first?.isUppercase ?? false) else { return text }
        return first.lowercased() + text.dropFirst()
    }

    // MARK: Not received

    func notReceivedCopy(_ payment: Payment) -> NotReceivedCopy {
        let name = firstName(payment.fromId)
        let amount = Money.format(payment.amount, payment.currency)
        let purpose = paymentFor(payment)
        let what = payment.expenseId != nil || payment.groupId != nil || payment.loanId != nil ? " for \(purpose)" : ""
        let check = switch payment.method {
        case .upi: " Could you check your UPI app?"
        case .bank: " Could you check your bank app?"
        case .card: " Could you check your card app?"
        case .cash, .other: " Could you check?"
        }
        return NotReceivedCopy(
            title: "Let \(name) know you haven’t received \(amount)?",
            context: claimCard(payment).detail,
            note: "Hi \(name), I haven’t received \(amount)\(what) yet.\(check)",
            helper: "\(name) still owes you \(amount) until a payment is confirmed."
        )
    }
}

nonisolated extension Obligation {
    /// Whether this item is the expense, group or loan a reminder names.
    func matches(_ context: ReminderContext) -> Bool {
        switch context {
        case .expense(let id): kind == .direct && ref == id
        case .group(let id): (kind == .group || kind == .project) && ref == id
        case .loan(let id): kind == .loan && ref == id
        }
    }
}
