import SwiftUI

/// Flag an issue (screens-activity §4.3-G, proposal): what looks wrong, then "Flag expense".
struct FlagExpenseSheet: View {
    let onClose: () -> Void
    let onFlag: (String) -> Void

    @State private var note = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        PBSheet(title: "Flag an issue", testIDPrefix: "flag", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBTextArea(nil, text: $note, prompt: "What looks wrong?", focus: $isFocused)
                PBButton("Flag expense", fillsWidth: true) { onFlag(note.trimmingCharacters(in: .whitespacesAndNewlines)) }
                    .disabled(note.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    .accessibilityIdentifier("flag.send")
            }
        }
        .task { isFocused = true }
    }
}

#Preview("FlagExpenseSheet") {
    FlagExpenseSheet(onClose: {}, onFlag: { _ in })
}
