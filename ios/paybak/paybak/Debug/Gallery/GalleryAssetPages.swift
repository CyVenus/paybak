#if DEBUG
import SwiftUI

struct GalleryIconsPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("24 pt, icon/primary (brand logos keep their colours)") {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 0), count: 8), spacing: PBSpace.s4) {
                    ForEach(PBIcon.allCases) { icon in
                        VStack(spacing: PBSpace.s2) {
                            PBIconView(icon)
                                .foregroundStyle(PBColor.iconPrimary)
                            Text(icon.rawValue)
                                .textStyle(.caption2)
                                .foregroundStyle(PBColor.textTertiary)
                                .lineLimit(1)
                                .minimumScaleFactor(0.6)
                        }
                        .frame(height: 42)
                    }
                }
            }
            GallerySection("Sizes 24 · 20 · 16 · 14 (stroke 1.5 · 1.25 · 1.0 · 0.875) and tints") {
                HStack(alignment: .bottom, spacing: PBSpace.s12) {
                    ForEach([PBSize.iconLg, PBSize.iconMd, PBSize.iconSm, 14], id: \.self) { size in
                        PBIconView(.bell, size: size)
                    }
                    PBIconView(.bell).foregroundStyle(PBColor.iconSecondary)
                    PBIconView(.bell).foregroundStyle(PBColor.iconTertiary)
                    PBIconView(.bell).foregroundStyle(PBColor.iconDestructive)
                    PBIconView(.apple)
                        .foregroundStyle(PBColor.iconInverse)
                        .background(PBColor.bgInverse)
                    PBIconView(.google)
                        .foregroundStyle(PBColor.iconInverse)
                }
                .foregroundStyle(PBColor.iconPrimary)
            }
        }
    }
}

struct GalleryBrandPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Brand / App Mark 160 · 96 · 40 · 28") {
                HStack(alignment: .bottom, spacing: PBSpace.s8) {
                    PBAppMark(size: 160)
                    PBAppMark(size: 96)
                    PBAppMark(size: 40)
                    PBAppMark(size: 28)
                }
            }
            GallerySection("Brand / Logo: Horizontal, Stacked") {
                HStack(alignment: .top, spacing: PBSpace.s32) {
                    PBLogo(layout: .horizontal)
                    PBLogo(layout: .stacked)
                }
            }
            GallerySection("Art / Peep Head (avatar-1…7)") {
                HStack(spacing: PBSpace.s4) {
                    ForEach(PBPeepHead.allCases) { head in
                        GalleryItem(head.name) {
                            PBAvatar(.art(head), diameter: 46)
                        }
                    }
                }
            }
        }
    }
}

/// Figma's component-set layout: Large Default / Pressed / Disabled on the left, Small on the right.
struct GalleryPillButtonsPage: View {
    let styles: [PBButton.Style]
    var showsCTAs = false

    var body: some View {
        GalleryPageScroll {
            ForEach(styles, id: \.self) { style in
                GallerySection("\(name(of: style)): Default · Pressed · Disabled") {
                    HStack(alignment: .top, spacing: PBSpace.s24) {
                        states(style, size: .large)
                        states(style, size: .small)
                    }
                    .padding(style == .onCard ? PBSpace.s16 : 0)
                    .background(style == .onCard ? PBColor.bgCard : .clear, in: .rect(cornerRadius: PBRadius.card))
                }
            }
            if showsCTAs {
                GallerySection("Get Started CTAs: 362 wide, leading icon 20") {
                    PBButton("Continue with Apple", icon: .apple, fillsWidth: true) {}
                    PBButton("Continue with Google", style: .secondary, icon: .google, fillsWidth: true) {}
                }
            }
        }
    }

    private func states(_ style: PBButton.Style, size: PBButton.Size) -> some View {
        let label = style == .destructive ? "Delete" : "Continue"
        return VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBButton(label, style: style, size: size) {}
            PBButton(label, style: style, size: size) {}.pbPreviewInteraction(.pressed)
            PBButton(label, style: style, size: size) {}.disabled(true)
        }
    }

    private func name(of style: PBButton.Style) -> String {
        switch style {
        case .primary: "Primary"
        case .secondary: "Secondary"
        case .onCard: "On Card (on a #F5F5F5 card)"
        case .destructive: "Destructive"
        }
    }
}
#endif
