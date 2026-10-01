#if DEBUG
import SwiftUI

/// A named token sample.
private struct GalleryToken: Identifiable {
    let name: String
    let color: Color

    var id: String { name }

    init(_ name: String, _ color: Color) {
        self.name = name
        self.color = color
    }
}

// MARK: - Colours

struct GalleryColoursPage: View {
    private static let primitives: [GalleryToken] = [
        .init("gray/0", PBPalette.gray0), .init("gray/50", PBPalette.gray50), .init("gray/100", PBPalette.gray100),
        .init("gray/200", PBPalette.gray200), .init("gray/300", PBPalette.gray300), .init("gray/400", PBPalette.gray400),
        .init("gray/600", PBPalette.gray600), .init("gray/800", PBPalette.gray800), .init("gray/900", PBPalette.gray900),
        .init("red/50", PBPalette.red50), .init("red/500", PBPalette.red500), .init("red/600", PBPalette.red600),
        .init("black-40", PBPalette.black40), .init("black-06", PBPalette.black06), .init("white-72", PBPalette.white72),
        .init("white-60", PBPalette.white60), .init("device/black", PBPalette.deviceBlack),
    ]

    private static let semantic: [(group: String, tokens: [GalleryToken])] = [
        ("bg", [
            .init("primary", PBColor.bgPrimary), .init("card", PBColor.bgCard), .init("card-pressed", PBColor.bgCardPressed),
            .init("selected", PBColor.bgSelected), .init("inverse", PBColor.bgInverse), .init("inverse-pressed", PBColor.bgInversePressed),
            .init("disabled", PBColor.bgDisabled), .init("destructive", PBColor.bgDestructive),
            .init("destructive-pressed", PBColor.bgDestructivePressed), .init("destructive-subtle", PBColor.bgDestructiveSubtle),
            .init("scrim", PBColor.bgScrim), .init("glass", PBColor.bgGlass), .init("indicator", PBColor.bgIndicator),
            .init("device", PBColor.bgDevice), .init("camera", PBColor.bgCamera),
        ]),
        ("text", [
            .init("primary", PBColor.textPrimary), .init("secondary", PBColor.textSecondary), .init("tertiary", PBColor.textTertiary),
            .init("inverse", PBColor.textInverse), .init("disabled", PBColor.textDisabled), .init("destructive", PBColor.textDestructive),
        ]),
        ("icon", [
            .init("primary", PBColor.iconPrimary), .init("secondary", PBColor.iconSecondary), .init("tertiary", PBColor.iconTertiary),
            .init("inverse", PBColor.iconInverse), .init("destructive", PBColor.iconDestructive),
        ]),
        ("border", [
            .init("subtle", PBColor.borderSubtle), .init("strong", PBColor.borderStrong),
            .init("destructive", PBColor.borderDestructive), .init("glass-highlight", PBColor.borderGlassHighlight),
        ]),
        ("illustration", [
            .init("line", PBColor.illustrationLine), .init("tint", PBColor.illustrationTint), .init("fill", PBColor.illustrationFill),
        ]),
        ("chart", [
            .init("track", PBColor.chartTrack), .init("bar", PBColor.chartBar), .init("fill", PBColor.chartFill),
            .init("over", PBColor.chartOver),
        ]),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Primitives") {
                Swatches(tokens: Self.primitives)
            }
            ForEach(Self.semantic, id: \.group) { group, tokens in
                GallerySection("color/\(group)") {
                    Swatches(tokens: tokens)
                }
            }
        }
    }
}

/// Chips of each colour, outlined so white and translucent tokens stay visible, with the token name
/// and its sRGB hex ("#0A0A0A", or "#0A0A0A 6%" for a colour with alpha).
private struct Swatches: View {
    let tokens: [GalleryToken]

    @Environment(\.self) private var environment

    var body: some View {
        GalleryFlow(spacing: 10, rowSpacing: PBSpace.s12) {
            ForEach(tokens) { token in
                VStack(alignment: .leading, spacing: PBSpace.s4) {
                    RoundedRectangle(cornerRadius: PBRadius.tile)
                        .fill(PBColor.bgInverse.opacity(0.04))
                        .overlay {
                            RoundedRectangle(cornerRadius: PBRadius.tile).fill(token.color)
                        }
                        .overlay {
                            RoundedRectangle(cornerRadius: PBRadius.tile).strokeBorder(PBColor.borderSubtle, lineWidth: PBSize.hairline)
                        }
                        .frame(width: 84, height: 44)
                    Text(token.name)
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                    Text(hex(token.color))
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textSecondary)
                        .lineLimit(1)
                }
                .frame(width: 84, alignment: .leading)
            }
        }
    }

    private func hex(_ color: Color) -> String {
        let resolved = color.resolve(in: environment)
        let channels = [resolved.red, resolved.green, resolved.blue].map { Int((min(max($0, 0), 1) * 255).rounded()) }
        let rgb = "#" + channels.map { String(format: "%02X", $0) }.joined()
        let alpha = Int((resolved.opacity * 100).rounded())
        return alpha == 100 ? rgb : "\(rgb) \(alpha)%"
    }
}

// MARK: - Text styles

/// Every Figma text style with its specs. The tinted band behind each sample is the line box, so the
/// line height can be checked against Figma.
struct GalleryTypePage: View {
    private static let samples: [(style: PBTextStyle, sample: String)] = [
        (.title1, "Split it. Track it."),
        (.title2, "Enter the code"),
        (.title3, "Recent activity"),
        (.amountDisplay, "₹2,450"),
        (.amountLarge, "+₹1,240"),
        (.amountMedium, "−₹380"),
        (.headline, "Indian Rupee"),
        (.body, "Totals show in this currency. You can still add expenses in others."),
        (.buttonLarge, "Continue with Apple"),
        (.buttonSmall, "See all"),
        (.subheadline, "INR · Based on your region"),
        (.footnote, "We’ll send a 6-digit code."),
        (.caption1, "DUE FRI"),
        (.caption2, "AM"),
        (.wordmarkS, "Paybak"),
        (.wordmarkL, "Paybak"),
    ]

