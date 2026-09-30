import SwiftUI

/// Text that wraps where Figma wraps it. iOS moves a word down rather than leave one word alone on
/// the last line; Figma doesn't, so copy that ends with a lone word in Figma is written with `\n`
/// at Figma's break. Where that line doesn't fit (narrow screens, larger text), the copy wraps
/// naturally instead. Colour it with `.foregroundStyle` as usual.
struct FigmaWrappedText: View {
    /// The copy, with `\n` where Figma breaks the line.
    let text: String
    let style: PBTextStyle

    init(_ text: String, style: PBTextStyle) {
        self.text = text
        self.style = style
    }

    var body: some View {
        ViewThatFits(in: .horizontal) {
            Text(text)
                .textStyle(style)
            Text(text.replacing("\n", with: " "))
                .textStyle(style)
        }
    }
}

#Preview("FigmaWrappedText") {
    VStack(alignment: .leading, spacing: PBSpace.s24) {
        FigmaWrappedText("Friends see your name and picture on shared\nexpenses.", style: .body)
        FigmaWrappedText("Friends see your name and picture on shared\nexpenses.", style: .body)
            .frame(width: 300, alignment: .leading)
    }
    .foregroundStyle(PBColor.textSecondary)
    .padding(PBLayout.screenMargin)
}
