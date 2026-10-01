import SwiftUI

/// The Remind sheet (settle §6–§7): who owes you what, a pre-written message in a Friendly or Neutral
/// tone that you can edit, then Send in Paybak (logs the reminder, closes, toast) or Share… (the
/// system share sheet with the same text). Reminders never change a balance.
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
        let item = books.reminderItem(for: personId, context: context)
        let firstName = books.firstName(personId)
        PBSheet(title: "Remind \(firstName)", testIDPrefix: "remind", onClose: router.dismissSheet) {
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                personCard(books: books)
                toneSwitch(item, books: books)
                PBTextArea("Message", text: $message, helper: "You can edit this message.", focus: $isMessageFocused)
                    .accessibilityIdentifier("remind.message")
                VStack(spacing: PBSpace.s12) {
                    PBButton("Send in Paybak", fillsWidth: true) {
                        if let item { send(item, firstName: firstName) }
                    }
                    .disabled(item == nil || message.isBlank)
                    .accessibilityIdentifier("remind.send")
                    PBButton("Share…", style: .secondary, fillsWidth: true) {
                        shareMessage(item)
                    }
                    .disabled(message.isBlank)
                    .accessibilityIdentifier("remind.share")
                }
            }
        }
        .onAppear {
            if message.isEmpty, let item { message = template(item, tone: tone, books: books) }
        }
        // settleRemindShare: the share sheet over this one (runs after the message is written).
        .onStartScreen([.settleRemindShare]) { _ in shareMessage(item) }
        .systemShare(item: $share)
        .sensoryFeedback(.selection, trigger: tone)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.remind")
    }

    // MARK: Parts

    /// Their row as the breakdown shows it: the net they owe you, what it's for and when it was due.
    @ViewBuilder
    private func personCard(books: Books) -> some View {
        let person = ledgerStore.ledger.person(personId)
        let row = ledgerStore.snapshot.owedBreakdown.first { $0.friend == personId }.map(books.breakdownRow)
        PBPersonRow(
            name: books.firstName(personId),
            avatar: person?.avatarContent ?? .icon(.profile),
            subtitle: row?.subtitle,
            isOnCard: true,
            trailing: row.map { .amount($0.amount, direction: .owed, label: $0.dueLabel, overdue: $0.overdue) } ?? .status("No balance"),
            showsDivider: false
        )
        .pbCard(padding: 0)
        .accessibilityIdentifier("remind.person")
    }

    private func toneSwitch(_ item: Obligation?, books: Books) -> some View {
        let selection = Binding {
            tone
        } set: { newTone in
            // A new tone rewrites the message, unless you've edited it.
            if let item, message == template(item, tone: tone, books: books) {
                message = template(item, tone: newTone, books: books)
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

    private func template(_ item: Obligation, tone: Int, books: Books) -> String {
        books.reminderMessage(item, tone: Self.tones[tone], upi: profileStore.profile.upiID)
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
