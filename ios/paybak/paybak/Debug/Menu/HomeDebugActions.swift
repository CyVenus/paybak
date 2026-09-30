#if DEBUG
/// Lane D's Home section of the debug menu (app-architecture §3.10): Home's state comes from the data,
/// so each item loads the demo moment that draws it, at Figma parity.
enum HomeDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            show("First day", detail: "An empty account", seeds: Scenario.empty),
            show("Active", detail: "The demo without Esha’s claim", seeds: Scenario.base),
            show("Confirm payment", detail: "The demo with Esha’s ₹700 claim pending", seeds: Scenario.demo),
            show("All settled", detail: "Every balance paid off", seeds: ["allSettled"]),
        ]
    }

    private static func show(_ state: String, detail: String, seeds: [String]) -> DebugAction {
        DebugAction(title: "Show Home \(state)", detail: detail) { context in
            try context.ledgerStore.loadDemo(scenarios: seeds)
            context.router.select(.home)
        }
    }
}
#endif
