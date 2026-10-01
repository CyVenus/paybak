import SwiftUI

/// The due-date quick chips under a Due row (add-expense §4.5, record-lend-group §4.4): Tomorrow ·
/// This weekend · Next week · Pick date, white on the #F5F5F5 card, black when chosen. They wrap
/// onto two rows and line up with the row titles (52 pt inset). Tapping the chosen chip again
/// clears the date. The chips are silent: Add expense ticks in its `onSelect` (Lend money doesn't), as
/// on Android. Test ids: `<prefix>.tomorrow|weekend|nextWeek|pick`.
struct PBDueChips: View {
    let selected: QuickDue?
    var testIDPrefix: String
    let onSelect: (QuickDue?) -> Void
    let onPickDate: () -> Void

    var body: some View {
        PBFlowLayout {
            ForEach(QuickDue.allCases, id: \.self) { chip in
                chipButton(chip.title, isSelected: chip == selected) {
                    onSelect(chip == selected ? nil : chip)
                }
                .accessibilityIdentifier("\(testIDPrefix).\(chip.rawValue)")
            }
            chipButton("Pick date", isSelected: false, action: onPickDate)
                .accessibilityIdentifier("\(testIDPrefix).pick")
        }
        .padding(.leading, 52)
        .padding(.trailing, PBSpace.s16)
        .padding(.bottom, PBSpace.s12)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func chipButton(_ title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .textStyle(.buttonSmall)
                .lineLimit(1)
                .padding(.horizontal, PBSpace.s16)
        }
        .buttonStyle(OnCardChipStyle(isSelected: isSelected))
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

/// Category Chip on a #F5F5F5 card: white, or black when selected; 44 pt hit area.
private struct OnCardChipStyle: ButtonStyle {
    let isSelected: Bool

    func makeBody(configuration: Configuration) -> some View {
        let fill: Color = switch (isSelected, configuration.isPressed) {
        case (false, false): PBColor.bgPrimary
        case (false, true): PBColor.bgCardPressed
        case (true, false): PBColor.bgInverse
        case (true, true): PBColor.bgInversePressed
        }
        configuration.label
            .foregroundStyle(isSelected ? PBColor.textInverse : PBColor.textPrimary)
            .frame(height: PBSize.buttonSm)
            .background(fill, in: .capsule)
            .padding(.vertical, (PBSize.tap - PBSize.buttonSm) / 2)
            .contentShape(.capsule)
            .padding(.vertical, -(PBSize.tap - PBSize.buttonSm) / 2)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
    }
}

#Preview("PBDueChips") {
    @Previewable @State var selected: QuickDue? = .weekend
    PBDueChips(selected: selected, testIDPrefix: "preview", onSelect: { selected = $0 }, onPickDate: {})
        .frame(width: 362)
        .pbCard(padding: 0)
        .padding(PBLayout.screenMargin)
}
