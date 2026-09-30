#if DEBUG
/// Lane C's Insights section of the debug menu (app-architecture §3.10). Add items here.
enum InsightsDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        []
    }
}
#endif
