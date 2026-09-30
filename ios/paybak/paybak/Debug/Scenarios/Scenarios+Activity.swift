#if DEBUG
extension Scenario {
    /// Activity, expense detail and notification ids (app-architecture §1.6).
    static let activity: [ScreenID: Scenario] = [
        .activityTimeline: Scenario(seeds: demo, tab: .activity, activitySegment: .timeline),
        .activityEmpty: Scenario(seeds: empty, tab: .activity, activitySegment: .timeline),
        .expenseVilla: Scenario(seeds: demo, tab: .activity, stack: [.expense("e-goa-villa")]),
        .expenseComment: Scenario(seeds: demo, tab: .activity, stack: [.expense("e-goa-villa")]),
        .expenseDelete: Scenario(seeds: demo, tab: .activity, stack: [.expense("e-goa-villa")]),
        .expenseDisputed: Scenario(seeds: demo + ["eshaFlagsSeafood"], tab: .activity, stack: [.expense("e-goa-seafood")]),
        .recentlyDeleted: Scenario(seeds: demo, tab: .activity, stack: [.recentlyDeleted]),
        .notifications: Scenario(seeds: demo, stack: [.notifications]),
        .activityLog: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-drone"), .activityLog(.project("pj-drone"))]),
        .lockConfirmRequest: Scenario(seeds: demo),
        .lockReminder: Scenario(seeds: demo),
    ]
}
#endif
