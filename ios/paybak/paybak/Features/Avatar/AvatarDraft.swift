import Foundation

/// The avatar editor's state (screens-profile §3.2, §3.5): the look being edited, the category on
/// show, and what the editor opened with, so Back knows whether there's anything to discard. Nothing
/// is saved until the screen commits `look`.
struct AvatarDraft: Equatable {
    /// What the editor opened with.
    let original: AvatarLook
    var look: AvatarLook
    /// The selected category chip ("hair", "beard" …).
    var category = "hair"

    /// Opens on the saved character (unknown picks back to their defaults), or on the defaults with a
    /// gender guessed from the Setup 1 preset (Priya and Esha's heads → Girl, everything else → Boy;
    /// §1.7 proposal). Always starts at Hair.
    init(avatar: UserProfile.Avatar?) {
        var look: AvatarLook
        switch avatar {
        case .character(let saved):
            look = saved.normalized()
        case .preset(let index):
            look = AvatarLook()
            look.gender = [1, 3].contains(index) ? .girl : .boy
        case .photo, nil:
            look = AvatarLook()
        }
        original = look
        self.look = look
    }

    /// Dirty = the look differs from the one the editor opened with (gender, Boy picks or Girl picks);
    /// switching gender and back with no picks changed isn't.
    var isDirty: Bool { look != original }

    var categories: [AvatarCatalog.Category] {
        AvatarCatalog.shared.character(look.gender).categories
    }

    var selectedCategory: AvatarCatalog.Category? {
        AvatarCatalog.shared.character(look.gender).category(category)
    }

    /// Boy | Girl keeps each character's picks; the chips go back to Hair.
    mutating func select(_ gender: AvatarLook.Gender) {
        guard gender != look.gender else { return }
        look.gender = gender
        category = "hair"
    }

    mutating func pick(_ option: String) {
        look.setPick(option, for: category)
    }

    /// A random option in every category of the current gender ("None" included), different from the
    /// current look; the other gender and the chip stay as they are.
    mutating func shuffle<Generator: RandomNumberGenerator>(using generator: inout Generator) {
        let current = look
        guard categories.contains(where: { $0.options.count > 1 }) else { return }
        repeat {
            for category in categories {
                if let option = category.options.randomElement(using: &generator) {
                    look.setPick(option.id, for: category.id)
                }
            }
        } while look == current
    }

    mutating func shuffle() {
        var generator = SystemRandomNumberGenerator()
        shuffle(using: &generator)
    }
}
