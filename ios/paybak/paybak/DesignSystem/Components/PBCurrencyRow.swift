import SwiftUI

/// Row / Currency (Figma 37:673): a 56 pt row with a 40 pt symbol tile, name and code, and a radio.
/// The whole row is the tap target; selection is single-choice across the list.
struct PBCurrencyRow: View {
    /// "₹", "$", "S$"… Symbols longer than two characters are shown as the ISO code in Caption/1
    /// (the AED row), so pass the code for those.
    let symbol: String
    let title: String
    let subtitle: String
    let isSelected: Bool
    let action: () -> Void

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        Button(action: action) {
            HStack(spacing: PBSpace.s12) {
                Text(symbol)
                    .textStyle(symbol.count <= 2 ? .headline : .caption1)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
                    .frame(width: PBSize.avatarMd, height: PBSize.avatarMd)
                    .background(PBColor.bgCard, in: .circle)
                VStack(alignment: .leading, spacing: PBSpace.s2) {
                    Text(title)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(subtitle)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                PBRadio(isSelected: isSelected)
            }
            .frame(height: 56)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .animation(reduceMotion ? nil : .easeOut(duration: 0.15), value: isSelected)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(title), \(subtitle)")
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
    }
}

/// The 22 pt radio of Row / Currency: black with a white 14 pt check when selected, otherwise a
/// 1.5 pt `bg/indicator` ring.
private struct PBRadio: View {
    let isSelected: Bool

    var body: some View {
        ZStack {
            if isSelected {
                Circle().fill(PBColor.bgInverse)
                PBIconView(.check, size: 14)
                    .foregroundStyle(PBColor.iconInverse)
            } else {
                Circle().strokeBorder(PBColor.bgIndicator, lineWidth: 1.5)
            }
        }
        .frame(width: 22, height: 22)
    }
}

#Preview("PBCurrencyRow") {
    @Previewable @State var selection = "INR"
    VStack(spacing: 0) {
        PBCurrencyRow(symbol: "₹", title: "Indian Rupee", subtitle: "INR · Based on your region", isSelected: selection == "INR") { selection = "INR" }
        PBCurrencyRow(symbol: "$", title: "US Dollar", subtitle: "USD", isSelected: selection == "USD") { selection = "USD" }
        PBCurrencyRow(symbol: "AED", title: "UAE Dirham", subtitle: "AED", isSelected: selection == "AED") { selection = "AED" }
        PBCurrencyRow(symbol: "S$", title: "Singapore Dollar", subtitle: "SGD", isSelected: selection == "SGD") { selection = "SGD" }
    }
    .padding(PBLayout.screenMargin)
}
