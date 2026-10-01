import SwiftUI

/// The currency sheet (add-expense §9): Recent (the default currency, then the ones on your recent
/// records, then the current pick) and All currencies (the majors first, then every other ISO
/// currency by name); search shows one flat list by name or code. A tap answers `.currency(code)`
/// and closes.
struct CurrencyPickerSheet: View {
    let request: CurrencyPickRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @State private var query = ""

    /// Figma's "All currencies" list, by name.
    private static let majors = ["AUD", "GBP", "CAD", "EUR", "JPY", "SGD", "USD"]

    var body: some View {
        PBSheet(title: request.title, search: $query, searchPrompt: "Search currencies", testIDPrefix: "currency", onClose: router.dismissSheet) {
            ScrollView {
                if query.trimmingCharacters(in: .whitespaces).isEmpty {
                    VStack(alignment: .leading, spacing: PBSpace.s24 - PBSpace.s4) {
                        section("Recent", codes: recent)
                        section("All currencies", codes: everything.filter { !recent.contains($0) })
                    }
                } else {
                    let results = Currency.search(query, in: everything.map { Self.currency($0) })
                    if results.isEmpty {
                        Text("No currencies match “\(query)”")
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: .infinity)
                            .padding(.top, PBSpace.s16)
                    } else {
                        rows(results.map(\.code))
                    }
                }
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.immediately)
        }
        .routeTestRoot("pickCurrency")
    }

    /// The default currency, the recent records' currencies, then the one picked now.
    private var recent: [String] {
        let codes = store.recentCurrencyCodes
        guard let selected = request.selected, !codes.contains(selected) else { return codes }
        return codes + [selected]
    }

    private var everything: [String] { Self.allCodes }

    /// The majors, then every other common ISO currency by name.
    private static let allCodes: [String] = {
        let others = Locale.commonISOCurrencyCodes
            .filter { !majors.contains($0) }
            .map { (code: $0, name: currency($0).name) }
            .sorted { $0.name.localizedStandardCompare($1.name) == .orderedAscending }
            .map(\.code)
        return majors + others
    }()

    private func section(_ title: String, codes: [String]) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader(title)
            rows(codes)
        }
    }

    private func rows(_ codes: [String]) -> some View {
        LazyVStack(spacing: PBSpace.s4) {
            ForEach(codes, id: \.self) { code in
                let currency = Self.currency(code)
                PBCurrencyRow(symbol: currency.tileText, title: currency.name, subtitle: code, isSelected: code == request.selected) {
                    router.complete(request.id, with: .currency(code))
                }
                .accessibilityIdentifier("currency.row.\(code)")
            }
        }
    }

    /// This sheet's sentence-case names: the designed ones as Figma writes them ("Indian rupee", "UAE
    /// dirham"), the platform's for the rest with the unit word in lower case ("Swiss franc").
    private static func currency(_ code: String) -> Currency {
        if let designed = Money.currencies[code] {
            return Currency(code: code, name: designed.name, symbol: designed.symbol)
        }
        let base = Currency(code: code, locale: Locale(identifier: "en_US"))
        var words = base.name.split(separator: " ").map(String.init)
        guard words.count > 1 else { return base }
        words[words.count - 1] = words[words.count - 1].lowercased()
        return Currency(code: code, name: words.joined(separator: " "), symbol: base.symbol)
    }
}

private extension Currency {
    init(code: String, name: String, symbol: String) {
        self.code = code
        self.name = name
        self.symbol = symbol
    }
}
