#if DEBUG
import SwiftUI

/// Icons, brand marks and the peep-head art.
struct GalleryAssetsPage: View {
    private static let tints: [(String, Color)] = [
        ("primary", PBColor.iconPrimary), ("secondary", PBColor.iconSecondary),
        ("tertiary", PBColor.iconTertiary), ("destructive", PBColor.iconDestructive),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Icons (\(PBIcon.allCases.count))") {
                GalleryFlow(spacing: PBSpace.s8, rowSpacing: PBSpace.s12) {
                    ForEach(PBIcon.allCases) { icon in
                        VStack(spacing: 0) {
                            PBIconView(icon)
                                .foregroundStyle(PBColor.iconPrimary)
                            Text(icon.galleryName)
                                .textStyle(.caption2)
                                .foregroundStyle(PBColor.textSecondary)
                                .lineLimit(1)
                        }
                        .frame(width: 56)
                    }
                }
            }
            GallerySection("Icon sizes (stroke scales with the icon)") {
                HStack(spacing: PBSpace.s16) {
                    ForEach([PBSize.iconLg, PBSize.iconMd, PBSize.iconSm, 14], id: \.self) { size in
                        VStack(spacing: 0) {
                            PBIconView(.calendar, size: size)
                                .foregroundStyle(PBColor.iconPrimary)
                            GalleryLabel("\(Int(size))")
                        }
                    }
                }
            }
            GallerySection("Icon tints") {
                HStack(spacing: PBSpace.s16) {
                    ForEach(Self.tints, id: \.0) { name, tint in
                        VStack(spacing: 0) {
                            PBIconView(.bell)
                                .foregroundStyle(tint)
                            GalleryLabel(name)
                        }
                    }
                    VStack(spacing: 0) {
                        PBIconView(.bell)
                            .foregroundStyle(PBColor.iconInverse)
                            .background(PBColor.bgInverse)
                        GalleryLabel("inverse")
                    }
                }
            }
            GallerySection("Brand logos (never recoloured)") {
                HStack(spacing: PBSpace.s16) {
                    PBIconView(.apple)
                        .foregroundStyle(PBColor.iconPrimary)
                    PBIconView(.apple)
                        .foregroundStyle(PBColor.iconInverse)
                        .padding(PBSpace.s8)
                        .background(PBColor.bgInverse, in: .circle)
                    PBIconView(.google)
                    PBIconView(.whatsapp)
                }
            }
            GallerySection("Brand / App Mark: 160 · 96 · 40 · 28") {
                HStack(alignment: .bottom, spacing: PBSpace.s16) {
                    ForEach([160, 96, 40, 28] as [CGFloat], id: \.self) { size in
                        PBAppMark(size: size)
                    }
                }
            }
            GallerySection("Brand / Logo") {
                PBLogo(layout: .horizontal)
                PBLogo(layout: .stacked)
            }
            GallerySection("Art / Peep Head (avatar-1…7)") {
                GalleryFlow(spacing: PBSpace.s12, rowSpacing: PBSpace.s12) {
                    ForEach(PBPeepHead.allCases) { head in
                        VStack(spacing: 0) {
                            PBAvatar(.art(head), diameter: PBSize.avatarLg)
                            GalleryLabel(head.name)
                        }
                    }
                }
            }
        }
    }
}

private extension PBIcon {
    /// The icon's name as the design system spells it: "ChevronRight", "QrCode", "WhatsApp".
    var galleryName: String {
        if self == .whatsapp { return "WhatsApp" }
        let name = String(describing: self)
        return name.prefix(1).uppercased() + name.dropFirst()
    }
}
#endif
