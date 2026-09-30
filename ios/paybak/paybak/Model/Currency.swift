import Foundation

/// An ISO 4217 currency as the currency picker shows it.
struct Currency: Identifiable, Hashable {
    let code: String
    let name: String
    let symbol: String

    var id: String { code }

    /// What the 40 pt symbol tile shows: the symbol when it has at most two characters ("₹", "S$"),
    /// otherwise the ISO code in Caption/1 (the Figma AED row).
    var tileText: String { symbol.count <= 2 ? symbol : code }

    /// Figma's names and symbols for the six designed currencies, then the platform's localized
    /// name and the en-US symbol for every other code.
    init(code: String, locale: Locale = .current) {
        self.code = code
        if let designed = Self.designed[code] {
            name = designed.name
            symbol = designed.symbol
        } else {
            name = locale.localizedString(forCurrencyCode: code) ?? code
            symbol = Self.englishSymbol(for: code)
        }
    }
}

extension Currency {
    static let fallbackCode = "INR"
    /// The Popular section, in order (minus the suggested currency).
    static let popularCodes = ["USD", "EUR", "GBP", "AED", "SGD"]

    /// Every common ISO 4217 currency, sorted by name.
    static func all(locale: Locale = .current) -> [Currency] {
        Locale.commonISOCurrencyCodes
            .map { Currency(code: $0, locale: locale) }
            .sorted { $0.name.localizedStandardCompare($1.name) == .orderedAscending }
    }

    /// The device-region currency, or INR when the region has none we know.
    /// `isFromRegion` is false for the fallback, so the row can drop "· Based on your region".
    static func suggested(for locale: Locale = .current) -> (currency: Currency, isFromRegion: Bool) {
        if let code = locale.currency?.identifier, Locale.commonISOCurrencyCodes.contains(code) {
            return (Currency(code: code, locale: locale), true)
        }
        return (Currency(code: fallbackCode, locale: locale), false)
    }

    static func popular(excluding suggestedCode: String, locale: Locale = .current) -> [Currency] {
        popularCodes.filter { $0 != suggestedCode }.map { Currency(code: $0, locale: locale) }
    }

    private static let designed: [String: (name: String, symbol: String)] = [
        "INR": ("Indian Rupee", "₹"),
        "USD": ("US Dollar", "$"),
        "EUR": ("Euro", "€"),
        "GBP": ("British Pound", "£"),
        "AED": ("UAE Dirham", "AED"),
        "SGD": ("Singapore Dollar", "S$"),
    ]

    private static let englishFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US")
        formatter.numberStyle = .currency
        return formatter
    }()

    private static func englishSymbol(for code: String) -> String {
        englishFormatter.currencyCode = code
        return englishFormatter.currencySymbol
    }
}
