import Foundation
import Observation

/// The Add expense form's state (add-expense §3): the amount as typed, the people on the expense,
/// the payer(s), the split and every optional row. `draft` turns it into what `addExpense` /
/// `updateExpense` take; the Save rule and the row values come from the same data.
@Observable
final class ExpenseForm {
    let editing: ExpenseID?
    var amountText: String
    var currency: String
    /// Saved with the expense when `currency` isn't the default one.
    var rate: Rate?
    var date: LocalDay
    var dueDate: LocalDay?
    var title: String
    /// Nil until chosen ("Choose"); saves as Other.
    var category: ExpenseCategory?
    var groupId: GroupID?
    /// Everyone on the expense in the order they were added (you first).
    var people: [PersonID]
    /// People on the expense whose share is 0 ("they didn't eat").
    var excluded: Set<PersonID>
    var splitMode: SplitMode
    /// Exact minor units, percent basis points or shares, per person.
    var splitValues: [PersonID: Int64]
    /// Several payers (the payer editor); empty = `payerId` paid everything.
    var payers: [Payer]
    var payerId: PersonID
    var notes: String
    var receipt: Receipt?
    var itemized: Itemized?
    var repeatRule: RepeatRule?

    @ObservationIgnored private var initial: ExpenseDraft?

    init(draft: ExpenseDraft, editing: ExpenseID? = nil, categoryChosen: Bool) {
        self.editing = editing
        amountText = draft.amount > 0 ? MoneyInput.text(draft.amount, currency: draft.currency) : ""
        currency = draft.currency
        rate = draft.rate
        date = draft.date
        dueDate = draft.dueDate
        title = draft.title
        category = categoryChosen ? draft.category : nil
        groupId = draft.groupId
        // A new expense lists you first; an edit keeps the saved rows' order.
        let ids = draft.rows.isEmpty ? [Person.me] : draft.rows.map(\.personId)
        people = editing == nil ? ids.filter { $0 == Person.me } + ids.filter { $0 != Person.me } : ids
        excluded = Set(draft.rows.filter { !$0.included }.map(\.personId))
        splitMode = draft.splitMode
        splitValues = Dictionary(draft.rows.compactMap { row in row.value.map { (row.personId, $0) } }, uniquingKeysWith: { first, _ in first })
        if draft.splitMode == .itemized {
            splitValues = Dictionary(draft.rows.map { ($0.personId, $0.share) }, uniquingKeysWith: { first, _ in first })
        }
        payers = draft.payers.count > 1 ? draft.payers : []
        payerId = draft.payers.first?.personId ?? Person.me
        notes = draft.notes ?? ""
        receipt = draft.receipt
        itemized = draft.itemized
        repeatRule = draft.repeatRule
        initial = self.draft
    }

    var amount: Int64 { MoneyInput.minor(amountText, currency: currency) }

    /// The people other than you.
    var others: [PersonID] { people.filter { $0 != Person.me } }

    var isEditing: Bool { editing != nil }

    /// What Save stores.
    var draft: ExpenseDraft {
        ExpenseDraft(
            groupId: groupId, title: title, category: category ?? .other, amount: amount, currency: currency,
            rate: rate, date: date, dueDate: dueDate, payers: savedPayers, splitMode: splitMode,
            rows: people.map { person in
                SplitRow(personId: person, included: !excluded.contains(person),
                         value: splitMode == .equal || excluded.contains(person) ? nil : splitValues[person] ?? 0,
                         share: splitMode == .itemized ? splitValues[person] ?? 0 : 0)
            },
            itemized: itemized, notes: notes.isEmpty ? nil : notes, receipt: receipt, repeatRule: repeatRule
        )
    }

    /// Anything changed since the form opened (✕ asks before discarding).
    var isDirty: Bool { draft != initial }

    /// Paid by: "You", "Priya" or "2 people".
    var payerIds: [PersonID] { payers.isEmpty ? [payerId] : payers.map(\.personId) }

    var payersAddUp: Bool { payers.isEmpty || payers.reduce(0) { $0 + $1.amount } == amount }

    /// Save: an amount, someone besides you, a split that adds up and payers that add up (§3.2).
    func canSave(split: SplitPreview) -> Bool {
        amount > 0 && !others.isEmpty && split.isBalanced && payersAddUp && split.includedCount > 0
    }

    // MARK: People

    /// Split with's result: you first (on a new expense), then the others in the order they were
    /// added; newcomers join the split (0 in Exact and Percent, 1 share in Shares); a removed payer
    /// resets the payer to you. A receipt's items stay only while the people don't change.
    func setPeople(_ ids: [PersonID]) {
        let kept = people.filter(ids.contains)
        let added = ids.filter { !people.contains($0) }
        let ordered = kept + added
        // An edit keeps the saved order, so an unchanged split isn't logged as changed.
        let next = isEditing ? ordered : ordered.filter { $0 == Person.me } + ordered.filter { $0 != Person.me }
        let changed = next != people
        people = next
        excluded = excluded.filter(people.contains)
        if excluded.count == people.count { excluded = [] }
        splitValues = splitValues.filter { people.contains($0.key) }
        for person in added {
            splitValues[person] = splitMode == .shares ? 1 : 0
        }
        if !people.contains(payerId) { payerId = Person.me }
        payers.removeAll { !people.contains($0.personId) }
        if payers.count == 1 {
            payerId = payers[0].personId
            payers = []
        }
        if splitMode == .itemized, changed {
            splitMode = .equal
            itemized = nil
        }
    }

    /// A group sets the currency and, when nobody is picked yet, brings its members.
    func setGroup(_ group: LedgerGroup?, rate: (String) -> Rate?) {
        groupId = group?.id
        guard let group else { return }
        if currency != group.currency {
            currency = group.currency
            self.rate = rate(group.currency)
        }
        if others.isEmpty {
            setPeople([Person.me] + group.memberIds.filter { $0 != Person.me })
        }
    }

    /// A currency other than the group's takes the expense out of the group.
    func setCurrency(_ code: String, rate: Rate?, group: LedgerGroup? = nil) {
        if let group, group.id == groupId, group.currency != code { groupId = nil }
        currency = code
        self.rate = rate
        amountText = MoneyInput.text(MoneyInput.minor(amountText, currency: code), currency: code).nonZero
    }

    /// A read receipt's itemized draft (Pro scan), or only its photo when it couldn't be read. The
    /// scan is paid by you; a reassignment (no title) keeps the form's title, category and date.
    func apply(_ result: ReceiptResult) {
        let scanned = result.draft
        receipt = scanned.receipt ?? receipt
        guard scanned.itemized != nil else { return }
        amountText = MoneyInput.text(scanned.amount, currency: scanned.currency)
        currency = scanned.currency
        rate = scanned.rate
        payers = []
        payerId = scanned.payers.first?.personId ?? Person.me
        if !scanned.title.isEmpty {
            title = scanned.title
            category = scanned.category
            date = scanned.date
        }
        people = scanned.rows.map(\.personId)
        excluded = []
        splitMode = .itemized
        splitValues = Dictionary(scanned.rows.map { ($0.personId, $0.share) }, uniquingKeysWith: { first, _ in first })
        itemized = scanned.itemized
    }

    private var savedPayers: [Payer] {
        payers.isEmpty ? [Payer(personId: payerId, amount: amount)] : payers
    }
}

private extension String {
    /// "0" → "" so an empty amount keeps its placeholder.
    var nonZero: String { self == "0" ? "" : self }
}
