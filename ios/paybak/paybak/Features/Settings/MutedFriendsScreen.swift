import SwiftUI

/// Muted friends (screens-settings §7 proposal): everyone whose automatic reminders were turned off on
/// their friend page. Unmute brings their reminders back; manual Remind always works.
struct MutedFriendsScreen: View {
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let muted = ledgerStore.ledger.people.filter(\.remindersMuted)
        SettingsScaffold(title: "Muted friends", testIDPrefix: "mutedFriends") {
            if muted.isEmpty {
                SettingsFooter("No muted friends. Mute a friend from their page.")
            } else {
                SettingsSection(footer: "Muted friends don’t get automatic reminders.") {
                    VStack(spacing: 0) {
                        ForEach(muted) { person in
                            PBPersonRow(
                                name: person.name,
                                avatar: person.avatarContent,
                                size: .compact,
                                trailing: .button("Unmute") { try? ledgerStore.setRemindersMuted(person.id, false) },
                                showsDivider: person.id != muted.last?.id
                            )
                            .accessibilityIdentifier("mutedFriends.row.\(person.id)")
                        }
                    }
                    .pbCard(padding: 0)
                }
            }
        }
        .animation(.easeOut(duration: 0.2), value: muted.map(\.id))
    }
}

#Preview("MutedFriendsScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-muted")!)
    MutedFriendsScreen()
        .environment(AppRouter())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-muted.json")), profileStore: profileStore))
}
