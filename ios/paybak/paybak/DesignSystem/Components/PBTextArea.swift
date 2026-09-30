import SwiftUI

/// Control / Text Area (Figma 115:9936): multi-line input for the Remind message and the Not
/// received note. Input Field styling (#F5F5F5, 14 pt corners, 16 pt padding) with Body text that
/// wraps: at least 104 pt tall, growing with the text. Optional Subheadline label above and
/// Footnote helper below. Focused = 1.5 pt `border/strong` inside ring; the text doesn't move.
struct PBTextArea: View {
    let label: String?
    @Binding var text: String
    var prompt = ""
    var helper: String?

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool
    @Environment(\.pbPreviewInteraction) private var previewInteraction
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    init(
        _ label: String?,
        text: Binding<String>,
        prompt: String = "",
        helper: String? = nil,
        focus: FocusState<Bool>.Binding? = nil
    ) {
        self.label = label
        _text = text
        self.prompt = prompt
        self.helper = helper
        self.externalFocus = focus
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var isFocused: Bool { focus.wrappedValue || previewInteraction == .focused }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            if let label {
                Text(label)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                    .accessibilityHidden(true)
            }
            TextField(label ?? prompt, text: $text, prompt: Text(prompt).foregroundStyle(PBColor.textTertiary), axis: .vertical)
                .font(PBTextStyle.body.font)
                .lineHeight(.exact(points: PBTextStyle.body.lineHeight))
                .foregroundStyle(PBColor.textPrimary)
                .tint(PBColor.textPrimary)
                .lineLimit(3...)
                .focused(focus)
                .frame(maxWidth: .infinity, minHeight: 104 - 2 * PBSpace.s16, alignment: .topLeading)
                .padding(PBSpace.s16)
                .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.input))
                .overlay {
                    if isFocused {
                        RoundedRectangle(cornerRadius: PBRadius.input)
                            .strokeBorder(PBColor.borderStrong, lineWidth: 1.5)
                    }
                }
                .animation(reduceMotion ? nil : .easeOut(duration: 0.15), value: isFocused)
                .contentShape(.rect)
                .onTapGesture { focus.wrappedValue = true }
                .accessibilityHint(helper ?? "")
            if let helper {
                Text(helper)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .accessibilityHidden(true)
            }
        }
    }
}

#Preview("PBTextArea") {
    @Previewable @State var message = "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
    @Previewable @State var note = ""
    VStack(spacing: PBSpace.s24) {
        PBTextArea("Message", text: $message, helper: "You can edit this message.")
        PBTextArea("Message", text: $message, helper: "You can edit this message.").pbPreviewInteraction(.focused)
        PBTextArea(nil, text: $note, prompt: "Add a note (optional)")
    }
    .padding(PBLayout.screenMargin)
}
