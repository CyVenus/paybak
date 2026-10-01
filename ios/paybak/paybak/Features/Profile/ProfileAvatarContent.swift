import SwiftUI

extension ProfileStore {
    /// What the user's avatar shows: their preset art, their photo, their custom character (the Head
    /// crop, drawn once into a bitmap) or their initials (the fallback everywhere an avatar shows).
    /// Before a name exists it's the profile icon.
    var avatarContent: PBAvatar.Content {
        switch profile.avatar {
        case .preset(let index) where PBPeepHead.presets.indices.contains(index):
            return .art(PBPeepHead.presets[index])
        case .photo:
            if let photo {
                return .photo(Image(uiImage: photo))
            }
        case .character(let look):
            if let image = AvatarBitmap.head(look) {
                return .photo(Image(uiImage: image))
            }
        default:
            break
        }
        return profile.name.isEmpty ? .icon(.profile) : .initials(profile.initials)
    }
}
