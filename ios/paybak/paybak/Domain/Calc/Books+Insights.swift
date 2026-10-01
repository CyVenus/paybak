import Foundation

/// A calendar month (Insights, monthly summaries).
nonisolated struct YearMonth: Hashable, Comparable, Sendable, Codable {
    var year: Int
    var month: Int

    init(year: Int, month: Int) {
        self.year = year
        self.month = month
    }

    init(_ day: LocalDay) {
        self.init(year: day.year, month: day.month)
    }

    var firstDay: LocalDay { LocalDay(year: year, month: month, day: 1) }
    var lastDay: LocalDay { firstDay.lastOfMonth }
    var name: String { Format.month(month) }

    func adding(months: Int) -> YearMonth {
        YearMonth(firstDay.adding(months: months, day: 1))
    }

    /// "2026-09" (deep links).
    var key: String { String(format: "%04d-%02d", year, month) }

    static func < (lhs: YearMonth, rhs: YearMonth) -> Bool {
        (lhs.year, lhs.month) < (rhs.year, rhs.month)
    }
}

/// Your share of a month's shared spending (domain.md §6.4).
nonisolated struct InsightsReport: Hashable, Sendable {
    struct CategoryAmount: Hashable, Sendable {
        var category: ExpenseCategory
        var amount: Int64
        var percent = 0
    }

    struct GroupAmount: Hashable, Sendable {
        /// The group name, or "Without a group" for direct expenses.
        var name: String
        var groupId: GroupID?
        var amount: Int64
        var percent = 0
    }

    /// Default-currency minor units.
    var total: Int64 = 0
    /// In first-seen order; `rankedCategories` sorts them for display.
    var categories: [CategoryAmount] = []
    /// In first-seen order; `rankedGroups` sorts them for display.
    var groups: [GroupAmount] = []

    /// By amount descending (ties keep first-seen order), ₹0 omitted, with whole percentages by
    /// largest remainder so they add up to 100.
    var rankedCategories: [CategoryAmount] {
        let sorted = Self.ranked(categories, amount: \.amount)
        let percents = Splits.largestRemainderPercent(sorted.map { ($0.category.rawValue, $0.amount) })
        return sorted.map { var row = $0; row.percent = percents[$0.category.rawValue, default: 0]; return row }
    }

    var rankedGroups: [GroupAmount] {
        let sorted = Self.ranked(groups, amount: \.amount)
        let percents = Splits.largestRemainderPercent(sorted.map { ($0.name, $0.amount) })
        return sorted.map { var row = $0; row.percent = percents[$0.name, default: 0]; return row }
    }

    /// A category's whole percentage among all categories in first-seen order (the assistant's answer).
    func percent(of category: ExpenseCategory) -> Int {
        Splits.largestRemainderPercent(categories.map { ($0.category.rawValue, $0.amount) })[category.rawValue, default: 0]
    }

    private static func ranked<Row>(_ rows: [Row], amount: KeyPath<Row, Int64>) -> [Row] {
        rows.enumerated()
            .filter { $0.element[keyPath: amount] != 0 }
            .sorted { lhs, rhs in
                let (a, b) = (lhs.element[keyPath: amount], rhs.element[keyPath: amount])
                return a != b ? a > b : lhs.offset < rhs.offset
            }
            .map(\.element)
    }
}

nonisolated extension Books {
    static let withoutAGroup = "Without a group"

    /// Your share of every live group or direct expense dated in `month`, converted (projects,
    /// payments, loans and drafts are excluded).
    func insightExpenses(_ month: YearMonth, asOf: Date? = nil) -> [(expense: Expense, share: Int64)] {
        liveExpenses(asOf: asOf).compactMap { expense in
            guard YearMonth(expense.date) == month, expense.share(of: Person.me) != 0 else { return nil }
            if let groupId = expense.groupId, ledger.group(groupId)?.isProject == true { return nil }
            return (expense, toDefault(expense.share(of: Person.me), currency: expense.currency, rate: expense.rate))
        }
    }

    func insights(_ month: YearMonth, asOf: Date? = nil) -> InsightsReport {
        var report = InsightsReport()
        for (expense, share) in insightExpenses(month, asOf: asOf) {
            report.total += share
            if let index = report.categories.firstIndex(where: { $0.category == expense.category }) {
                report.categories[index].amount += share
            } else {
                report.categories.append(.init(category: expense.category, amount: share))
            }
            let name = expense.groupId.map(groupName) ?? Self.withoutAGroup
            if let index = report.groups.firstIndex(where: { $0.name == name }) {
                report.groups[index].amount += share
            } else {
                report.groups.append(.init(name: name, groupId: expense.groupId, amount: share))
            }
        }
        return report
    }

    /// The chart's months: `month` and the `count − 1` before it, oldest first, with their totals.
    func monthlyTotals(endingAt month: YearMonth, count: Int = 6) -> [(month: YearMonth, total: Int64)] {
        (0..<count).reversed().map { back in
            let m = month.adding(months: -back)
            return (m, insights(m).total)
        }
    }

    /// "Up 5% from August" · "Down 3% from August" · "Same as August"; nil when last month was 0.
    static func trend(current: Int64, previous: Int64, previousMonth: YearMonth) -> String? {
        guard previous > 0 else { return nil }
        let difference = abs(current - previous)
        let percent = (difference * 200 + previous) / (2 * previous)
        if current == previous { return "Same as \(previousMonth.name)" }
        return "\(current > previous ? "Up" : "Down") \(percent)% from \(previousMonth.name)"
    }

    /// Loans created since `start`: what you lent and what you borrowed (default currency).
    func lentAndBorrowed(since start: LocalDay) -> (lent: Int64, borrowed: Int64) {
        ledger.loans.filter { $0.date >= start }.reduce((Int64(0), Int64(0))) { sum, loan in
            let value = toDefault(loan.amount, currency: loan.currency, rate: loan.rate)
            return loan.lenderId == Person.me ? (sum.0 + value, sum.1) : (sum.0, loan.borrowerId == Person.me ? sum.1 + value : sum.1)
        }
    }
}
