import SwiftUI

/// Currency (screens-settings §6): the default currency for totals and new groups (the card opens the
/// currency picker), Keep balances per currency, and how exchange rates work. Changing the default
/// only re-converts totals; stored expenses keep their currency and saved rate.
struct CurrencySettingsScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var pickRequest = CurrencyPickRequest(title: "Default currency")

    var body: some View {
        let currency = Currency(code: profileStore.profile.defaultCurrency)
        SettingsScaffold(title: "Currency", testIDPrefix: "settingsCurrency") {
            SettingsSection(title: "Default currency", footer: "Home totals and new groups use this currency.") {
                PBCurrencyRow(symbol: currency.tileText, title: currency.code, subtitle: Self.sentenceCase(currency.name),
                              isSelected: true, isOnCard: true, action: pickCurrency)
                    .padding(.horizontal, PBSpace.s16)
                    .pbCard(padding: 0)
                    .accessibilityIdentifier("settingsCurrency.default")
            }
            SettingsSection(
                title: "Balances",
                footer: "Show what you owe in each currency instead of converting it.\ne.g. You owe Kabir AED 60 and ₹1,400"
            ) {
                PBSettingRow("Keep balances per currency", trailing: .toggle(keepsBalancesPerCurrency), showsDivider: false)
                    .pbCard(padding: 0)
                    .accessibilityIdentifier("settingsCurrency.perCurrency")
            }
            SettingsSection(title: "Exchange rates") {
                SettingsInfoLine(icon: .exchange, text: "Rates are saved when an expense is added. Balances don’t change when rates move.")
            }
        }
        .onRouteResult(pickRequest.id) { result in
            guard case .currency(let code) = result else { return }
            profileStore.update { $0.currencyCode = code }
            ledgerStore.refresh()
        }
    }

    private var keepsBalancesPerCurrency: Binding<Bool> {
        Binding {
            ledgerStore.ledger.settings.keepBalancesPerCurrency
        } set: { isOn in
            ledgerStore.updateSettings { $0.keepBalancesPerCurrency = isOn }
        }
    }

    private func pickCurrency() {
        pickRequest.selected = profileStore.profile.defaultCurrency
        router.open(.pickCurrency(pickRequest))
    }

    /// This screen's own casing (screens-settings §6): "Indian Rupee" → "Indian rupee", "US Dollar" →
    /// "US dollar". Every word after the first is lower-cased; acronyms stay upper case.
    static func sentenceCase(_ name: String) -> String {
        name.split(separator: " ", omittingEmptySubsequences: false)
            .enumerated()
            .map { index, word in
                let isAcronym = word.allSatisfy { !$0.isLetter || $0.isUppercase }
                return index == 0 || isAcronym ? String(word) : word.lowercased()
            }
            .joined(separator: " ")
    }
}

#Preview("CurrencySettingsScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-currency")!)
    profileStore.replace(with: .sample)
    return CurrencySettingsScreen()
        .environment(AppRouter())
        .environment(profileStore)
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-currency.json")), profileStore: profileStore))
}
