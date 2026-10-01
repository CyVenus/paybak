import Foundation

/// The shared store actions (domain.md §11), stamped with the clock's now. Each throws the action's
/// `LedgerError` when it refuses; the view shows the message.
extension LedgerStore {
    // MARK: Expenses

    @discardableResult
    func addExpense(_ draft: ExpenseDraft, by person: PersonID = Person.me) throws -> ExpenseID {
        try mutate { try $0.addExpense(draft, by: person, at: clock.now) }
    }

    func updateExpense(_ id: ExpenseID, with draft: ExpenseDraft) throws {
        try mutate { try $0.updateExpense(id, with: draft, at: clock.now) }
    }

    func deleteExpense(_ id: ExpenseID) throws {
        try mutate { try $0.deleteExpense(id, at: clock.now) }
    }

    func restoreExpense(_ id: ExpenseID) throws {
        try mutate { try $0.restoreExpense(id, at: clock.now) }
    }

    func addComment(to expenseId: ExpenseID, text: String, by person: PersonID = Person.me) throws {
        try mutate { try $0.addComment(to: expenseId, text: text, by: person, at: clock.now) }
    }

    func flagExpense(_ id: ExpenseID, by person: PersonID = Person.me, note: String) throws {
        try mutate { try $0.flagExpense(id, by: person, note: note, at: clock.now) }
    }

    func resolveFlag(_ id: ExpenseID, by person: PersonID = Person.me) throws {
        try mutate { try $0.resolveFlag(id, by: person, at: clock.now) }
    }

    @discardableResult
    func enterDraftAmount(_ draftId: DraftID, amount: Int64) throws -> ExpenseID {
        try mutate { try $0.enterDraftAmount(draftId, amount: amount, at: clock.now) }
    }

    // MARK: Payments and loans

    @discardableResult
    func recordPayment(_ draft: PaymentDraft) throws -> PaymentID {
        try mutate { try $0.recordPayment(draft, at: clock.now) }
    }

    func updatePayment(_ id: PaymentID, with draft: PaymentDraft) throws {
        try mutate { try $0.updatePayment(id, with: draft) }
    }

    func cancelPayment(_ id: PaymentID) throws {
        try mutate { try $0.cancelPayment(id) }
    }

    func confirmPayment(_ id: PaymentID) throws {
        try mutate { try $0.confirmPayment(id, at: clock.now) }
    }

    func markNotReceived(_ id: PaymentID, note: String) throws {
        try mutate { try $0.markNotReceived(id, note: note, at: clock.now) }
    }

    @discardableResult
    func addLoan(_ draft: LoanDraft) throws -> LoanID {
        try mutate { try $0.addLoan(draft, at: clock.now) }
    }

    func updateLoan(_ id: LoanID, with draft: LoanDraft) throws {
        try mutate { try $0.updateLoan(id, with: draft) }
    }

    // MARK: Groups, projects and people

    @discardableResult
    func addGroup(_ draft: GroupDraft) -> GroupID {
        mutate { $0.addGroup(draft, at: clock.now) }
    }

    func updateGroup(_ id: GroupID, _ change: (inout LedgerGroup) -> Void) throws {
        try mutate { try $0.updateGroup(id, change) }
    }

    func addMembers(_ ids: [PersonID], to groupId: GroupID) throws {
        try mutate { try $0.addMembers(ids, to: groupId) }
    }

    func removeMember(_ id: PersonID, from groupId: GroupID) throws {
        try mutate { try $0.removeMember(id, from: groupId) }
    }

    func leaveGroup(_ id: GroupID) throws {
        try mutate { try $0.leaveGroup(id) }
    }

    @discardableResult
    func addComponent(_ draft: ComponentDraft) throws -> ComponentID {
        try mutate { try $0.addComponent(draft, at: clock.now) }
    }

    func updateComponent(_ id: ComponentID, status: ProjectComponent.Status, actualCost: Int64?, paidBy: PersonID,
                         name: String? = nil, estimatedCost: Int64?? = nil) throws {
        try mutate {
            try $0.updateComponent(id, status: status, actualCost: actualCost, paidBy: paidBy, name: name,
                                   estimatedCost: estimatedCost, at: clock.now)
        }
    }

    func closeProject(_ id: GroupID) throws {
        try mutate { try $0.closeProject(id, at: clock.now) }
    }

    @discardableResult
    func addFriend(_ person: Person) -> PersonID {
        mutate { $0.addFriend(person) }
    }

    @discardableResult
    func addGuest(name: String, contact: String?) -> PersonID {
        mutate { $0.addGuest(name: name, contact: contact, at: clock.now) }
    }

    func setRemindersMuted(_ personId: PersonID, _ muted: Bool) throws {
        try mutate { try $0.setRemindersMuted(personId, muted) }
    }

    // MARK: Reminders, inbox, rules

    @discardableResult
    func sendReminder(about item: Obligation, tone: Reminder.Tone, message: String?, via: Reminder.Via) -> ReminderID {
        mutate { $0.sendReminder(about: item, tone: tone, message: message, via: via, at: clock.now) }
    }

    func markInboxRead(_ id: InboxItemID) {
        mutate { $0.markInboxRead(id) }
    }

    func markAllInboxRead() {
        mutate { $0.markAllInboxRead() }
    }

    @discardableResult
    func addRecurringRule(_ rule: RecurringRule) -> RuleID {
        mutate { $0.addRecurringRule(rule) }
    }

    func updateRecurringRule(_ id: RuleID, _ change: (inout RecurringRule) -> Void) throws {
        try mutate { try $0.updateRecurringRule(id, change) }
    }

    func deleteRecurringRule(_ id: RuleID) throws {
        try mutate { try $0.deleteRecurringRule(id) }
    }

    // MARK: Pro and settings

    func setPro(_ isPro: Bool) {
        mutate { $0.setPro(isPro, at: clock.now) }
    }

    func updateSettings(_ change: (inout LedgerSettings) -> Void) {
        mutate { $0.updateSettings(change) }
    }

    /// Everything but the settings: a new account.
    func clear() {
        mutate { $0.clear() }
    }
}
