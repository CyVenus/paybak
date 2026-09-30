import SwiftUI

extension ProfileStore {
    /// What the user's avatar shows: their preset art, their photo, or their initials (the
    /// fallback everywhere an avatar shows). Before a name exists it's the profile icon.
    var avatarContent: PBAvatar.Content {
        switch profile.avatar {
        case .preset(let index) where PBPeepHead.presets.indices.contains(index):
            return .art(PBPeepHead.presets[index])
        case .photo:
            if let photo {
                return .photo(Image(uiImage: photo))
            }
        default:
            break
        }
        return profile.name.isEmpty ? .icon(.profile) : .initials(profile.initials)
    }
}
