import Foundation

/// Export records (screens-settings §9, domain.md §6.5): the ranges, the group rows with their default
/// ticks, and the records that go into the file.
nonisolated enum ExportRange: String, CaseIterable, Sendable {
    case thisMonth
    case last3Months
    case allTime

    var label: String {
        switch self {
        case .thisMonth: "This month"
        case .last3Months: "Last 3 months"
        case .allTime: "All time"
        }
    }
}

/// One row of the Groups list: a group or project you're in, or "Without a group".
nonisolated struct ExportGroupRow: Identifiable, Hashable, Sendable {
    /// The group id, or `ExportGroupRow.withoutGroupID`.
    let id: String
    let name: String
    /// Starts ticked iff it has a record in the range.
    let hasRecords: Bool

    static let withoutGroupID = "withoutGroup"
}

/// One exported line: an expense, a payment or a loan.
nonisolated struct ExportRecord: Hashable, Sendable {
    enum Kind: String, Sendable {
        case expense = "Expense"
        case payment = "Payment"
        case loan = "Loan"
    }

    struct Share: Hashable, Sendable {
        let name: String
        let amount: Int64
    }

    let date: LocalDay
    /// The row it belongs to (`ExportGroupRow.id`).
    let groupKey: String
    let groupName: String
    let kind: Kind
    let title: String
    /// The category name of an expense; empty otherwise.
    let category: String
    /// The full name ("Priya Sharma"), or "You".
    let paidBy: String
    let amount: Int64
    let currency: String
    let rate: Rate?
    /// In the default currency, at the saved rate.
    let defaultAmount: Int64
    /// Each person's share of an expense; for a payment or a loan, the person who received it.
    let shares: [Share]
}

nonisolated extension Books {
    /// The days a range covers: this calendar month, the two months before it too, or everything from
    /// the earliest record to today.
    func exportInterval(_ range: ExportRange) -> (start: LocalDay, end: LocalDay) {
        switch range {
        case .thisMonth:
            (today.firstOfMonth, today.lastOfMonth)
        case .last3Months:
            (today.firstOfMonth.adding(months: -2), today.lastOfMonth)
        case .allTime:
            (min(firstRecordDate ?? today, today), today)
        }
    }

    /// "1 Sep – 30 Sep 2026" for a range.
    func exportRangeLabel(_ range: ExportRange) -> String {
        let (start, end) = exportInterval(range)
        return Format.exportRange(start, end)
    }

    /// Every group and project you belong to (archived included), in ledger order, then "Without a
    /// group"; each marked with whether it has a record between `start` and `end`.
    func exportGroups(from start: LocalDay, to end: LocalDay) -> [ExportGroupRow] {
        let ticked = exportTicks(from: start, to: end)
        let groups = ledger.groups
            .filter { $0.memberIds.contains(Person.me) }
            .map { ExportGroupRow(id: $0.id, name: $0.name, hasRecords: ticked.contains($0.id)) }
        let direct = ExportGroupRow(id: ExportGroupRow.withoutGroupID, name: Self.withoutAGroup,
                                    hasRecords: ticked.contains(ExportGroupRow.withoutGroupID))
        return groups + [direct]
    }

    /// The records between `start` and `end` in the ticked rows, row by row in the list's order and
    /// oldest first within a row: live expenses, confirmed payments (direct ones only when they involve
    /// you) and loans (always "Without a group").
    func exportRecords(from start: LocalDay, to end: LocalDay, groups: Set<String>) -> [ExportRecord] {
        let order = exportGroups(from: start, to: end).map(\.id)
        func inRange(_ day: LocalDay) -> Bool { start <= day && day <= end }
        func row(_ groupId: GroupID?) -> String? {
            let key = groupId ?? ExportGroupRow.withoutGroupID
            return order.contains(key) && groups.contains(key) ? key : nil
        }
        func fullName(_ id: PersonID) -> String {
            id == Person.me ? "You" : ledger.person(id)?.name ?? "Someone"
        }
        let expenses = liveExpenses().filter { inRange($0.date) }.compactMap { expense -> ExportRecord? in
            guard let key = row(expense.groupId) else { return nil }
            return record(
                date: expense.date, key: key, kind: .expense, title: expense.title, category: expense.category.name,
                paidBy: fullName(expense.payerId), amount: expense.amount, currency: expense.currency, rate: expense.rate,
                shares: expense.split.rows.filter(\.included).map { ExportRecord.Share(name: fullName($0.personId), amount: $0.share) }
            )
        }
        let payments = confirmedPayments()
            .filter { inRange($0.date) && ($0.groupId != nil || $0.fromId == Person.me || $0.toId == Person.me) }
            .compactMap { payment -> ExportRecord? in
                guard let key = row(payment.groupId) else { return nil }
                let payee = payment.toId == Person.me ? "you" : fullName(payment.toId)
                return record(
                    date: payment.date, key: key, kind: .payment, title: "\(fullName(payment.fromId)) paid \(payee)", category: "",
                    paidBy: fullName(payment.fromId), amount: payment.amount, currency: payment.currency, rate: payment.rate,
                    shares: [ExportRecord.Share(name: fullName(payment.toId), amount: payment.amount)]
                )
            }
        let loans = ledger.loans.filter { inRange($0.date) }.compactMap { loan -> ExportRecord? in
            guard let key = row(nil) else { return nil }
            return record(
                date: loan.date, key: key, kind: .loan, title: loan.title, category: "", paidBy: fullName(loan.lenderId),
                amount: loan.amount, currency: loan.currency, rate: loan.rate,
                shares: [ExportRecord.Share(name: fullName(loan.borrowerId), amount: loan.amount)]
            )
        }
        return (expenses + payments + loans).enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (order.firstIndex(of: lhs.element.groupKey) ?? .max, order.firstIndex(of: rhs.element.groupKey) ?? .max)
                if a != b { return a < b }
                if lhs.element.date != rhs.element.date { return lhs.element.date < rhs.element.date }
                return lhs.offset < rhs.offset
            }
            .map(\.element)
    }

    // MARK: Private

    /// The earliest record date (any expense, payment or loan): where "All time" starts.
    private var firstRecordDate: LocalDay? {
        (ledger.expenses.map(\.date) + ledger.payments.map(\.date) + ledger.loans.map(\.date)).min()
    }

    /// The rows that start ticked (domain.md §6.5): each group with a record in range (an expense
    /// date, a confirmed payment involving you, a project part's status change), and "Without a
    /// group" for direct expenses, payments and loans.
    private func exportTicks(from start: LocalDay, to end: LocalDay) -> Set<String> {
        func inRange(_ day: LocalDay) -> Bool { start <= day && day <= end }
        var ticked = Set<String>()
        for expense in liveExpenses() where inRange(expense.date) {
            ticked.insert(expense.groupId ?? ExportGroupRow.withoutGroupID)
        }
        for payment in confirmedPayments() where inRange(payment.date) && (payment.fromId == Person.me || payment.toId == Person.me) {
            ticked.insert(payment.groupId ?? ExportGroupRow.withoutGroupID)
        }
        for component in ledger.components where inRange(day(of: component.statusChangedAt)) {
            ticked.insert(component.projectId)
        }
        if ledger.loans.contains(where: { inRange($0.date) }) {
            ticked.insert(ExportGroupRow.withoutGroupID)
        }
        return ticked
    }

    private func record(date: LocalDay, key: String, kind: ExportRecord.Kind, title: String, category: String,
                        paidBy: String, amount: Int64, currency: String, rate: Rate?,
                        shares: [ExportRecord.Share]) -> ExportRecord {
        ExportRecord(
            date: date,
            groupKey: key,
            groupName: key == ExportGroupRow.withoutGroupID ? Self.withoutAGroup : groupName(key),
            kind: kind, title: title, category: category, paidBy: paidBy,
            amount: amount, currency: currency, rate: rate,
            defaultAmount: toDefault(amount, currency: currency, rate: rate),
            shares: shares
        )
    }
}

