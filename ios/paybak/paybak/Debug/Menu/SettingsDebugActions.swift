#if DEBUG
/// Lane C's Settings section of the debug menu (app-architecture §3.10). Add items here.
enum SettingsDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        []
    }
}
#endif
