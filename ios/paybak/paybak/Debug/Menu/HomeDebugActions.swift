#if DEBUG
/// Lane D's Home section of the debug menu (app-architecture §3.10): switch Home between its designed
/// states by loading the demo at the Figma date (flow.md's "switch Home state"), and stack a second
/// claim to check the several-claims layout (home-v2 §3.11).
enum HomeDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            homeState("Home: First day", detail: "An empty account", seeds: Scenario.empty),
            homeState("Home: Active", detail: "The demo without Esha’s claim", seeds: Scenario.base),
            homeState("Home: Confirm payment", detail: "The demo with Esha’s claim", seeds: Scenario.demo),
            homeState("Home: All settled", detail: "Everyone has paid", seeds: ["allSettled"]),
            CoreDebugActions.claim(from: "p-dev", title: "Dev says he paid ₹700", detail: "Another pending claim, for Dinner at Olive Garden"),
        ]
    }

    private static func homeState(_ title: String, detail: String, seeds: [String]) -> DebugAction {
        DebugAction(title: title, detail: detail) { context in
            try context.ledgerStore.loadDemo(scenarios: seeds)
            context.router.resetMain()
        }
    }
}
#endif
