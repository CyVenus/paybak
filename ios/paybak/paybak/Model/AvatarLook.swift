import Foundation

/// A custom character saved from Edit avatar (screens-profile §1.7): both genders' picks are kept, the
/// saved gender is the one showing. Option ids are the kebab-case Figma names (§1.3); unknown ids fall
/// back to the category default when drawn.
struct AvatarLook: Codable, Hashable {
    enum Gender: String, Codable, CaseIterable {
        case boy
        case girl
    }

    struct Boy: Codable, Hashable {
        var hair = "curly"
        var beard = "none"
        var eyewear = "round"
        var eyes = "dots"
        var mouth = "smile"
        var outfit = "hoodie"
    }

    struct Girl: Codable, Hashable {
        var hair = "long-wavy"
        var accessory = "none"
        var eyewear = "round"
        var eyes = "dots"
        var mouth = "smile"
        var outfit = "t-shirt"
    }

    var gender: Gender = .boy
    var boy = Boy()
    var girl = Girl()

    /// Curly, no beard, Round, Dots, Smile, Hoodie (the Profile start screens' seed).
    static let defaultBoy = AvatarLook()
}
