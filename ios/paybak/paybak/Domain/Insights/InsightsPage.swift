import Foundation

/// Everything the Insights report shows for one month (screens-insights-ai §2.6, domain.md §6.4), as
/// display copy computed from the ledger.
nonisolated struct InsightsPage: Hashable, Sendable {
    /// A share-bar row: a category, a group (or "Without a group") or a friend.
    struct BarRow: Hashable, Sendable, Identifiable {
        enum Leading: Hashable, Sendable {
            /// A `PBIcon` raw value.
            case icon(String)
            case person(PersonID)
        }

        /// The records a tap lists.
        enum Target: Hashable, Sendable {
            case category(ExpenseCategory, YearMonth)
            case group(GroupID)
            case person(PersonID)
        }

        var id: String
        var title: String
        /// "51%"
        var caption: String
        var amount: String
        /// The displayed percentage as 0…1 (Figma sets the bar from the caption).
        var progress: Double
        var leading: Leading
        /// nil when there is no log for it ("Without a group").
        var target: Target?
    }

    /// A loan in the Lent vs borrowed card.
    struct LoanRow: Hashable, Sendable, Identifiable {
        enum Status: Hashable, Sendable {
            case paidBack
            case overdue
            case due(LocalDay)
            case open
        }

        var id: LoanID
        var personId: PersonID
        /// "Kabir · Bike service"
        var text: String
        var status: Status

        var badge: String? {
            switch status {
            case .paidBack: "Paid back"
            case .overdue: "Overdue"
            case .due(let day): "Due \(Format.short(day))"
            case .open: nil
            }
        }
    }

    struct ChartMonth: Hashable, Sendable {
        var label: String
        var total: Int64
        /// "April ₹18,400"
        var spoken: String
    }

    var month: YearMonth
    /// "September 2026"
    var title: String
    var canGoBack: Bool
    var canGoForward: Bool
    var total: String
    /// "Up 5% from August"
    var trend: String?
    /// Oldest first; the last is the selected month.
    var chart: [ChartMonth]
    var categories: [BarRow]
    var groups: [BarRow]
    var friends: [BarRow]
    /// "Lent vs borrowed since April"
    var loansTitle: String
    var lent: String
    var borrowed: String
    var loans: [LoanRow]

    var isEmpty: Bool { categories.isEmpty }
    /// The line that replaces the lists for a month with no shared expenses.
    var emptyLine: String { "No shared expenses in \(month.name)." }
    /// "Your share by month: April ₹18,400, …, September ₹23,300"
    var chartLabel: String { "Your share by month: " + chart.map(\.spoken).joined(separator: ", ") }

    static let footnote = "Totals are your share of expenses in groups and with friends. Projects, payments and loans aren’t counted."
}

