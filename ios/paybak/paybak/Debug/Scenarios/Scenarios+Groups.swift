#if DEBUG
extension Scenario {
    /// Groups & Friends ids (app-architecture §1.4).
    static let groups: [ScreenID: Scenario] = [
        .groupsList: Scenario(seeds: demo, tab: .groups, groupsSegment: .groups),
        .friendsList: Scenario(seeds: demo, tab: .groups, groupsSegment: .friends),
        .groupsEmpty: Scenario(seeds: empty, tab: .groups, groupsSegment: .groups),
        .friendsEmpty: Scenario(seeds: empty, tab: .groups, groupsSegment: .friends),
        .groupGoaTrip: Scenario(seeds: demo, tab: .groups, stack: [.group("g-goa")]),
        .groupDubaiWeekend: Scenario(seeds: demo, tab: .groups, stack: [.group("g-dubai")]),
        .groupSettings: Scenario(seeds: demo, tab: .groups, stack: [.group("g-goa"), .groupSettings("g-goa")]),
        .groupLeaveBlocked: Scenario(seeds: demo, tab: .groups, stack: [.group("g-goa"), .groupSettings("g-goa")]),
        .friendRohan: Scenario(seeds: demo, tab: .groups, stack: [.friend("p-rohan")], groupsSegment: .friends),
        .friendAnanyaGuest: Scenario(seeds: demo, tab: .groups, stack: [.friend("p-ananya")], groupsSegment: .friends),
        .addFriend: Scenario(seeds: demo, tab: .groups, stack: [.addFriend], groupsSegment: .friends),
        .myQrCode: Scenario(seeds: demo, tab: .groups, stack: [.addFriend], groupsSegment: .friends),
    ]
}
#endif
