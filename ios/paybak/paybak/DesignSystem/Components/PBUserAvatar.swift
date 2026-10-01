import SwiftUI

/// The current user's avatar in any size and surface (screens-profile §1.7): their preset head, photo,
/// custom character (head crop) or initials. Every circle that shows "You" uses this.
struct PBUserAvatar: View {
    var diameter: CGFloat = PBSize.avatarMd
    /// White circle inside #F5F5F5 cards.
    var isOnCard = false

    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        if case .character(let look) = profileStore.profile.avatar {
            AvatarCharacterView(look: look, crop: .head)
                .frame(width: diameter, height: diameter)
                .background(isOnCard ? PBColor.bgPrimary : PBColor.bgCard, in: .circle)
                .clipShape(.circle)
                .accessibilityHidden(true)
        } else {
            PBAvatar(profileStore.avatarContent, diameter: diameter, isOnCard: isOnCard)
        }
    }
}

extension Person {
    /// A friend's circle content: their peep head, else their initials.
    var avatarContent: PBAvatar.Content {
        avatar.flatMap(PBPeepHead.init(rawValue:)).map { .art($0) } ?? .initials(initials)
    }
}

extension ExpenseCategory {
    var pbIcon: PBIcon { PBIcon(key: icon) }
}

extension LedgerGroup {
    /// The group's stored icon (`plane`, `home`, `drone` …); the tag for an unknown key.
    var pbIcon: PBIcon { PBIcon(key: icon) }
}

#Preview("PBUserAvatar") {
    let store = ProfileStore(defaults: UserDefaults(suiteName: "preview")!)
    store.replace(with: .sample)
    return HStack(spacing: PBSpace.s12) {
        PBUserAvatar(diameter: 24)
        PBUserAvatar()
        PBUserAvatar(diameter: 56, isOnCard: true)
    }
    .padding()
    .background(PBColor.bgCard)
    .environment(store)
}
