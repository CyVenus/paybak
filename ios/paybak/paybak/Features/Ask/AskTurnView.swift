import SwiftUI

/// One exchange in Ask Paybak (§3.3–§3.4): your bubble, the assistant's reply 16 below it, then 12
/// apart its card (people, a category bar, the drafted reminder, a draft expense or the suggestions)
/// and the action chips.
struct AskTurnView: View {
    let turn: AskConversation.Turn
    let conversation: AskConversation
    let onSend: (String) -> Void

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    private var reply: AssistantReply { turn.reply }

    /// A reminder reply carries the drafted message, shown in its own card; anything else the reply
    /// adds ("I couldn’t find Kabir.") continues its one bubble.
    private var isReminder: Bool {
        reply.card == nil && reply.chips.contains { if case .remind = $0 { true } else { false } }
    }

    private var replyText: String {
        isReminder ? reply.text : ([reply.text] + reply.more).joined(separator: " ")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s16) {
            PBChatBubble(role: .user, text: turn.prompt)
                .accessibilityIdentifier("ask.message.\(turn.id).prompt")
            PBChatBubble(role: .assistant, text: replyText) {
                if isReminder {
                    ForEach(reply.more, id: \.self, content: reminderCard)
                }
                if let card = reply.card {
                    cardView(card)
                }
                if !reply.chips.isEmpty {
                    PBFlowLayout {
                        ForEach(reply.chips, id: \.self, content: chip)
                    }
                }
            }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("ask.message.\(turn.id)")
        }
    }

    // MARK: Cards

    @ViewBuilder
    private func cardView(_ card: AssistantReply.Card) -> some View {
        switch card {
        case .people(let lines):
            VStack(spacing: 0) {
                ForEach(lines) { line in
                    PBPersonRow(name: line.name, avatar: avatar(line.id), size: .compact, isOnCard: true,
                                trailing: .amount(line.amount, overdue: line.overdue), showsDivider: line.id != lines.last?.id) {
                        router.open(.friend(line.id))
                    }
                }
            }
            .pbCard(padding: 0)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("ask.answerCard")
        case .category(let row):
            PBBarRow(leading: .icon(PBIcon(rawValue: row.leading.iconName ?? "") ?? .tag), title: row.title, caption: row.caption,
                     amount: row.amount, progress: row.progress, isOnCard: true)
                .padding(.horizontal, PBSpace.s16)
                .pbCard(padding: 0)
                .accessibilityIdentifier("ask.answerCard")
        case .draft(let draft):
            PBDraftExpenseCard(
                icon: PBIcon(rawValue: draft.icon) ?? .tag, title: draft.title, amount: draft.amount, paidLine: draft.paidLine,
                splitLine: draft.splitLine, memberAvatars: draft.people.prefix(4).map(member), eachLine: draft.eachLine,
                isSaved: turn.savedExpense != nil, testIDPrefix: "ask.draft",
                onSave: { save(draft) }, onEdit: { edit(draft) }, onView: view
            )
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(turn.savedExpense == nil ? "ask.draft" : "ask.draft.saved")
        case .suggestions:
            AskSuggestionsCard(onSend: onSend)
        }
    }

    /// The drafted reminder, as the Remind sheet will send it.
    private func reminderCard(_ message: String) -> some View {
        Text(message)
            .textStyle(.body)
            .foregroundStyle(PBColor.textPrimary)
            .fixedSize(horizontal: false, vertical: true)
            .frame(maxWidth: .infinity, alignment: .leading)
            .pbCard()
            .accessibilityIdentifier("ask.reminder")
    }

    // MARK: Chips

    @ViewBuilder
    private func chip(_ chip: AssistantReply.Chip) -> some View {
        switch chip {
        case .remind(let id, let name, let item):
            PBButton("Remind \(name)", style: .secondary, size: .small, icon: .bell) {
                router.open(.remind(personId: id, context: context(item)))
            }
            .accessibilityIdentifier("ask.chip.remind.\(name.lowercased())")
        case .seeInsights(let month):
            PBButton("See Insights", style: .secondary, size: .small, icon: .chart) {
                router.select(.activity)
                router.activitySegment = .insights
                router.insightsMonth = month == YearMonth(ledgerStore.books.today) ? nil : month
            }
            .accessibilityIdentifier("ask.chip.insights")
        case .settleUp(let id):
            PBButton("Settle up", style: .secondary, size: .small, icon: .wallet) { leaveChat(for: .settleUp(groupId: id)) }
                .accessibilityIdentifier("ask.chip.settleUp")
        case .openGroup(let id, let name, let isProject):
            PBButton("Open \(name)", style: .secondary, size: .small, icon: .groups) {
                leaveChat(for: isProject ? .project(id) : .group(id))
            }
                .accessibilityIdentifier("ask.chip.openGroup")
        }
    }

    // MARK: Actions

    /// Save creates the expense and the card turns into "Expense added" in place: no toast, no
    /// navigation (§3.4).
    private func save(_ draft: AssistantReply.DraftCard) {
        guard let id = try? ledgerStore.addExpense(draft.draft) else { return }
        Haptics.success()
        withAnimation(.easeOut(duration: 0.25)) { conversation.markSaved(turn.id, expense: id) }
    }

    /// Edit opens the full form over the chat; if it saves, the card turns Saved.
    private func edit(_ draft: AssistantReply.DraftCard) {
        conversation.beginEditing(turn.id, ledger: ledgerStore.ledger)
        router.open(.addExpense(AddExpenseArgs(draft: draft.draft)))
    }

    private func view() {
        guard let id = turn.savedExpense else { return }
        leaveChat(for: .expense(id))
    }

    /// Closes the chat, then opens `route` where it was asked from.
    private func leaveChat(for route: Route) {
        router.dismissModal()
        router.open(route)
    }

    private func context(_ item: Obligation) -> ReminderContext {
        switch item.kind {
        case .direct: .expense(item.ref)
        case .group, .project: .group(item.ref)
        case .loan: .loan(item.ref, installment: item.installment)
        }
    }

    private func avatar(_ id: PersonID) -> PBAvatar.Content {
        id == Person.me ? profileStore.avatarContent : ledgerStore.ledger.person(id)?.avatarContent ?? .icon(.profile)
    }

    /// You as your own avatar, a friend's head or initials, "?" for someone no longer in the ledger.
    private func member(_ id: PersonID) -> PBAvatarStack.Member {
        if id == Person.me { return .user }
        return .content(ledgerStore.ledger.person(id)?.avatarContent ?? .initials("?"))
    }
}

private extension InsightsPage.BarRow.Leading {
    var iconName: String? {
        if case .icon(let name) = self { name } else { nil }
    }
}
