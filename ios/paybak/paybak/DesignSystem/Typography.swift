import SwiftUI

/// A Figma text style: face, size, line height and letter spacing, exactly as in tokens.md.
/// Apply it to a `Text` with `.textStyle(_:)`; see `TextStyleModifier` for how the line box is matched.
struct PBTextStyle: Hashable {
    /// The Figma style name, e.g. "Title/1".
    let name: String
    let face: PBFont
    let size: CGFloat
    /// Figma line height in pt.
    let lineHeight: CGFloat
    /// Figma letter spacing as a fraction of the font size (−2 % → −0.02; Title/1: 32 × −0.02 = −0.64 pt).
    let letterSpacing: CGFloat
    /// The Dynamic Type style this one scales with. Figma sizes are the default (Large) size.
    let dynamicTypeStyle: Font.TextStyle

    /// For text inputs, which lay out their own single line: the face at the style's size,
    /// scaled with Dynamic Type.
    var font: Font {
        .custom(face.postScriptName, size: size, relativeTo: dynamicTypeStyle)
    }
}

extension PBTextStyle {
    static let title1 = PBTextStyle(name: "Title/1", face: .extraBold, size: 32, lineHeight: 38, letterSpacing: -0.02, dynamicTypeStyle: .largeTitle)
    static let title2 = PBTextStyle(name: "Title/2", face: .bold, size: 24, lineHeight: 30, letterSpacing: -0.015, dynamicTypeStyle: .title)
    static let title3 = PBTextStyle(name: "Title/3", face: .bold, size: 20, lineHeight: 26, letterSpacing: -0.01, dynamicTypeStyle: .title3)
    static let amountDisplay = PBTextStyle(name: "Amount/Display", face: .extraBold, size: 56, lineHeight: 64, letterSpacing: -0.02, dynamicTypeStyle: .largeTitle)
    static let amountLarge = PBTextStyle(name: "Amount/Large", face: .extraBold, size: 26, lineHeight: 32, letterSpacing: -0.02, dynamicTypeStyle: .title)
    static let amountMedium = PBTextStyle(name: "Amount/Medium", face: .bold, size: 17, lineHeight: 22, letterSpacing: -0.005, dynamicTypeStyle: .headline)
    static let headline = PBTextStyle(name: "Headline", face: .semiBold, size: 16, lineHeight: 22, letterSpacing: -0.0025, dynamicTypeStyle: .headline)
    static let body = PBTextStyle(name: "Body", face: .regular, size: 16, lineHeight: 24, letterSpacing: 0, dynamicTypeStyle: .body)
    static let buttonLarge = PBTextStyle(name: "Button/Large", face: .semiBold, size: 17, lineHeight: 22, letterSpacing: -0.005, dynamicTypeStyle: .body)
    static let buttonSmall = PBTextStyle(name: "Button/Small", face: .semiBold, size: 15, lineHeight: 20, letterSpacing: -0.0025, dynamicTypeStyle: .subheadline)
    static let subheadline = PBTextStyle(name: "Subheadline", face: .medium, size: 14, lineHeight: 20, letterSpacing: 0, dynamicTypeStyle: .subheadline)
    static let footnote = PBTextStyle(name: "Footnote", face: .medium, size: 13, lineHeight: 18, letterSpacing: 0, dynamicTypeStyle: .footnote)
    static let caption1 = PBTextStyle(name: "Caption/1", face: .bold, size: 12, lineHeight: 16, letterSpacing: 0.01, dynamicTypeStyle: .caption)
    static let caption2 = PBTextStyle(name: "Caption/2", face: .semiBold, size: 11, lineHeight: 13, letterSpacing: 0.01, dynamicTypeStyle: .caption2)
    static let wordmarkS = PBTextStyle(name: "Brand/Wordmark S", face: .extraBold, size: 20, lineHeight: 24, letterSpacing: -0.03, dynamicTypeStyle: .title3)
    static let wordmarkL = PBTextStyle(name: "Brand/Wordmark L", face: .extraBold, size: 40, lineHeight: 44, letterSpacing: -0.03, dynamicTypeStyle: .largeTitle)

    /// Every style, in the order of tokens.md.
    static let all: [PBTextStyle] = [
        .title1, .title2, .title3, .amountDisplay, .amountLarge, .amountMedium, .headline, .body,
        .buttonLarge, .buttonSmall, .subheadline, .footnote, .caption1, .caption2, .wordmarkS, .wordmarkL,
    ]
}
