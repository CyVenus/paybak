#if DEBUG
/// Lane C's Settings section of the debug menu (app-architecture §3.10): data for Muted friends.
enum SettingsDebugActions {
    private static let rohan: PersonID = "p-rohan"

    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Mute Rohan’s reminders", detail: "Fills Settings › Muted friends") { context in
                guard context.ledgerStore.ledger.person(rohan) != nil else { return }
                try context.ledgerStore.setRemindersMuted(rohan, true)
            },
        ]
    }
}
#endif
