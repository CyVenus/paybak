import SwiftUI

/// Row / Receipt Line (Figma 117:993): one line of a scanned receipt, 44 pt inside a #F5F5F5 card
/// with 16 pt side padding: the label on the left, the amount on the right, both Body (Headline for
/// the Total line, which has a hairline above it). While `isEditing`, the amount is an inline white
/// field so a misread value can be fixed in place; tapping the amount starts editing.
struct PBReceiptLineRow: View {
    let label: String
    /// The amount without the currency symbol, as typed.
    @Binding var amount: String
    var currency = "INR"
    var isTotal = false
    @Binding var isEditing: Bool

    @FocusState private var isFocused: Bool

    private var style: PBTextStyle { isTotal ? .headline : .body }
    /// "₹2,000", "AED 1,800", as `Money.format` writes amounts.
    private var atRest: String { PBAmountField.prefix(currency) + amount }

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            Text(label)
                .textStyle(style)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            if isEditing {
                PBInlineField(text: $amount, accessibilityLabel: label, currency: currency, style: style, focus: $isFocused)
                    .onChange(of: isFocused) { if !isFocused { isEditing = false } }
            } else {
                Button { isEditing = true } label: {
                    Text(atRest)
                        .textStyle(style)
                        .foregroundStyle(PBColor.textPrimary)
                        .frame(minHeight: PBSize.tap)
                        .contentShape(.rect)
                }
                .buttonStyle(PBDimButtonStyle())
                .accessibilityLabel(label)
                .accessibilityValue(atRest)
                .accessibilityHint("Edit the amount")
            }
        }
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: PBSize.tap)
        // Tapping the amount focuses the new field; leaving it ends editing.
        .onChange(of: isEditing) { if isEditing { isFocused = true } }
        .overlay(alignment: .top) {
            if isTotal {
                PBDivider().padding(.horizontal, PBSpace.s16)
            }
        }
    }
}

#Preview("PBReceiptLineRow") {
    @Previewable @State var biryani = "430"
    @Previewable @State var total = "2,300"
    @Previewable @State var editing = false
    @Previewable @State var editingTotal = true
    // Shown with its ring and no keyboard; tap an amount to edit it for real.
    VStack(spacing: 0) {
        PBReceiptLineRow(label: "Chicken biryani", amount: $biryani, isEditing: $editing)
        PBReceiptLineRow(label: "Paneer tikka", amount: .constant("370"), isEditing: .constant(false))
        PBReceiptLineRow(label: "Total", amount: $total, isTotal: true, isEditing: $editingTotal)
            .pbPreviewInteraction(.focused)
    }
    .padding(.vertical, PBSpace.s8)
    .pbCard(padding: 0)
    .padding(PBLayout.screenMargin)
}
