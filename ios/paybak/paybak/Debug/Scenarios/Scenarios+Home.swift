#if DEBUG
extension Scenario {
    /// Home ids (app-architecture §1.2).
    static let home: [ScreenID: Scenario] = [
        .homeFirstDay: Scenario(seeds: empty),
        .homeActive: Scenario(seeds: base),
        .homeAllSettled: Scenario(seeds: ["allSettled"]),
        .homeConfirmPayment: Scenario(seeds: demo),
        .settlePaymentConfirmed: Scenario(seeds: ["eshaPaymentConfirmed"], toast: "Payment confirmed"),
        .homeAddSheet: Scenario(seeds: base, sheet: .addSheet),
        .debugMenu: Scenario(seeds: demo, sheet: .debugMenu),
    ]
}
#endif
