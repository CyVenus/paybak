import SwiftUI

/// Muted friends (screens-settings §7 proposal): everyone whose automatic reminders were turned off on
/// their friend page, each with a switch (On = muted). Switching one off unmutes them; the row stays
/// until the screen closes, so a mistaken tap can be undone. Manual Remind always works.
struct MutedFriendsScreen: View {
    @Environment(LedgerStore.self) private var ledgerStore

    /// The friends muted when the screen opened, in ledger order.
    @State private var shownIDs: [PersonID]?

    var body: some View {
        let people = ledgerStore.ledger.people
        let ids = shownIDs ?? people.filter(\.remindersMuted).map(\.id)
        let rows = ids.compactMap { id in people.first { $0.id == id } }
        SettingsScaffold(title: "Muted friends", testIDPrefix: "mutedFriends") {
            if rows.isEmpty {
                SettingsFooter("No muted friends. Mute a friend from their page.")
            } else {
                SettingsSection(footer: "Turn off to send automatic reminders again.") {
                    VStack(spacing: 0) {
                        ForEach(rows) { person in
                            MutedFriendRow(person: person, showsDivider: person.id != rows.last?.id) { isMuted in
                                try? ledgerStore.setRemindersMuted(person.id, isMuted)
                            }
                            .accessibilityIdentifier("mutedFriends.row.\(person.id)")
                        }
                    }
                    .pbCard(padding: 0)
                }
            }
        }
        .onAppear {
            if shownIDs == nil {
                shownIDs = ids
            }
        }
    }
}

/// A friend's 32 pt avatar and name with the switch; tapping anywhere on the row flips it.
private struct MutedFriendRow: View {
    let person: Person
    let showsDivider: Bool
    let onChange: (Bool) -> Void

    var body: some View {
        let isMuted = Binding { person.remindersMuted } set: { onChange($0) }
        HStack(spacing: PBSpace.s12) {
            PBAvatar(person.avatarContent, diameter: PBSize.avatarSm, isOnCard: true)
            Text(person.name)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            // The row flips it, so a tap on the switch itself doesn't flip it twice.
            Toggle(person.name, isOn: isMuted)
                .labelsHidden()
                .tint(PBColor.bgInverse)
                .allowsHitTesting(false)
        }
        .padding(.vertical, PBSpace.s12)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: 56)
        .contentShape(.rect)
        .onTapGesture { isMuted.wrappedValue.toggle() }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider().padding(.leading, 60)
            }
        }
        .accessibilityRepresentation { Toggle(person.name, isOn: isMuted) }
    }
}

#Preview("MutedFriendsScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-muted")!)
    MutedFriendsScreen()
        .environment(AppRouter())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-muted.json")), profileStore: profileStore))
}
