import SwiftUI

/// Control / Composer (Figma 115:907): the single-line input for expense comments and Ask Paybak.
/// A 52 pt #F5F5F5 field with 14 pt corners. Empty shows the placeholder and, with `onMic`, the
/// 24 pt mic; any typing swaps it for the black 36 pt send button, which (like Return) hands the
/// trimmed text to `onSend` and clears the field (blank text sends nothing). `isPinned` wraps it in
/// the white keyboard bar: full width, a top hairline, 8/20 padding, 68 pt tall; put that in
/// `.safeAreaInset(edge: .bottom)` so it rides the keyboard. Test ids: `<prefix>.field`, `<prefix>.send`, `<prefix>.mic`.
struct PBComposer: View {
    @Binding var text: String
    let placeholder: String
    var isPinned = false
    var testIDPrefix: String?
    /// Starts dictation into the field (it never sends by itself). Without it there is no mic.
    var onMic: (() -> Void)?
    let onSend: (String) -> Void

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool

    init(
        text: Binding<String>,
        placeholder: String,
        isPinned: Bool = false,
        testIDPrefix: String? = nil,
        focus: FocusState<Bool>.Binding? = nil,
        onMic: (() -> Void)? = nil,
        onSend: @escaping (String) -> Void
    ) {
        _text = text
        self.placeholder = placeholder
        self.isPinned = isPinned
        self.testIDPrefix = testIDPrefix
        self.externalFocus = focus
        self.onMic = onMic
        self.onSend = onSend
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var trimmed: String { text.trimmingCharacters(in: .whitespacesAndNewlines) }

    // One view in both states, so pinning on focus keeps the same text field (and its focus).
    var body: some View {
        field
            .padding(.vertical, isPinned ? PBSpace.s8 : 0)
            .padding(.horizontal, isPinned ? PBLayout.screenMargin : 0)
            .background(isPinned ? PBColor.bgPrimary : .clear)
            .overlay(alignment: .top) {
                if isPinned {
                    PBDivider()
                }
            }
    }

    private var field: some View {
        HStack(spacing: PBSpace.s8) {
            TextField(placeholder, text: $text, prompt: Text(placeholder).foregroundStyle(PBColor.textTertiary))
                .font(PBTextStyle.body.font)
                .foregroundStyle(PBColor.textPrimary)
                .tint(PBColor.textPrimary)
                .submitLabel(.send)
                .focused(focus)
                .onSubmit(send)
                .frame(maxHeight: .infinity)
                .accessibilityIdentifier(testID("field"))
            if !text.isEmpty {
                PBIconButton(.arrowUp, accessibilityLabel: "Send", style: .inverse, diameter: PBSize.buttonSm, action: send)
                    .accessibilityIdentifier(testID("send"))
            } else if let onMic {
                Button(action: onMic) {
                    PBIconView(.mic)
                        .foregroundStyle(PBColor.iconSecondary)
                        .frame(width: PBSize.tap, height: PBSize.tap)
                        .contentShape(.rect)
                }
                .buttonStyle(PBDimButtonStyle())
                .padding(.horizontal, -(PBSize.tap - PBSize.iconLg) / 2)
                .accessibilityLabel("Dictate")
                .accessibilityIdentifier(testID("mic"))
            }
        }
        .padding(.leading, PBSpace.s16)
        .padding(.trailing, text.isEmpty ? PBSpace.s16 : PBSpace.s8)
        .frame(height: PBSize.buttonLg)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.input))
        .contentShape(.rect)
        .onTapGesture { focus.wrappedValue = true }
    }

    private func send() {
        guard !trimmed.isEmpty else { return }
        onSend(trimmed)
        text = ""
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBComposer") {
    @Previewable @State var empty = ""
    @Previewable @State var typing = "Add a comment"
    VStack(spacing: PBSpace.s24) {
        PBComposer(text: $empty, placeholder: "Add a comment", onMic: {}) { _ in }
            .padding(.horizontal, PBLayout.screenMargin)
        PBComposer(text: $typing, placeholder: "Add a comment") { _ in }
            .padding(.horizontal, PBLayout.screenMargin)
        PBComposer(text: $empty, placeholder: "Ask or add an expense", isPinned: true, onMic: {}) { _ in }
        PBComposer(text: $typing, placeholder: "Ask or add an expense", isPinned: true) { _ in }
    }
}
