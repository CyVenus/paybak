#if DEBUG
/// Lane B's Projects section of the debug menu (app-architecture §3.10, screens-projects §11): the
/// other members' side of Build a Drone, so Closed can walk to Archived on one device. Each action
/// does nothing when the demo isn't in that state.
enum ProjectsDebugActions {
    private static let drone: GroupID = "pj-drone"
    private static let gps: ComponentID = "c-drone-gps"
    private static let dev: PersonID = "p-dev"

    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Dev buys the GPS module", detail: "₹9,500: Build a Drone goes over budget") { context in
                guard context.ledgerStore.ledger.component(gps)?.status == .planned else { return }
                try context.ledgerStore.applyScenario("devBuysGps")
            },
            DebugAction(title: "Close Build a Drone") { context in
                guard context.ledgerStore.ledger.group(drone)?.project?.status == .active else { return }
                try context.ledgerStore.applyScenario("closeDrone")
            },
            payDev("p-rohan", name: "Rohan", demoAmount: "₹8,500"),
            payDev("p-priya", name: "Priya", demoAmount: "₹4,000"),
            DebugAction(title: "Confirm pending project payments", detail: "As each receiver would") { context in
                let store = context.ledgerStore
                let ledger = store.ledger
                let pending = ledger.payments.filter { payment in
                    payment.status == .pending && (payment.groupId.flatMap(ledger.group)?.isProject ?? false)
                }
                for payment in pending {
                    try store.confirmPayment(payment.id)
                }
                var seen: Set<GroupID> = []
                for groupId in pending.compactMap(\.groupId) {
                    guard seen.insert(groupId).inserted else { continue }
                    store.archiveIfSettled(groupId)
                }
            },
        ]
    }

    /// `name` records paying Dev what Build a Drone's plan says they owe him, pending until Dev
    /// confirms; once is enough.
    private static func payDev(_ personId: PersonID, name: String, demoAmount: String) -> DebugAction {
        DebugAction(title: "\(name) pays Dev \(demoAmount)", detail: "In Build a Drone, for Dev to confirm") { context in
            let store = context.ledgerStore
            let waiting = store.ledger.payments.contains { $0.groupId == drone && $0.fromId == personId && $0.status == .pending }
            guard !waiting,
                  let transfer = store.books.groupPlan(drone).first(where: { $0.from == personId && $0.to == dev }) else { return }
            try store.recordPayment(PaymentDraft(fromId: personId, toId: dev, amount: transfer.amount,
                                                 currency: store.ledger.group(drone)?.currency ?? "INR",
                                                 date: store.clock.today, groupId: drone, recordedBy: personId))
        }
    }
}
#endif
