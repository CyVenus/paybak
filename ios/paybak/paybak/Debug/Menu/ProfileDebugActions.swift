#if DEBUG
/// Lane C's Profile section of the debug menu (app-architecture §3.10). Add items here.
enum ProfileDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        []
    }
}
#endif
