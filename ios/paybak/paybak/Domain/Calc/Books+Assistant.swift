import Foundation

/// Ask Paybak's answers from the same read models as Home and Insights (domain.md §6.9). Lane C's
/// assistant parses prompts and builds its cards on these.
nonisolated extension Books {
    /// "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each
    /// for tonight’s dinner."
    func whoOwesAnswer() -> String {
        let contexts = contexts()
        let nets = friendNets(contexts: contexts)
        let items = openItems(contexts: contexts)
        var parts: [String] = []
        var groupedKeys: [String] = []
        var grouped: [String: (expenseId: ExpenseID, amount: Int64, names: [String])] = [:]
        for row in settleRows(contexts: contexts).get {
            let mine = items.filter { $0.friend == row.friend }
            let overdue = mine.filter { $0.due.map { $0 < today } ?? false }
            if let first = overdue.first, let due = first.due {
                parts.append("\(firstName(row.friend)) \(Money.format(row.amount, defaultCurrency)) (overdue since \(Format.short(due)))")
            } else if mine.count == 1, mine[0].kind == .direct {
                let key = "\(mine[0].ref)|\(row.amount)"
                if grouped[key] == nil {
                    groupedKeys.append(key)
                    grouped[key] = (mine[0].ref, row.amount, [])
                }
                grouped[key]?.names.append(firstName(row.friend))
            } else {
                parts.append("\(firstName(row.friend)) \(Money.format(row.amount, defaultCurrency))")
            }
        }
        for key in groupedKeys {
            guard let entry = grouped[key], let expense = ledger.expense(entry.expenseId) else { continue }
            let what = if expense.date == today && expense.category == .food && expense.title.hasPrefix("Dinner") {
                "tonight’s dinner"
            } else if expense.date == today {
                "today’s \(expense.title.lowercased())"
            } else {
                expense.title
            }
            let each = entry.names.count > 1 ? " each" : ""
            parts.append("\(Format.joinedNames(entry.names)) \(Money.format(entry.amount, defaultCurrency))\(each) for \(what)")
        }
        let owed = nets.values.filter { $0 > 0 }
        guard !parts.isEmpty else { return "No one owes you anything right now." }
        let joined = parts.count == 1 ? parts[0] : parts.dropLast().joined(separator: ", ") + ", and " + parts[parts.count - 1]
        let people = owed.count == 1 ? "1 person owes" : "\(owed.count) people owe"
        return "\(people) you \(Money.format(owed.reduce(0, +), defaultCurrency)): \(joined)."
    }
}
