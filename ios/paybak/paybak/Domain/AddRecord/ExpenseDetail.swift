import Foundation

/// Everything the Expense detail template shows (screens-activity §4.3, add-expense §11), worked out
/// from the records: hero, share card, split, receipt, comments, history and the flag.
nonisolated struct ExpenseDetail: Sendable {
    struct SplitLine: Hashable, Sendable, Identifiable {
        var id: PersonID { personId }
        let personId: PersonID
        /// "You" or the first name.
        let name: String
        /// "Paid ₹2,800" on payer rows.
        let subtitle: String?
        let value: String
    }

    struct CommentLine: Hashable, Sendable, Identifiable {
        let id: String
        let personId: PersonID
        let name: String
        let date: String
        let text: String
    }

    struct HistoryLine: Hashable, Sendable {
        let text: String
        let date: String
    }

    let expense: Expense
    let title: String
    /// "₹2,800" / "AED 1,200".
    let amount: String
    /// "Paid by you · Today".
    let meta: String
    /// "≈ ₹27,420 · ₹22.85 per AED" for a foreign-currency expense.
    let rateLine: String?
    let groupName: String?
    let categoryName: String
    /// Your share, nil when you're not on it.
    let yourShare: String?
    /// "Sun 4 Oct", nil without a due date.
    let due: String?
    /// "Your Goa Trip balance" and your signed net there.
    let groupBalance: (title: String, net: Int64, currency: String)?
    /// "Split equally · 4 people".
    let splitHeader: String
    let splitLines: [SplitLine]
    /// "Added by Kabir · 21 Sep".
    let receiptCaption: String?
    let comments: [CommentLine]
    /// Newest first.
    let history: [HistoryLine]
    /// "Esha flagged this expense" and the quoted note.
    let flag: (title: String, note: String)?
    /// Flag an issue: people on it who didn't pay and haven't flagged it.
    let canFlag: Bool
}

