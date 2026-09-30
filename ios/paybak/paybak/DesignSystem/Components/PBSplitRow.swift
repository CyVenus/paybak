import SwiftUI

/// Row / Split Person (Figma 126:1596): one person in the split editor, 64 pt inside a #F5F5F5 card.
/// The select circle includes or excludes them, then a white 32 pt avatar, the name and the value:
/// - Equally: the computed amount on the right; tapping the row toggles inclusion.
/// - Exact: a 96 pt field with the amount.
/// - Percent: the field shows the percentage; the amount is the Footnote under the name.
/// - Shares: a 48 pt field with the share count plus the system stepper; amount under the name.
/// Excluded people show ₹0 / 0% / 0 in gray and can't be edited. The divider starts at the name.
/// Pass `focus` to focus the value field from code (the editor's Next button, a start state).
struct PBSplitRow: View {
    enum Mode {
        case equally
        /// The amount as typed, without the currency symbol.
        case exact(Binding<String>)
        /// The percentage as typed, without "%".
        case percent(Binding<String>)
        case shares(Binding<Int>)
    }

    let name: String
    let avatar: PBAvatar.Content
    let mode: Mode
    /// The person's share as display text ("₹700").
    let amount: String
    /// The currency symbol shown in the Exact field and for excluded amounts.
    var currencySymbol = "₹"
    @Binding var isIncluded: Bool
    var showsDivider = true
    var focus: FocusState<Bool>.Binding?

    var body: some View {
        Group {
            if isEqually {
                Button { isIncluded.toggle() } label: { row }
                    .buttonStyle(PBRowButtonStyle(surface: .white))
                    .accessibilityElement(children: .combine)
                    .accessibilityValue(isIncluded ? "Included" : "Excluded")
                    .accessibilityAddTraits(isIncluded ? .isSelected : [])
            } else {
                row
            }
        }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider().padding(.leading, 96)
            }
        }
    }

    private var row: some View {
        HStack(spacing: PBSpace.s12) {
            if isEqually {
                PBSelectCircle(isOn: isIncluded)
            } else {
                selectButton
            }
            PBAvatar(avatar, diameter: PBSize.avatarSm, isOnCard: true)
            VStack(alignment: .leading, spacing: 0) {
                Text(name)
                    .textStyle(.headline)
                    .foregroundStyle(isIncluded ? PBColor.textPrimary : PBColor.textTertiary)
                    .lineLimit(1)
                if showsAmountUnderName {
                    Text(isIncluded ? amount : currencySymbol + "0")
                        .textStyle(.footnote)
                        .foregroundStyle(isIncluded ? PBColor.textSecondary : PBColor.textTertiary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityElement(children: .combine)
            trailing
        }
        .padding(.vertical, PBSpace.s8)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: 64)
        .contentShape(.rect)
    }

    /// In the other modes the field takes taps, so only the circle toggles inclusion.
    private var selectButton: some View {
        Button { isIncluded.toggle() } label: {
            PBSelectCircle(isOn: isIncluded)
                .frame(width: PBSize.tap, height: PBSize.tap)
                .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .padding(.horizontal, -(PBSize.tap - PBSize.iconLg) / 2)
        .accessibilityLabel(name)
        .accessibilityValue(isIncluded ? "Included" : "Excluded")
        .accessibilityAddTraits(isIncluded ? .isSelected : [])
    }

    private var isEqually: Bool {
        if case .equally = mode { return true }
        return false
    }

    private var showsAmountUnderName: Bool {
        switch mode {
        case .percent, .shares: true
        case .equally, .exact: false
        }
    }

    @ViewBuilder
    private var trailing: some View {
        switch mode {
        case .equally:
            Text(isIncluded ? amount : currencySymbol + "0")
                .textStyle(.headline)
                .foregroundStyle(isIncluded ? PBColor.textPrimary : PBColor.textTertiary)
        case .exact(let value):
            PBInlineField(
                text: isIncluded ? value : .constant("0"),
                accessibilityLabel: "\(name)’s amount",
                prefix: currencySymbol,
                isDimmed: !isIncluded,
                focus: focus
            )
        case .percent(let value):
            PBInlineField(
                text: isIncluded ? value : .constant("0"),
                accessibilityLabel: "\(name)’s percentage",
                suffix: "%",
                isDimmed: !isIncluded,
                focus: focus
            )
        case .shares(let count):
            HStack(spacing: PBSpace.s8) {
                PBInlineField(
                    text: isIncluded ? sharesText(count) : .constant("0"),
                    accessibilityLabel: "\(name)’s shares",
                    alignment: .center,
                    minWidth: 48,
                    keyboard: .numberPad,
                    isDimmed: !isIncluded,
                    focus: focus
                )
                Stepper("\(name)’s shares", value: count, in: 0...99)
                    .labelsHidden()
                    .disabled(!isIncluded)
                    .opacity(isIncluded ? 1 : 0.4)
            }
        }
    }

    private func sharesText(_ count: Binding<Int>) -> Binding<String> {
        Binding(
            get: { String(count.wrappedValue) },
            set: { count.wrappedValue = min(99, Int($0.filter(\.isWholeNumber)) ?? 0) }
        )
    }
}

#Preview("PBSplitRow") {
    @Previewable @State var included = true
    @Previewable @State var exact = "700"
    @Previewable @State var percent = "25"
    @Previewable @State var shares = 1
    VStack(spacing: 0) {
        PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .equally, amount: "₹700", isIncluded: $included)
        PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .exact($exact), amount: "₹700", isIncluded: $included)
        PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .percent($percent), amount: "₹700", isIncluded: $included)
        PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .shares($shares), amount: "₹700", isIncluded: $included, showsDivider: false)
    }
    .pbCard(padding: 0)
    .padding(PBLayout.screenMargin)
}
