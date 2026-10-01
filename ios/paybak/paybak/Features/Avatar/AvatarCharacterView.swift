import SwiftUI

/// Avatar / Character (Figma 61:272, `PBAvatarView`): a custom character composed natively from its
/// part layers (screens-profile §1; there is no avatar.riv). The layers are drawn bottom to top in
/// the manifest order at one scale, placed by the crop, so a 120 pt circle shows the Head crop at
/// ×0.2. It doesn't draw a background or clip: the circle, tile or stage around it does both.
struct AvatarCharacterView: View {
    typealias Crop = AvatarCrop

    let look: AvatarLook
    var crop: Crop = .head

    var body: some View {
        GeometryReader { proxy in
            let placement = crop.placement(in: proxy.size)
            let rig = AvatarCrop.rigSize
            ZStack(alignment: .topLeading) {
                ForEach(Array(AvatarCatalog.shared.layerAssets(for: look).enumerated()), id: \.offset) { _, asset in
                    Image(asset)
                        .resizable()
                        .frame(width: rig.width * placement.scale, height: rig.height * placement.scale)
                }
            }
            .offset(x: placement.origin.x, y: placement.origin.y)
        }
        .accessibilityHidden(true)
    }
}

#Preview("AvatarCharacterView") {
    let quiff = AvatarLook.defaultBoy.setting("quiff", for: "hair").setting("stubble", for: "beard")
    var girl = AvatarLook.defaultBoy
    girl.gender = .girl
    girl.setPick("top-bun", for: "hair")
    girl.setPick("beanie", for: "accessory")
    return VStack(spacing: PBSpace.s16) {
        HStack(spacing: PBSpace.s16) {
            AvatarCharacterView(look: .defaultBoy)
                .frame(width: 120, height: 120)
                .background(PBColor.bgCard, in: .circle)
                .clipShape(.circle)
            AvatarCharacterView(look: girl)
                .frame(width: 120, height: 120)
                .background(PBColor.bgCard, in: .circle)
                .clipShape(.circle)
            AvatarCharacterView(look: quiff, crop: .bust)
                .frame(width: 112, height: 112)
                .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
                .clipShape(.rect(cornerRadius: PBRadius.card))
        }
        AvatarCharacterView(look: quiff, crop: .stage)
            .frame(width: 362, height: 300)
            .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            .clipShape(.rect(cornerRadius: PBRadius.card))
    }
}
