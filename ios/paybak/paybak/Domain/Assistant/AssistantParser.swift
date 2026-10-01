import Foundation

/// How Ask Paybak reads a prompt (app-architecture §4.3, screens-insights-ai §3.6): normalised text,
/// the question shapes it knows, and the "Add ₹600 for a cab, split with Esha and Dev" grammar.
nonisolated enum AssistantParser {
    /// What a prompt asks for, before it's matched against the ledger.
    enum Intent: Equatable {
        case whoOwesMe
        /// The category word as typed ("food"), and a month name if one was given.
        case spend(category: String, month: String?)
        case due(group: String)
        case reminder(name: String)
        case expense(ExpensePhrase)
        case unknown
    }

    struct ExpensePhrase: Equatable {
        /// The amount as typed, without grouping ("600", "1250.50").
        var amount: String
        /// "cab"
        var what: String?
        var names: [String]
        var group: String?
    }

    /// Lower-case, trimmed, single spaces, straight quotes, no trailing "?" or ".".
    static func normalize(_ text: String) -> String {
        var text = text.lowercased()
            .replacingOccurrences(of: "’", with: "'")
            .replacingOccurrences(of: "‘", with: "'")
            .replacingOccurrences(of: "“", with: "\"")
            .replacingOccurrences(of: "”", with: "\"")
            .split(whereSeparator: \.isWhitespace).joined(separator: " ")
        while let last = text.last, "?.!".contains(last) { text.removeLast() }
        return text.trimmingCharacters(in: .whitespaces)
    }

    static func intent(of prompt: String) -> Intent {
        let text = normalize(prompt)
        if ["who owes me", "who owes me money", "what am i owed", "how much am i owed"].contains(text) {
            return .whoOwesMe
        }
        if let match = text.firstMatch(of: /^how much (?:did|have) i spend on (.+?)(?: this month| in ([a-z]+))?$/) {
            return .spend(category: String(match.1), month: match.2.map(String.init))
        }
        if let match = text.firstMatch(of: /^when (?:is|do i pay) (.+?)(?: due)?$/) {
            return .due(group: String(match.1))
        }
        if let match = text.firstMatch(of: /^(?:draft a reminder for|remind) ([a-z]+)$/) {
            return .reminder(name: String(match.1))
        }
        if let phrase = expensePhrase(text) {
            return .expense(phrase)
        }
        return .unknown
    }

    /// `[add|log|split]? {money} (for (a|an|the)? {what})? (,)? (split)? (with {names})? (in {group})?`
    /// A bare number only counts after a verb, with a currency word, or followed by "for" / "with".
    static func expensePhrase(_ text: String) -> ExpensePhrase? {
        let money = /(₹|rs\.? ?|inr ?)?([0-9][0-9,]*(?:\.[0-9]{1,2})?)( ?rupees| ?rs| ?inr)?/
        guard let amountMatch = text.firstMatch(of: money) else { return nil }
        let amount = amountMatch.2.replacingOccurrences(of: ",", with: "")
        var rest = String(text[amountMatch.range.upperBound...])
        let hasVerb = text.firstMatch(of: #/^(add|log|split|record|spent|paid) /#) != nil
        let hasCurrency = amountMatch.1 != nil || amountMatch.3 != nil
        guard Double(amount).map({ $0 > 0 }) == true,
              hasVerb || hasCurrency || rest.hasPrefix(" for ") || rest.hasPrefix(" with ") else { return nil }
        var group: String?
        if let match = rest.firstMatch(of: #/ in (.+)$/#) {
            group = String(match.1)
            rest = String(rest[..<match.range.lowerBound])
        }
        var names: [String] = []
        if let match = rest.firstMatch(of: /(?:,? ?split)? with (.+)$/) {
            names = String(match.1)
                .split(separator: #/,| and |&/#)
                .map { $0.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }
            rest = String(rest[..<match.range.lowerBound])
        }
        var what: String?
        if let match = rest.firstMatch(of: /for (?:a |an |the )?([^,]+)/) {
            what = String(match.1).trimmingCharacters(in: .whitespaces)
        }
        return ExpensePhrase(amount: amount, what: what?.isEmpty == true ? nil : what, names: names, group: group)
    }

    /// The category a word or phrase suggests (insights §3.6.6, with groceries → Food per domain §12 #9).
    static func category(for text: String) -> ExpenseCategory {
        let words = Set(normalize(text).split(whereSeparator: { !$0.isLetter && $0 != "-" }).map(String.init))
        for (category, keywords) in keywords where !words.isDisjoint(with: keywords) {
            return category
        }
        return ExpenseCategory.allCases.first { words.contains($0.name.lowercased()) } ?? .other
    }

    private static let keywords: [(ExpenseCategory, Set<String>)] = [
        (.travel, ["cab", "taxi", "uber", "ola", "auto", "metro", "train", "bus", "flight", "fuel", "petrol", "parking"]),
        (.food, ["dinner", "lunch", "breakfast", "food", "coffee", "snacks", "pizza", "biryani", "restaurant", "groceries"]),
        (.rent, ["rent"]),
        (.bills, ["electricity", "wifi", "wi-fi", "internet", "gas", "water", "bill", "bills"]),
        (.fun, ["movie", "movies", "tickets", "concert", "game"]),
        (.stays, ["hotel", "stay", "hostel", "airbnb", "villa"]),
        (.shopping, ["shopping"]),
    ]
}
