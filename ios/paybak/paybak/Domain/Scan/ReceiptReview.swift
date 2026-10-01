import Foundation

/// A read receipt as the user checks and assigns it (screens-insights-ai §4.3–§4.6): editable lines,
/// the total they make, who had each item, and the itemized expense it becomes.
nonisolated struct ReceiptReview: Hashable, Sendable {
    /// Empty for a reassignment, whose draft then leaves the form's title alone.
    var merchant: String
    var date: LocalDay
    /// "1:15 pm"
    var time: String?
    var items: [ReceiptScan.Line]
    var subtotal: Int64
    /// Tax, service and tip lines, in receipt order.
    var charges: [ReceiptScan.Line]
    /// Who had each item (index-aligned with `items`).
    var assignment: [Set<PersonID>]

    init(_ scan: ReceiptScan, today: LocalDay) {
        merchant = scan.merchant ?? "Receipt"
        // A receipt can't be from the future: that's a misread date.
        date = min(scan.date ?? today, today)
        time = scan.time
        items = scan.items
        subtotal = scan.subtotal ?? scan.items.reduce(0) { $0 + $1.amount }
        charges = scan.taxes + (scan.tip.map { [$0] } ?? [])
        assignment = scan.items.map { _ in [] }
    }

    /// A scanned expense's items, as assigned, to change who had what (the form's Split row). People
    /// since taken off the expense drop out of the assignment.
    init(reassigning itemized: Itemized, among people: [PersonID], on date: LocalDay) {
        merchant = ""
        self.date = date
        items = itemized.items.map { .init(label: $0.label, amount: $0.amount) }
        subtotal = itemized.subtotal
        charges = itemized.lines.map { .init(label: $0.label, amount: $0.amount) }
        assignment = itemized.items.map { Set($0.personIds.filter(people.contains)) }
    }

    /// Subtotal + tax + tip (the Total row isn't edited directly).
    var total: Int64 { subtotal + charges.reduce(0) { $0 + $1.amount } }
    var itemsTotal: Int64 { items.reduce(0) { $0 + $1.amount } }
    var addsUp: Bool { itemsTotal == subtotal }
    var unassignedCount: Int { assignment.filter(\.isEmpty).count }

    mutating func addItem(_ label: String, amount: Int64) {
        items.append(.init(label: label, amount: amount))
        assignment.append([])
    }

    mutating func toggle(_ person: PersonID, onItem index: Int) {
        if assignment[index].contains(person) {
            assignment[index].remove(person)
        } else {
            assignment[index].insert(person)
        }
    }

    /// "We found 6 items." / "We found 1 item."
    var foundLine: String { "We found \(items.count) item\(items.count == 1 ? "" : "s")." }

    /// "Items add up to ₹1,900, the subtotal is ₹2,000." while they don't match.
    func mismatchLine(currency: String) -> String? {
        addsUp ? nil : "Items add up to \(Money.format(itemsTotal, currency)), the subtotal is \(Money.format(subtotal, currency))."
    }

    /// "Wed 30 Sep · 1:15 pm"
    var dateLine: String { [Format.day(date), time].compactMap(\.self).joined(separator: " · ") }

    /// "Shared by 3 · ₹80 each" for an item two or more people had.
    func sharedCaption(_ index: Int, currency: String) -> String? {
        let people = assignment[index]
        guard people.count > 1 else { return nil }
        let (each, leftover) = items[index].amount.quotientAndRemainder(dividingBy: Int64(people.count))
        let prefix = leftover == 0 ? "" : "About "
        return "Shared by \(people.count) · \(prefix)\(Money.format(leftover == 0 ? each : Money.roundedToWholeUnits(each, currency), currency)) each"
    }

    /// Each person's part with tax and tip in proportion (§4.6 #4). While items are unassigned, only
    /// the assigned ones count (with their share of tax and tip).
    func shares(order: [PersonID]) -> [PersonID: Int64] {
        let assigned = zip(items, assignment).filter { !$0.1.isEmpty }
        let lines = assigned.map { item, people in
            Itemized.Item(label: item.label, amount: item.amount, personIds: order.filter(people.contains))
        }
        let assignedTotal = assigned.reduce(Int64(0)) { $0 + $1.0.amount }
        let scaled = subtotal > 0 ? (assignedTotal * total + subtotal / 2) / subtotal : assignedTotal
        return Splits.itemized(items: lines, total: unassignedCount == 0 ? total : scaled, order: order).shares
    }

    /// The Add expense draft the scan ends in: "{meal} at {merchant}", the receipt's date, paid by
    /// you, split itemized among `order` with tax and tip in proportion.
    func expenseDraft(order: [PersonID], currency: String, receipt: Receipt?) -> ExpenseDraft {
        let shares = shares(order: order)
        let category = Self.category(merchant: merchant, items: items.map(\.label))
        return ExpenseDraft(
            title: merchant.isEmpty ? "" : Self.title(merchant: merchant, time: time, category: category), category: category, amount: total,
            currency: currency, date: date, payers: [Payer(personId: Person.me, amount: total)], splitMode: .itemized,
            rows: order.map { SplitRow(personId: $0, included: true, value: shares[$0], share: shares[$0] ?? 0) },
            itemized: Itemized(
                items: zip(items, assignment).map { item, people in
                    Itemized.Item(label: item.label, amount: item.amount, personIds: order.filter(people.contains))
                },
                lines: charges.map { Itemized.Line(label: $0.label, amount: $0.amount) },
                subtotal: subtotal
            ),
            receipt: receipt
        )
    }

    /// Food for cafés and restaurants (or a receipt of mostly food), else Other.
    static func category(merchant: String, items: [String]) -> ExpenseCategory {
        let words = merchant.lowercased()
        if ["cafe", "café", "restaurant", "dhaba", "bar", "bistro", "kitchen"].contains(where: words.contains) { return .food }
        let food = items.filter { AssistantParser.category(for: $0) == .food }.count
        return !items.isEmpty && food * 2 >= items.count ? .food : .other
    }

    /// "Lunch at Leopold Cafe" for food by the receipt's time (05–11 Breakfast, 11–16 Lunch, 16–19
    /// Snacks, else Dinner); otherwise the merchant.
    static func title(merchant: String, time: String?, category: ExpenseCategory) -> String {
        guard category == .food, let time, let hour = hour(time) else { return merchant }
        let meal = switch hour {
        case 5..<11: "Breakfast"
        case 11..<16: "Lunch"
        case 16..<19: "Snacks"
        default: "Dinner"
        }
        return "\(meal) at \(merchant)"
    }

    /// "1:15 pm" → 13.
    private static func hour(_ time: String) -> Int? {
        guard let match = time.firstMatch(of: /(\d{1,2}):\d{2} (am|pm)/), let hour = Int(match.1) else { return nil }
        return hour % 12 + (match.2 == "pm" ? 12 : 0)
    }
}
