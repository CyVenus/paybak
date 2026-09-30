import SwiftUI

/// Card / Split Total (Figma 125:1170): the live footer of the split editor, a 56 pt #F5F5F5 bar.
/// "₹0 left" in gray on the left, "₹2,800 of ₹2,800" right-aligned. Error (something is left over
/// or over-assigned) adds the alert icon and turns the left text red; the screen keeps Done disabled.
struct PBSplitTotalBar: View {
    /// "₹0 left", "₹150 left", "₹150 over".
    let left: String
    /// "₹2,650 of ₹2,800".
    let detail: String
    var isError = false

    var body: some View {
        HStack(spacing: PBSpace.s8) {
            if isError {
                PBIconView(.alert, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconDestructive)
            }
            Text(left)
                .textStyle(.amountMedium)
                .foregroundStyle(isError ? PBColor.textDestructive : PBColor.textSecondary)
                .lineLimit(1)
            Text(detail)
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(.horizontal, PBLayout.cardPadding)
        .frame(height: 56)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .combine)
    }
}

#Preview("PBSplitTotalBar") {
    VStack(spacing: PBSpace.s24) {
        PBSplitTotalBar(left: "₹0 left", detail: "₹2,800 of ₹2,800")
        PBSplitTotalBar(left: "₹150 left", detail: "₹2,650 of ₹2,800", isError: true)
    }
    .padding(PBLayout.screenMargin)
}
