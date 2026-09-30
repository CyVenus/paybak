#if DEBUG
import Foundation

/// One debug menu item: a title and what it does with the app's stores and router.
struct DebugAction: Identifiable {
    var id: String { title }
    let title: String
    var detail: String?
    let perform: (DebugContext) throws -> Void
}

/// A titled group of debug actions (one per module).
struct DebugSection: Identifiable {
    var id: String { title }
    let title: String
    let actions: [DebugAction]
}

/// What a debug action can act on.
struct DebugContext {
    let profileStore: ProfileStore
    let ledgerStore: LedgerStore
    let router: AppRouter
}
#endif
