import SwiftUI

/// Group settings › Name (screens-groups §5.3 proposal): a fitted sheet with the name focused and
/// Save, which is off while the name is blank. Renaming shows everywhere the group does.
struct RenameGroupSheet: View {
    let onClose: () -> Void
    let onSave: (String) -> Void

    @State private var name: String
    @FocusState private var isFocused: Bool

    init(name: String, onClose: @escaping () -> Void, onSave: @escaping (String) -> Void) {
        _name = State(initialValue: name)
        self.onClose = onClose
        self.onSave = onSave
    }

    var body: some View {
        PBSheet(title: "Group name", testIDPrefix: "groupSettings.renameSheet", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBTextField(nil, text: $name, prompt: "Goa Trip", showsClearButton: true, focus: $isFocused)
                    .textInputAutocapitalization(.words)
                    .submitLabel(.done)
                    .onSubmit(save)
                    .accessibilityIdentifier("groupSettings.renameField")
                PBButton("Save", fillsWidth: true, action: save)
                    .disabled(trimmed.isEmpty)
                    .accessibilityIdentifier("groupSettings.renameSave")
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("groupSettings.renameSheet")
        .task { isFocused = true }
    }

    private var trimmed: String {
        name.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func save() {
        guard !trimmed.isEmpty else { return }
        isFocused = false
        onSave(trimmed)
    }
}

#if DEBUG
#Preview("RenameGroupSheet") {
    @Previewable @State var isPresented = true
    Color.clear.pbSheet(isPresented: $isPresented) {
        RenameGroupSheet(name: "Goa Trip", onClose: { isPresented = false }) { _ in isPresented = false }
    }
}
#endif
