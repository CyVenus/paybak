import SwiftUI

/// Avatar / Pair (Figma 116:1005): "from → to" for transfers and payments, two avatars with a gray
/// arrow between them. Size 32 (16 pt arrow, 4 pt gaps) in Row / Transfer; size 56 (20 pt arrow,
/// 8 pt gaps) in Header / Amount Hero. Inside a #F5F5F5 card the circles turn white (`isOnCard`).
struct PBAvatarPair: View {
    let from: PBAvatar.Content
    let to: PBAvatar.Content
    var diameter: CGFloat = PBSize.avatarSm
    var isOnCard = false

    private var isLarge: Bool { diameter >= PBSize.avatarLg }

    var body: some View {
        HStack(spacing: isLarge ? PBSpace.s8 : PBSpace.s4) {
            PBAvatar(from, diameter: diameter, isOnCard: isOnCard)
            PBIconView(.arrowRight, size: isLarge ? PBSize.iconMd : PBSize.iconSm)
                .foregroundStyle(PBColor.iconTertiary)
            PBAvatar(to, diameter: diameter, isOnCard: isOnCard)
        }
        .accessibilityHidden(true)
    }
}

#Preview("PBAvatarPair") {
    VStack(spacing: PBSpace.s16) {
        PBAvatarPair(from: .art(.arjun), to: .art(.kabir))
        PBAvatarPair(from: .art(.arjun), to: .art(.kabir), diameter: PBSize.avatarLg)
        PBAvatarPair(from: .art(.rohan), to: .art(.dev), isOnCard: true)
            .pbCard()
    }
}
