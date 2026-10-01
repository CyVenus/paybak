import Foundation

/// The bundled "today's rates" (seed/rates.json): INR per one unit of each currency. A record in a
/// currency other than the default saves the cross rate from here once and never recomputes it
/// (add-expense §3.9).
nonisolated struct RateTable: Decodable, Sendable {
    /// Decimal strings, INR per unit ("22.85" for AED).
    let inrPerUnit: [String: String]

    /// The rate from `code` to `target` (`{value: "22.85", to: "INR"}`), nil when either is unknown
    /// or they're the same currency.
    func rate(from code: String, to target: String) -> Rate? {
        guard code != target,
              let from = decimal(code), let to = decimal(target), to != 0 else { return nil }
        var value = from / to
        var rounded = Decimal()
        NSDecimalRound(&rounded, &value, 6, .plain)
        return Rate(value: NSDecimalNumber(decimal: rounded).stringValue, to: target)
    }

    private func decimal(_ code: String) -> Decimal? {
        inrPerUnit[code].flatMap { Decimal(string: $0, locale: Locale(identifier: "en_US_POSIX")) }
    }
}