nonisolated extension Books {
    /// The Insights report for `month`. The chart and the loan window are the month and the 5 before it.
    func insightsPage(_ month: YearMonth) -> InsightsPage {
        let report = insights(month)
        let chart = monthlyTotals(endingAt: month)
        let previous = chart.count > 1 ? chart[chart.count - 2].total : 0
        let windowStart = month.adding(months: -5)
        let loans = insightsLoans(from: windowStart.firstDay, through: month.lastDay)
        let lent = loans.filter { $0.lenderId == Person.me }.reduce(0) { $0 + toDefault($1.amount, currency: $1.currency, rate: $1.rate) }
        let borrowed = loans.filter { $0.borrowerId == Person.me }.reduce(0) { $0 + toDefault($1.amount, currency: $1.currency, rate: $1.rate) }
        return InsightsPage(
            month: month,
            title: "\(month.name) \(month.year)",
            canGoBack: firstInsightsMonth.map { month > $0 } ?? false,
            canGoForward: month < YearMonth(today),
            total: Money.format(report.total, defaultCurrency),
            trend: Self.trend(current: report.total, previous: previous, previousMonth: month.adding(months: -1)),
            chart: chart.map { entry in
                InsightsPage.ChartMonth(label: Format.shortMonth(entry.month.month), total: entry.total,
                                        spoken: "\(entry.month.name) \(Money.format(entry.total, defaultCurrency))")
            },
            categories: report.rankedCategories.map { row in
                bar(id: row.category.rawValue, title: row.category.name, amount: row.amount, percent: row.percent,
                    leading: .icon(row.category.icon), target: .category(row.category, month))
            },
            groups: report.rankedGroups.map { row in
                let group = row.groupId.flatMap(ledger.group)
                return bar(id: row.groupId ?? "none", title: row.name, amount: row.amount, percent: row.percent,
                           leading: .icon(group?.icon ?? "people"), target: row.groupId.map { .group($0) })
            },
            friends: insightFriends(month).map { row in
                bar(id: row.personId, title: firstName(row.personId), amount: row.amount, percent: row.percent,
                    leading: .person(row.personId), target: .person(row.personId))
            },
            loansTitle: "Lent vs borrowed since \(windowStart.name)" + (windowStart.year == month.year ? "" : " \(windowStart.year)"),
            lent: Money.format(lent, defaultCurrency),
            borrowed: Money.format(borrowed, defaultCurrency),
            loans: loans.map(loanRow)
        )
    }

    /// Who you spent with, Friends (§2.6 #8): your share of each expense split evenly across the
    /// other people on it, the leftover paise rotating fairly from one expense to the next, so the
    /// friends add up to the month's total. By amount descending (ties in the order they first
    /// appear), with largest-remainder percentages.
    func insightFriends(_ month: YearMonth) -> [(personId: PersonID, amount: Int64, percent: Int)] {
        var amounts: [PersonID: Int64] = [:]
        var firstSeen: [PersonID] = []
        var counter = 0
        for (expense, share) in insightExpenses(month) {
            let others = expense.split.rows.filter { $0.included && $0.personId != Person.me }.map(\.personId)
            guard !others.isEmpty else { continue }
            let split = Splits.equal(share, among: others, counter: counter)
            counter = split.counter
            for person in others {
                if amounts[person] == nil { firstSeen.append(person) }
                amounts[person, default: 0] += split.shares[person, default: 0]
            }
        }
        let rows: [(key: PersonID, value: Int64)] = firstSeen.map { ($0, amounts[$0, default: 0]) }.filter { $0.1 != 0 }
        // By amount, ties in the order they first appeared (a stable sort).
        let sorted: [(key: PersonID, value: Int64)] = rows.enumerated()
            .sorted { lhs, rhs in lhs.element.value != rhs.element.value ? lhs.element.value > rhs.element.value : lhs.offset < rhs.offset }
            .map(\.element)
        let percents = Splits.largestRemainderPercent(sorted)
        return sorted.map { ($0.key, $0.value, percents[$0.key, default: 0]) }
    }

    /// The earliest month with something to report (the month ‹ stops there).
    var firstInsightsMonth: YearMonth? {
        liveExpenses()
            .filter { expense in expense.share(of: Person.me) != 0 && !(expense.groupId.flatMap(ledger.group)?.isProject ?? false) }
            .map { YearMonth($0.date) }
            .min()
    }

    /// Loans created between `start` and `end`, newest first.
    func insightsLoans(from start: LocalDay, through end: LocalDay) -> [Loan] {
        ledger.loans
            .filter { $0.date >= start && $0.date <= end && ($0.lenderId == Person.me || $0.borrowerId == Person.me) }
            .enumerated()
            .sorted { $0.element.date != $1.element.date ? $0.element.date > $1.element.date : $0.offset < $1.offset }
            .map(\.element)
    }

    private func loanRow(_ loan: Loan) -> InsightsPage.LoanRow {
        let other = loan.lenderId == Person.me ? loan.borrowerId : loan.lenderId
        let unpaid = installments(of: loan).filter { $0.paidOn == nil }
        let status: InsightsPage.LoanRow.Status = if unpaid.isEmpty {
            .paidBack
        } else if unpaid.contains(where: { $0.due.map { $0 < today } ?? false }) {
            .overdue
        } else if let due = unpaid.compactMap(\.due).min() {
            .due(due)
        } else {
            .open
        }
        let text = "\(firstName(other)) · \(loan.title)"
        return InsightsPage.LoanRow(id: loan.id, personId: other, text: text, status: status)
    }

    private func bar(id: String, title: String, amount: Int64, percent: Int, leading: InsightsPage.BarRow.Leading,
                     target: InsightsPage.BarRow.Target?) -> InsightsPage.BarRow {
        InsightsPage.BarRow(id: id, title: title, caption: "\(percent)%", amount: Money.format(amount, defaultCurrency),
                            progress: Double(percent) / 100, leading: leading, target: target)
    }
}
