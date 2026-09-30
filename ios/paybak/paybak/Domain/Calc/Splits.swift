import Foundation

/// Splits and rounding (domain.md §4, verify.py §4). Every function gives exact minor units that add
/// up to the total, and returns the rotation counter to save for the context.
nonisolated enum Splits {
    /// Gives `extra` single minor units to people in `order`, starting at `counter mod n` (§4.1).
    static func rotateExtra(_ order: [String], extra: Int64, counter: Int) -> (bonus: [String: Int64], counter: Int) {
        var bonus = Dictionary(uniqueKeysWithValues: order.map { ($0, Int64(0)) })
        guard !order.isEmpty else { return (bonus, counter) }
        for index in 0..<Int(extra) {
            bonus[order[(counter + index) % order.count], default: 0] += 1
        }
        return (bonus, counter + Int(extra))
    }

    /// ⌊T/n⌋ each plus the fair rotation of the leftover.
    static func equal(_ total: Int64, among order: [String], counter: Int = 0) -> (shares: [String: Int64], counter: Int) {
        guard !order.isEmpty else { return ([:], counter) }
        let (base, extra) = total.quotientAndRemainder(dividingBy: Int64(order.count))
        let (bonus, next) = rotateExtra(order, extra: extra, counter: counter)
        return (Dictionary(uniqueKeysWithValues: order.map { ($0, base + bonus[$0, default: 0]) }), next)
    }

    /// Percent (basis points) and Shares: floor, then the largest remainder, ties by fair rotation.
    static func weighted(_ total: Int64, weights: [String: Int64], order: [String], counter: Int = 0) -> (shares: [String: Int64], counter: Int) {
        let weightSum = order.reduce(Int64(0)) { $0 + weights[$1, default: 0] }
        guard weightSum > 0, !order.isEmpty else { return (Dictionary(uniqueKeysWithValues: order.map { ($0, 0) }), counter) }
        var floors: [String: Int64] = [:]
        var remainders: [String: Int64] = [:]
        for person in order {
            let product = total.multipliedFullWidth(by: weights[person, default: 0])
            let (quotient, remainder) = weightSum.dividingFullWidth(product)
            floors[person] = quotient
            remainders[person] = remainder
        }
        let extra = Int(total - floors.values.reduce(0, +))
        let start = counter % order.count
        let rotated = Array(order[start...] + order[..<start])
        // Stable: equal remainders keep the rotated order.
        let ranked = rotated.enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (remainders[lhs.element, default: 0], remainders[rhs.element, default: 0])
                return a != b ? a > b : lhs.offset < rhs.offset
            }
            .map(\.element)
        for person in ranked.prefix(extra) {
            floors[person, default: 0] += 1
        }
        return (floors, counter + extra)
    }

    /// The Exact editor's footer (§4.2): what's left, "₹150 left" / "₹20 over", "₹2,650 of ₹2,800".
    static func exactStatus(total: Int64, amounts: [Int64], currency: String = "INR") -> (remaining: Int64, left: String, detail: String) {
        let entered = amounts.reduce(0, +)
        let remaining = total - entered
        let left = remaining >= 0 ? "\(Money.format(remaining, currency)) left" : "\(Money.format(-remaining, currency)) over"
        return (remaining, left, "\(Money.format(entered, currency)) of \(Money.format(total, currency))")
    }

    /// Whole percentages that add up to 100 (Insights captions, §6.4). Ties by input order.
    static func largestRemainderPercent(_ values: [(key: String, value: Int64)]) -> [String: Int] {
        let total = values.reduce(Int64(0)) { $0 + $1.value }
        guard total > 0 else { return Dictionary(uniqueKeysWithValues: values.map { ($0.key, 0) }) }
        var floors = Dictionary(uniqueKeysWithValues: values.map { ($0.key, Int($0.value * 100 / total)) })
        let remainders = Dictionary(uniqueKeysWithValues: values.map { ($0.key, $0.value * 100 % total) })
        let missing = 100 - floors.values.reduce(0, +)
        let ranked = values.enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (remainders[lhs.element.key, default: 0], remainders[rhs.element.key, default: 0])
                return a != b ? a > b : lhs.offset < rhs.offset
            }
            .map(\.element.key)
        for key in ranked.prefix(max(0, missing)) {
            floors[key, default: 0] += 1
        }
        return floors
    }

    /// A receipt split (§4.2 itemized): each item evenly among its people (rotation), then the total
    /// (with tax and tip) in proportion to the per-person subtotals.
    static func itemized(items: [Itemized.Item], total: Int64, order: [String], counter: Int = 0) -> (shares: [String: Int64], subtotals: [String: Int64], counter: Int) {
        var subtotals = Dictionary(uniqueKeysWithValues: order.map { ($0, Int64(0)) })
        var counter = counter
        for item in items {
            let (shares, next) = equal(item.amount, among: item.personIds, counter: counter)
            counter = next
            for (person, share) in shares {
                subtotals[person, default: 0] += share
            }
        }
        let (shares, next) = weighted(total, weights: subtotals, order: order, counter: counter)
        return (shares, subtotals, next)
    }

    /// Loan installments: `amount ÷ count`, the leftover to the earliest installments (§4.3).
    static func installments(_ amount: Int64, count: Int) -> [Int64] {
        guard count > 0 else { return [] }
        let (base, extra) = amount.quotientAndRemainder(dividingBy: Int64(count))
        return (0..<count).map { base + (Int64($0) < extra ? 1 : 0) }
    }
}
