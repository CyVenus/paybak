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

    /// This screen's copy: "Indian rupee", "US dollar" (the last word of a multi-word name in lower case).
    static func sentenceCase(_ name: String) -> String {
        var words = name.split(separator: " ").map(String.init)
        guard words.count > 1, let last = words.popLast() else { return name }
        return (words + [last.lowercased()]).joined(separator: " ")
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
