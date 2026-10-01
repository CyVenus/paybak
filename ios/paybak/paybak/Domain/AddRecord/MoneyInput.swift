import Foundation

/// Converts between what the user types in an amount or percent field ("2800.5", "25") and the
/// integer units the ledger stores (minor units, basis points). Never floating point.
nonisolated enum MoneyInput {
    /// "2800.5" → 280050 for INR; "1200" → 1200 for JPY. Empty or invalid → 0.
    static func minor(_ text: String, currency: String) -> Int64 {
        fixedPoint(text, decimals: Money.info(currency).exponent)
    }

    /// 280050 → "2800.5"; 280000 → "2800" (the raw text a field starts from).
    static func text(_ minor: Int64, currency: String) -> String {
        fixedText(minor, decimals: Money.info(currency).exponent)
    }

    /// The typed text with its whole part grouped the currency's way, as amounts show it:
    /// "2800" → "2,800"; "280000.5" → "2,80,000.5" for INR.
    static func grouped(_ text: String, currency: String) -> String {
        let whole = text.prefix { $0 != "." }
        guard !whole.hasPrefix("0"), let value = UInt64(whole) else { return text }
        return Money.groupDigits(value, code: currency) + text.dropFirst(whole.count)
    }

    /// "25" → 2500 bps; "33.33" → 3333.
    static func basisPoints(_ text: String) -> Int64 {
        fixedPoint(text, decimals: 2)
    }

    /// 2500 → "25"; 3333 → "33.33"; 1250 → "12.5".
    static func percentText(_ basisPoints: Int64) -> String {
        fixedText(basisPoints, decimals: 2)
    }

    private static func fixedPoint(_ text: String, decimals: Int) -> Int64 {
        let parts = text.split(separator: ".", maxSplits: 1, omittingEmptySubsequences: false)
        let whole = Int64(parts.first.map(String.init) ?? "") ?? 0
        var fraction: Int64 = 0
        if decimals > 0, parts.count > 1 {
            let digits = String(parts[1].prefix(decimals))
            let padded = digits + String(repeating: "0", count: decimals - digits.count)
            fraction = Int64(padded) ?? 0
        }
        return whole * Money.pow10(decimals) + fraction
    }

    private static func fixedText(_ value: Int64, decimals: Int) -> String {
        guard decimals > 0 else { return String(value) }
        let unit = Money.pow10(decimals)
        let (whole, fraction) = value.quotientAndRemainder(dividingBy: unit)
        guard fraction != 0 else { return String(whole) }
        var digits = String(abs(fraction))
        digits = String(repeating: "0", count: decimals - digits.count) + digits
        while digits.hasSuffix("0") { digits.removeLast() }
        return "\(whole).\(digits)"
    }
}
