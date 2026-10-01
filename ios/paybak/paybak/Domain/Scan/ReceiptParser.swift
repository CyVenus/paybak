import Foundation

/// What a receipt photo says (app-architecture §4.1).
nonisolated struct ReceiptScan: Hashable, Sendable {
    struct Line: Hashable, Sendable {
        var label: String
        var amount: Int64
    }

    var merchant: String?
    var date: LocalDay?
    /// "1:15 pm"
    var time: String?
    var items: [Line] = []
    var subtotal: Int64?
    /// GST, CGST, SGST, service charge…
    var taxes: [Line] = []
    var tip: Line?
    var total: Int64?
}

/// A piece of recognised text and where it sits on the photo (0…1, y growing downwards).
nonisolated struct ReceiptText: Hashable, Sendable {
    var text: String
    var minX: Double
    var maxX: Double
    var minY: Double
    var maxY: Double
}

/// Turns recognised text into a `ReceiptScan` (insights §4.6): rows by position, the right-most
/// money token as each row's amount, and Subtotal / GST x% / CGST / SGST / Service charge / Tip x% /
/// Total by name. The merchant is the first line; the date and time are read where they appear.
/// Text recognition often reads "₹" as a digit ("₹2,000" → "72,000"), so the amounts are then
/// checked against the receipt's own sums (items = subtotal, subtotal + charges = total).
nonisolated enum ReceiptParser {
    static func parse(_ texts: [ReceiptText], currency: String) -> ReceiptScan? {
        var scan = ReceiptScan()
        var items: [(String, Reading)] = []
        var charges: [(String, Reading, isTip: Bool)] = []
        var subtotal: Reading?
        var total: Reading?
        for row in rows(texts) {
            let words = row.map(\.text).joined(separator: " ")
            if scan.merchant == nil {
                scan.merchant = words
                continue
            }
            if scan.date == nil, let date = date(in: words) {
                scan.date = date
                scan.time = time(in: words)
                continue
            }
            guard row.count > 1, let last = row.last, let reading = reading(last.text, currency: currency) else { continue }
            let label = Self.label(row.dropLast().map(\.text).joined(separator: " "))
            switch kind(of: label) {
            case .subtotal: subtotal = reading
            case .tax: charges.append((label, reading, false))
            case .tip: charges.append((label, reading, true))
            case .total: total = reading
            case .item where subtotal == nil && charges.isEmpty && total == nil: items.append((label, reading))
            case .item: continue
            }
        }
        guard !items.isEmpty else { return nil }
        let values = reconcile(items: items.map(\.1), subtotal: subtotal, charges: charges.map(\.1), total: total)
        scan.items = zip(items, values.items).map { .init(label: $0.0, amount: $1) }
        scan.subtotal = values.subtotal
        for ((label, _, isTip), amount) in zip(charges, values.charges) {
            if isTip { scan.tip = .init(label: label, amount: amount) } else { scan.taxes.append(.init(label: label, amount: amount)) }
        }
        scan.total = values.total
        return scan
    }

    /// An amount as read, and what it is without its first digit when that may be a misread "₹".
    struct Reading: Equatable {
        var value: Int64
        var withoutFirstDigit: Int64?
    }

    /// The text's money value when it ends in a number after at most a currency mark ("₹2,300",
    /// "·430", "R240", "Rs. 99.50", "72,000").
    static func reading(_ text: String, currency: String) -> Reading? {
        let text = text.trimmingCharacters(in: .whitespaces)
        guard let match = text.firstMatch(of: /([0-9][0-9,]*(?:\.[0-9]{1,2})?)$/) else { return nil }
        let prefix = text[..<match.range.lowerBound].trimmingCharacters(in: .whitespaces)
        guard prefix.count <= 2 || ["rs.", "inr"].contains(prefix.lowercased()) else { return nil }
        let digits = String(match.1)
        let value = MoneyInput.minor(digits.replacingOccurrences(of: ",", with: ""), currency: currency)
        guard prefix.isEmpty, digits.count > 1 else { return Reading(value: value) }
        let rest = digits.dropFirst().drop { $0 == "," }.replacingOccurrences(of: ",", with: "")
        return Reading(value: value, withoutFirstDigit: rest.isEmpty ? nil : MoneyInput.minor(rest, currency: currency))
    }

    /// Picks, for every amount, the reading that makes the receipt add up, changing as few as it can;
    /// with no consistent choice the amounts stay as read (Check receipt then flags the mismatch).
    static func reconcile(items: [Reading], subtotal: Reading?, charges: [Reading], total: Reading?)
        -> (items: [Int64], subtotal: Int64?, charges: [Int64], total: Int64?) {
        let readings = items + charges + [subtotal, total].compactMap(\.self)
        let flexible = readings.indices.filter { readings[$0].withoutFirstDigit != nil }
        var best = (score: -1, drops: Int.max, values: readings.map(\.value))
        if flexible.count <= 16 {
            for mask in 0..<(1 << flexible.count) {
                var values = readings.map(\.value)
                for (bit, index) in flexible.enumerated() where mask & (1 << bit) != 0 {
                    values[index] = readings[index].withoutFirstDigit!
                }
                let itemSum = values[..<items.count].reduce(0, +)
                let chargeSum = values[items.count..<(items.count + charges.count)].reduce(0, +)
                var next = items.count + charges.count
                let sub = subtotal.map { _ in values[next] }
                if sub != nil { next += 1 }
                let tot = total.map { _ in values[next] }
                var score = 0
                if let sub, sub == itemSum { score += 2 }
                if let tot, tot == (sub ?? itemSum) + chargeSum { score += 1 }
                let drops = mask.nonzeroBitCount
                if score > best.score || (score == best.score && drops < best.drops) {
                    best = (score, drops, values)
                }
            }
        }
        let values = best.values
        var next = items.count + charges.count
        let sub = subtotal.map { _ in values[next] }
        if sub != nil { next += 1 }
        return (Array(values[..<items.count]), sub, Array(values[items.count..<(items.count + charges.count)]),
                total.map { _ in values[next] })
    }

    /// A row's words before its amount, tidied as Android does: no trailing ".", ":" or "-", and a
    /// quantity written "×3" ("Fresh lime soda x3" → "Fresh lime soda ×3").
    static func label(_ text: String) -> String {
        var label = text.trimmingCharacters(in: .whitespaces)
        while let last = label.last, ".:-".contains(last) { label.removeLast() }
        label = label.trimmingCharacters(in: .whitespaces)
        if let match = label.firstMatch(of: /\s[xX](\d+)$/) {
            label = String(label[..<match.range.lowerBound]) + " ×" + String(match.1)
        }
        return label
    }

    private enum Kind {
        case item, subtotal, tax, tip, total
    }

    private static func kind(of label: String) -> Kind {
        let label = label.lowercased()
        if label.hasPrefix("subtotal") || label.hasPrefix("sub total") || label.hasPrefix("sub-total") { return .subtotal }
        if ["gst", "cgst", "sgst", "igst", "vat", "tax", "service charge"].contains(where: label.hasPrefix) { return .tax }
        if label.hasPrefix("tip") || label.hasPrefix("gratuity") { return .tip }
        if label.hasPrefix("total") || label.hasPrefix("grand total") || label.hasPrefix("amount due") { return .total }
        return .item
    }

    /// Groups texts whose vertical ranges overlap by at least half the shorter one, top to bottom,
    /// each row left to right.
    static func rows(_ texts: [ReceiptText]) -> [[ReceiptText]] {
        var rows: [[ReceiptText]] = []
        for text in texts.sorted(by: { $0.minY < $1.minY }) {
            if let index = rows.lastIndex(where: { row in row.contains { overlaps($0, text) } }) {
                rows[index].append(text)
            } else {
                rows.append([text])
            }
        }
        return rows.map { $0.sorted { $0.minX < $1.minX } }
    }

    private static func overlaps(_ a: ReceiptText, _ b: ReceiptText) -> Bool {
        let overlap = min(a.maxY, b.maxY) - max(a.minY, b.minY)
        return overlap >= 0.5 * min(a.maxY - a.minY, b.maxY - b.minY)
    }

    /// "Wed 30 Sep 2026 · 1:15 pm" → 30 Sep 2026; also 30/09/2026 and 30-09-26.
    static func date(in text: String) -> LocalDay? {
        let lower = text.lowercased()
        if let match = lower.firstMatch(of: /(\d{1,2})\s+([a-z]{3})[a-z]*\.?,?\s+(\d{4})/),
           let month = Format.monthNames.firstIndex(where: { $0.lowercased().hasPrefix(String(match.2)) }),
           let day = Int(match.1), let year = Int(match.3) {
            return valid(year: year, month: month + 1, day: day)
        }
        if let match = lower.firstMatch(of: /(\d{1,2})[\/\-.](\d{1,2})[\/\-.](\d{2,4})/),
           let day = Int(match.1), let month = Int(match.2), let year = Int(match.3) {
            return valid(year: year < 100 ? 2000 + year : year, month: month, day: day)
        }
        return nil
    }

    /// "1:15 pm" (lower-case, as the app writes times).
    static func time(in text: String) -> String? {
        guard let match = text.lowercased().firstMatch(of: /(\d{1,2}):(\d{2})\s*(am|pm)?/) else { return nil }
        if let half = match.3 { return "\(Int(match.1) ?? 0):\(match.2) \(half)" }
        guard let hour = Int(match.1), hour < 24 else { return nil }
        return "\(hour % 12 == 0 ? 12 : hour % 12):\(match.2) \(hour < 12 ? "am" : "pm")"
    }

    private static func valid(year: Int, month: Int, day: Int) -> LocalDay? {
        guard (1...12).contains(month), (1...LocalDay.daysIn(month: month, year: year)).contains(day) else { return nil }
        return LocalDay(year: year, month: month, day: day)
    }
}
