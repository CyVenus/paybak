import SwiftUI

/// The small white value field inside #F5F5F5 cards: Row / Split Person (Exact, Percent, Shares) and
/// Row / Receipt Line (Editing). 36 pt tall, 14 pt corners, 12 pt side padding, at least `minWidth`
/// wide and growing with longer values. Focused = 1.5 pt `border/strong` inside ring and the 2 × 20
/// caret after the value. Dimmed shows the value in `text/tertiary` and can't be edited (an excluded
/// person). An amount names its `currency`, which puts the symbol before the typed number and groups
/// its digits ("₹2,800" for "2800"); `suffix` ("%") follows it. Neither is part of the text.
///
/// Like the code field, the number is typed into a hidden text field and drawn as text, so the
/// value, caret and ring sit exactly where Figma puts them.
struct PBInlineField: View {
    @Binding var text: String
    /// Spoken by VoiceOver, e.g. "Priya's share".
    let accessibilityLabel: String
    var currency: String?
    var suffix: String?
    var style: PBTextStyle = .headline
    var alignment: Alignment = .trailing
    var minWidth: CGFloat = 96
    var keyboard: UIKeyboardType = .decimalPad
    var isDimmed = false

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    init(
        text: Binding<String>,
        accessibilityLabel: String,
        currency: String? = nil,
        suffix: String? = nil,
        style: PBTextStyle = .headline,
        alignment: Alignment = .trailing,
        minWidth: CGFloat = 96,
        keyboard: UIKeyboardType = .decimalPad,
        isDimmed: Bool = false,
        focus: FocusState<Bool>.Binding? = nil
    ) {
        _text = text
        self.accessibilityLabel = accessibilityLabel
        self.currency = currency
        self.suffix = suffix
        self.style = style
        self.alignment = alignment
        self.minWidth = minWidth
        self.keyboard = keyboard
        self.isDimmed = isDimmed
        self.externalFocus = focus
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var isFocused: Bool { !isDimmed && (focus.wrappedValue || previewInteraction == .focused) }
    private var display: String {
        let number = text.isEmpty ? "0" : text
        guard let currency else { return number + (suffix ?? "") }
        return Money.info(currency).symbol + MoneyInput.grouped(number, currency: currency) + (suffix ?? "")
    }

    var body: some View {
        HStack(spacing: PBSpace.s2) {
            Text(display)
                .textStyle(style)
                .foregroundStyle(isDimmed || text.isEmpty ? PBColor.textTertiary : PBColor.textPrimary)
                .lineLimit(1)
            if isFocused {
                Rectangle()
                    .fill(PBColor.textPrimary)
                    .frame(width: 2, height: 20)
            }
        }
        .frame(minWidth: minWidth - 2 * PBSpace.s12, alignment: alignment)
        .padding(.horizontal, PBSpace.s12)
        .frame(height: PBSize.buttonSm)
        .background {
            TextField("", text: $text)
                .keyboardType(keyboard)
                .focused(focus)
                .disabled(isDimmed)
                .opacity(0)
                .accessibilityHidden(true)
        }
        .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.input))
        .overlay {
            if isFocused {
                RoundedRectangle(cornerRadius: PBRadius.input)
                    .strokeBorder(PBColor.borderStrong, lineWidth: 1.5)
            }
        }
        .contentShape(.rect)
        .onTapGesture { if !isDimmed { focus.wrappedValue = true } }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityValue(display)
        .accessibilityAddTraits(isDimmed ? [] : .isButton)
        .accessibilityAction { if !isDimmed { focus.wrappedValue = true } }
    }
}

#Preview("PBInlineField") {
    @Previewable @State var amount = "2800"
    @Previewable @State var shares = "1"
    VStack(spacing: PBSpace.s12) {
        PBInlineField(text: $amount, accessibilityLabel: "Priya's share", currency: "INR")
        PBInlineField(text: $amount, accessibilityLabel: "Priya's share", currency: "INR").pbPreviewInteraction(.focused)
        PBInlineField(text: .constant("25"), accessibilityLabel: "Priya's percent", suffix: "%")
        PBInlineField(text: $shares, accessibilityLabel: "Shares", alignment: .center, minWidth: 48, keyboard: .numberPad)
        PBInlineField(text: .constant("0"), accessibilityLabel: "Priya's share", currency: "INR", isDimmed: true)
    }
    .padding(PBSpace.s24)
    .background(PBColor.bgCard)
}
