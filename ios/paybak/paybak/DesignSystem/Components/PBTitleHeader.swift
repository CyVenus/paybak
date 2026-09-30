import SwiftUI

/// Header / Title Row (Figma 128:1857): the content header 16 pt under the Push Header on group,
/// friend and project detail. A 56 pt leading (a group-type icon tile or the person's avatar), then
/// the Title/2 title with an optional Muted pill ("Guest", "Archived"), the Subheadline subtitle and
/// an optional avatar stack of members 8 pt below. A title-only header centres on the leading.
struct PBTitleHeader: View {
    let title: String
    /// `.icon(.plane)` for a group tile (Plane, Home, People, Tag; Drone, Package for projects), or
    /// the person's art / initials.
    let leading: PBAvatar.Content
    var subtitle: String?
    var tag: String?
    var members: [PBPeepHead] = []

    var body: some View {
        HStack(alignment: .top, spacing: PBSpace.s16) {
            PBAvatar(leading, diameter: PBSize.avatarLg)
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                VStack(alignment: .leading, spacing: PBSpace.s2) {
                    HStack(spacing: PBSpace.s8) {
                        Text(title)
                            .textStyle(.title2)
                            .foregroundStyle(PBColor.textPrimary)
                            .lineLimit(1)
                            .accessibilityAddTraits(.isHeader)
                        if let tag {
                            PBBadge(tag)
                        }
                    }
                    if let subtitle {
                        Text(subtitle)
                            .textStyle(.subheadline)
                            .foregroundStyle(PBColor.textSecondary)
                            .lineLimit(1)
                    }
                }
                .frame(minHeight: PBSize.avatarLg)
                if !members.isEmpty {
                    PBAvatarStack(heads: Array(members.prefix(4)))
                        .accessibilityLabel("\(members.count) members")
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

#Preview("PBTitleHeader") {
    VStack(alignment: .leading, spacing: PBSpace.s24) {
        PBTitleHeader(title: "Goa Trip", leading: .icon(.plane), subtitle: "21–25 Sep · 5 members · ₹39,500 spent", members: [.arjun, .priya, .rohan, .esha])
        PBTitleHeader(title: "Rohan", leading: .art(.rohan), subtitle: "You’re owed ₹800")
        PBTitleHeader(title: "Arjun R", leading: .initials("AR"), tag: "Guest")
    }
    .padding(PBLayout.screenMargin)
}
