#if DEBUG
extension Scenario {
    /// Settle up ids (app-architecture §1.5).
    static let settle: [ScreenID: Scenario] = [
        .settleOwedBreakdown: Scenario(seeds: demo, stack: [.owedBreakdown]),
        .settleOweBreakdown: Scenario(seeds: demo, stack: [.oweBreakdown]),
        .settleUp: Scenario(seeds: demo, stack: [.settleUp(groupId: nil)]),
        .settleRemind: Scenario(seeds: base, sheet: .remind(personId: "p-rohan", context: .expense("e-movie"))),
        .settleRemindShare: Scenario(seeds: base, sheet: .remind(personId: "p-rohan", context: .expense("e-movie"))),
        .settleNotReceived: Scenario(seeds: demo, sheet: .notReceived("pay-esha-olive")),
    ]
}
#endif
