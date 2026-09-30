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

/// One exported line: an expense, a payment, a loan or a bought project part.
nonisolated struct ExportRecord: Hashable, Sendable {
    enum Kind: String, Sendable {
        case expense = "Expense"
        case payment = "Payment"
        case loan = "Loan"
        case component = "Component"
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
    let category: String
    let paidBy: String
    let amount: Int64
    let currency: String
    let rate: Rate?
    /// In the default currency, at the saved rate.
    let defaultAmount: Int64
    /// Each person's share (expenses); empty otherwise.
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
            (exportCandidates(includingPlanned: true).map(\.date).min() ?? today, today)
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
        let inRange = Set(exportCandidates(includingPlanned: true).filter { start <= $0.date && $0.date <= end }.map(\.groupKey))
        let groups = ledger.groups
            .filter { $0.memberIds.contains(Person.me) || $0.createdBy == Person.me }
            .map { ExportGroupRow(id: $0.id, name: $0.name, hasRecords: inRange.contains($0.id)) }
        let direct = ExportGroupRow(id: ExportGroupRow.withoutGroupID, name: Self.withoutAGroup,
                                    hasRecords: inRange.contains(ExportGroupRow.withoutGroupID))
        return groups + [direct]
    }

    /// The records between `start` and `end` in the ticked rows, oldest first.
    func exportRecords(from start: LocalDay, to end: LocalDay, groups: Set<String>) -> [ExportRecord] {
        exportCandidates()
            .filter { start <= $0.date && $0.date <= end && groups.contains($0.groupKey) }
            .sorted { ($0.date, $0.kind.rawValue) < ($1.date, $1.kind.rawValue) }
    }

    // MARK: Private

    /// Live expenses, confirmed payments you made or received, loans and bought project parts. A
    /// planned part's status change also counts as activity for the default ticks (domain.md §6.5),
    /// but only spending goes into the file.
    private func exportCandidates(includingPlanned: Bool = false) -> [ExportRecord] {
        let expenses = liveExpenses().map { expense in
            record(
                date: expense.date, group: expense.groupId, kind: .expense, title: expense.title,
                category: expense.category.name, paidBy: expense.payers.map { firstName($0.personId) }.joined(separator: ", "),
                amount: expense.amount, currency: expense.currency, rate: expense.rate,
                shares: expense.split.rows.filter(\.included).map { ExportRecord.Share(name: firstName($0.personId), amount: $0.share) }
            )
        }
        let payments = confirmedPayments().filter { $0.fromId == Person.me || $0.toId == Person.me }.map { payment in
            record(
                date: payment.date, group: payment.groupId, kind: .payment,
                title: "\(firstName(payment.fromId)) paid \(name(payment.toId)) · \(paymentFor(payment))",
                category: "", paidBy: firstName(payment.fromId), amount: payment.amount, currency: payment.currency,
                rate: payment.rate, shares: []
            )
        }
        let loans = ledger.loans.map { loan in
            record(
                date: loan.date, group: nil, kind: .loan, title: loan.title, category: "",
                paidBy: firstName(loan.lenderId), amount: loan.amount, currency: loan.currency, rate: loan.rate, shares: []
            )
        }
        let components = ledger.components.filter { includingPlanned || $0.status.isSpent }.map { component in
            let group = ledger.group(component.projectId)
            let currency = group?.currency ?? defaultCurrency
            return record(
                date: day(of: component.statusChangedAt), group: component.projectId, kind: .component, title: component.name,
                category: "", paidBy: firstName(component.paidBy),
                amount: component.actualCost ?? component.estimatedCost ?? 0, currency: currency, rate: nil, shares: []
            )
        }
        return expenses + payments + loans + components
    }

    private func record(date: LocalDay, group: GroupID?, kind: ExportRecord.Kind, title: String, category: String,
                        paidBy: String, amount: Int64, currency: String, rate: Rate?,
                        shares: [ExportRecord.Share]) -> ExportRecord {
        ExportRecord(
            date: date,
            groupKey: group ?? ExportGroupRow.withoutGroupID,
            groupName: group.map { groupName($0) } ?? Self.withoutAGroup,
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

    /// 70000 paise → "700.00"; no grouping, no symbol.
    static func plain(_ minor: Int64, _ code: String) -> String {
        let exponent = Money.info(code).exponent
        guard exponent > 0 else { return String(minor) }
        let unit = Money.pow10(exponent)
        let (whole, fraction) = minor.magnitude.quotientAndRemainder(dividingBy: UInt64(unit))
        let digits = String(fraction)
        return (minor < 0 ? "-" : "") + "\(whole)." + String(repeating: "0", count: exponent - digits.count) + digits
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
