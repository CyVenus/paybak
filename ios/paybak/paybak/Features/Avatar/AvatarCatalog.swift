import Foundation
import UIKit
import os

/// The avatar parts (screens-profile §1): each character's categories and options in UI order, its
/// layer order bottom to top, and the defaults, decoded once from the bundled manifest
/// (`Assets.xcassets/AvatarParts/manifest`). Every part is a full 772 × 842 rig canvas, so the layers
/// stack at the same origin and scale; the art is in `AvatarParts/<gender>-<category>-<option>`.
struct AvatarCatalog {
    struct Option: Identifiable, Hashable {
        /// The kebab-case Figma name ("side-part"): what `AvatarLook` stores.
        let id: String
        /// The Figma name ("Side part"), the tile's accessibility label.
        let name: String
        /// The front layer; nil draws nothing (Beard None, Eyewear None, Accessory None).
        let asset: String?
        /// The back layer driven by the same pick (Hoodie's back, Girl hair's back).
        let backAsset: String?
    }

    struct Category: Identifiable, Hashable {
        let id: String
        let label: String
        /// Outfit tiles show the Bust crop; the face parts show the Head crop.
        let tileCrop: AvatarCrop
        let defaultOption: String
        let options: [Option]

        func option(_ id: String) -> Option {
            options.first { $0.id == id } ?? options.first { $0.id == defaultOption } ?? options[0]
        }
    }

    /// One layer of the stack: a fixed base part, or the front or back file of a category's pick.
    enum Layer: Hashable {
        case base(asset: String)
        case front(category: String)
        case back(category: String)
    }

    struct Character {
        let categories: [Category]
        let layers: [Layer]

        func category(_ id: String) -> Category? {
            categories.first { $0.id == id }
        }
    }

    let boy: Character
    let girl: Character

    func character(_ gender: AvatarLook.Gender) -> Character {
        gender == .boy ? boy : girl
    }

    /// The asset names to draw for `look`, bottom to top. Unknown option ids fall back to the category
    /// default (§7), and "none" options draw nothing.
    func layerAssets(for look: AvatarLook) -> [String] {
        let character = character(look.gender)
        return character.layers.compactMap { layer in
            switch layer {
            case .base(let asset):
                asset
            case .front(let id):
                character.category(id)?.option(look.pick(id)).asset
            case .back(let id):
                character.category(id)?.option(look.pick(id)).backAsset
            }
        }
    }

    // MARK: Loading

    static let shared = AvatarCatalog.load()

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Avatar")

    private static func load() -> AvatarCatalog {
        do {
            guard let data = NSDataAsset(name: "AvatarParts/manifest")?.data else { throw CocoaError(.fileNoSuchFile) }
            let manifest = try JSONDecoder().decode(Manifest.self, from: data)
            return AvatarCatalog(boy: manifest.genders.boy.character, girl: manifest.genders.girl.character)
        } catch {
            log.error("Could not load the avatar manifest: \(String(describing: error), privacy: .public)")
            return AvatarCatalog(boy: Character(categories: [], layers: []), girl: Character(categories: [], layers: []))
        }
    }

    /// "boy/hair/side-part.svg" → "AvatarParts/boy-hair-side-part".
    fileprivate static func assetName(_ file: String?) -> String? {
        guard let file else { return nil }
        let stem = file.hasSuffix(".svg") ? String(file.dropLast(4)) : file
        return "AvatarParts/" + stem.replacingOccurrences(of: "/", with: "-")
    }
}

/// The rig rectangle a container shows (screens-profile §1.5).
enum AvatarCrop: Hashable {
    /// (72, 60, 600, 600): avatar circles and the face-part tiles.
    case head
    /// (61, 192, 650, 650): the Outfit tiles.
    case bust
    /// Rig y 60 to the bottom (782 tall), centred horizontally: the editor stage.
    case stage
    /// The whole 772 × 842 rig, fitted.
    case full

    static let rigSize = CGSize(width: 772, height: 842)

    /// Where the rig's origin goes and how much it's scaled inside a container of `size`.
    func placement(in size: CGSize) -> (scale: CGFloat, origin: CGPoint) {
        let rig = Self.rigSize
        switch self {
        case .head:
            let scale = size.width / 600
            return (scale, CGPoint(x: -72 * scale, y: -60 * scale))
        case .bust:
            let scale = size.width / 650
            return (scale, CGPoint(x: -61 * scale, y: -192 * scale))
        case .stage:
            let scale = size.height / 782
            return (scale, CGPoint(x: (size.width - rig.width * scale) / 2, y: size.height - rig.height * scale))
        case .full:
            let scale = min(size.width / rig.width, size.height / rig.height)
            return (scale, CGPoint(x: (size.width - rig.width * scale) / 2, y: (size.height - rig.height * scale) / 2))
        }
    }
}

// MARK: - Manifest (assets/avatar-parts/manifest.json)

private struct Manifest: Decodable {
    struct Genders: Decodable {
        let boy: Gender
        let girl: Gender
    }

    struct Gender: Decodable {
        struct Category: Decodable {
            struct Option: Decodable {
                let id: String
                let name: String
                let file: String?
                let backFile: String?
            }

            let id: String
            let label: String
            let tileCrop: String
            let `default`: String
            let options: [Option]
        }

        struct Base: Decodable {
            let file: String
        }

        struct Layer: Decodable {
            let base: String?
            let category: String?
            let uses: String?
        }

        let categories: [Category]
        let base: [String: Base]
        let layers: [Layer]

        var character: AvatarCatalog.Character {
            let categories = categories.map { category in
                AvatarCatalog.Category(
                    id: category.id,
                    label: category.label,
                    tileCrop: category.tileCrop == "bust" ? .bust : .head,
                    defaultOption: category.default,
                    options: category.options.map { option in
                        AvatarCatalog.Option(
                            id: option.id,
                            name: option.name,
                            asset: AvatarCatalog.assetName(option.file),
                            backAsset: AvatarCatalog.assetName(option.backFile)
                        )
                    }
                )
            }
            let layers: [AvatarCatalog.Layer] = layers.compactMap { layer in
                if let base = layer.base, let asset = AvatarCatalog.assetName(self.base[base]?.file) {
                    return .base(asset: asset)
                }
                guard let category = layer.category else { return nil }
                return layer.uses == "backFile" ? .back(category: category) : .front(category: category)
            }
            return AvatarCatalog.Character(categories: categories, layers: layers)
        }
    }

    let genders: Genders
}
