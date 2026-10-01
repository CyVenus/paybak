import Foundation

// The Settle up copy (screens-settle §1–§8) built from the read models, so every string the refs show
// comes out of the records.

/// A person row of the You’re owed / You owe breakdowns (settle §1–§2).
nonisolated struct BreakdownRowCopy: Hashable, Sendable, Identifiable {
    var id: PersonID
    var name: String
    /// The lead item's expense, group or loan ("Movie tickets", "Goa Trip").
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
    /// The row's one open item, when it's just one (Remind is then about it, else about everything:
    /// Android's `row.items.singleOrNull()`).
    var onlyItem: Obligation?
    /// A payment you recorded to them that waits for their Confirm: the row shows "Pending", hides
    /// Settle and opens the payment.
    var pendingPayment: PaymentID?
}

/// What the Remind sheet shows for one friend (settle §6): their row (what it's for, the amount it
/// covers, overdue or due) and the pre-written message in each tone.
nonisolated struct RemindDraft: Hashable, Sendable {
    var personId: PersonID
    var name: String
    /// The lead item's title ("Movie tickets", "Goa Trip").
    var subtitle: String
    /// Default-currency minor units: every item the reminder covers.
    var amount: Int64
    var amountText: String
    /// "Due Sun 4 Oct" while nothing is overdue.
    var dueLabel: String?
    /// "Overdue 3 days".
    var overdue: String?
    /// What the sent reminder is filed under (the lead item, with the whole amount).
    var item: Obligation
    var friendly: String
    var neutral: String

    /// The sheet's "Remind Rohan".
    var title: String { "Remind \(name)" }

    func message(_ tone: Reminder.Tone) -> String {
        tone == .neutral ? neutral : friendly
    }
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
            subtitle: row.context,
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

    /// Your side of the fewest-payments plan (settle §3.1): everyone, or one group's own plan (its
    /// transfers with you, in the default currency). Each list is overdue first, then by due date,
    /// then in friend order.
    func settlePlan(groupId: GroupID? = nil, pay: [SettleRow], get: [SettleRow]) -> (pay: [PlanRowCopy], get: [PlanRowCopy]) {
        if let groupId, ledger.group(groupId) != nil {
            let rows = groupSettleRows(groupId)
            return (rows.pay.map(planRow), rows.get.map(planRow))
        }
        let rows = (pay + get).map(planRow)
        return (rows.filter(\.pays), rows.filter { !$0.pays })
    }

    /// One group's plan from your side (settle §3.1 step 1): a row per transfer between you and a
    /// member, in the default currency, ordered like the full plan.
    func groupSettleRows(_ groupId: GroupID) -> (pay: [SettleRow], get: [SettleRow]) {
        let order = ledger.people.map(\.id)
        let rows = contexts().filter { $0.ref == groupId && $0.amount != 0 }.map { context in
            SettleRow(friend: context.friend, amount: abs(context.amount), pay: context.amount < 0, lead: context.items.first,
                      itemCount: context.items.count)
        }
        let today = today
        func key(_ row: SettleRow) -> (Int, LocalDay, Int) {
            let overdue = row.due.map { $0 < today } ?? false
            return (overdue ? 0 : 1, row.due ?? LocalDay(year: 9999, month: 12, day: 31), order.firstIndex(of: row.friend) ?? .max)
        }
        let sorted = rows.enumerated()
            .sorted { key($0.element) != key($1.element) ? key($0.element) < key($1.element) : $0.offset < $1.offset }
            .map(\.element)
        return (sorted.filter(\.pay), sorted.filter { !$0.pay })
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
        // A payment you recorded to them, waiting for their Confirm: "Pending", no action.
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
            onlyItem: row.itemCount == 1 ? row.lead : nil,
            pendingPayment: pending
        )
    }

    /// Your newest payment to `friend` that still waits for their Confirm (in one group, or anywhere).
    func pendingPayment(to friend: PersonID, groupId: GroupID?) -> PaymentID? {
        ledger.payments.last { payment in
            payment.fromId == Person.me && payment.toId == friend && payment.status == .pending
                && (groupId == nil || payment.groupId == groupId)
        }?.id
    }

    // MARK: Remind

    /// What a reminder to `friend` covers (settle §6): the open items `context` names (or everything
    /// they owe you when it names nothing still open), led by the earliest due. Nil when they owe you
    /// nothing.
    func remindDraft(for friend: PersonID, context: ReminderContext?, upi: String?) -> RemindDraft? {
        let owed = openItems().filter { $0.friend == friend && $0.isOwedToMe }
        let matching = context.map { context in owed.filter { $0.matches(context) } } ?? []
        let items = matching.isEmpty ? owed : matching
        let far = LocalDay(year: 9999, month: 12, day: 31)
        guard let lead = items.enumerated().min(by: { ($0.element.due ?? far, $0.offset) < ($1.element.due ?? far, $1.offset) })?.element
        else { return nil }
        let amount = items.reduce(0) { $0 + $1.amount }
        let overdue = lead.due.flatMap { $0 < today ? Format.dueBadge($0, today: today) : nil }
        return RemindDraft(
            personId: friend, name: firstName(friend), subtitle: lead.title, amount: amount,
            amountText: Money.format(amount, defaultCurrency),
            dueLabel: overdue == nil ? lead.due.map(Format.dueLabel) : nil, overdue: overdue,
            item: Self.covering(lead, amount: amount),
            friendly: reminderText(lead, amount: amount, more: items.count - 1, tone: .friendly, upi: upi),
            neutral: reminderText(lead, amount: amount, more: items.count - 1, tone: .neutral, upi: upi)
        )
    }

    /// The open item a reminder to `friend` is about: the earliest-due one `context` names (else of
    /// everything they owe you), carrying the whole amount the reminder covers.
    func reminderItem(for friend: PersonID, context: ReminderContext?) -> Obligation? {
        remindDraft(for: friend, context: context, upi: nil)?.item
    }

    /// The Remind sheet's pre-written message (settle §6.3) for `item` as `reminderItem` returns it.
    /// Friendly is Figma's copy; Neutral is the spec's proposal. The UPI sentence goes when you have no
    /// UPI ID.
    func reminderMessage(_ item: Obligation, tone: Reminder.Tone, upi: String?) -> String {
        reminderText(item, amount: item.amount, more: 0, tone: tone, upi: upi)
    }

    /// "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on
    /// UPI at arjun@okaxis. Thanks." · "Hi Rohan, this is a reminder that ₹800 for movie tickets
    /// (20 Sep) is still due." With more items: "… for Goa Trip and 1 more …".
    private func reminderText(_ lead: Obligation, amount: Int64, more: Int, tone: Reminder.Tone, upi: String?) -> String {
        let first = firstName(lead.friend)
        let money = Money.format(amount, defaultCurrency)
        let upi = upi?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let payMe = upi.isEmpty ? "" : " You can pay me on UPI at \(upi)."
        let others = switch more {
        case ..<1: ""
        case 1: " and 1 more"
        default: " and \(more) more"
        }
        switch tone {
        case .friendly, .firm:
            return "Hi \(first)! Just a gentle reminder about \(money) for \(reminderSubject(lead, neutral: false))\(others).\(payMe) Thanks."
        case .neutral:
            return "Hi \(first), this is a reminder that \(money) for \(reminderSubject(lead, neutral: true))\(others) is still due.\(payMe)"
        }
    }

    /// "the movie tickets on 20 Sep" (Neutral: "movie tickets (20 Sep)"), a group's name, or "the bike
    /// service" (Neutral: "bike service") for a loan.
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
            let title = Self.lowerFirst(item.title)
            return neutral ? title : "the \(title)"
        }
    }

    /// The lead item standing for everything a reminder covers.
    private static func covering(_ lead: Obligation, amount: Int64) -> Obligation {
        var item = lead
        item.amount = amount
        return item
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
    /// Whether this item is the expense, group or loan (or the one loan installment) a reminder names.
    func matches(_ context: ReminderContext) -> Bool {
        switch context {
        case .expense(let id): kind == .direct && ref == id
        case .group(let id): (kind == .group || kind == .project) && ref == id
        case .loan(let id, let installment): kind == .loan && ref == id && (installment == nil || self.installment == installment)
        }
    }
}
