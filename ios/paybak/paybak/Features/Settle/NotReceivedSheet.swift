import SwiftUI

/// Not received (settle §8): instead of silently rejecting a friend's claim, you send them an
/// editable note. The claim leaves your Confirm cards, and what they owe stays until a payment is
/// confirmed. The sheet has no header: the question is its title.
struct NotReceivedSheet: View {
    let paymentId: PaymentID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var note = ""
    @FocusState private var isNoteFocused: Bool

    var body: some View {
        PBSheet {
            if let payment = ledgerStore.ledger.payment(paymentId) {
                content(ledgerStore.books.notReceivedCopy(payment))
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.notReceived")
    }

    private func content(_ copy: NotReceivedCopy) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s24) {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                Text(copy.title)
                    .textStyle(.title3)
                    .foregroundStyle(PBColor.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)
                    .accessibilityAddTraits(.isHeader)
                Text(copy.context)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
            }
            PBTextArea("Note", text: $note, helper: copy.helper, focus: $isNoteFocused)
                .accessibilityIdentifier("notReceived.note")
            VStack(spacing: PBSpace.s12) {
                PBButton("Send", fillsWidth: true, action: send)
                    .disabled(note.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    .accessibilityIdentifier("notReceived.send")
                PBButton("Cancel", style: .secondary, fillsWidth: true, action: router.dismissSheet)
                    .accessibilityIdentifier("notReceived.cancel")
            }
        }
        .padding(.top, PBSpace.s8)
        .onAppear {
            if note.isEmpty { note = copy.note }
        }
    }

    /// Marks the claim Not received with the note; balances don't change.
    private func send() {
        isNoteFocused = false
        do {
            try ledgerStore.markNotReceived(paymentId, note: note)
            router.dismissSheet()
        } catch {
            router.toast(error.localizedDescription)
        }
    }
}

#if DEBUG
#Preview("NotReceivedSheet · Esha") {
    GroupsPreview {
        NotReceivedSheet(paymentId: "pay-esha-olive")
    }
}
#endif
