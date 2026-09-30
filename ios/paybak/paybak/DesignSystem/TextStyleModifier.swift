import CoreText
import SwiftUI

extension View {
    /// Styles text like the Figma text style: Manrope face and size, letter spacing, and a line box
    /// of exactly the Figma line height with the glyphs placed where Figma places them.
    /// Apply it to `Text` (not to stacks that contain other views).
    func textStyle(_ style: PBTextStyle) -> some View {
        modifier(TextStyleModifier(style: style))
    }
}

/// How Figma text metrics are reproduced.
///
/// Figma gives every line a box of exactly `lineHeight` and centres the font's ascender + descender
/// inside it (half the extra space above, half below; negative when the line height is tighter than
/// the font). A multi-line text box is `lines × lineHeight` tall.
///
/// SwiftUI's `lineSpacing` can only add space (negative values are ignored), and many Paybak styles
/// are tighter than Manrope's natural 1.366 em line (Title/1 is 38 pt vs 43.7 pt). So:
/// 1. `.lineHeight(.exact(points:))` makes every line exactly the Figma line height, and the text
///    frame `lines × lineHeight`.
/// 2. That API puts the first baseline at its own position, not Figma's, so `FigmaLineBox` moves the
///    text (without changing its frame) until the first baseline sits at
///    `(lineHeight − (ascender − descender)) / 2 + ascender`, Figma's half-leading position.
/// 3. `.tracking` applies the letter spacing (a fraction of the size).
///
/// Size, line height and letter spacing scale together with Dynamic Type through `@ScaledMetric`,
/// so text matches Figma exactly at the default size.
private struct TextStyleModifier: ViewModifier {
    let style: PBTextStyle
    @ScaledMetric private var size: CGFloat

    init(style: PBTextStyle) {
        self.style = style
        _size = ScaledMetric(wrappedValue: style.size, relativeTo: style.dynamicTypeStyle)
    }

    func body(content: Content) -> some View {
        let lineHeight = style.lineHeight * size / style.size
        let metrics = style.face.uiFont(size: size)
        let glyphHeight = metrics.ascender - metrics.descender
        FigmaLineBox(firstBaseline: (lineHeight - glyphHeight) / 2 + metrics.ascender) {
            content
                .font(.custom(style.face.postScriptName, fixedSize: size))
                .tracking(size * style.letterSpacing)
                .lineHeight(.exact(points: lineHeight))
        }
    }
}

/// Keeps its single subview's size and shifts it vertically so its first text baseline lands at
/// `firstBaseline` from the top. Text baseline alignment guides report the shifted positions.
private struct FigmaLineBox: Layout {
    let firstBaseline: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        subviews.first?.sizeThatFits(proposal) ?? .zero
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        guard let text = subviews.first else { return }
        let offset = baselineOffset(of: text, proposal: proposal)
        text.place(at: CGPoint(x: bounds.minX, y: bounds.minY + offset), proposal: proposal)
    }

    func explicitAlignment(
        of guide: VerticalAlignment,
        in bounds: CGRect,
        proposal: ProposedViewSize,
        subviews: Subviews,
        cache: inout ()
    ) -> CGFloat? {
        guard let text = subviews.first else { return nil }
        let offset = baselineOffset(of: text, proposal: proposal)
        switch guide {
        case .firstTextBaseline:
            return bounds.minY + firstBaseline
        case .lastTextBaseline:
            return bounds.minY + offset + text.dimensions(in: proposal)[.lastTextBaseline]
        default:
            return nil
        }
    }

    private func baselineOffset(of text: LayoutSubview, proposal: ProposedViewSize) -> CGFloat {
        firstBaseline - text.dimensions(in: proposal)[.firstTextBaseline]
    }
}
