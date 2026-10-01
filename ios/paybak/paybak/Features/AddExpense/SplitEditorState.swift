import Foundation
import SwiftUI

/// The split editor's working copy (add-expense §3.5, §7): the mode, what's typed per person and who
/// is left out. Switching modes starts from the current money split; the preview comes from the
/// same calculation Save uses.
@Observable
final class SplitEditorState {
    static let modes: [SplitMode] = [.equal, .exact, .percent, .shares]
    static let modeTitles = ["Equally", "Exact", "%", "Shares"]

    let people: [PersonID]
    private(set) var mode: SplitMode
    /// Exact amounts or percentages as typed.
    var texts: [PersonID: String]
    var shares: [PersonID: Int]
    private(set) var excluded: Set<PersonID>

    private let amount: Int64
    private let currency: String
    private let base: ExpenseDraft

    init(form: ExpenseForm, books: Books) {
        people = form.people
        base = form.draft
        amount = form.amount
        currency = form.currency
        excluded = form.excluded
        texts = [:]
        shares = [:]
        mode = form.splitMode == .itemized ? .exact : form.splitMode
        switch form.splitMode {
        case .exact, .itemized:
            for person in people { texts[person] = MoneyInput.text(form.splitValues[person] ?? 0, currency: currency) }
        case .percent:
            for person in people { texts[person] = MoneyInput.percentText(form.splitValues[person] ?? 0) }
        case .shares:
            for person in people { shares[person] = Int(form.splitValues[person] ?? 1) }
        case .equal:
            break
        }
    }

    /// The draft as it would save with this split.
    var draft: ExpenseDraft {
        var draft = base
        draft.splitMode = mode
        draft.itemized = nil
        draft.rows = people.map { person in
            SplitRow(personId: person, included: isIncluded(person), value: mode == .equal ? nil : value(person))
        }
        return draft
    }

    func preview(_ books: Books) -> SplitPreview {
        books.previewSplit(draft)
    }

    func isIncluded(_ person: PersonID) -> Bool { !excluded.contains(person) }

    /// The last ticked person can't be left out; leaving someone out zeroes their value.
    func setIncluded(_ person: PersonID, _ included: Bool) {
        if included {
            excluded.remove(person)
            if mode == .shares, shares[person, default: 0] == 0 { shares[person] = 1 }
        } else if people.count - excluded.count > 1 {
            excluded.insert(person)
            texts[person] = "0"
            shares[person] = 0
        }
    }

    /// Equally → Exact prefills each amount, Exact → % converts to percentages, → Shares starts at
    /// 1 each (add-expense §3.5).
    func switchMode(to newMode: SplitMode, preview: SplitPreview) {
        guard newMode != mode else { return }
        let included = people.filter(isIncluded)
        switch newMode {
        case .exact:
            for person in people { texts[person] = MoneyInput.text(preview.shares[person, default: 0], currency: currency) }
        case .percent:
            let points = Self.basisPoints(of: preview.shares, among: included, total: amount)
            for person in people { texts[person] = MoneyInput.percentText(points[person, default: 0]) }
        case .shares:
            for person in people { shares[person] = isIncluded(person) ? 1 : 0 }
        case .equal, .itemized:
            break
        }
        mode = newMode
    }

    func textBinding(_ person: PersonID) -> Binding<String> {
        Binding {
            self.texts[person] ?? ""
        } set: { raw in
            self.texts[person] = PBAmountField.sanitize(raw, allowsDecimals: Money.info(self.currency).exponent > 0 || self.mode == .percent)
        }
    }

    func sharesBinding(_ person: PersonID) -> Binding<Int> {
        Binding { self.shares[person] ?? 1 } set: { self.shares[person] = max(1, $0) }
    }

    /// Hands the split to the form.
    func apply(to form: ExpenseForm) {
        form.splitMode = mode
        form.excluded = excluded
        form.itemized = nil
        form.splitValues = Dictionary(uniqueKeysWithValues: people.map { ($0, value($0) ?? 0) })
    }

    private func value(_ person: PersonID) -> Int64? {
        switch mode {
        case .equal, .itemized: nil
        case .exact: isIncluded(person) ? MoneyInput.minor(texts[person] ?? "", currency: currency) : 0
        case .percent: isIncluded(person) ? MoneyInput.basisPoints(texts[person] ?? "") : 0
        case .shares: isIncluded(person) ? Int64(shares[person] ?? 1) : 0
        }
    }

    /// Money shares as basis points that add up to 10,000 (leftover to the first people).
    static func basisPoints(of shares: [PersonID: Int64], among people: [PersonID], total: Int64) -> [PersonID: Int64] {
        guard !people.isEmpty else { return [:] }
        guard total > 0 else {
            let (each, extra) = Int64(10_000).quotientAndRemainder(dividingBy: Int64(people.count))
            return Dictionary(uniqueKeysWithValues: people.enumerated().map { ($1, each + (Int64($0) < extra ? 1 : 0)) })
        }
        var points = Dictionary(uniqueKeysWithValues: people.map { ($0, shares[$0, default: 0] * 10_000 / total) })
        var left = 10_000 - points.values.reduce(0, +)
        for person in people where left > 0 {
            points[person, default: 0] += 1
            left -= 1
        }
        return points
    }
}
