#if DEBUG
extension Scenario {
    /// Settings and Pro ids (app-architecture §1.8).
    static let settings: [ScreenID: Scenario] = [
        .paywall: Scenario(seeds: demo, tab: .profile, modals: [Layer(root: .paywall(continueTo: nil))]),
        .proWelcome: Scenario(seeds: pro(), tab: .profile, stack: [.privacyData],
                              modals: [Layer(root: .paywall(continueTo: .privacyExport))]),
        .paymentDetails: Scenario(seeds: demo, tab: .profile, stack: [.paymentDetails]),
        .paymentAddUpi: Scenario(seeds: demo, tab: .profile, stack: [.paymentDetails]),
        .paymentAddUpiError: Scenario(seeds: demo, tab: .profile, stack: [.paymentDetails]),
        .settingsCurrency: Scenario(seeds: demo, tab: .profile, stack: [.settingsCurrency]),
        .settingsNotifications: Scenario(seeds: demo, tab: .profile, stack: [.settingsNotifications]),
        .mutedFriends: Scenario(seeds: demo, tab: .profile, stack: [.settingsNotifications, .mutedFriends]),
        .privacyData: Scenario(seeds: demo, tab: .profile, stack: [.privacyData]),
        .privacyExport: Scenario(seeds: pro(), tab: .profile, stack: [.privacyData, .privacyExport]),
        .privacyDeleteBlocked: Scenario(seeds: demo, tab: .profile, stack: [.privacyData]),
        .helpFeedback: Scenario(seeds: demo, tab: .profile, stack: [.helpFeedback]),
        .helpAnswer: Scenario(seeds: demo, tab: .profile, stack: [.helpFeedback, .helpAnswer(index: 1)]),
    ]
}
#endif
