#if DEBUG
import SwiftUI
import UIKit

// MARK: - Colours

struct GalleryColourPrimitivesPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Primitives") {
                SwatchGrid(swatches: [
                    ("gray/0", PBPalette.gray0), ("gray/50", PBPalette.gray50), ("gray/100", PBPalette.gray100),
                    ("gray/200", PBPalette.gray200), ("gray/300", PBPalette.gray300), ("gray/400", PBPalette.gray400),
                    ("gray/600", PBPalette.gray600), ("gray/800", PBPalette.gray800), ("gray/900", PBPalette.gray900),
                    ("red/50", PBPalette.red50), ("red/500", PBPalette.red500), ("red/600", PBPalette.red600),
                    ("black-40", PBPalette.black40), ("black-06", PBPalette.black06), ("white-72", PBPalette.white72),
                    ("white-60", PBPalette.white60), ("device/black", PBPalette.deviceBlack),
                ])
            }
            GallerySection("bg/*") {
                SwatchGrid(swatches: [
                    ("primary", PBColor.bgPrimary), ("card", PBColor.bgCard), ("card-pressed", PBColor.bgCardPressed),
                    ("selected", PBColor.bgSelected), ("inverse", PBColor.bgInverse), ("inverse-pressed", PBColor.bgInversePressed),
                    ("disabled", PBColor.bgDisabled), ("destructive", PBColor.bgDestructive), ("destr.-pressed", PBColor.bgDestructivePressed),
                    ("destr.-subtle", PBColor.bgDestructiveSubtle), ("scrim", PBColor.bgScrim), ("glass", PBColor.bgGlass),
                    ("indicator", PBColor.bgIndicator), ("device", PBColor.bgDevice), ("camera", PBColor.bgCamera),
                ])
            }
        }
    }
}

struct GalleryColourSemanticPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("text/*") {
                SwatchGrid(swatches: [
                    ("primary", PBColor.textPrimary), ("secondary", PBColor.textSecondary), ("tertiary", PBColor.textTertiary),
                    ("inverse", PBColor.textInverse), ("disabled", PBColor.textDisabled), ("destructive", PBColor.textDestructive),
                ])
            }
            GallerySection("icon/*") {
                SwatchGrid(swatches: [
                    ("primary", PBColor.iconPrimary), ("secondary", PBColor.iconSecondary), ("tertiary", PBColor.iconTertiary),
                    ("inverse", PBColor.iconInverse), ("destructive", PBColor.iconDestructive),
                ])
            }
            GallerySection("border/*") {
                SwatchGrid(swatches: [
                    ("subtle", PBColor.borderSubtle), ("strong", PBColor.borderStrong),
                    ("destructive", PBColor.borderDestructive), ("glass-highlight", PBColor.borderGlassHighlight),
                ])
            }
            GallerySection("illustration/* · chart/*") {
                SwatchGrid(swatches: [
                    ("illus/line", PBColor.illustrationLine), ("illus/tint", PBColor.illustrationTint), ("illus/fill", PBColor.illustrationFill),
                    ("chart/track", PBColor.chartTrack), ("chart/bar", PBColor.chartBar), ("chart/fill", PBColor.chartFill),
                    ("chart/over", PBColor.chartOver),
                ])
            }
        }
    }
}

/// Swatches with the token name and the resolved sRGB hex (plus opacity when it isn't 100 %).
private struct SwatchGrid: View {
    let swatches: [(name: String, color: Color)]

    @Environment(\.self) private var environment

    var body: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: PBSpace.s8, alignment: .topLeading), count: 4), spacing: PBSpace.s8) {
            ForEach(swatches, id: \.name) { swatch in
                VStack(alignment: .leading, spacing: PBSpace.s2) {
                    RoundedRectangle(cornerRadius: PBRadius.sm)
                        .fill(swatch.color)
                        .overlay {
                            RoundedRectangle(cornerRadius: PBRadius.sm).strokeBorder(PBColor.borderSubtle)
                        }
                        .frame(height: 32)
                    Text(swatch.name)
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(hex(swatch.color))
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textTertiary)
                }
                .lineLimit(1)
            }
        }
    }

    private func hex(_ color: Color) -> String {
        let resolved = color.resolve(in: environment)
        let channels = [resolved.red, resolved.green, resolved.blue].map { Int(($0 * 255).rounded()) }
        let rgb = channels.map { String(format: "%02X", $0) }.joined()
        return resolved.opacity < 1 ? "#\(rgb) \(Int((resolved.opacity * 100).rounded()))%" : "#\(rgb)"
    }
}

// MARK: - Text styles

/// Each style on its Figma line box (tinted), with its spec and the face actually loaded, so a
/// missing font registration shows up as "system font".
struct GalleryTextStylesPage: View {
    let styles: [PBTextStyle]

    var body: some View {
        GalleryPageScroll {
            ForEach(styles, id: \.name) { style in
                VStack(alignment: .leading, spacing: PBSpace.s4) {
                    Text(sample(for: style))
                        .textStyle(style)
                        .foregroundStyle(PBColor.textPrimary)
                        .background(PBColor.bgDestructiveSubtle)
                    Text(spec(for: style))
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textTertiary)
                }
            }
        }
    }

    private func sample(for style: PBTextStyle) -> String {
        switch style {
        case .amountDisplay, .amountLarge, .amountMedium: "+₹2,450"
        case .wordmarkS, .wordmarkL: "Paybak"
        case .title1: "Split it. Track it.\nSettle it."
        default: "Split it. Track it. Settle it."
        }
    }

    private func spec(for style: PBTextStyle) -> String {
        let loaded = UIFont(name: style.face.postScriptName, size: style.size)?.fontName ?? "system font (Manrope missing)"
        let percent = Double(style.letterSpacing * 100).formatted(.number.precision(.fractionLength(0...2)))
        return "\(style.name) · \(loaded) \(Int(style.size))/\(Int(style.lineHeight)) · \(percent) %"
    }
}

