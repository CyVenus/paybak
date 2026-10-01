import SwiftUI

/// What the loan was for (proposal): a one-line field and Done.
struct LoanReasonSheet: View {
    let onClose: () -> Void
    let onDone: (String) -> Void

    @State private var text: String
    @FocusState private var isFocused: Bool

    init(text: String, onClose: @escaping () -> Void, onDone: @escaping (String) -> Void) {
        _text = State(initialValue: text)
        self.onClose = onClose
        self.onDone = onDone
    }

    var body: some View {
        PBSheet(title: "Reason", testIDPrefix: "loanReason", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBTextField(nil, text: Binding {
                    text
                } set: {
                    text = String($0.prefix(LoanForm.maxReason))
                }, prompt: "What’s it for?", focus: $isFocused)
                    .textInputAutocapitalization(.sentences)
                    .submitLabel(.done)
                    .onSubmit(done)
                    .accessibilityIdentifier("loanReason.field")
                PBButton("Done", fillsWidth: true, action: done)
                    .accessibilityIdentifier("loanReason.done")
            }
        }
        .task { isFocused = true }
    }

    private func done() {
        onDone(String(text.trimmingCharacters(in: .whitespacesAndNewlines).prefix(LoanForm.maxReason)))
    }
}

/// How often installments fall due (proposal: Weekly, Every 2 weeks, Monthly).
struct RepeatsSheet: View {
    let selected: Loan.Frequency
    let onClose: () -> Void
    let onSelect: (Loan.Frequency) -> Void

    private let options: [Loan.Frequency] = [.weekly, .biweekly, .monthly]

    var body: some View {
        PBSheet(title: "Repeats", testIDPrefix: "repeats", onClose: onClose) {
            VStack(spacing: 0) {
                ForEach(options, id: \.self) { frequency in
                    PBSettingRow(frequency.title, trailing: frequency == selected ? .check : .unchecked,
                                 showsDivider: frequency != options.last) {
                        Haptics.selection()
                        onSelect(frequency)
                    }
                    .accessibilityIdentifier("repeats.row.\(frequency.rawValue)")
                }
            }
            .pbCard(padding: 0)
        }
    }
}
