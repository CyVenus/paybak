import SwiftUI

/// Control / Code Input (Figma 36:702): six Code Digit boxes backed by one hidden number-pad text
/// field with the one-time-code content type, so SMS codes autofill. Typing fills left to right, the
/// next empty box shows the caret, Backspace clears the last digit, and tapping the row focuses it.
/// `onComplete` runs when the sixth digit is entered; the screen decides whether the code is right.
/// In the error state, typing a digit starts over from the first box.
///
/// The row fills the width with space-between (362 pt → 14.8 pt gaps, boxes at 0, 62.8, …, 314).
/// On narrow screens the boxes shrink to keep a 12 pt minimum gap.
struct PBCodeField: View {
    static let length = 6

    @Binding var code: String
    /// Error state: red rings on all six boxes.
    var isError = false
    var onComplete: (String) -> Void = { _ in }

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    /// - Parameter focus: Pass the screen's focus binding to focus the field on appear.
    init(
        code: Binding<String>,
        isError: Bool = false,
        focus: FocusState<Bool>.Binding? = nil,
        onComplete: @escaping (String) -> Void = { _ in }
    ) {
        _code = code
        self.isError = isError
        self.externalFocus = focus
        self.onComplete = onComplete
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var isFocused: Bool { focus.wrappedValue || previewInteraction == .focused }

    var body: some View {
        ZStack {
            TextField("", text: $code)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused(focus)
                .opacity(0)
                .accessibilityHidden(true)
            CodeRowLayout {
                ForEach(0..<Self.length, id: \.self) { index in
                    PBCodeDigit(digit: digit(at: index), state: state(at: index))
                }
            }
        }
        .contentShape(.rect)
        .onTapGesture { focus.wrappedValue = true }
        .onChange(of: code) { oldValue, newValue in
            var digits = newValue.filter(\.isWholeNumber)
            if isError, newValue.count > oldValue.count, newValue.hasPrefix(oldValue) {
                // Typing after a wrong code starts over with what was just typed.
                digits = String(newValue.dropFirst(oldValue.count)).filter(\.isWholeNumber)
            }
            digits = String(digits.prefix(Self.length))
            if digits != newValue {
                code = digits
            } else if digits.count == Self.length {
                onComplete(digits)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Verification code, \(Self.length) digits")
        .accessibilityValue(code.isEmpty ? "Empty" : code.map(String.init).joined(separator: " "))
        .accessibilityAddTraits(.isButton)
        .accessibilityAction { focus.wrappedValue = true }
    }

    private func digit(at index: Int) -> Character? {
        index < code.count ? code[code.index(code.startIndex, offsetBy: index)] : nil
    }

    private func state(at index: Int) -> PBCodeDigit.DigitState {
        if isError { return .error }
        if index < code.count { return .filled }
        if index == code.count && isFocused { return .focused }
        return .empty
    }
}

/// Spreads equal boxes across the proposed width: at most 48 pt wide, at least 12 pt apart.
private struct CodeRowLayout: Layout {
    private let maxBoxWidth: CGFloat = 48
    private let minGap: CGFloat = PBSpace.s12
    private let height: CGFloat = 56

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let count = CGFloat(subviews.count)
        let idealWidth = count * maxBoxWidth + (count - 1) * minGap
        return CGSize(width: proposal.width ?? idealWidth, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        guard subviews.count > 1 else { return }
        let count = CGFloat(subviews.count)
        let boxWidth = min(maxBoxWidth, (bounds.width - (count - 1) * minGap) / count)
        let gap = (bounds.width - count * boxWidth) / (count - 1)
        for (index, box) in subviews.enumerated() {
            box.place(
                at: CGPoint(x: bounds.minX + CGFloat(index) * (boxWidth + gap), y: bounds.minY),
                proposal: ProposedViewSize(width: boxWidth, height: height)
            )
        }
    }
}

#Preview("PBCodeField") {
    @Previewable @State var typing = "4829"
    @Previewable @State var wrong = "482917"
    VStack(spacing: PBSpace.s24) {
        PBCodeField(code: $typing).pbPreviewInteraction(.focused)
        PBCodeField(code: $wrong, isError: true)
    }
    .padding(PBLayout.screenMargin)
}
