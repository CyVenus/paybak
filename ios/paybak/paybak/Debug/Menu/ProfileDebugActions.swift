#if DEBUG
/// Lane C's Profile section of the debug menu (app-architecture §3.10): switch the user's avatar kind.
enum ProfileDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Use the preset avatar", detail: "Arjun’s Setup 1 head, as the demo") { context in
                context.profileStore.update { $0.avatar = .preset(0) }
            },
            DebugAction(title: "Use the custom avatar", detail: "The default Boy character") { context in
                context.profileStore.update { $0.avatar = .character(.defaultBoy) }
            },
        ]
    }
}
#endif
