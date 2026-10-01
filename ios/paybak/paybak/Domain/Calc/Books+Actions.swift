import Foundation

/// The store actions (domain.md §11): each validates, updates records, appends history, reminder and
/// inbox entries, and never touches derived data. `at` is the action's moment (the clock's now, or a
/// scenario step's). `LedgerStore` calls these through `mutate`, and the demo scenarios call them
/// directly, so a simulated friend goes through exactly the same code as the user.
nonisolated extension Books {
    // MARK: Expenses

    @discardableResult
    mutating func addExpense(_ draft: ExpenseDraft, by actor: PersonID = Person.me, at moment: Date) throws -> ExpenseID {
        let split = try computeSplit(draft)
        let id = RecordID.make()
        // Only `created`: a receipt attached while adding isn't a separate history entry.
        let history = [HistoryEntry(kind: .created, at: moment, by: actor)]
        ledger.expenses.append(Expense(
            id: id, groupId: draft.groupId, title: title(for: draft), category: draft.category, amount: draft.amount,
            currency: draft.currency, rate: draft.currency == defaultCurrency ? nil : draft.rate, date: draft.date,
            dueDate: draft.dueDate, payers: payers(for: draft),
            split: split, itemized: draft.itemized, notes: draft.notes, receipt: draft.receipt, recurringRuleId: nil,
            occurrenceDate: nil, createdAt: moment, createdBy: actor, history: history, comments: [], flag: nil,
            deletedAt: nil, deletedBy: nil
        ))
        if actor != Person.me, let group = draft.groupId.flatMap(ledger.group), group.memberIds.contains(Person.me) {
            // A friend's expense in your group reaches you as "New expense in {group}".
            let expense = ledger.expenses[ledger.expenses.count - 1]
            ledger.inbox.append(InboxItem(
                id: "n-new-\(id)", type: .newExpenseInGroup, createdAt: moment, read: false,
                params: InboxParams(actorId: actor, expenseId: id, groupId: group.id, title: expense.title, total: expense.amount,
                                    share: expense.share(of: Person.me), currency: expense.currency)
            ))
        }
        if let repeatRule = draft.repeatRule {
            let ruleId = addRecurringRule(from: draft, repeat: repeatRule, at: moment)
            if let index = ledger.expenses.firstIndex(where: { $0.id == id }) {
                ledger.expenses[index].recurringRuleId = ruleId
                ledger.expenses[index].occurrenceDate = draft.date
            }
        }
        return id
    }

    /// Recomputes shares only when the amount, people or split changed; logs one history entry per
    /// changed field; clears a flag.
    mutating func updateExpense(_ id: ExpenseID, with draft: ExpenseDraft, by actor: PersonID = Person.me, at moment: Date) throws {
        guard let index = ledger.expenses.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        let old = ledger.expenses[index]
        var expense = old
        // Who is on it and how it's split, apart from the amounts that follow the total.
        let shapeChanged = old.split.mode != draft.splitMode
            || old.split.rows.map { [$0.personId, "\($0.included)", "\($0.value ?? -1)"] } != draft.rows.map { [$0.personId, "\($0.included)", "\($0.value ?? -1)"] }
        // No payers in the draft keeps who paid (one payer now paying the new amount).
        var payers = draft.payers
        if payers.isEmpty {
            payers = old.payers.count == 1 ? [Payer(personId: old.payerId, amount: draft.amount)] : old.payers
        }
        let splitChanged = old.amount != draft.amount || shapeChanged || (!draft.payers.isEmpty && draft.payers != old.payers)
        if splitChanged {
            var computed = draft
            computed.payers = payers
            expense.split = try computeSplit(computed)
            expense.payers = payers
        }
        let trimmed = draft.title.trimmingCharacters(in: .whitespacesAndNewlines)
        expense.groupId = draft.groupId
        expense.title = trimmed.isEmpty ? old.title : trimmed
        expense.category = draft.category
        expense.amount = draft.amount
        expense.currency = draft.currency
        expense.rate = draft.rate ?? old.rate
        expense.date = draft.date
        expense.dueDate = draft.dueDate
        expense.itemized = draft.itemized ?? old.itemized
        expense.notes = draft.notes
        expense.receipt = draft.receipt ?? old.receipt
        func log(_ kind: HistoryEntry.Kind, old: HistoryValue? = nil, new: HistoryValue? = nil) {
            expense.history.append(HistoryEntry(kind: kind, at: moment, by: actor, old: old, new: new))
        }
        if old.amount != expense.amount { log(.amountChanged, old: .amount(old.amount), new: .amount(expense.amount)) }
        if old.title != expense.title { log(.titleChanged, old: .text(old.title), new: .text(expense.title)) }
        if old.date != expense.date { log(.dateChanged, old: .text(old.date.description), new: .text(expense.date.description)) }
        if old.category != expense.category { log(.categoryChanged, old: .text(old.category.rawValue), new: .text(expense.category.rawValue)) }
        if shapeChanged { log(.splitChanged) }
        // A single payer whose amount just follows the new total isn't a payer change.
        if splitChanged, expense.payers.map(\.personId) != old.payers.map(\.personId) || (old.payers.count > 1 && expense.payers != old.payers) {
            log(.payersChanged)
        }
        if old.receipt == nil, draft.receipt != nil { log(.receiptAdded) }
        expense.flag = nil
        ledger.expenses[index] = expense
    }

    mutating func deleteExpense(_ id: ExpenseID, by actor: PersonID = Person.me, at moment: Date) throws {
        try editExpense(id) { expense in
            expense.deletedAt = moment
            expense.deletedBy = actor
            expense.history.append(HistoryEntry(kind: .deleted, at: moment, by: actor))
        }
    }

    mutating func restoreExpense(_ id: ExpenseID, by actor: PersonID = Person.me, at moment: Date) throws {
        try editExpense(id) { expense in
            expense.deletedAt = nil
            expense.deletedBy = nil
            expense.history.append(HistoryEntry(kind: .restored, at: moment, by: actor))
        }
    }

    @discardableResult
    mutating func addComment(to expenseId: ExpenseID, text: String, by actor: PersonID = Person.me, at moment: Date) throws -> String {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { throw LedgerError.rule("Write a comment first.") }
        let id = RecordID.make()
        try editExpense(expenseId) { $0.comments.append(Comment(id: id, by: actor, at: moment, text: trimmed)) }
        return id
    }

    mutating func flagExpense(_ id: ExpenseID, by actor: PersonID, note: String, at moment: Date) throws {
        try editExpense(id) { expense in
            expense.flag = Flag(by: actor, note: note, at: moment)
            expense.history.append(HistoryEntry(kind: .flagged, at: moment, by: actor))
        }
    }

    /// Resolve (anyone) or remove (the flagger) a flag.
    mutating func resolveFlag(_ id: ExpenseID, by actor: PersonID = Person.me, at moment: Date) throws {
        try editExpense(id) { expense in
            guard let flag = expense.flag else { return }
            expense.flag = nil
            expense.history.append(HistoryEntry(kind: flag.by == actor ? .flagRemoved : .flagResolved, at: moment, by: actor))
        }
    }

    /// A variable rule's draft becomes its expense, dated the occurrence and split as the rule says.
    @discardableResult
    mutating func enterDraftAmount(_ draftId: DraftID, amount: Int64, at moment: Date) throws -> ExpenseID {
        guard amount > 0 else { throw LedgerError.invalidAmount }
        guard let draftIndex = ledger.drafts.firstIndex(where: { $0.id == draftId }),
              let rule = ledger.rule(ledger.drafts[draftIndex].ruleId) else { throw LedgerError.notFound }
        let occurrence = ledger.drafts[draftIndex].occurrenceDate
        let id = "e-\(rule.id)-\(occurrence)"
        appendExpense(from: rule, id: id, amount: amount, occurrence: occurrence, createdBy: Person.me, at: moment)
        ledger.drafts[draftIndex].expenseId = id
        return id
    }

    // MARK: Payments

    /// Pending unless the person recording it is its receiver (then confirmed at once).
    @discardableResult
    mutating func recordPayment(_ draft: PaymentDraft, at moment: Date) throws -> PaymentID {
        guard draft.amount > 0 else { throw LedgerError.invalidAmount }
        guard draft.fromId != draft.toId else { throw LedgerError.rule("Pick who paid and who received it.") }
        let id = draft.id ?? RecordID.make()
        let confirmed = draft.recordedBy == draft.toId
        ledger.payments.append(Payment(
            id: id, fromId: draft.fromId, toId: draft.toId, amount: draft.amount, currency: draft.currency,
            rate: draft.currency == defaultCurrency ? nil : draft.rate,
            method: draft.method, date: draft.date, groupId: draft.groupId, loanId: draft.loanId, expenseId: draft.expenseId,
            note: draft.note, proof: draft.proof, status: confirmed ? .confirmed : .pending, recordedBy: draft.recordedBy,
            createdAt: moment, confirmedAt: confirmed ? moment : nil, notReceivedNote: nil
        ))
        return id
    }

    /// Edit mode of Record payment: the details change, the status doesn't.
    mutating func updatePayment(_ id: PaymentID, with draft: PaymentDraft) throws {
        guard draft.amount > 0 else { throw LedgerError.invalidAmount }
        try editPayment(id) { payment in
            payment.fromId = draft.fromId
            payment.toId = draft.toId
            payment.amount = draft.amount
            payment.currency = draft.currency
            payment.rate = draft.rate
            payment.method = draft.method
            payment.date = draft.date
            payment.groupId = draft.groupId
            payment.loanId = draft.loanId
            payment.expenseId = draft.expenseId
            payment.note = draft.note
            payment.proof = draft.proof
        }
    }

    /// The recorder cancels a pending payment (kept for history, excluded everywhere).
    mutating func cancelPayment(_ id: PaymentID) throws {
        try editPayment(id) { payment in
            guard payment.status == .pending else { return }
            payment.status = .cancelled
        }
    }

    /// The receiver confirms; a payment to you also adds the (read) "Payment confirmed" inbox item.
    mutating func confirmPayment(_ id: PaymentID, at moment: Date) throws {
        try editPayment(id) { payment in
            payment.status = .confirmed
            payment.confirmedAt = moment
        }
        guard let payment = ledger.payment(id), payment.toId == Person.me else { return }
        let inboxId = "n-confirmed-\(payment.id)"
        guard !ledger.inbox.contains(where: { $0.id == inboxId }) else { return }
        ledger.inbox.append(InboxItem(
            id: inboxId, type: .paymentConfirmed, createdAt: moment, read: true,
            params: InboxParams(personId: payment.fromId, paymentId: payment.id, title: paymentFor(payment),
                                amount: payment.amount, currency: payment.currency, method: payment.method)
        ))
    }

    /// The receiver says the money didn't arrive: the debt stays.
    /// A blank note is stored as none (Android: `note.trim().ifEmpty { null }`).
    mutating func markNotReceived(_ id: PaymentID, note: String, at moment: Date) throws {
        let trimmed = note.trimmingCharacters(in: .whitespacesAndNewlines)
        let note: String? = trimmed.isEmpty ? nil : trimmed
        try editPayment(id) { payment in
            payment.status = .notReceived
            payment.notReceivedNote = note
        }
        guard let payment = ledger.payment(id), payment.fromId == Person.me else { return }
        ledger.inbox.append(InboxItem(
            id: "n-not-received-\(payment.id)", type: .paymentNotReceived, createdAt: moment, read: false,
            params: InboxParams(personId: payment.toId, paymentId: payment.id, note: note)
        ))
    }

    // MARK: Loans

    @discardableResult
    mutating func addLoan(_ draft: LoanDraft, at moment: Date) throws -> LoanID {
        guard draft.amount > 0 else { throw LedgerError.invalidAmount }
        guard draft.lenderId == Person.me || draft.borrowerId == Person.me, draft.lenderId != draft.borrowerId else {
            throw LedgerError.rule("One side of a loan is you.")
        }
        let id = draft.id ?? RecordID.make()
        ledger.loans.append(Loan(
            id: id, lenderId: draft.lenderId, borrowerId: draft.borrowerId, amount: draft.amount, currency: draft.currency,
            rate: draft.currency == defaultCurrency ? nil : draft.rate, reason: Self.loanReason(draft.reason), date: draft.date,
            installments: draft.installments,
            dueDate: draft.dueDate, createdAt: moment, createdBy: Person.me
        ))
        return id
    }

    mutating func updateLoan(_ id: LoanID, with draft: LoanDraft) throws {
        guard draft.amount > 0 else { throw LedgerError.invalidAmount }
        guard let index = ledger.loans.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        ledger.loans[index].lenderId = draft.lenderId
        ledger.loans[index].borrowerId = draft.borrowerId
        ledger.loans[index].amount = draft.amount
        ledger.loans[index].currency = draft.currency
        ledger.loans[index].rate = draft.rate
        ledger.loans[index].reason = Self.loanReason(draft.reason)
        ledger.loans[index].date = draft.date
        ledger.loans[index].installments = draft.installments
        ledger.loans[index].dueDate = draft.dueDate
    }

    // MARK: Groups and projects

    @discardableResult
    mutating func addGroup(_ draft: GroupDraft, at moment: Date) -> GroupID {
        let id = draft.id ?? RecordID.make()
        // You're always the first member.
        var members = [Person.me]
        for member in draft.memberIds where !members.contains(member) {
            members.append(member)
        }
        let isProject = draft.kind == .project
        ledger.groups.append(LedgerGroup(
            id: id, kind: draft.kind, type: isProject ? nil : draft.type, icon: draft.icon,
            name: draft.name.trimmingCharacters(in: .whitespacesAndNewlines),
            currency: draft.currency, memberIds: members, simplifyDebts: isProject ? true : draft.simplifyDebts,
            settleBy: isProject ? nil : draft.settleBy, createdAt: moment, createdBy: Person.me,
            project: isProject ? draft.project ?? ProjectInfo(contribution: Contribution()) : nil
        ))
        return id
    }

    /// Name, type, currency (only while the group has no records), simplify, settle-by, project details.
    mutating func updateGroup(_ id: GroupID, _ change: (inout LedgerGroup) -> Void) throws {
        guard let index = ledger.groups.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        guard !ledger.groups[index].isArchived else { throw LedgerError.notAllowed }
        let currency = ledger.groups[index].currency
        change(&ledger.groups[index])
        if ledger.groups[index].currency != currency, hasRecords(id) {
            ledger.groups[index].currency = currency
            throw LedgerError.notAllowed
        }
    }

    mutating func addMembers(_ ids: [PersonID], to groupId: GroupID) throws {
        try updateGroup(groupId) { group in
            for id in ids where !group.memberIds.contains(id) {
                group.memberIds.append(id)
            }
        }
    }

    /// Throws while the member's net in the group isn't 0.
    mutating func removeMember(_ personId: PersonID, from groupId: GroupID) throws {
        guard let group = ledger.group(groupId) else { throw LedgerError.notFound }
        let net = groupNets(groupId)[personId, default: 0]
        guard net == 0 else {
            let name = ledger.person(personId)?.firstName ?? "They"
            throw LedgerError.rule("\(name) still has a balance of \(Money.format(abs(net), group.currency)) in \(group.name).")
        }
        try updateGroup(groupId) { $0.memberIds.removeAll { $0 == personId } }
    }

    /// Only at a net of exactly 0 (§6.5): "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then
    /// you can leave."
    mutating func leaveGroup(_ groupId: GroupID) throws {
        guard let group = ledger.group(groupId) else { throw LedgerError.notFound }
        let net = groupNets(groupId)[Person.me, default: 0]
        guard net == 0 else {
            let amount = Money.format(abs(net), group.currency)
            if net < 0 {
                let payees = groupPlan(groupId).filter { $0.from == Person.me }.map { firstName($0.to) }
                throw LedgerError.rule("You owe \(amount) in \(group.name). Settle up with \(payees.joined(separator: " and ")) first, then you can leave.")
            }
            throw LedgerError.rule("You’re owed \(amount) in \(group.name). Settle up first, then you can leave.")
        }
        try updateGroup(groupId) { $0.memberIds.removeAll { $0 == Person.me } }
    }

    @discardableResult
    mutating func addComponent(_ draft: ComponentDraft, at moment: Date) throws -> ComponentID {
        guard !draft.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { throw LedgerError.rule("Name the part.") }
        if draft.status.isSpent, (draft.actualCost ?? 0) <= 0 { throw LedgerError.rule("Add what it cost.") }
        let id = RecordID.make()
        ledger.components.append(ProjectComponent(
            id: id, projectId: draft.projectId, name: draft.name, status: draft.status, estimatedCost: draft.estimatedCost,
            actualCost: draft.actualCost, paidBy: draft.paidBy, receipt: nil, createdAt: moment, statusChangedAt: moment,
            history: [ProjectComponent.HistoryEntry(kind: "added", at: moment, by: Person.me)]
        ))
        return id
    }

    /// Moves a part through its lifecycle; bought and done need an actual cost. The status change is
    /// logged as `by` (the payer when nil: a simulated friend's purchase).
    mutating func updateComponent(_ id: ComponentID, status: ProjectComponent.Status, actualCost: Int64?, paidBy: PersonID,
                                  name: String? = nil, estimatedCost: Int64?? = nil, by actor: PersonID? = nil,
                                  at moment: Date) throws {
        guard let index = ledger.components.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        if status.isSpent, (actualCost ?? 0) <= 0 { throw LedgerError.rule("Add what it cost.") }
        var part = ledger.components[index]
        if part.status != status {
            part.statusChangedAt = moment
            part.history.append(.init(kind: status.rawValue, at: moment, by: actor ?? paidBy))
        }
        part.status = status
        part.actualCost = actualCost
        part.paidBy = paidBy
        if let name { part.name = name }
        if let estimatedCost { part.estimatedCost = estimatedCost }
        ledger.components[index] = part
    }

    /// Closes a project: it archives at once when every net is already 0.
    mutating func closeProject(_ id: GroupID, at moment: Date) throws {
        guard let index = ledger.groups.firstIndex(where: { $0.id == id }), ledger.groups[index].isProject else {
            throw LedgerError.notFound
        }
        ledger.groups[index].project?.status = .closed
        ledger.groups[index].project?.closedAt = moment
        if groupNets(id).values.allSatisfy({ $0 == 0 }) {
            ledger.groups[index].project?.status = .archived
            ledger.groups[index].project?.archivedAt = moment
        }
    }

    // MARK: People

    @discardableResult
    mutating func addFriend(_ person: Person) -> PersonID {
        if ledger.person(person.id) == nil {
            ledger.people.append(person)
        }
        return person.id
    }

    /// Someone not on Paybak, added by name and phone or email.
    @discardableResult
    mutating func addGuest(name: String, contact: String?, at moment: Date) -> PersonID {
        // Someone already here with that phone or email is kept.
        if let contact, let known = ledger.people.first(where: { $0.contact == contact }) { return known.id }
        let id = RecordID.make()
        let name = name.trimmingCharacters(in: .whitespacesAndNewlines)
        ledger.people.append(Person(id: id, name: name, avatar: nil, upi: nil, username: nil, pronoun: .they, isGuest: true,
                                    contact: contact, remindersMuted: false, addedAt: moment))
        return id
    }

    mutating func setRemindersMuted(_ personId: PersonID, _ muted: Bool) throws {
        guard let index = ledger.people.firstIndex(where: { $0.id == personId }) else { throw LedgerError.notFound }
        ledger.people[index].remindersMuted = muted
    }

    // MARK: Reminders and inbox

    /// A manual reminder from the Remind sheet (never muted).
    @discardableResult
    mutating func sendReminder(about item: Obligation, tone: Reminder.Tone, message: String?, via: Reminder.Via, at moment: Date) -> ReminderID {
        let id = RecordID.make()
        ledger.reminders.append(Reminder(
            id: id, toId: item.friend, amount: item.amount, currency: defaultCurrency,
            expenseId: item.kind == .direct ? item.ref : nil, groupId: item.kind == .group || item.kind == .project ? item.ref : nil,
            loanId: item.kind == .loan ? item.ref : nil, installment: item.installment, sentAt: moment, automatic: false,
            message: message, tone: tone, via: via
        ))
        return id
    }

    mutating func markInboxRead(_ id: InboxItemID) {
        guard let index = ledger.inbox.firstIndex(where: { $0.id == id }) else { return }
        ledger.inbox[index].read = true
    }

    mutating func markAllInboxRead() {
        for index in ledger.inbox.indices {
            ledger.inbox[index].read = true
        }
    }

    // MARK: Recurring rules

    @discardableResult
    mutating func addRecurringRule(_ rule: RecurringRule) -> RuleID {
        ledger.recurringRules.append(rule)
        return rule.id
    }

    mutating func updateRecurringRule(_ id: RuleID, _ change: (inout RecurringRule) -> Void) throws {
        guard let index = ledger.recurringRules.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        change(&ledger.recurringRules[index])
    }

    /// Stops the rule; expenses it created stay.
    mutating func deleteRecurringRule(_ id: RuleID) throws {
        try updateRecurringRule(id) { $0.active = false }
    }

    // MARK: Pro and settings

    /// The yearly plan's 7-day trial ("Your free trial ends Wed 7 Oct.").
    mutating func startTrial(_ period: Entitlement.Period = .yearly, at moment: Date) {
        ledger.settings.entitlement = Entitlement(plan: .pro, period: period, trialEndsAt: day(of: moment).adding(days: 7), since: moment)
    }

    mutating func subscribe(_ period: Entitlement.Period, at moment: Date) {
        ledger.settings.entitlement = Entitlement(plan: .pro, period: period, trialEndsAt: nil, since: moment)
    }

    mutating func setPro(_ isPro: Bool, at moment: Date) {
        ledger.settings.entitlement = isPro
            ? Entitlement(plan: .pro, period: .yearly, trialEndsAt: nil, since: moment)
            : Entitlement()
    }

    mutating func updateSettings(_ change: (inout LedgerSettings) -> Void) {
        change(&ledger.settings)
    }

    /// Everything but the rotation and the scheduler cursor (a new account).
    mutating func clear() {
        let rotation = ledger.rotation
        let scheduler = ledger.scheduler
        let settings = ledger.settings
        ledger = Ledger()
        ledger.rotation = rotation
        ledger.scheduler = scheduler
        ledger.settings = settings
    }

    // MARK: Private

    private mutating func editExpense(_ id: ExpenseID, _ change: (inout Expense) -> Void) throws {
        guard let index = ledger.expenses.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        change(&ledger.expenses[index])
    }

    private mutating func editPayment(_ id: PaymentID, _ change: (inout Payment) -> Void) throws {
        guard let index = ledger.payments.firstIndex(where: { $0.id == id }) else { throw LedgerError.notFound }
        change(&ledger.payments[index])
    }

    private func hasRecords(_ groupId: GroupID) -> Bool {
        ledger.expenses.contains { $0.groupId == groupId } || ledger.payments.contains { $0.groupId == groupId }
            || ledger.components.contains { $0.projectId == groupId }
    }

    /// A loan's reason, trimmed; blank is no reason ("Loan").
    private static func loanReason(_ reason: String?) -> String? {
        let trimmed = reason?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    /// An empty title saves as the category name.
    private func title(for draft: ExpenseDraft) -> String {
        let trimmed = draft.title.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? draft.category.name : trimmed
    }

    /// One payer paying everything when the draft names none.
    private func payers(for draft: ExpenseDraft) -> [Payer] {
        draft.payers.isEmpty ? [Payer(personId: Person.me, amount: draft.amount)] : draft.payers
    }

    /// The saved split (§4): validates the draft and computes each included person's share with the
    /// context's fair rotation, which it advances.
    private mutating func computeSplit(_ draft: ExpenseDraft) throws -> Split {
        guard draft.amount > 0 else { throw LedgerError.invalidAmount }
        let included = draft.includedIds
        let payerIds = payers(for: draft).map(\.personId)
        guard Set(included).union(payerIds).count > 1 else { throw LedgerError.needsSomeoneElse }
        guard payers(for: draft).reduce(0, { $0 + $1.amount }) == draft.amount else {
            throw LedgerError.rule("The payers’ amounts must add up to the total.")
        }
        let order: [PersonID]
        if let groupId = draft.groupId, let group = ledger.group(groupId) {
            order = group.memberIds.filter(included.contains) + included.filter { !group.memberIds.contains($0) }
        } else {
            order = included.contains(Person.me) ? [Person.me] + included.filter { $0 != Person.me } : included
        }
        let key = draft.groupId ?? Self.rotationKey(draft.rows.map(\.personId))
        let counter = ledger.rotation[key, default: 0]
        let values = Dictionary(draft.rows.map { ($0.personId, $0.value ?? 0) }, uniquingKeysWith: { first, _ in first })
        var shares: [PersonID: Int64]
        var next = counter
        switch draft.splitMode {
        case .equal:
            (shares, next) = Splits.equal(draft.amount, among: order, counter: counter)
        case .exact:
            let status = Splits.exactStatus(total: draft.amount, amounts: order.map { values[$0, default: 0] }, currency: draft.currency)
            guard status.remaining == 0 else { throw LedgerError.rule(status.left) }
            shares = values
        case .percent:
            guard order.reduce(0, { $0 + values[$1, default: 0] }) == 10_000 else {
                throw LedgerError.rule("The percentages must add up to 100%.")
            }
            (shares, next) = Splits.weighted(draft.amount, weights: values, order: order, counter: counter)
        case .shares:
            guard order.allSatisfy({ values[$0, default: 0] >= 1 }) else { throw LedgerError.rule("Everyone needs at least 1 share.") }
            (shares, next) = Splits.weighted(draft.amount, weights: values, order: order, counter: counter)
        case .itemized:
            if let itemized = draft.itemized {
                // Each item among its people, then the total (tax and tip included) in proportion.
                let result = Splits.itemized(items: itemized.items, total: draft.amount, order: order, counter: counter)
                (shares, next) = (result.shares, result.counter)
            } else {
                shares = Dictionary(draft.rows.map { ($0.personId, $0.share) }, uniquingKeysWith: { first, _ in first })
            }
        }
        let total = order.reduce(0) { $0 + shares[$1, default: 0] }
        guard total == draft.amount else { throw LedgerError.splitDoesNotAddUp(remaining: draft.amount - total) }
        ledger.rotation[key] = next
        return Split(mode: draft.splitMode, rows: draft.rows.map { row in
            SplitRow(personId: row.personId, included: row.included, value: draft.splitMode == .equal ? nil : row.value,
                     share: row.included ? shares[row.personId, default: 0] : 0)
        })
    }

    /// Adds the expense a rule makes for an occurrence and advances the rotation.
    mutating func appendExpense(from rule: RecurringRule, id: ExpenseID, amount: Int64, occurrence: LocalDay,
                                createdBy: PersonID, at moment: Date) {
        let (expense, counter) = expense(from: rule, id: id, amount: amount, occurrence: occurrence, createdBy: createdBy, at: moment)
        ledger.rotation[rule.groupId ?? Self.rotationKey(rule.split.personIds)] = counter
        ledger.expenses.append(expense)
    }

    private mutating func addRecurringRule(from draft: ExpenseDraft, repeat rule: RepeatRule, at moment: Date) -> RuleID {
        addRecurringRule(RecurringRule(
            id: RecordID.make(), groupId: draft.groupId, title: title(for: draft), category: draft.category,
            amount: rule.variable ? nil : draft.amount, currency: draft.currency, variable: rule.variable,
            frequency: rule.frequency, anchorDate: rule.anchorDate, startDate: draft.date, lastOccurrence: draft.date,
            payerId: payers(for: draft).first?.personId ?? Person.me,
            split: .init(mode: .equal, personIds: draft.includedIds), createdAt: moment, createdBy: Person.me, active: true
        ))
    }
}

