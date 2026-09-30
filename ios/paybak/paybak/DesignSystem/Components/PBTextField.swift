import SwiftUI

/// Control / Input Field (Figma 12:296): an optional label, a 52 pt `bg/card` field with 14 pt corners,
/// and an optional helper line. Focused = 1.5 pt black ring; error = red ring and the error message in
/// red in place of the helper. Disable it with `.disabled(true)`.
///
/// The ring is an inside overlay, so the text keeps its 16 pt inset in every state (Figma shifts it to
/// 17.5 because the stroke counts in its layout; README rule 6).
struct PBTextField: View {
    let label: String?
    @Binding var text: String
    let prompt: String
    var helper: String?
    /// The validation message. When set, it replaces the helper and the ring turns red.
    var error: String?
    var icon: PBIcon?

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool
    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.pbPreviewInteraction) private var previewInteraction
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// - Parameters:
    ///   - focus: Pass the screen's focus binding to focus the field from code (e.g. auto-focus on
    ///     appear). Without it the field manages its own focus.
    init(
        _ label: String?,
        text: Binding<String>,
        prompt: String,
        helper: String? = nil,
        error: String? = nil,
        icon: PBIcon? = nil,
        focus: FocusState<Bool>.Binding? = nil
    ) {
        self.label = label
        _text = text
        self.prompt = prompt
        self.helper = helper
        self.error = error
        self.icon = icon
        self.externalFocus = focus
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var isFocused: Bool { focus.wrappedValue || previewInteraction == .focused }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            if let label {
                Text(label)
                    .textStyle(.subheadline)
                    .foregroundStyle(isEnabled ? PBColor.textSecondary : PBColor.textDisabled)
                    .accessibilityHidden(true)
            }
            field
            if let footnote = error ?? helper {
                Text(footnote)
                    .textStyle(.footnote)
                    .foregroundStyle(footnoteColor)
                    .accessibilityHidden(true)
            }
        }
    }

    private var field: some View {
        HStack(spacing: PBSpace.s12) {
            if let icon {
                PBIconView(icon, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconSecondary)
            }
            TextField(label ?? prompt, text: $text, prompt: Text(prompt).foregroundStyle(promptColor))
                .font(PBTextStyle.body.font)
                .foregroundStyle(isEnabled ? PBColor.textPrimary : PBColor.textDisabled)
                .tint(PBColor.textPrimary)
                .focused(focus)
                .frame(maxHeight: .infinity)
                .accessibilityHint(error ?? helper ?? "")
        }
        .padding(.horizontal, PBSpace.s16)
        .frame(height: PBSize.buttonLg)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.input))
        .overlay {
            if let ringColor {
                RoundedRectangle(cornerRadius: PBRadius.input)
                    .strokeBorder(ringColor, lineWidth: 1.5)
            }
        }
        .animation(reduceMotion ? nil : .easeOut(duration: 0.15), value: ringColor)
        .contentShape(.rect)
        .onTapGesture { focus.wrappedValue = true }
    }

    private var ringColor: Color? {
        if error != nil { return PBColor.borderDestructive }
        return isFocused && isEnabled ? PBColor.borderStrong : nil
    }

    private var promptColor: Color { isEnabled ? PBColor.textTertiary : PBColor.textDisabled }

    private var footnoteColor: Color {
        if !isEnabled { return PBColor.textDisabled }
        return error == nil ? PBColor.textTertiary : PBColor.textDestructive
    }
}

#Preview("PBTextField") {
    @Previewable @State var empty = ""
    @Previewable @State var filled = "you@example.com"
    VStack(spacing: PBSpace.s16) {
        PBTextField("Email", text: $empty, prompt: "you@example.com", helper: "We’ll send a 6-digit code.")
        PBTextField("Email", text: $filled, prompt: "you@example.com", helper: "We’ll send a 6-digit code.")
            .pbPreviewInteraction(.focused)
        PBTextField("Email", text: $filled, prompt: "you@example.com", error: "Enter a valid email or phone number.")
        PBTextField(nil, text: $empty, prompt: "Search currencies", icon: .search)
        PBTextField("Email", text: $empty, prompt: "you@example.com", helper: "We’ll send a 6-digit code.")
            .disabled(true)
    }
    .padding(PBLayout.screenMargin)
}
