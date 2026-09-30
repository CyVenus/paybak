import SwiftUI

/// Row / History (Figma 116:1058): one entry of an expense's edit history, newest first. An 8 pt gray
/// dot with a 1 pt line down to the next entry, then the Subheadline text (wraps) and the Footnote
/// date. The last entry has no line and no bottom padding. Stack rows with no gap.
struct PBHistoryRow: View {
    let text: String
    let date: String
    var isLast = false

    var body: some View {
        HStack(alignment: .top, spacing: PBSpace.s12) {
            VStack(spacing: PBSpace.s4) {
                Circle()
                    .fill(PBColor.iconTertiary)
                    .frame(width: 8, height: 8)
                if !isLast {
                    Rectangle()
                        .fill(PBColor.borderSubtle)
                        .frame(width: PBSize.hairline)
                        .frame(maxHeight: .infinity)
                }
            }
            .padding(.top, 6)
            .frame(width: 8)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(text)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)
                Text(date)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
            }
            .padding(.bottom, isLast ? 0 : PBSpace.s16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .accessibilityElement(children: .combine)
    }
}

#Preview("PBHistoryRow") {
    VStack(spacing: 0) {
        PBHistoryRow(text: "Kabir changed the amount from ₹17,500 to ₹18,000", date: "28 Sep")
        PBHistoryRow(text: "Kabir added this", date: "21 Sep", isLast: true)
    }
    .padding(PBLayout.screenMargin)
}
