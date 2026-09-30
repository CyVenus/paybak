#if DEBUG
extension Scenario {
    /// Profile and avatar editor ids (app-architecture §1.8): they also seed the default Boy character.
    static let profile: [ScreenID: Scenario] = {
        var table: [ScreenID: Scenario] = [
            .profile: Scenario(seeds: demo, tab: .profile, avatar: .defaultBoy),
            .profileSignOut: Scenario(seeds: demo, tab: .profile, avatar: .defaultBoy),
        ]
        for id in [ScreenID.editAvatarBoyHair, .editAvatarBoyBeard, .editAvatarBoyEyewear, .editAvatarBoyOutfit,
                   .editAvatarGirlHair, .editAvatarGirlAccessory, .editAvatarGirlOutfit, .editAvatarDiscard] {
            table[id] = Scenario(seeds: demo, tab: .profile, stack: [.editAvatar], avatar: .defaultBoy)
        }
        return table
    }()
}
#endif
