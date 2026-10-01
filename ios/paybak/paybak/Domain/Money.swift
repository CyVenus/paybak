import Foundation

/// A saved exchange rate (domain.md §0): `value` units of `to` per 1 unit of the record currency, as
/// a decimal string so it's never rounded by floating point.
nonisolated struct Rate: Codable, Hashable, Sendable {
    var value: String
    var to: String

    var decimal: Decimal { Decimal(string: value, locale: Locale(identifier: "en_US_POSIX")) ?? 0 }
}

/// How `Money.format` writes the sign (domain.md §2.2).
nonisolated enum MoneySign: Sendable {
    /// Unsigned (timeline, settle rows, chat).
    case none
    /// `+₹2,900` / `−₹1,850`.
    case signed
    /// `−₹450` for negatives only.
    case debit
}

/// Money in integer minor units (paise, fils, whole yen): formatting and conversion
/// (domain.md §2, verify.py §2). Never floating point.
nonisolated enum Money {
    struct CurrencyInfo: Sendable {
        let symbol: String
        /// Minor-unit exponent: 2 for INR (paise), 0 for JPY.
        let exponent: Int
        let name: String
    }

    /// U+2212, the minus in every amount the user sees.
    static let minus = "\u{2212}"

    static let currencies: [String: CurrencyInfo] = [
        "INR": CurrencyInfo(symbol: "₹", exponent: 2, name: "Indian rupee"),
        "AED": CurrencyInfo(symbol: "AED", exponent: 2, name: "UAE dirham"),
        "USD": CurrencyInfo(symbol: "$", exponent: 2, name: "US dollar"),
        "EUR": CurrencyInfo(symbol: "€", exponent: 2, name: "Euro"),
        "GBP": CurrencyInfo(symbol: "£", exponent: 2, name: "British pound"),
        "SGD": CurrencyInfo(symbol: "S$", exponent: 2, name: "Singapore dollar"),
        "AUD": CurrencyInfo(symbol: "A$", exponent: 2, name: "Australian dollar"),
        "CAD": CurrencyInfo(symbol: "C$", exponent: 2, name: "Canadian dollar"),
        "JPY": CurrencyInfo(symbol: "¥", exponent: 0, name: "Japanese yen"),
    ]

    /// ISO 4217 minor units that aren't 2, for currencies outside the designed table (the same
    /// digits Android reads from `java.util.Currency`).
    private static let minorUnits: [String: Int] = [
        "BIF": 0, "CLP": 0, "DJF": 0, "GNF": 0, "ISK": 0, "JPY": 0, "KMF": 0, "KRW": 0, "PYG": 0, "RWF": 0,
        "UGX": 0, "UYI": 0, "VND": 0, "VUV": 0, "XAF": 0, "XOF": 0, "XPF": 0,
        "BHD": 3, "IQD": 3, "JOD": 3, "KWD": 3, "LYD": 3, "OMR": 3, "TND": 3,
        "CLF": 4, "UYW": 4,
    ]

    /// The designed symbol and exponent; any other ISO code is written as its code with its ISO
    /// minor unit (2 for most).
    static func info(_ code: String) -> CurrencyInfo {
        currencies[code] ?? CurrencyInfo(symbol: code, exponent: minorUnits[code] ?? 2, name: code)
    }

    /// `₹2,900` · `₹1,00,000` · `₹1,234.50` · `AED 1,800` · `+₹2,900` · `−₹1,850` (§2.2).
    static func format(_ minor: Int64, _ code: String = "INR", sign: MoneySign = .none) -> String {
        let info = info(code)
        let value = minor.magnitude
        let unit = UInt64(pow10(info.exponent))
        let (whole, fraction) = info.exponent == 0 ? (value, 0) : value.quotientAndRemainder(dividingBy: unit)
        var text = groupDigits(whole, code: code)
        if fraction > 0 {
            let digits = String(fraction)
            text += "." + String(repeating: "0", count: info.exponent - digits.count) + digits
        }
        let body = info.symbol.count <= 2 ? info.symbol + text : "\(info.symbol) \(text)"
        switch (sign, minor.signum()) {
        case (.signed, 1): return "+" + body
        case (.signed, -1), (.debit, -1): return minus + body
        default: return body
        }
    }

    /// Indian grouping for INR (last 3 digits, then pairs), thousands for everything else.
    static func groupDigits(_ whole: UInt64, code: String) -> String {
        let digits = String(whole)
        let groupSizes = code == "INR" ? (first: 3, rest: 2) : (first: 3, rest: 3)
        guard digits.count > groupSizes.first else { return digits }
        var groups = [String(digits.suffix(groupSizes.first))]
        var head = digits.dropLast(groupSizes.first)
        while head.count > groupSizes.rest {
            groups.insert(String(head.suffix(groupSizes.rest)), at: 0)
            head = head.dropLast(groupSizes.rest)
        }
        groups.insert(String(head), at: 0)
        return groups.joined(separator: ",")
    }

    /// `minor` of `from` → minor units of `to` at a saved decimal rate, rounded half up (§2.1).
    static func convert(_ minor: Int64, rate: Decimal, from: String, to: String) -> Int64 {
        let shift = info(to).exponent - info(from).exponent
        var value = Decimal(minor) * rate * pow10Decimal(shift)
        var rounded = Decimal()
        NSDecimalRound(&rounded, &value, 0, .plain)
        return NSDecimalNumber(decimal: rounded).int64Value
    }

    /// An amount in the default currency: unchanged when it already is, else converted at its rate.
    static func toDefault(_ minor: Int64, currency: String, rate: Rate?, defaultCurrency: String) -> Int64 {
        guard currency != defaultCurrency else { return minor }
        guard let rate, rate.to == defaultCurrency else { return minor }
        return convert(minor, rate: rate.decimal, from: currency, to: defaultCurrency)
    }

    /// Whole units rounded half up (the "≈ ₹6,870" lines), back in minor units.
    static func roundedToWholeUnits(_ minor: Int64, _ code: String) -> Int64 {
        let unit = pow10(info(code).exponent)
        guard unit > 1 else { return minor }
        let (quotient, remainder) = minor.quotientAndRemainder(dividingBy: unit)
        return (quotient + (remainder * 2 >= unit ? 1 : 0)) * unit
    }

    /// "≈ ₹21,936 · ₹22.85 per AED" (§2.2): the converted amount in whole units (half to even, as
    /// Android rounds it) and the rate.
    static func approximateLine(_ minor: Int64, currency: String, rate: Rate) -> String {
        let converted = convert(minor, rate: rate.decimal, from: currency, to: rate.to)
        return "≈ \(format(wholeUnitsHalfEven(converted, rate.to), rate.to)) · \(info(rate.to).symbol)\(rateText(rate.decimal)) per \(currency)"
    }

    /// Whole units rounded half to even, back in minor units.
    private static func wholeUnitsHalfEven(_ minor: Int64, _ code: String) -> Int64 {
        let unit = pow10(info(code).exponent)
        guard unit > 1 else { return minor }
        let (quotient, remainder) = minor.quotientAndRemainder(dividingBy: unit)
        let twice = abs(remainder) * 2
        let roundsAway = twice > unit || (twice == unit && quotient % 2 != 0)
        return (quotient + (roundsAway ? minor.signum() : 0)) * unit
    }

    /// A rate with exactly two decimals, rounded half up ("22.9" → "22.90").
    static func rateText(_ rate: Decimal) -> String {
        var value = rate * 100
        var hundredths = Decimal()
        NSDecimalRound(&hundredths, &value, 0, .plain)
        let (whole, cents) = NSDecimalNumber(decimal: hundredths).int64Value.quotientAndRemainder(dividingBy: 100)
        return "\(whole)." + (cents < 10 ? "0\(cents)" : "\(cents)")
    }

    /// 10^exponent in minor units.
    static func pow10(_ exponent: Int) -> Int64 {
        (0..<max(exponent, 0)).reduce(1) { value, _ in value * 10 }
    }

    private static func pow10Decimal(_ exponent: Int) -> Decimal {
        exponent >= 0 ? Decimal(pow10(exponent)) : 1 / Decimal(pow10(-exponent))
    }
}
