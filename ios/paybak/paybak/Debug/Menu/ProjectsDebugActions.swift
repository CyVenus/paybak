#if DEBUG
/// Lane B's Projects section of the debug menu (app-architecture §3.10, screens-projects §11): walks
/// Build a Drone through overspending, closing and the other members' payments to its archive,
/// without a second device.
enum ProjectsDebugActions {
    private static let drone: GroupID = "pj-drone"

    static func actions(_ ledger: Ledger) -> [DebugAction] {
        guard ledger.group(drone) != nil else { return [] }
        return [
            DebugAction(title: "Dev buys the GPS module", detail: "₹9,500, so Build a Drone goes over budget") { context in
                try context.ledgerStore.updateComponent("c-drone-gps", status: .bought, actualCost: 9_500_00, paidBy: "p-dev")
            },
            DebugAction(title: "Close Build a Drone", detail: "Locks it and freezes the final plan") { context in
                try context.ledgerStore.closeProject(drone)
            },
            DebugAction(title: "Members record their Build a Drone payments", detail: "Each transfer between the others, pending") { context in
                let store = context.ledgerStore
                for transfer in store.books.groupPlan(drone) where transfer.from != Person.me && transfer.to != Person.me {
                    try store.recordPayment(PaymentDraft(fromId: transfer.from, toId: transfer.to, amount: transfer.amount, currency: "INR",
                                                         date: store.clock.today, groupId: drone, recordedBy: transfer.from))
                }
            },
            DebugAction(title: "Confirm pending Build a Drone payments", detail: "A closed project then archives") { context in
                let store = context.ledgerStore
                for payment in store.ledger.payments where payment.groupId == drone && payment.status == .pending {
                    try store.confirmPayment(payment.id)
                }
                store.archiveIfSettled(drone)
            },
        ]
    }
}
#endif
