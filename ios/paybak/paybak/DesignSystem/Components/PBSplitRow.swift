import SwiftUI

/// Row / Split Person (Figma 126:1596): one person in the split editor, 64 pt inside a #F5F5F5 card.
/// The select circle includes or excludes them, then a white 32 pt avatar, the name and the value:
/// - Equally: the computed amount on the right; tapping the row toggles inclusion.
/// - Exact: a 96 pt field with the amount.
/// - Percent: the field shows the percentage; the amount is the Footnote under the name.
/// - Shares: a 48 pt field with the share count plus the system stepper; amount under the name.
/// Tapping the row anywhere outside the field and stepper includes or excludes the person (pressed:
/// `bg/card-pressed`). Excluded people show ₹0 / 0% / 0 in gray and can't be edited; a tap on their
/// field includes them again. The divider starts at the name.
/// Pass `focus` to focus the value field from code (a start state, dismissing on a tap outside).
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
    /// The currency of the Exact field and of excluded amounts.
    var currency = "INR"
    @Binding var isIncluded: Bool
    var showsDivider = true
    var focus: FocusState<Bool>.Binding?

    @State private var isPressed = false
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            Button { isIncluded.toggle() } label: {
                HStack(spacing: PBSpace.s12) {
                    PBSelectCircle(isOn: isIncluded)
                    PBAvatar(avatar, diameter: PBSize.avatarSm, isOnCard: true)
                    names
                    if isEqually {
                        trailing
                    }
                }
                .padding(.vertical, PBSpace.s8)
                .padding(.leading, PBSpace.s16)
                .padding(.trailing, isEqually ? PBSpace.s16 : 0)
                .frame(maxWidth: .infinity, minHeight: 64, alignment: .leading)
                .contentShape(.rect)
            }
            .buttonStyle(PressReportingStyle(isPressed: $isPressed))
            .accessibilityElement(children: .combine)
            .accessibilityValue(isIncluded ? "Included" : "Excluded")
            .accessibilityAddTraits(isIncluded ? .isSelected : [])
            if !isEqually {
                // The field and the stepper take their own taps; an excluded person's don't, so a
                // tap there includes them again, like the rest of the row.
                trailing
                    .allowsHitTesting(isIncluded)
                    .padding(.trailing, PBSpace.s16)
                    .frame(minHeight: 64)
                    .contentShape(.rect)
                    .gesture(TapGesture().onEnded { isIncluded = true }, isEnabled: !isIncluded)
            }
        }
        .background {
            PBColor.bgCardPressed
                .opacity(isPressed || previewInteraction == .pressed ? 1 : 0)
                .animation(.easeOut(duration: 0.1), value: isPressed)
        }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider().padding(.leading, 96)
            }
        }
    }

    private var names: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(name)
                .textStyle(.headline)
                .foregroundStyle(isIncluded ? PBColor.textPrimary : PBColor.textTertiary)
                .lineLimit(1)
            if showsAmountUnderName {
                Text(isIncluded ? amount : zero)
                    .textStyle(.footnote)
                    .foregroundStyle(isIncluded ? PBColor.textSecondary : PBColor.textTertiary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var isEqually: Bool {
        if case .equally = mode { return true }
        return false
    }

    /// An excluded person's share: "₹0" ("AED 0").
    private var zero: String { PBAmountField.prefix(currency) + "0" }

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
            Text(isIncluded ? amount : zero)
                .textStyle(.headline)
                .foregroundStyle(isIncluded ? PBColor.textPrimary : PBColor.textTertiary)
        case .exact(let value):
            PBInlineField(
                text: isIncluded ? value : .constant("0"),
                accessibilityLabel: "\(name)’s amount",
                currency: currency,
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

/// Reports the press so the whole row, field included, takes the pressed fill.
private struct PressReportingStyle: ButtonStyle {
    @Binding var isPressed: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .onChange(of: configuration.isPressed, initial: true) { _, pressed in
                isPressed = pressed
            }
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