nonisolated extension Books {
    func expenseDetail(_ id: ExpenseID) -> ExpenseDetail? {
        guard let expense = ledger.expense(id) else { return nil }
        let payerIds = expense.payers.map(\.personId)
        let group = expense.groupId.flatMap(ledger.group)
        let myRow = expense.split.rows.first { $0.personId == Person.me }
        return ExpenseDetail(
            expense: expense,
            title: expense.title,
            amount: Money.format(expense.amount, expense.currency),
            meta: "Paid by \(payerPhrase(payerIds)) · \(Format.rowDate(expense.date, today: today))",
            rateLine: expense.rate.map { Money.approximateLine(expense.amount, currency: expense.currency, rate: $0) },
            groupName: group?.name,
            categoryName: expense.category.name,
            yourShare: myRow.map { Money.format($0.share, expense.currency) },
            due: effectiveDue(expense).map(Format.day),
            groupBalance: group.map { group in
                ("Your \(group.name) balance", groupNets(group.id)[Person.me, default: 0], group.currency)
            },
            splitHeader: splitHeader(expense),
            splitLines: splitLines(expense, group: group),
            receiptCaption: expense.receipt.map { "Added by \(name($0.addedBy)) · \(Format.short(day(of: $0.addedAt)))" },
            comments: expense.comments.map { comment in
                ExpenseDetail.CommentLine(id: comment.id, personId: comment.by, name: firstName(comment.by),
                                          date: Format.rowDate(day(of: comment.at), today: today), text: comment.text)
            },
            history: historyLines(expense),
            flag: expense.flag.map { ("\(firstName($0.by)) flagged this expense", "“\($0.note)”") },
            canFlag: myRow != nil && !payerIds.contains(Person.me) && expense.flag?.by != Person.me
        )
    }

    /// "you", "Kabir", "Kabir and you", "3 people".
    private func payerPhrase(_ ids: [PersonID]) -> String {
        switch ids.count {
        case 0, 1: name(ids.first ?? Person.me)
        case 2: Format.joinedNames(ids.sorted { $1 == Person.me }.map(name))
        default: "\(ids.count) people"
        }
    }

    private func splitHeader(_ expense: Expense) -> String {
        let count = expense.split.rows.filter(\.included).count
        let people = "\(count) \(count == 1 ? "person" : "people")"
        let mode = switch expense.split.mode {
        case .equal: "equally"
        case .exact: "by exact amounts"
        case .percent: "by percentages"
        case .shares: "by shares"
        case .itemized: "by items"
        }
        return "Split \(mode) · \(people)"
    }

    /// Payers first, then you, then everyone else in group-member (or added) order.
    private func splitLines(_ expense: Expense, group: LedgerGroup?) -> [ExpenseDetail.SplitLine] {
        let payerIds = expense.payers.map(\.personId)
        let rows = expense.split.rows.filter { $0.included || payerIds.contains($0.personId) }
        let memberOrder = group?.memberIds ?? []
        func rank(_ id: PersonID) -> (Int, Int) {
            if let payer = payerIds.firstIndex(of: id) { return (0, payer) }
            if id == Person.me { return (1, 0) }
            return (2, memberOrder.firstIndex(of: id) ?? memberOrder.count)
        }
        let ordered = rows.enumerated().sorted { lhs, rhs in
            let (a, b) = (rank(lhs.element.personId), rank(rhs.element.personId))
            return a != b ? a < b : lhs.offset < rhs.offset
        }
        return ordered.map { _, row in
            let paid = expense.paid(by: row.personId)
            return ExpenseDetail.SplitLine(personId: row.personId, name: firstName(row.personId),
                                           subtitle: paid > 0 ? "Paid \(Money.format(paid, expense.currency))" : nil,
                                           value: Money.format(row.share, expense.currency))
        }
    }

    /// History copy (activity §4.3-F), newest first.
    private func historyLines(_ expense: Expense) -> [ExpenseDetail.HistoryLine] {
        var lastFlagger: PersonID?
        var lines: [ExpenseDetail.HistoryLine] = []
        for entry in expense.history {
            let actor = firstName(entry.by)
            let text: String
            switch entry.kind {
            case .created: text = "\(actor) added this"
            case .amountChanged:
                let old = entry.old?.amount.map { Money.format($0, expense.currency) } ?? ""
                let new = entry.new?.amount.map { Money.format($0, expense.currency) } ?? ""
                text = "\(actor) changed the amount from \(old) to \(new)"
            case .titleChanged: text = "\(actor) changed the title to “\(entry.new?.textValue ?? expense.title)”"
            case .dateChanged:
                let date = entry.new?.textValue.flatMap(LocalDay.init(string:)) ?? expense.date
                text = "\(actor) changed the date to \(Format.short(date))"
            case .splitChanged: text = "\(actor) changed the split"
            case .payersChanged: text = "\(actor) changed who paid"
            case .categoryChanged:
                let category = entry.new?.textValue.flatMap(ExpenseCategory.init(rawValue:)) ?? expense.category
                text = "\(actor) changed the category to \(category.name)"
            case .receiptAdded: text = "\(actor) added a receipt"
            case .flagged:
                lastFlagger = entry.by
                text = "\(actor) flagged this"
            case .flagRemoved: text = "\(actor) removed their flag"
            case .flagResolved:
                text = lastFlagger.map { "\(actor) resolved \(firstName($0))’s flag" } ?? "\(actor) resolved a flag"
            case .deleted: text = "\(actor) deleted this"
            case .restored: text = "\(actor) restored this"
            }
            lines.append(ExpenseDetail.HistoryLine(text: text, date: Format.rowDate(day(of: entry.at), today: today)))
        }
        return lines.reversed()
    }
}

private nonisolated extension HistoryValue {
    var textValue: String? {
        if case .text(let text) = self { return text }
        return nil
    }
}
