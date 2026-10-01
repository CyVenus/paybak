#if DEBUG
/// Lane C's Settings section of the debug menu (app-architecture §3.10).
enum SettingsDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        let rohanMuted = ledger.person("p-rohan")?.remindersMuted ?? false
        return [
            DebugAction(title: rohanMuted ? "Unmute Rohan" : "Mute Rohan", detail: "Settings › Muted friends lists him") { context in
                try context.ledgerStore.setRemindersMuted("p-rohan", !rohanMuted)
            },
            DebugAction(title: "Subscribe monthly", detail: "Pro without a trial (₹99/month)") { context in
                context.ledgerStore.subscribe(.monthly)
            },
            DebugAction(title: "Remove every payment method", detail: "Payment details’ empty preview") { context in
                context.profileStore.update { profile in
                    profile.paymentMethods = []
                    profile.mirrorPrimaryUPI()
                }
            },
        ]
    }
}
#endif
