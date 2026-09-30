#if DEBUG
extension Scenario {
    /// Project ids (app-architecture §1.7).
    static let projects: [ScreenID: Scenario] = [
        .projectDrone: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-drone")]),
        .projectOverBudget: Scenario(seeds: demo + ["devBuysGps"], tab: .groups, stack: [.project("pj-drone")]),
        .projectAddComponent: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-drone")]),
        .projectSettings: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-drone"), .projectSettings("pj-drone")]),
        .projectCloseAlert: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-drone"), .projectSettings("pj-drone")]),
        .projectClosed: Scenario(seeds: demo + ["closeDrone"], tab: .groups, stack: [.project("pj-drone")]),
        .projectArchived: Scenario(seeds: demo, tab: .groups, stack: [.project("pj-hackathon")]),
    ]
}
#endif
