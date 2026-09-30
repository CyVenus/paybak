import SwiftUI
import UIKit

/// Privacy & data (screens-settings §8, §10): discovery switches, Export records (Pro), Recently
/// deleted and Delete account, which is blocked while any balance is open ("Settle up first") and
/// otherwise asks before wiping the device's profile and ledger.
struct PrivacyScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var isBlockedAlertPresented = false
    @State private var isDeleteAlertPresented = false
    @State private var isContactsDeniedPresented = false

    private var discovery: LedgerSettings.Discovery { ledgerStore.ledger.settings.discovery }
    private var totals: HomeTotals { ledgerStore.snapshot.home.totals }

    var body: some View {
        SettingsScaffold(title: "Privacy", testIDPrefix: "privacyData") {
            SettingsSection(title: "Discovery", footer: "Contacts are only used to find friends already on Paybak.") {
                VStack(spacing: 0) {
                    PBSettingRow("Find me by phone or email", icon: .search, trailing: .toggle(findMe))
                        .accessibilityIdentifier("privacyData.findMe")
                    PBSettingRow("Contacts sync", icon: .people, trailing: .toggle(contactsSync), showsDivider: false)
                        .accessibilityIdentifier("privacyData.contactsSync")
                }
                .pbCard(padding: 0)
            }
            SettingsSection(title: "Your data", footer: "Deleted items can be restored for 30 days.") {
                VStack(spacing: 0) {
                    PBSettingRow("Export records", icon: .download, badge: ledgerStore.isPro ? nil : "Pro") {
                        router.requirePro(.privacyExport)
                    }
                    .accessibilityIdentifier("privacyData.export")
                    PBSettingRow("Recently deleted", value: deletedValue, icon: .restore, showsDivider: false) {
                        router.open(.recentlyDeleted)
                    }
                    .accessibilityIdentifier("privacyData.recentlyDeleted")
                }
                .pbCard(padding: 0)
            }
            SettingsSection(footer: "Your past records stay in friends’ groups, shown as a former member.") {
                PBSettingRow("Delete account", icon: .delete, trailing: .none, tone: .destructive, showsDivider: false) {
                    if totals.owe != 0 || totals.owed != 0 {
                        isBlockedAlertPresented = true
                    } else {
                        isDeleteAlertPresented = true
                    }
                }
                .pbCard(padding: 0)
                .accessibilityIdentifier("privacyData.deleteAccount")
            }
        }
        .pbAlert(
            isPresented: $isBlockedAlertPresented,
            title: "Settle up first",
            message: Self.blockedMessage(owe: totals.owe, owed: totals.owed, currency: profileStore.profile.defaultCurrency),
            cancelLabel: "Not now",
            actionLabel: "Settle up",
            role: .primary,
            testIDPrefix: "privacyDeleteBlocked"
        ) { router.open(.settleUp(groupId: nil)) }
        .pbAlert(
            isPresented: $isDeleteAlertPresented,
            title: "Delete account?",
            message: "This removes your profile and settings from this device. Your past records stay in friends’ groups, shown as a former member.",
            cancelLabel: "Cancel",
            actionLabel: "Delete",
            testIDPrefix: "privacyData.deleteAlert",
            onAction: deleteAccount
        )
        .alert("Contacts access is off", isPresented: $isContactsDeniedPresented) {
            Button("Not now", role: .cancel) {}
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
        } message: {
            Text("Turn on Contacts for Paybak in Settings to find friends already on Paybak.")
        }
        .onStartScreen([.privacyDeleteBlocked]) { _ in isBlockedAlertPresented = true }
    }

    /// "1 item", "{n} items"; hidden when nothing can be restored.
    private var deletedValue: String? {
        let count = ledgerStore.snapshot.recentlyDeleted.count
        return count == 0 ? nil : "\(count) item\(count == 1 ? "" : "s")"
    }

    private var findMe: Binding<Bool> {
        Binding { discovery.findMeByContact } set: { isOn in
            ledgerStore.updateSettings { $0.discovery.findMeByContact = isOn }
        }
    }

    /// Turning sync on asks for contacts access; refused, it flips back off and says how to allow it.
    private var contactsSync: Binding<Bool> {
        Binding { discovery.contactsSync } set: { isOn in
            ledgerStore.updateSettings { $0.discovery.contactsSync = isOn }
            guard isOn else { return }
            Task {
                guard await !ContactsDirectory.requestAccess() else { return }
                ledgerStore.updateSettings { $0.discovery.contactsSync = false }
                isContactsDeniedPresented = true
            }
        }
    }

    /// Removes the profile and the ledger from the device and starts over at Welcome.
    private func deleteAccount() {
        profileStore.reset()
        ledgerStore.reset()
        router.restartOnboarding()
    }

    /// §10: the Home totals without signs, in the default currency.
    static func blockedMessage(owe: Int64, owed: Int64, currency: String) -> String {
        let tail = "Settle every balance before deleting your account."
        switch (owe != 0, owed != 0) {
        case (true, true): return "You still owe \(Money.format(owe, currency)) and are owed \(Money.format(owed, currency)). \(tail)"
        case (true, false): return "You still owe \(Money.format(owe, currency)). \(tail)"
        default: return "You’re still owed \(Money.format(owed, currency)). \(tail)"
        }
    }
}

#Preview("PrivacyScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-privacy")!)
    PrivacyScreen()
        .environment(AppRouter())
        .environment(profileStore)
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-privacy.json")), profileStore: profileStore))
}