// MARK: - Metrics

struct GalleryMetricsPage: View {
    private let spaces: [(String, CGFloat)] = [
        ("2", PBSpace.s2), ("4", PBSpace.s4), ("6", PBSpace.s6), ("8", PBSpace.s8), ("12", PBSpace.s12),
        ("16", PBSpace.s16), ("20", PBSpace.s20), ("24", PBSpace.s24), ("28", PBSpace.s28), ("32", PBSpace.s32),
        ("40", PBSpace.s40), ("48", PBSpace.s48), ("64", PBSpace.s64), ("96", PBSpace.s96),
    ]
    private let radii: [(String, CGFloat)] = [
        ("xs 6", PBRadius.xs), ("sm 10", PBRadius.sm), ("input 14", PBRadius.input), ("tile 14", PBRadius.tile),
        ("card 20", PBRadius.card), ("sheet 40", PBRadius.sheet), ("full", PBRadius.full),
    ]
    private let sizes: [(String, CGFloat)] = [
        ("icon-sm", PBSize.iconSm), ("icon-md", PBSize.iconMd), ("icon-lg", PBSize.iconLg), ("avatar-xs", PBSize.avatarXs),
        ("avatar-sm", PBSize.avatarSm), ("button-sm", PBSize.buttonSm), ("avatar-md", PBSize.avatarMd), ("tap", PBSize.tap),
        ("button-lg", PBSize.buttonLg), ("add-button", PBSize.addButton), ("avatar-lg", PBSize.avatarLg), ("tabbar", PBSize.tabbar),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("space/* (layout: margin 20 · card padding 16 · section gap 24)") {
                ForEach(spaces, id: \.0) { name, value in
                    HStack(spacing: PBSpace.s8) {
                        Text(name).textStyle(.caption2).foregroundStyle(PBColor.textSecondary).frame(width: 20, alignment: .trailing)
                        Rectangle().fill(PBColor.bgInverse).frame(width: value, height: 8)
                    }
                }
            }
            GallerySection("radius/*") {
                HStack(spacing: PBSpace.s8) {
                    ForEach(radii, id: \.0) { name, value in
                        GalleryItem(name) {
                            UnevenRoundedRectangle(topLeadingRadius: min(value, 44), style: .circular)
                                .fill(PBColor.bgCard)
                                .frame(width: 44, height: 44)
                        }
                    }
                }
            }
            GallerySection("size/* (hairline 1)") {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), alignment: .bottomLeading), count: 4), spacing: PBSpace.s12) {
                    ForEach(sizes, id: \.0) { name, value in
                        GalleryItem("\(name) \(Int(value))") {
                            RoundedRectangle(cornerRadius: PBRadius.xs)
                                .fill(PBColor.bgCardPressed)
                                .frame(width: value, height: value)
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Materials

struct GalleryMaterialsPage: View {
    @State private var showsScrim = false

    var body: some View {
        GalleryPageScroll {
            GallerySection("Glass, Glass Small, Frosted over content") {
                ZStack {
                    backdrop
                    VStack(spacing: PBSpace.s24) {
                        Text("Material/Glass")
                            .textStyle(.headline)
                            .frame(width: 280, height: PBSize.tabbar)
                            .pbMaterial(.glass, in: .capsule)
                        HStack(spacing: PBSpace.s12) {
                            PBIconButton(.bell, accessibilityLabel: "Notifications", style: .glass, showsBadge: true) {}
                            Text("Material/Glass Small")
                                .textStyle(.footnote)
                                .padding(.horizontal, PBSpace.s8)
                                .background(PBColor.bgPrimary, in: .capsule)
                        }
                        Text("Material/Frosted")
                            .textStyle(.headline)
                            .frame(width: 280, height: PBSize.tabbar)
                            .pbMaterial(.frosted, in: .rect(cornerRadius: PBRadius.card))
                    }
                    .foregroundStyle(PBColor.textPrimary)
                }
                .frame(height: 300)
                .clipShape(.rect(cornerRadius: PBRadius.card))
            }
            GallerySection("Scrim (bg/scrim 40 %)") {
                PBButton("Show the scrim", style: .secondary, size: .small) { showsScrim = true }
            }
        }
        .overlay {
            if showsScrim {
                PBScrim { showsScrim = false }
            }
        }
    }

    /// Stripes and art behind the glass so the refraction and blur are visible.
    private var backdrop: some View {
        ZStack {
            HStack(spacing: 0) {
                ForEach(0..<12) { index in
                    Rectangle().fill(index.isMultiple(of: 2) ? PBColor.bgInverse : PBColor.bgCard)
                }
            }
            HStack {
                PBPeepHead.priya.image.resizable().frame(width: 120, height: 120)
                PBPeepHead.rohan.image.resizable().frame(width: 120, height: 120)
            }
        }
    }
}
#endif
