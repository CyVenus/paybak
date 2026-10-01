import Foundation

/// Picks by category id, so the editor and the catalog can treat every slot alike (screens-profile
/// §1.3). Boy and Girl each keep their own picks; a category the gender doesn't have reads "none".
extension AvatarLook {
    /// The current gender's pick in `category` ("hair", "beard", "accessory" …).
    func pick(_ category: String) -> String {
        pick(category, for: gender)
    }

    func pick(_ category: String, for gender: Gender) -> String {
        switch (gender, category) {
        case (.boy, "hair"): boy.hair
        case (.boy, "beard"): boy.beard
        case (.boy, "eyewear"): boy.eyewear
        case (.boy, "eyes"): boy.eyes
        case (.boy, "mouth"): boy.mouth
        case (.boy, "outfit"): boy.outfit
        case (.girl, "hair"): girl.hair
        case (.girl, "accessory"): girl.accessory
        case (.girl, "eyewear"): girl.eyewear
        case (.girl, "eyes"): girl.eyes
        case (.girl, "mouth"): girl.mouth
        case (.girl, "outfit"): girl.outfit
        default: "none"
        }
    }

    /// Sets the current gender's pick in `category`.
    mutating func setPick(_ option: String, for category: String) {
        switch (gender, category) {
        case (.boy, "hair"): boy.hair = option
        case (.boy, "beard"): boy.beard = option
        case (.boy, "eyewear"): boy.eyewear = option
        case (.boy, "eyes"): boy.eyes = option
        case (.boy, "mouth"): boy.mouth = option
        case (.boy, "outfit"): boy.outfit = option
        case (.girl, "hair"): girl.hair = option
        case (.girl, "accessory"): girl.accessory = option
        case (.girl, "eyewear"): girl.eyewear = option
        case (.girl, "eyes"): girl.eyes = option
        case (.girl, "mouth"): girl.mouth = option
        case (.girl, "outfit"): girl.outfit = option
        default: break
        }
    }

    /// Unknown option ids (after an asset update) fall back to their category's default (§7), for both
    /// characters; the gender on show stays.
    func normalized(catalog: AvatarCatalog = .shared) -> AvatarLook {
        var look = self
        for gender in Gender.allCases {
            look.gender = gender
            for category in catalog.character(gender).categories
            where !category.options.contains(where: { $0.id == look.pick(category.id) }) {
                look.setPick(category.defaultOption, for: category.id)
            }
        }
        look.gender = self.gender
        return look
    }

    /// This look with one pick changed (the editor's tiles).
    func setting(_ option: String, for category: String) -> AvatarLook {
        var look = self
        look.setPick(option, for: category)
        return look
    }
}
