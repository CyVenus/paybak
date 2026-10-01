import SwiftUI

/// Avatar / Circle (Figma 11:136): a clipped circle with a Peep head, a photo, initials (the
/// fallback) or a category icon. Decorative: the row or label next to it carries the name.
struct PBAvatar: View {
    enum Content {
        case art(PBPeepHead)
        case photo(Image)
        case initials(String)
        case icon(PBIcon)
    }

    let content: Content
    /// 24, 32, 40 or 56 in Figma (`size/avatar-xs…lg`); Setup 1 draws 46 inside the option ring.
    var diameter: CGFloat = PBSize.avatarMd
    /// "On a card, nested avatars turn white": `bg/primary` instead of `bg/card` inside #F5F5F5 cards.
    var isOnCard = false
    /// The icon colour (`.icon` content only); archived groups use `icon/tertiary`.
    var iconTint = PBColor.iconPrimary

    init(_ content: Content, diameter: CGFloat = PBSize.avatarMd, isOnCard: Bool = false, iconTint: Color = PBColor.iconPrimary) {
        self.content = content
        self.diameter = diameter
        self.isOnCard = isOnCard
        self.iconTint = iconTint
    }

    var body: some View {
        ZStack {
            Circle().fill(isOnCard ? PBColor.bgPrimary : PBColor.bgCard)
            switch content {
            case .art(let head):
                // The 120 × 120 art box is scaled to the circle with no inset.
                head.image.resizable()
            case .photo(let image):
                image.resizable().scaledToFill()
            case .initials(let initials):
                Text(initials)
                    .textStyle(initialsStyle)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
            case .icon(let icon):
                PBIconView(icon, size: iconSize)
                    .foregroundStyle(iconTint)
            }
        }
        .frame(width: diameter, height: diameter)
        .clipShape(.circle)
        .accessibilityHidden(true)
    }

    /// Profile's 120 pt circle draws Title/1, as Android's Profile does.
    private var initialsStyle: PBTextStyle {
        switch diameter {
        case ...PBSize.avatarXs: .caption2
        case ...PBSize.avatarSm: .caption1
        case ..<PBSize.avatarLg: .headline
        case ..<120: .title3
        default: .title1
        }
    }

    /// 24 → 14, 32 → 16, 40 → 20, 56 → 24 (the stroke scales with the icon).
    private var iconSize: CGFloat {
        switch diameter {
        case ...PBSize.avatarXs: 14
        case ...PBSize.avatarSm: PBSize.iconSm
        case ..<PBSize.avatarLg: PBSize.iconMd
        default: PBSize.iconLg
        }
    }
}

/// Avatar / Stack (Figma 11:417): 2–4 overlapping 32 pt avatars, each with a 2 pt white ring;
/// later avatars sit on top of earlier ones. Any avatar can be in it: a Peep head, a guest's
/// initials, or the user's own (photo, character or preset). Decorative for VoiceOver.
struct PBAvatarStack: View {
    enum Member {
        case content(PBAvatar.Content)
        /// The current user, as `PBUserAvatar` draws them.
        case user
    }

    let members: [Member]

    init(heads: [PBPeepHead]) {
        members = heads.map { .content(.art($0)) }
    }

    init(members: [Member]) {
        self.members = members
    }

    var body: some View {
        HStack(spacing: -PBSpace.s8) {
            ForEach(members.indices, id: \.self) { index in
                avatar(members[index])
                    .background {
                        Circle()
                            .fill(PBColor.bgPrimary)
                            .padding(-2)
                    }
            }
        }
        .accessibilityHidden(true)
    }

    @ViewBuilder
    private func avatar(_ member: Member) -> some View {
        switch member {
        case .content(let content):
            PBAvatar(content, diameter: PBSize.avatarSm)
        case .user:
            PBUserAvatar(diameter: PBSize.avatarSm)
        }
    }
}

#Preview("PBAvatar") {
    VStack(spacing: PBSpace.s16) {
        HStack(spacing: PBSpace.s12) {
            ForEach([PBSize.avatarXs, PBSize.avatarSm, PBSize.avatarMd, PBSize.avatarLg], id: \.self) { size in
                PBAvatar(.art(.arjun), diameter: size)
            }
        }
        HStack(spacing: PBSpace.s12) {
            ForEach([PBSize.avatarXs, PBSize.avatarSm, PBSize.avatarMd, PBSize.avatarLg], id: \.self) { size in
                PBAvatar(.initials("AM"), diameter: size)
            }
        }
        HStack(spacing: PBSpace.s12) {
            PBAvatar(.icon(.groups))
            PBAvatar(.icon(.groups), isOnCard: true)
                .padding(PBSpace.s16)
                .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        }
        PBAvatarStack(heads: [.arjun, .priya, .rohan, .esha])
    }
}
