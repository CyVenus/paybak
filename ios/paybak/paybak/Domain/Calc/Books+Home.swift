import Foundation

/// The two Home totals and their captions' counts (domain.md §6.1).
nonisolated struct HomeTotals: Hashable, Sendable {
    /// Σ positive friend nets.
    var owed: Int64 = 0
    var owedPeople = 0
    /// Σ |negative friend nets|.
    var owe: Int64 = 0
    /// Distinct groups and projects among what you owe.
    var oweGroups = 0
    /// Distinct people you owe outside groups.
    var owePeople = 0

    /// "from 4 people" / "from 1 person".
    var owedCaption: String {
        "from \(owedPeople) \(owedPeople == 1 ? "person" : "people")"
    }

    /// "across 2 groups" · "to 1 person" · "across 2 groups and 1 person".
    var oweCaption: String {
        let groups = "\(oweGroups) group" + (oweGroups == 1 ? "" : "s")
        let people = "\(owePeople) " + (owePeople == 1 ? "person" : "people")
        if oweGroups > 0 && owePeople > 0 { return "across \(groups) and \(people)" }
        return oweGroups > 0 ? "across \(groups)" : "to \(people)"
    }
}

/// One friend's line in Settle up and the breakdowns (§6.2–6.3).
nonisolated struct SettleRow: Hashable, Sendable {
    var friend: PersonID
    /// |net|, default currency.
    var amount: Int64
    /// You pay them (net < 0).
    var pay: Bool
    /// The earliest-due open item (else the first one).
    var lead: Obligation?
    /// How many open items make up the amount.
    var itemCount: Int

    var context: String { lead?.title ?? "" }
    var due: LocalDay? { lead?.due }
}

/// A Home "Due soon" row (§6.1).
nonisolated struct DueSoonRow: Hashable, Sendable {
    enum Action: Hashable, Sendable {
        case remind
        case settle
    }

    /// The person's first name, or the group for a group debt you owe.
    var title: String
    /// The item title, or "Your share".
    var detail: String
    var amount: Int64
    var badge: String
    var isOverdue: Bool
    var action: Action
    var due: LocalDay
    var obligation: Obligation
}

nonisolated enum HomeState: String, Sendable {
    case firstDay
    case active
    case allSettled
}

/// A Home "Recent activity" row (§6.1 copy).
nonisolated struct RecentActivityRow: Hashable, Sendable, Identifiable {
    enum Kind: Hashable, Sendable {
        case expense(ExpenseID, ExpenseCategory)
        case payment(PaymentID, person: PersonID)
    }

    var id: String
    var kind: Kind
    var title: String
    var subtitle: String
    /// Unsigned for money in, "−…" for money out.
    var amount: String
    /// Black amount (money in / you paid), else gray.
    var isIncoming: Bool
    var date: String
}

/// A payment a friend recorded to you, waiting for Confirm (§6.7).
nonisolated struct PendingClaim: Hashable, Sendable, Identifiable {
    var id: PaymentID { payment.id }
    var payment: Payment
    /// "Esha says she paid you ₹700".
    var title: String
    /// "Dinner at Olive Garden · UPI · 9:12 pm".
    var detail: String
    /// What it's for ("Dinner at Olive Garden").
    var purpose: String
}

