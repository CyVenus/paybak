import Foundation

/// Which way a balance goes, as rows and cards show it.
nonisolated enum BalanceTone: Hashable, Sendable {
    /// Someone owes you (black "+").
    case owed
    /// You owe (gray "−").
    case owe
    case settled
}

/// Where a "Settle up" goes: straight to Record payment when you pay one person, else the group's
/// Settle up plan.
nonisolated enum SettleTarget: Hashable, Sendable {
    case pay(Transfer, currency: String, groupId: GroupID)
    case plan(GroupID)
}

/// Row / Group copy for the Groups list and "Groups together" (screens-groups §2.5–2.6).
nonisolated struct GroupRowCopy: Hashable, Sendable {
    enum Trailing: Hashable, Sendable {
        /// "−₹1,400" over "You owe" (the amount has no sign).
        case owe(String)
        /// "+₹1,400" over "You’re owed".
        case owed(String)
        /// "Settled", "You’re settled".
        case status(String)
        /// Archived projects: "Read-only".
        case readOnly
    }

    struct Budget: Hashable, Sendable {
        /// Spent ÷ budget, 0…1; over budget, budget ÷ spent (where the red starts).
        var progress: Double
        /// "₹52,000 of ₹60,000".
        var spent: String
        /// "₹8,000 left", "₹8,000 under budget" or "₹2,000 over budget" (the project's budget line).
        var left: String
        var isOver = false
    }

    var subtitle: String
    var trailing: Trailing
    var budget: Budget?
}

/// Row / Person copy for the Friends list (screens-groups §2.4).
nonisolated struct FriendRowCopy: Hashable, Sendable {
    enum Trailing: Hashable, Sendable {
        /// "+₹700" with "Owes you", or the red overdue pill in its place.
        case owed(String, label: String?, overdue: String?)
        /// "−₹1,400" with "You owe".
        case owe(String, label: String)
        /// "Settled", "No balance".
        case status(String)
    }

    var subtitle: String?
    var trailing: Trailing
}

/// The "Your balance" card of a group (screens-groups §2.1, record-lend-group §7).
nonisolated struct GroupBalanceCopy: Hashable, Sendable {
    var tone: BalanceTone
    /// "−₹1,400", "Settled", "₹0".
    var amount: String
    var caption: String?
    /// Shows "Settle up" (disabled when there's nothing to settle yet).
    var showsSettleUp: Bool
    var settle: SettleTarget?
}

/// One member's line in a group's Balances card.
nonisolated struct MemberBalanceCopy: Hashable, Sendable, Identifiable {
    enum Trailing: Hashable, Sendable {
        case owe(String, label: String)
        case owed(String, label: String)
        case status(String)
    }

    var id: PersonID
    /// "You" or the first name.
    var name: String
    /// "Paid ₹6,500 · Share ₹7,900".
    var subtitle: String
    var trailing: Trailing
}

/// Whether you may leave a group (screens-groups §2.9).
nonisolated enum LeaveCheck: Hashable, Sendable {
    case allowed
    /// "You can’t leave yet": the message and where its "Settle up" goes.
    case blocked(message: String, settle: SettleTarget)
}

/// The balance card of a friend page (screens-groups §6.3).
nonisolated struct FriendBalanceCopy: Hashable, Sendable {
    var tone: BalanceTone
    /// "Rohan owes you", "You owe Kabir", "Your balance".
    var label: String
    var amount: String
    var caption: String
    /// "Overdue 3 days" when they owe you and it's past due.
    var overdue: String?
    /// The item the actions act on (what a reminder or payment is about).
    var lead: Obligation?
}

/// A friend page History row (screens-groups §6.4).
nonisolated struct FriendHistoryRow: Hashable, Sendable, Identifiable {
    enum Leading: Hashable, Sendable {
        /// A category or loan icon key.
        case icon(String)
        case person(PersonID)
    }

    enum Target: Hashable, Sendable {
        case expense(ExpenseID)
        case payment(PaymentID)
        case loan(LoanID)
    }

    var id: String
    var leading: Leading
    var title: String
    var subtitle: String
    var amount: String
    var date: String
    /// Open items are black; settled ones gray.
    var isOpen: Bool
    var target: Target
}

