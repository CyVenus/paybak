import Foundation

/// The Add / Edit component sheet as typed (screens-projects §5, §1.5). Costs are the raw digits from
/// the decimal pad ("6000"); `receipt` is a saved photo's file name. New parts start Planned with you
/// as the payer.
nonisolated struct ComponentForm: Hashable, Sendable {
    var name = ""
    var estimate = ""
    var actual = ""
    var status: ProjectComponent.Status = .planned
    var paidBy: PersonID = Person.me
    var receipt: String?

    init() {}

    /// The sheet prefilled to edit `part`.
    init(_ part: ProjectComponent, currency: String) {
        name = part.name
        estimate = part.estimatedCost.map { MoneyInput.text($0, currency: currency) } ?? ""
        actual = part.actualCost.map { MoneyInput.text($0, currency: currency) } ?? ""
        status = part.status
        paidBy = part.paidBy
        receipt = part.receipt?.photo
    }

    var trimmedName: String { name.trimmingCharacters(in: .whitespacesAndNewlines) }

    var isEmpty: Bool { self == ComponentForm() }

    /// A name, and an actual cost once the part is bought or done.
    func canSave(currency: String) -> Bool {
        !trimmedName.isEmpty && (status == .planned || MoneyInput.minor(actual, currency: currency) > 0)
    }

    /// Typing an actual cost buys a planned part; clearing it plans a bought one again (§5.3). Done
    /// keeps its status either way.
    mutating func setActual(_ text: String) {
        actual = text
        if !text.isEmpty, status == .planned {
            status = .bought
        } else if text.isEmpty, status == .bought {
            status = .planned
        }
    }

    func estimatedCost(currency: String) -> Int64? {
        positive(MoneyInput.minor(estimate, currency: currency))
    }

    /// A planned part keeps no actual cost.
    func actualCost(currency: String) -> Int64? {
        status == .planned ? nil : positive(MoneyInput.minor(actual, currency: currency))
    }

    private func positive(_ minor: Int64) -> Int64? {
        minor > 0 ? minor : nil
    }
}

/// Lane B's project edits (screens-projects §1.5–§1.6, §6.7): parts from the sheet, the contribution
/// rule, budget and pool from Project settings.
nonisolated extension Books {
    /// Adds a part from the sheet; one added as bought or done logs that status after "added".
    @discardableResult
    mutating func addComponent(to projectId: GroupID, _ form: ComponentForm, at moment: Date) throws -> ComponentID {
        let currency = try editableProject(projectId).currency
        guard form.canSave(currency: currency) else { throw LedgerError.invalidAmount }
        let id = try addComponent(ComponentDraft(projectId: projectId, name: form.trimmedName, status: form.status,
                                                 estimatedCost: form.estimatedCost(currency: currency),
                                                 actualCost: form.actualCost(currency: currency), paidBy: form.paidBy),
                                  at: moment)
        guard let index = ledger.components.firstIndex(where: { $0.id == id }) else { return id }
        ledger.components[index].receipt = receipt(form.receipt, at: moment)
        if form.status.isSpent {
            ledger.components[index].history.append(.init(kind: form.status.rawValue, at: moment, by: form.paidBy))
        }
        return id
    }

    /// Saves the sheet over part `id`: every field as typed (an emptied estimate goes); a status
    /// change is logged and moves the part to the top of its section.
    mutating func editComponent(_ id: ComponentID, _ form: ComponentForm, at moment: Date) throws {
        guard let part = ledger.components.first(where: { $0.id == id }) else { throw LedgerError.notFound }
        let currency = try editableProject(part.projectId).currency
        guard form.canSave(currency: currency) else { throw LedgerError.invalidAmount }
        try updateComponent(id, status: form.status, actualCost: form.actualCost(currency: currency), paidBy: form.paidBy,
                            name: form.trimmedName, estimatedCost: .some(form.estimatedCost(currency: currency)), at: moment)
        guard let index = ledger.components.firstIndex(where: { $0.id == id }) else { return }
        if form.receipt != part.receipt?.photo {
            ledger.components[index].receipt = receipt(form.receipt, at: moment)
        }
    }

    /// Removes a part; its cost comes off the project.
    mutating func deleteComponent(_ id: ComponentID) throws {
        guard let part = ledger.components.first(where: { $0.id == id }) else { throw LedgerError.notFound }
        _ = try editableProject(part.projectId)
        ledger.components.removeAll { $0.id == id }
    }

    /// Changes the project part of an active project (contribution rule, budget, pool).
    mutating func updateProject(_ projectId: GroupID, _ change: (inout ProjectInfo) -> Void) throws {
        _ = try editableProject(projectId)
        try updateGroup(projectId) { group in
            if var info = group.project {
                change(&info)
                group.project = info
            }
        }
    }

    /// Parts, rules and budget change only while the project is active.
    private func editableProject(_ projectId: GroupID) throws -> LedgerGroup {
        guard let project = ledger.group(projectId), project.isProject else { throw LedgerError.notFound }
        guard project.project?.status == .active else { throw LedgerError.notAllowed }
        return project
    }

    private func receipt(_ photo: String?, at moment: Date) -> Receipt? {
        photo.map { Receipt(photo: $0, addedBy: Person.me, addedAt: moment) }
    }
}
