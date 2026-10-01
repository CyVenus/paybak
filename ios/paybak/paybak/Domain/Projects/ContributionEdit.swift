import Foundation

/// The contribution rule as edited on Project settings (screens-projects §6.7). Equal shows each
/// member's "25%"; Percent and Fixed turn the rows into fields ("25", "15000") that must add up. The
/// rule applies only while it adds up; until then the saved one stays in force.
nonisolated struct ContributionEdit: Hashable, Sendable {
    /// Whether the typed values can be applied, and the helper under the rule control.
    struct Check: Hashable, Sendable {
        var isValid: Bool
        var helper: String
    }

    var rule: Contribution.Rule
    /// Percent or Fixed values as typed, per member.
    var typed: [PersonID: String]

    /// The saved rule as the fields show it.
    init(_ contribution: Contribution, members: [PersonID], currency: String) {
        rule = contribution.rule
        typed = switch contribution.rule {
        case .equal: [:]
        case .percent: Dictionary(uniqueKeysWithValues: members.map { ($0, MoneyInput.percentText(contribution.values[$0, default: 0])) })
        case .fixed: Dictionary(uniqueKeysWithValues: members.map { ($0, MoneyInput.text(contribution.values[$0, default: 0], currency: currency)) })
        }
    }

    private init(rule: Contribution.Rule, typed: [PersonID: String]) {
        self.rule = rule
        self.typed = typed
    }

    /// Switching to `rule` starts from an equal split: 25 % each, or the budget (else what's spent so
    /// far) ÷ n each, with the leftover units on the first members.
    static func prefill(_ rule: Contribution.Rule, members: [PersonID], budget: Int64?, spent: Int64, currency: String) -> ContributionEdit {
        func equal(_ total: Int64, text: (Int64) -> String) -> [PersonID: String] {
            guard !members.isEmpty else { return [:] }
            let (each, extra) = total.quotientAndRemainder(dividingBy: Int64(members.count))
            return Dictionary(uniqueKeysWithValues: members.enumerated().map { index, id in
                (id, text(each + (Int64(index) < extra ? 1 : 0)))
            })
        }
        return switch rule {
        case .equal: ContributionEdit(rule: .equal, typed: [:])
        case .percent: ContributionEdit(rule: .percent, typed: equal(Self.wholePercent, text: MoneyInput.percentText))
        case .fixed: ContributionEdit(rule: .fixed, typed: equal(budget ?? spent) { MoneyInput.text($0, currency: currency) })
        }
    }

    /// Percent must make 100 %; Fixed must make the budget (or, without one, be set for everyone).
    func check(members: [PersonID], budget: Int64?, currency: String) -> Check {
        let helper = Self.helper(rule)
        switch rule {
        case .equal:
            return Check(isValid: true, helper: helper)
        case .percent:
            let left = Self.wholePercent - members.reduce(0) { $0 + MoneyInput.basisPoints(typed[$1] ?? "") }
            let amount = "\(MoneyInput.percentText(abs(left)))%"
            if left == 0 { return Check(isValid: true, helper: helper) }
            return Check(isValid: false, helper: "\(helper) \(amount) \(left > 0 ? "left" : "over").")
        case .fixed:
            let amounts = members.map { MoneyInput.minor(typed[$0] ?? "", currency: currency) }
            guard let budget else { return Check(isValid: amounts.allSatisfy { $0 > 0 }, helper: helper) }
            let left = budget - amounts.reduce(0, +)
            if left == 0 { return Check(isValid: true, helper: helper) }
            let amount = Money.format(abs(left), currency)
            return Check(isValid: false, helper: "\(helper) \(amount) \(left > 0 ? "of the budget left" : "over the budget").")
        }
    }

    /// The rule to save: basis points (Percent) or minor units (Fixed).
    func contribution(members: [PersonID], currency: String) -> Contribution {
        switch rule {
        case .equal: Contribution()
        case .percent: Contribution(rule: .percent, values: Dictionary(uniqueKeysWithValues: members.map { ($0, MoneyInput.basisPoints(typed[$0] ?? "")) }))
        case .fixed: Contribution(rule: .fixed, values: Dictionary(uniqueKeysWithValues: members.map { ($0, MoneyInput.minor(typed[$0] ?? "", currency: currency)) }))
        }
    }

    /// The helper under the rule control (New group's copy, record-lend-group §8.3).
    static func helper(_ rule: Contribution.Rule) -> String {
        switch rule {
        case .equal: "Everyone pays the same share of what’s spent."
        case .percent: "Set each person’s share. Shares must add up to 100%."
        case .fixed: "Set a fixed amount for each person."
        }
    }

    /// Equal's read-only value: "25%", or one decimal when it doesn't divide ("33.3%").
    static func equalShareText(count: Int) -> String {
        guard count > 0 else { return "100%" }
        let tenths = (1000 + count / 2) / count
        let (whole, fraction) = tenths.quotientAndRemainder(dividingBy: 10)
        return fraction == 0 ? "\(whole)%" : "\(whole).\(fraction)%"
    }

    /// 100 % in basis points.
    private static let wholePercent: Int64 = 10_000
}