nonisolated extension Books {
    // MARK: Groups list

    func groupRowCopy(_ summary: GroupSummary) -> GroupRowCopy {
        let group = summary.group
        let members = Self.members(group.memberIds.count)
        if group.isArchived {
            let subtitle = group.project?.closedAt.map { "Project · Closed \(Format.short(day(of: $0)))" } ?? "Project · \(members)"
            return GroupRowCopy(subtitle: subtitle, trailing: .readOnly)
        }
        let trailing: GroupRowCopy.Trailing = switch summary.myNet.signum() {
        case -1: .owe(Money.format(-summary.myNet, group.currency))
        case 1: .owed(Money.format(summary.myNet, group.currency))
        default: .status(group.isProject && summary.isOpen ? "You’re settled" : "Settled")
        }
        if group.isProject {
            return GroupRowCopy(subtitle: "Project · \(members)", trailing: trailing, budget: budget(group))
        }
        // The settle-by date shows while your balance there is open.
        var parts = [members]
        if group.currency != defaultCurrency { parts.append(group.currency) }
        if summary.myNet != 0, let due = group.settleBy { parts.append(Format.dueLabel(due)) }
        return GroupRowCopy(subtitle: parts.joined(separator: " · "), trailing: trailing)
    }

    /// The project budget bar under a Row / Group, when the project has a budget.
    private func budget(_ project: LedgerGroup) -> GroupRowCopy.Budget? {
        guard let info = project.project, let budget = info.budget, budget > 0 else { return nil }
        let spent = projectSpent(project.id)
        let over = spent > budget
        let left = if over {
            "\(Money.format(spent - budget, project.currency)) over budget"
        } else if info.status == .active {
            "\(Money.format(budget - spent, project.currency)) left"
        } else {
            "\(Money.format(budget - spent, project.currency)) under budget"
        }
        return GroupRowCopy.Budget(
            progress: over ? Double(budget) / Double(spent) : Double(spent) / Double(budget),
            spent: "\(Money.format(spent, project.currency)) of \(Money.format(budget, project.currency))",
            left: left,
            isOver: over
        )
    }

    // MARK: Friends list

    func friendRowCopy(_ balance: FriendBalance) -> FriendRowCopy {
        let lead = leadItem(balance)
        switch balance.net.signum() {
        case 1:
            let amount = Money.format(balance.net, defaultCurrency)
            if let lead, let due = lead.due, due < today {
                return FriendRowCopy(subtitle: lead.title, trailing: .owed(amount, label: nil, overdue: Format.dueBadge(due, today: today)))
            }
            return FriendRowCopy(subtitle: lead.map(context), trailing: .owed(amount, label: "Owes you", overdue: nil))
        case -1:
            return FriendRowCopy(subtitle: lead.map(context), trailing: .owe(Money.format(-balance.net, defaultCurrency), label: "You owe"))
        default:
            return FriendRowCopy(subtitle: nil, trailing: .status(sharesAnything(with: balance.id) ? "Settled" : "No balance"))
        }
    }

    /// The item a friend's row, card and actions talk about: the earliest due (undated last).
    func leadItem(_ balance: FriendBalance) -> Obligation? {
        balance.items.enumerated()
            .min { lhs, rhs in
                let far = LocalDay(year: 9999, month: 12, day: 31)
                return (lhs.element.due ?? far, lhs.offset) < (rhs.element.due ?? far, rhs.offset)
            }?
            .element
    }

    /// "Due Sun 4 Oct" for a direct item; a group, project or loan names itself first ("Goa Trip · Due
    /// Fri 2 Oct"); the title when there's no due date.
    private func context(_ item: Obligation) -> String {
        guard let due = item.due else { return item.title }
        switch item.kind {
        case .group, .project, .loan: return "\(item.title) · \(Format.dueLabel(due))"
        case .direct: return Format.dueLabel(due)
        }
    }

    /// Whether anything is recorded with a person ("Settled" rather than "No balance"): their friend
    /// page has History or Groups together.
    func sharesAnything(with person: PersonID) -> Bool {
        guard let page = friendPage(person) else { return false }
        return !page.history.isEmpty || !page.groupsTogether.isEmpty
    }

    // MARK: Group detail

    /// "21–25 Sep · 5 members · ₹39,500 spent" · "6–8 Mar · 3 members · AED" · a new group's
    /// "Trip · 4 members · INR".
    func groupHeaderSubtitle(_ sheet: GroupSheet) -> String {
        let group = sheet.group
        let members = Self.members(group.memberIds.count)
        guard let range = sheet.dateRange else {
            return [group.type.map(Self.typeName), members, group.currency].compactMap(\.self).joined(separator: " · ")
        }
        let end = group.currency == defaultCurrency ? "\(Money.format(sheet.spent, group.currency)) spent" : group.currency
        return "\(range) · \(members) · \(end)"
    }

    func groupBalanceCopy(_ sheet: GroupSheet) -> GroupBalanceCopy {
        let group = sheet.group
        let net = sheet.myNet
        if sheet.expenses.isEmpty && net == 0 {
            return GroupBalanceCopy(tone: .settled, amount: Money.format(0, group.currency), caption: nil, showsSettleUp: true)
        }
        let dueSuffix = group.settleBy.map { " · \(Format.dueLabel($0))" } ?? ""
        if net < 0 {
            let transfers = sheet.plan.filter { $0.from == Person.me }
            let names = Format.joinedNames(transfers.map { firstName($0.to) })
            return GroupBalanceCopy(tone: .owe, amount: Money.format(net, group.currency, sign: .signed),
                                    caption: "You owe \(names)\(dueSuffix)", showsSettleUp: true,
                                    settle: settleTarget(transfers, group: group))
        }
        if net > 0 {
            // Settle up opens the group's plan.
            let transfers = sheet.plan.filter { $0.to == Person.me }
            let who = transfers.count == 1 ? "\(firstName(transfers[0].from)) owes" : "\(transfers.count) people owe"
            return GroupBalanceCopy(tone: .owed, amount: Money.format(net, group.currency, sign: .signed),
                                    caption: "\(who) you\(dueSuffix)", showsSettleUp: true, settle: .plan(group.id))
        }
        return GroupBalanceCopy(tone: .settled, amount: "Settled", caption: lastSettlement(inGroup: group.id) ?? "Nothing pending",
                                showsSettleUp: false)
    }

    /// "You paid Kabir AED 60 on 14 Mar" / "Kabir paid you AED 60 on 14 Mar": your latest confirmed
    /// payment in the group.
    private func lastSettlement(inGroup groupId: GroupID) -> String? {
        let payments = confirmedPayments().filter { $0.groupId == groupId && ($0.fromId == Person.me || $0.toId == Person.me) }
        return payments.max { ($0.confirmedAt ?? $0.createdAt) < ($1.confirmedAt ?? $1.createdAt) }.map(settlementText)
    }

    private func settlementText(_ payment: Payment) -> String {
        let amount = Money.format(payment.amount, payment.currency)
        let when = Format.short(payment.date)
        return payment.fromId == Person.me
            ? "You paid \(firstName(payment.toId)) \(amount) on \(when)"
            : "\(firstName(payment.fromId)) paid you \(amount) on \(when)"
    }

    private func settleTarget(_ transfers: [Transfer], group: LedgerGroup) -> SettleTarget {
        transfers.count == 1 ? .pay(transfers[0], currency: group.currency, groupId: group.id) : .plan(group.id)
    }

    /// "You" first, then the other members in member order.
    func memberBalances(_ sheet: GroupSheet) -> [MemberBalanceCopy] {
        let group = sheet.group
        let members = group.memberIds.filter { $0 == Person.me } + group.memberIds.filter { $0 != Person.me }
        return members.map { member in
            let net = sheet.nets[member, default: 0]
            let isMe = member == Person.me
            let trailing: MemberBalanceCopy.Trailing = switch net.signum() {
            case -1: .owe(Money.format(-net, group.currency), label: isMe ? "You owe" : "Owes")
            case 1: .owed(Money.format(net, group.currency), label: isMe ? "You’re owed" : "Gets back")
            default: .status("Settled")
            }
            let paid = Money.format(sheet.paid[member, default: 0], group.currency)
            let share = Money.format(sheet.share[member, default: 0], group.currency)
            return MemberBalanceCopy(id: member, name: firstName(member), subtitle: "Paid \(paid) · Share \(share)", trailing: trailing)
        }
    }

    /// "Total AED 1,800 · ≈ ₹41,118 at saved rates" for a foreign-currency group.
    func groupTotalFootnote(_ sheet: GroupSheet) -> String? {
        guard let converted = sheet.spentInDefault, !sheet.expenses.isEmpty else { return nil }
        return "Total \(Money.format(sheet.spent, sheet.group.currency)) · ≈ \(Money.format(converted, defaultCurrency)) at saved rates"
    }

    /// "Dev paid · Your share ₹500"; "Kabir paid" when you have no share.
    func groupExpenseSubtitle(_ expense: Expense) -> String {
        let payer = "\(firstName(expense.payerId)) paid"
        let share = expense.share(of: Person.me)
        return share == 0 ? payer : "\(payer) · Your share \(Money.format(share, expense.currency))"
    }

    /// "Fri 25 Sep" (with the year when it isn't this year).
    func expenseDayLabel(_ day: LocalDay) -> String {
        day.year == today.year ? Format.day(day) : "\(Format.day(day)) \(day.year)"
    }

    // MARK: Group settings

    /// "None", "1 rule", "3 rules".
    func recurringValue(_ groupId: GroupID) -> String {
        let count = ledger.recurringRules.count { $0.groupId == groupId && $0.active }
        return count == 0 ? "None" : "\(count) rule\(count == 1 ? "" : "s")"
    }

    func leaveCheck(_ groupId: GroupID) -> LeaveCheck {
        guard let group = ledger.group(groupId) else { return .allowed }
        let net = groupNets(groupId)[Person.me, default: 0]
        let amount = Money.format(abs(net), group.currency)
        let plan = groupPlan(groupId)
        if net < 0 {
            let transfers = plan.filter { $0.from == Person.me }
            let with = transfers.count == 1 ? " with \(firstName(transfers[0].to))" : ""
            return .blocked(message: "You owe \(amount) in \(group.name). Settle up\(with) first, then you can leave.",
                            settle: settleTarget(transfers, group: group))
        }
        if net > 0 {
            let transfers = plan.filter { $0.to == Person.me }
            let who = transfers.count == 1 ? "\(firstName(transfers[0].from)) owes you" : "You’re owed"
            return .blocked(message: "\(who) \(amount) in \(group.name). Settle up first, then you can leave.", settle: .plan(groupId))
        }
        return .allowed
    }

    // MARK: Friend page

    /// nil when you share nothing yet (the "No balance yet" state).
    func friendBalanceCopy(_ page: FriendPage) -> FriendBalanceCopy? {
        let balance = page.balance
        let first = firstName(balance.id)
        let lead = leadItem(balance)
        let caption = lead.map { item in item.due.map { "\(item.title) · \(Format.dueLabel($0))" } ?? item.title } ?? ""
        switch balance.net.signum() {
        case 1:
            let overdue = lead?.due.flatMap { $0 < today ? Format.dueBadge($0, today: today) : nil }
            return FriendBalanceCopy(tone: .owed, label: "\(first) owes you", amount: Money.format(balance.net, defaultCurrency, sign: .signed),
                                     caption: caption, overdue: overdue, lead: lead)
        case -1:
            return FriendBalanceCopy(tone: .owe, label: "You owe \(first)", amount: Money.format(balance.net, defaultCurrency, sign: .signed),
                                     caption: caption, lead: lead)
        default:
            guard sharesAnything(with: balance.id) else { return nil }
            let last = confirmedPayments()
                .filter { Set([$0.fromId, $0.toId]) == Set([Person.me, balance.id]) }
                .max { ($0.confirmedAt ?? $0.createdAt) < ($1.confirmedAt ?? $1.createdAt) }
            return FriendBalanceCopy(tone: .settled, label: "Your balance", amount: "Settled",
                                     caption: last.map(settlementText) ?? "Nothing pending")
        }
    }

    func friendHistoryRows(_ page: FriendPage) -> [FriendHistoryRow] {
        let friend = page.balance.id
        let first = firstName(friend)
        let open = page.balance.items
        return page.history.map { item in
            switch item {
            case .expense(let expense):
                // "You paid · Rohan owes ₹800" while a direct expense is open; a group's says where you
                // stand there: "College Gang · Settled", "Goa Trip · You paid", "Goa Trip · Your share ₹3,600".
                let payer = "\(firstName(expense.payerId)) paid"
                let subtitle: String
                let isOpen: Bool
                if let groupId = expense.groupId {
                    let name = groupName(groupId)
                    isOpen = groupNets(groupId)[Person.me, default: 0] != 0
                    if !isOpen {
                        subtitle = "\(name) · Settled"
                    } else if expense.payerId == Person.me {
                        subtitle = "\(name) · You paid"
                    } else {
                        subtitle = "\(name) · Your share \(Money.format(expense.share(of: Person.me), expense.currency))"
                    }
                } else {
                    let owed = open.first { $0.ref == expense.id }
                    isOpen = owed != nil
                    subtitle = "\(payer) · " + (owed.map { debtText($0, friend: first) } ?? "Settled")
                }
                return FriendHistoryRow(id: expense.id, leading: .icon(expense.category.icon), title: expense.title, subtitle: subtitle,
                                        amount: Money.format(expense.amount, expense.currency), date: Format.rowDate(expense.date, today: today),
                                        isOpen: isOpen, target: .expense(expense.id))
            case .payment(let payment):
                // "Weekend groceries · UPI", "Payment · Cash · Pending".
                let toMe = payment.toId == Person.me
                let title = toMe ? "\(first) paid you" : "You paid \(first)"
                var parts = [paymentFor(payment), payment.method.label]
                switch payment.status {
                case .pending: parts.append("Pending")
                case .notReceived: parts.append("Not received")
                case .confirmed, .cancelled: break
                }
                return FriendHistoryRow(id: payment.id, leading: .icon(toMe ? "money-in" : "money-out"), title: title,
                                        subtitle: parts.joined(separator: " · "),
                                        amount: Money.format(payment.amount, payment.currency), date: Format.rowDate(payment.date, today: today),
                                        isOpen: toMe, target: .payment(payment.id))
            case .loan(let loan):
                // "You lent · Dev owes ₹6,000", "Kabir lent you · Paid back".
                let remaining = abs(loanContext(loan)?.amount ?? 0)
                let lent = loan.lenderId == Person.me
                let who = lent ? "You lent" : "\(first) lent you"
                let state = remaining == 0 ? "Paid back" : "\(lent ? "\(first) owes" : "You owe") \(Money.format(remaining, loan.currency))"
                return FriendHistoryRow(id: loan.id, leading: .icon(lent ? "money-out" : "money-in"), title: loan.title,
                                        subtitle: "\(who) · \(state)",
                                        amount: Money.format(loan.amount, loan.currency), date: Format.rowDate(loan.date, today: today),
                                        isOpen: remaining != 0, target: .loan(loan.id))
            }
        }
    }

    /// "Rohan owes ₹800" / "You owe ₹1,400".
    private func debtText(_ item: Obligation, friend first: String) -> String {
        let amount = Money.format(item.amount, defaultCurrency)
        return item.isOwedToMe ? "\(first) owes \(amount)" : "You owe \(amount)"
    }

    /// "Turn off to stop Paybak nudging Rohan."
    func autoRemindersFootnote(_ person: PersonID) -> String {
        "Turn off to stop Paybak nudging \(firstName(person))."
    }

    // MARK: Helpers

    static func members(_ count: Int) -> String {
        "\(count) member\(count == 1 ? "" : "s")"
    }

    static func typeName(_ type: LedgerGroup.GroupType) -> String {
        switch type {
        case .trip: "Trip"
        case .home: "Home"
        case .friends: "Friends"
        case .other: "Other"
        }
    }
}
