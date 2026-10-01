import SwiftUI

/// The Remind sheet (settle §6–§7): who owes you what, a pre-written message in a Friendly or Neutral
/// tone that you can edit, then Send in Paybak (logs the reminder, closes, toast) or Share… (the
/// system share sheet with the same text). Reminders never change a balance. When they owe you
/// nothing (any more), there's nothing to remind them of and the sheet closes.
struct RemindSheet: View {
    let personId: PersonID
    let context: ReminderContext?

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore
    @State private var tone = 0
    @State private var message = ""
    @State private var share: ShareItem?
    @FocusState private var isMessageFocused: Bool

    private static let tones: [Reminder.Tone] = [.friendly, .neutral]

    var body: some View {
        let books = ledgerStore.books
        let draft = books.remindDraft(for: personId, context: context, upi: profileStore.profile.upiID)
        let firstName = books.firstName(personId)
        PBSheet(title: "Remind \(firstName)", testIDPrefix: "remind", onClose: router.dismissSheet) {
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                if let draft {
                    personCard(draft)
                }
                toneSwitch(draft)
                PBTextArea("Message", text: $message, helper: "You can edit this message.", focus: $isMessageFocused)
                    .accessibilityIdentifier("remind.message")
                VStack(spacing: PBSpace.s12) {
                    PBButton("Send in Paybak", fillsWidth: true) {
                        if let draft { send(draft.item, firstName: firstName) }
                    }
                    .disabled(draft == nil || message.isBlank)
                    .accessibilityIdentifier("remind.send")
                    PBButton("Share…", style: .secondary, fillsWidth: true) {
                        shareMessage(draft?.item)
                    }
                    .disabled(message.isBlank)
                    .accessibilityIdentifier("remind.share")
                }
            }
        }
        .onAppear {
            if message.isEmpty, let draft { message = draft.message(Self.tones[tone]) }
        }
        // They owe you nothing (any more): there's nothing to remind them of.
        .task {
            if draft == nil { router.dismissSheet() }
        }
        // settleRemindShare: the share sheet over this one, once this sheet has finished opening
        // (UIKit can't present during a transition).
        .onStartScreen([.settleRemindShare]) { _ in
            Task {
                try? await Task.sleep(for: .seconds(1))
                shareMessage(draft?.item)
            }
        }
        .systemShare(item: $share)
        .sensoryFeedback(.selection, trigger: tone)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.remind")
    }

    // MARK: Parts

    /// Their row: what the reminder covers in total, what it's for, and under the amount when that
    /// was due (the red badge once it's overdue).
    private func personCard(_ draft: RemindDraft) -> some View {
        let person = ledgerStore.ledger.person(personId)
        return PBPersonRow(
            name: draft.name,
            avatar: person?.avatarContent ?? .initials(String(draft.name.prefix(1))),
            subtitle: draft.subtitle,
            isOnCard: true,
            trailing: .amount(draft.amountText, direction: .owed, label: draft.dueLabel, overdue: draft.overdue),
            showsDivider: false
        )
        .pbCard(padding: 0)
        .accessibilityIdentifier("remind.person")
    }

    private func toneSwitch(_ draft: RemindDraft?) -> some View {
        let selection = Binding {
            tone
        } set: { newTone in
            // A new tone rewrites the message, unless you've edited it.
            if let draft, message == draft.message(Self.tones[tone]) {
                message = draft.message(Self.tones[newTone])
            }
            tone = newTone
        }
        return VStack(alignment: .leading, spacing: PBSpace.s8) {
            Text("Tone")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
                .accessibilityHidden(true)
            PBSegmentedControl(options: ["Friendly", "Neutral"], selection: selection, testIDPrefix: "remind.tone")
                .accessibilityLabel("Tone")
        }
    }

    // MARK: Actions

    private func send(_ item: Obligation, firstName: String) {
        isMessageFocused = false
        ledgerStore.sendReminder(about: item, tone: Self.tones[tone], message: message, via: .paybak)
        router.dismissSheet()
        router.toast("Reminder sent to \(firstName)")
    }

    /// The system share sheet with the current text. Paybak can't see where it went, so a finished
    /// share logs the reminder and closes this sheet quietly.
    private func shareMessage(_ item: Obligation?) {
        isMessageFocused = false
        let tone = Self.tones[tone]
        let text = message
        share = ShareItem(text: text) { completed in
            guard completed else { return }
            if let item { ledgerStore.sendReminder(about: item, tone: tone, message: text, via: .share) }
            router.dismissSheet()
        }
    }
}

private extension String {
    var isBlank: Bool { trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
}

#if DEBUG
#Preview("RemindSheet · Rohan") {
    GroupsPreview {
        RemindSheet(personId: "p-rohan", context: .expense("e-movie"))
    }
}
#endif
