#if DEBUG
import Foundation

/// One debug menu row: a title, an optional subtitle and what it does with the app's stores and
/// router. It runs after the menu closes.
struct DebugAction: Identifiable {
    var id: String { title }
    let title: String
    var detail: String?
    let perform: (DebugContext) throws -> Void
}

/// A titled group of debug rows (each module has its own file of actions).
struct DebugSection: Identifiable {
    var id: String { title }
    let title: String
    let actions: [DebugAction]
}

/// What a debug action can reach.
struct DebugContext {
    let profileStore: ProfileStore
    let ledgerStore: LedgerStore
    let router: AppRouter
}
#endif
