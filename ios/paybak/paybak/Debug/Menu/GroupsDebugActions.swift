#if DEBUG
/// Lane B's Groups section of the debug menu (app-architecture §3.10).
enum GroupsDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Simulate a QR scan of paybak.app/i/meera", detail: "Opens the friend the code names") { context in
                guard case .friend(let id) = context.ledgerStore.openInviteLink("https://paybak.app/i/meera") else { return }
                context.router.select(.groups)
                context.router.groupsSegment = .friends
                context.router.open(.friend(id))
            },
        ]
    }
}
#endif
