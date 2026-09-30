#if DEBUG
/// Lane B's Settle section of the debug menu (app-architecture §3.10). Add items here.
enum SettleDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        []
    }
}
#endif