nonisolated extension Books {
    func homeTotals(asOf: Date? = nil) -> HomeTotals {
        homeTotals(contexts: contexts(asOf: asOf))
    }

    func homeTotals(contexts: [BalanceContext]) -> HomeTotals {
        let nets = friendNets(contexts: contexts)
        var groups = Set<String>()
        var people = Set<String>()
        for item in openItems(contexts: contexts) where item.debtor == Person.me {
            if item.kind == .group || item.kind == .project {
                groups.insert(item.ref)
            } else {
                people.insert(item.friend)
            }
        }
        let owed = nets.values.filter { $0 > 0 }
        let owe = nets.values.filter { $0 < 0 }
        return HomeTotals(owed: owed.reduce(0, +), owedPeople: owed.count, owe: -owe.reduce(0, +),
                          oweGroups: groups.count, owePeople: people.count)
    }

    /// Settle up (§6.3): one row per friend with a non-zero net; payments to make and people who owe
    /// you, each overdue first, then by due date, then friend order.
    func settleRows(contexts: [BalanceContext]? = nil) -> (pay: [SettleRow], get: [SettleRow]) {
        let contexts = contexts ?? self.contexts()
        let nets = friendNets(contexts: contexts)
        let items = openItems(contexts: contexts)
        let order = ledger.people.map(\.id)
        var rows: [SettleRow] = []
        for friend in order {
            let net = nets[friend, default: 0]
            guard net != 0 else { continue }
            let mine = items.filter { $0.friend == friend }
            let lead = mine.filter { $0.due != nil }.enumerated()
                .min { $0.element.due! != $1.element.due! ? $0.element.due! < $1.element.due! : $0.offset < $1.offset }?
                .element ?? mine.first
            rows.append(SettleRow(friend: friend, amount: abs(net), pay: net < 0, lead: lead, itemCount: mine.count))
        }
        let today = today
        func key(_ row: SettleRow) -> (Int, LocalDay, Int) {
            let overdue = row.due.map { $0 < today } ?? false
            return (overdue ? 0 : 1, row.due ?? LocalDay(year: 9999, month: 12, day: 31), order.firstIndex(of: row.friend) ?? 0)
        }
        let sorted = rows.sorted { key($0) < key($1) }
        return (sorted.filter(\.pay), sorted.filter { !$0.pay })
    }

    /// Home Due soon (§6.1): open items overdue or due within 2 days, most urgent first, at most 3.
    func dueSoon(contexts: [BalanceContext]? = nil) -> [DueSoonRow] {
        let items = openItems(contexts: contexts ?? self.contexts())
        let limit = today.adding(days: 2)
        let rows = items.compactMap { item -> DueSoonRow? in
            guard let due = item.due, due <= limit else { return nil }
            let badge = Format.dueBadge(due, today: today)
            if item.debtor == Person.me && item.kind == .group {
                return DueSoonRow(title: groupName(item.ref), detail: "Your share", amount: item.amount, badge: badge,
                                  isOverdue: due < today, action: .settle, due: due, obligation: item)
            }
            return DueSoonRow(title: firstName(item.friend), detail: item.title, amount: item.amount, badge: badge,
                              isOverdue: due < today, action: item.isOwedToMe ? .remind : .settle, due: due, obligation: item)
        }
        return rows.enumerated()
            .sorted { $0.element.due != $1.element.due ? $0.element.due < $1.element.due : $0.offset < $1.offset }
            .prefix(3)
            .map(\.element)
    }

    func homeState(totals: HomeTotals, hasClaims: Bool) -> HomeState {
        if ledger.isEmpty { return .firstDay }
        if totals.owed == 0 && totals.owe == 0 && !hasClaims { return .allSettled }
        return .active
    }

    /// The 3 newest expense and payment events, in Home's row copy (§6.1).
    func recentActivity(timeline: [TimelineEvent]) -> [RecentActivityRow] {
        timeline.filter(\.showsOnHome).prefix(3).compactMap { event in
            let date = Format.rowDate(day(of: event.at), today: today)
            switch event.kind {
            case .expenseAdded(let id):
                guard let expense = ledger.expense(id) else { return nil }
                let group = expense.groupId.flatMap { ledger.group($0) }
                if expense.payerId == Person.me {
                    return RecentActivityRow(id: event.id, kind: .expense(id, expense.category), title: expense.title,
                                             subtitle: "You paid · \(expense.split.rows.count) people",
                                             amount: Money.format(expense.amount, expense.currency), isIncoming: true, date: date)
                }
                let word = iOwePayer(expense) ? "You owe" : "Your share"
                return RecentActivityRow(id: event.id, kind: .expense(id, expense.category), title: expense.title,
                                         subtitle: "\(group?.name ?? firstName(expense.payerId)) · \(word)",
                                         amount: Money.format(-expense.share(of: Person.me), expense.currency, sign: .debit),
                                         isIncoming: false, date: date)
            case .payment(let id):
                guard let payment = ledger.payment(id) else { return nil }
                let toMe = payment.toId == Person.me
                let other = payment.otherPartyId
                return RecentActivityRow(id: event.id, kind: .payment(id, person: other),
                                         title: toMe ? "\(firstName(other)) paid you" : "You paid \(firstName(other))",
                                         subtitle: payment.method.label,
                                         amount: Money.format(toMe ? payment.amount : -payment.amount, payment.currency, sign: .debit),
                                         isIncoming: toMe, date: date)
            default:
                return nil
            }
        }
    }

    /// Payments friends recorded to you that wait for your Confirm, newest first (§6.7).
    func pendingClaims() -> [PendingClaim] {
        ledger.payments.enumerated()
            .filter { $0.element.toId == Person.me && $0.element.status == .pending }
            .sorted { $0.element.createdAt != $1.element.createdAt ? $0.element.createdAt > $1.element.createdAt : $0.offset < $1.offset }
            .map { claimCard($0.element) }
    }

    func claimCard(_ payment: Payment) -> PendingClaim {
        let pronoun = ledger.person(payment.fromId)?.pronoun.rawValue ?? "they"
        return PendingClaim(
            payment: payment,
            title: "\(firstName(payment.fromId)) says \(pronoun) paid you \(Money.format(payment.amount, payment.currency))",
            detail: "\(paymentFor(payment)) · \(payment.method.label) · \(Format.time(payment.createdAt, calendar: calendar))",
            purpose: paymentFor(payment)
        )
    }

    /// "You owe ₹450" when the expense's payer is someone you currently pay in that context, else
    /// "Your share" (§6.6).
    func iOwePayer(_ expense: Expense) -> Bool {
        iOwePayer(expense, openItems: expense.groupId == nil ? openItems() : [])
    }
}
