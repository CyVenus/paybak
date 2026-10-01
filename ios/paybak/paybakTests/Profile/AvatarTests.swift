import CoreGraphics
import Testing
@testable import paybak

/// The avatar parts, crops and the editor's draft (screens-profile §1, §3).
@MainActor
struct AvatarTests {
    private let catalog = AvatarCatalog.shared

    @Test func catalogMatchesTheManifest() {
        #expect(catalog.boy.categories.map(\.label) == ["Hair", "Beard", "Eyewear", "Eyes", "Mouth", "Outfit"])
        #expect(catalog.girl.categories.map(\.label) == ["Hair", "Accessory", "Eyewear", "Eyes", "Mouth", "Outfit"])
        #expect(catalog.boy.categories.allSatisfy { $0.options.count == 6 })
        #expect(catalog.boy.category("hair")?.options.map(\.name) == ["Curly", "Quiff", "Side part", "Spiky", "Man bun", "Crew cut"])
        #expect(catalog.boy.category("outfit")?.tileCrop == .bust)
        #expect(catalog.boy.layers.count == 11)
        #expect(catalog.girl.layers.count == 12)
    }

    @Test func defaultBoyLayersBottomToTop() {
        #expect(catalog.layerAssets(for: .defaultBoy) == [
            "AvatarParts/boy-outfit-back-hoodie", "AvatarParts/boy-base-body", "AvatarParts/boy-outfit-hoodie",
            "AvatarParts/boy-base-shadow", "AvatarParts/boy-base-face", "AvatarParts/boy-hair-curly",
            "AvatarParts/boy-mouth-smile", "AvatarParts/boy-base-nose", "AvatarParts/boy-eyes-dots",
            "AvatarParts/boy-eyewear-round",
        ])
    }

    @Test func girlHairDrivesBothLayersAndBunsNeedNoMask() {
        var look = AvatarLook()
        look.gender = .girl
        look.setPick("top-bun", for: "hair")
        look.setPick("beanie", for: "accessory")
        let layers = catalog.layerAssets(for: look)
        #expect(layers.first == "AvatarParts/girl-hair-back-top-bun")
        #expect(layers.contains("AvatarParts/girl-hair-top-bun"))
        #expect(layers.firstIndex(of: "AvatarParts/girl-accessory-beanie")! > layers.firstIndex(of: "AvatarParts/girl-hair-top-bun")!)
    }

    @Test func unknownPicksFallBackToTheDefault() {
        let look = AvatarLook.defaultBoy.setting("mohawk", for: "hair")
        #expect(catalog.layerAssets(for: look).contains("AvatarParts/boy-hair-curly"))
    }

    @Test func crops() {
        let head = AvatarCrop.head.placement(in: CGSize(width: 120, height: 120))
        #expect(abs(head.scale - 0.2) < 0.0001)
        #expect(abs(head.origin.x + 14.4) < 0.001 && abs(head.origin.y + 12) < 0.001)
        let tile = AvatarCrop.bust.placement(in: CGSize(width: 112, height: 112))
        #expect(abs(tile.scale - 0.172308) < 0.00001)
        let stage = AvatarCrop.stage.placement(in: CGSize(width: 362, height: 300))
        #expect(abs(stage.origin.x - 32.92) < 0.01 && abs(stage.origin.y + 23.02) < 0.01)
    }

    @Test func genderSwitchKeepsPicksAndIsNotDirtyWhenUndone() {
        var draft = AvatarDraft(avatar: .character(.defaultBoy))
        draft.category = "beard"
        draft.select(.girl)
        #expect(draft.category == "hair")
        #expect(draft.isDirty)
        draft.select(.boy)
        #expect(!draft.isDirty)
        draft.pick("quiff")
        draft.select(.girl)
        draft.pick("ponytail")
        draft.select(.boy)
        #expect(draft.look.boy.hair == "quiff")
        #expect(draft.look.girl.hair == "ponytail")
        #expect(draft.isDirty)
    }

    @Test func startingGenderFollowsThePreset() {
        #expect(AvatarDraft(avatar: .preset(1)).look.gender == .girl)
        #expect(AvatarDraft(avatar: .preset(2)).look.gender == .boy)
        #expect(AvatarDraft(avatar: nil).look == AvatarLook())
    }

    @Test func shuffleChangesOnlyTheCurrentCharacter() {
        var generator = SeededGenerator(seed: 7)
        var draft = AvatarDraft(avatar: .character(.defaultBoy))
        draft.category = "eyes"
        for _ in 0..<20 {
            let before = draft.look
            draft.shuffle(using: &generator)
            #expect(draft.look != before)
            #expect(draft.look.girl == before.girl)
            #expect(draft.category == "eyes")
        }
    }
}

/// A deterministic generator for the shuffle tests (SplitMix64).
private struct SeededGenerator: RandomNumberGenerator {
    var state: UInt64

    init(seed: UInt64) {
        state = seed
    }

    mutating func next() -> UInt64 {
        state &+= 0x9E37_79B9_7F4A_7C15
        var z = state
        z = (z ^ (z >> 30)) &* 0xBF58_476D_1CE4_E5B9
        z = (z ^ (z >> 27)) &* 0x94D0_49BB_1331_11EB
        return z ^ (z >> 31)
    }
}