    var body: some View {
        GalleryPageScroll {
            ForEach(Self.samples, id: \.style.name) { style, sample in
                VStack(alignment: .leading, spacing: PBSpace.s4) {
                    GalleryLabel("\(style.name) · \(specs(style))")
                    Text(sample)
                        .textStyle(style)
                        .foregroundStyle(PBColor.textPrimary)
                        .background(PBColor.bgSelected)
                }
            }
        }
    }

    /// "SemiBold 16/22 · -0.25%".
    private func specs(_ style: PBTextStyle) -> String {
        let weight = switch style.face {
        case .regular: "Regular"
        case .medium: "Medium"
        case .semiBold: "SemiBold"
        case .bold: "Bold"
        case .extraBold: "ExtraBold"
        }
        let tracking = String(format: "%+.2f", Double(style.letterSpacing * 100))
        return "\(weight) \(Int(style.size))/\(Int(style.lineHeight)) · \(tracking)%"
    }
}

// MARK: - Spacing, radius, size, materials

struct GalleryLayoutPage: View {
    private static let spaces: [(String, CGFloat)] = [
        ("0", PBSpace.s0), ("2", PBSpace.s2), ("4", PBSpace.s4), ("6", PBSpace.s6), ("8", PBSpace.s8), ("12", PBSpace.s12),
        ("16", PBSpace.s16), ("20", PBSpace.s20), ("24", PBSpace.s24), ("28", PBSpace.s28), ("32", PBSpace.s32),
        ("40", PBSpace.s40), ("48", PBSpace.s48), ("64", PBSpace.s64), ("96", PBSpace.s96),
    ]
    private static let layouts: [(String, CGFloat)] = [
        ("screen-margin", PBLayout.screenMargin), ("card-padding", PBLayout.cardPadding), ("section-gap", PBLayout.sectionGap),
    ]
    private static let radii: [(String, CGFloat)] = [
        ("xs", PBRadius.xs), ("sm", PBRadius.sm), ("input", PBRadius.input), ("tile", PBRadius.tile),
        ("card", PBRadius.card), ("sheet", PBRadius.sheet),
    ]
    private static let sizes: [(String, CGFloat)] = [
        ("button-lg", PBSize.buttonLg), ("button-sm", PBSize.buttonSm), ("tap", PBSize.tap), ("icon-sm", PBSize.iconSm),
        ("icon-md", PBSize.iconMd), ("icon-lg", PBSize.iconLg), ("avatar-xs", PBSize.avatarXs), ("avatar-sm", PBSize.avatarSm),
        ("avatar-md", PBSize.avatarMd), ("avatar-lg", PBSize.avatarLg), ("tabbar", PBSize.tabbar), ("add-button", PBSize.addButton),
        ("hairline", PBSize.hairline),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("space/*") {
                ForEach(Self.spaces, id: \.0) { name, value in bar("space/\(name)", value) }
            }
            GallerySection("layout/*") {
                ForEach(Self.layouts, id: \.0) { name, value in bar("layout/\(name)", value) }
            }
            GallerySection("radius/*") {
                GalleryFlow(spacing: PBSpace.s12, rowSpacing: PBSpace.s12) {
                    ForEach(Self.radii, id: \.0) { name, radius in
                        VStack(alignment: .leading, spacing: PBSpace.s4) {
                            RoundedRectangle(cornerRadius: radius)
                                .fill(PBColor.bgCard)
                                .frame(width: 104, height: 64)
                            GalleryLabel("radius/\(name) · \(Int(radius))")
                        }
                    }
                    VStack(alignment: .leading, spacing: PBSpace.s4) {
                        Capsule()
                            .fill(PBColor.bgCard)
                            .frame(width: 104, height: 64)
                        GalleryLabel("radius/full")
                    }
                }
            }
            GallerySection("size/*") {
                ForEach(Self.sizes, id: \.0) { name, value in bar("size/\(name)", value) }
            }
            GallerySection("Materials") {
                materials
            }
        }
    }

    private func bar(_ name: String, _ value: CGFloat) -> some View {
        HStack(spacing: 0) {
            GalleryLabel("\(name) · \(Int(value))")
                .frame(width: 150, alignment: .leading)
            Rectangle()
                .fill(PBColor.bgInverse)
                .frame(width: value, height: 12)
        }
    }

    /// Each material over stripes, so the translucent fill, highlight and shadow show.
    private var materials: some View {
        ZStack {
            GalleryStripes()
            VStack(spacing: 0) {
                Spacer(minLength: 0)
                HStack(spacing: PBSpace.s16) {
                    GalleryLabel("Glass")
                        .frame(width: 150, height: 62)
                        .pbMaterial(.glass, in: .capsule)
                    GalleryLabel("Small")
                        .frame(width: 44, height: 44)
                        .pbMaterial(.glassSmall, in: .circle)
                }
                Spacer(minLength: 0)
                HStack(spacing: PBSpace.s16) {
                    GalleryLabel("Frosted")
                        .frame(width: 150, height: 62)
                        .pbMaterial(.frosted, in: .rect(cornerRadius: PBRadius.card))
                    GalleryLabel("Scrim")
                        .frame(width: 150, height: 62)
                        .background(PBColor.bgScrim)
                }
                Spacer(minLength: 0)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 220)
    }
}
#endif