/// The CSV file (§9 proposal): UTF-8 with a BOM, one row per record, amounts as plain numbers.
nonisolated enum ExportCSV {
    static func make(_ records: [ExportRecord], defaultCurrency: String) -> String {
        let header = ["Date", "Group", "Type", "Title", "Category", "Paid by", "Amount", "Currency", "Rate",
                      "Amount (\(defaultCurrency))", "Shares"]
        let rows = records.map { record in
            [
                record.date.description, record.groupName, record.kind.rawValue, record.title, record.category,
                record.paidBy, plain(record.amount, record.currency), record.currency, record.rate?.value ?? "",
                plain(record.defaultAmount, defaultCurrency),
                record.shares.map { "\($0.name) \(Money.format($0.amount, record.currency))" }.joined(separator: "; "),
            ]
        }
        return "\u{FEFF}" + ([header] + rows).map { $0.map(escape).joined(separator: ",") }.joined(separator: "\r\n") + "\r\n"
    }

    /// 70000 paise → "700.00"; always two decimals (1500 yen → "1500.00"), no grouping, no symbol.
    static func plain(_ minor: Int64, _ code: String) -> String {
        let exponent = Money.info(code).exponent
        // In hundredths of a unit, rounded half up (a currency with 3 decimals loses one).
        var value = Decimal(minor) * pow10Decimal(2 - exponent)
        var hundredths = Decimal()
        NSDecimalRound(&hundredths, &value, 0, .plain)
        let cents = NSDecimalNumber(decimal: hundredths).int64Value
        let (whole, fraction) = cents.magnitude.quotientAndRemainder(dividingBy: 100)
        return (cents < 0 ? "-" : "") + "\(whole)." + (fraction < 10 ? "0" : "") + "\(fraction)"
    }

    private static func pow10Decimal(_ exponent: Int) -> Decimal {
        exponent >= 0 ? Decimal(Money.pow10(exponent)) : 1 / Decimal(Money.pow10(-exponent))
    }

    private static func escape(_ field: String) -> String {
        guard field.contains(where: { $0 == "," || $0 == "\"" || $0 == "\n" || $0 == "\r" }) else { return field }
        return "\"" + field.replacingOccurrences(of: "\"", with: "\"\"") + "\""
    }
}

nonisolated extension Format {
    /// "1 Sep – 30 Sep 2026"; with the year on both ends when they differ ("6 Mar 2025 – 30 Sep 2026").
    static func exportRange(_ start: LocalDay, _ end: LocalDay) -> String {
        if start.year == end.year {
            return fullRange(start, end)
        }
        return "\(short(start)) \(start.year) – \(short(end)) \(end.year)"
    }
}
