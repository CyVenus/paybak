import SwiftUI

/// Row / Comment (Figma 116:1007): one comment on an expense. A 32 pt avatar, the Headline name with
/// the Footnote date on its baseline, and the Body text that wraps. 8 pt top and bottom padding;
/// stack rows with no gap and no divider.
struct PBCommentRow: View {
    let name: String
    let avatar: PBAvatar.Content
    let date: String
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: PBSpace.s12) {
            PBAvatar(avatar, diameter: PBSize.avatarSm)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                HStack(alignment: .firstTextBaseline, spacing: PBSpace.s8) {
                    Text(name)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                    // A long name truncates; the date keeps its width.
                    Text(date)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                        .lineLimit(1)
                        .layoutPriority(1)
                }
                Text(text)
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, PBSpace.s8)
        .accessibilityElement(children: .combine)
    }
}

#Preview("PBCommentRow") {
    VStack(spacing: 0) {
        PBCommentRow(name: "Priya", avatar: .art(.priya), date: "27 Sep", text: "Was breakfast included?")
        PBCommentRow(name: "Kabir", avatar: .art(.kabir), date: "28 Sep", text: "Yes, and the late checkout too. I added the receipt photo.")
    }
    .padding(PBLayout.screenMargin)
}
