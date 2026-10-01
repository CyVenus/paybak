#if DEBUG
/// Lane B's Groups & Friends section of the debug menu (app-architecture §3.10).
enum GroupsDebugActions {
    private static let ananya: PersonID = "p-ananya"

    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Simulate a QR scan of paybak.app/i/meera", detail: "Adds Meera if needed, opens her") { context in
                guard case .friend(let id) = context.ledgerStore.openInviteLink("https://paybak.app/i/meera") else { return }
                context.router.open(.friend(id))
            },
            DebugAction(title: "Ananya joins Paybak", detail: "The guest loses the Guest tag") { context in
                context.ledgerStore.mutate { books in
                    guard let index = books.ledger.people.firstIndex(where: { $0.id == ananya && $0.isGuest }) else { return }
                    books.ledger.people[index].isGuest = false
                    if books.ledger.people[index].username == nil {
                        books.ledger.people[index].username = books.ledger.people[index].firstName.lowercased()
                    }
                }
            },
        ]
    }
}
#endif
