#if DEBUG
/// Lane C's Profile section of the debug menu (app-architecture §3.10).
enum ProfileDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Use the custom avatar", detail: "The default Boy look, as on Profile") { context in
                context.profileStore.update { $0.avatar = .character(.defaultBoy) }
            },
            DebugAction(title: "Use the Setup avatar", detail: "Arjun’s preset head, as on every other page") { context in
                context.profileStore.update { $0.avatar = .preset(0) }
            },
        ]
    }
}
#endif
