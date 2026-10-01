import SwiftUI

/// Ask Paybak (screens-insights-ai §3): a full-screen chat over Home. It starts with a greeting and
/// four suggested prompts; each prompt gets an answer from live data, with cards and action chips.
/// The composer sends typed questions and dictates with the mic (it never sends by itself).
struct AskScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    @State private var conversation = AskConversation.session
    @State private var text = ""
    @State private var isScrolled = false
    @State private var scrollPosition = ScrollPosition()
    /// Set by a send: the next growth of the chat scrolls to its end.
    @State private var followsNewest = false
    @State private var dictation: Task<Void, Never>?
    @FocusState private var isComposerFocused: Bool

    var body: some View {
        ScrollView {
            Group {
                if conversation.turns.isEmpty {
                    start
                } else {
                    chat
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s16)
            .phoneContentWidth()
        }
        .scrollPosition($scrollPosition)
        .scrollDismissesKeyboard(.interactively)
        .onScrollGeometryChange(for: Bool.self) { $0.contentOffset.y + $0.contentInsets.top > 1 } action: { _, scrolled in
            isScrolled = scrolled
        }
        .safeAreaInset(edge: .top, spacing: 0) { header }
        .safeAreaInset(edge: .bottom, spacing: 0) { composer }
        .background(PBColor.bgPrimary)
        .onChange(of: ledgerStore.revision) { conversation.noticeSave(in: ledgerStore.ledger) }
        .onScrollGeometryChange(for: CGFloat.self) { geometry in
            // How far the chat scrolls: its end just above the composer.
            let visible = geometry.containerSize.height - geometry.contentInsets.top - geometry.contentInsets.bottom
            return max(0, geometry.contentSize.height - visible)
        } action: { old, end in
            // After a send, once the new turn has its size, bring its end into view.
            guard followsNewest, end > old else { return }
            followsNewest = false
            withAnimation(.easeOut(duration: 0.3)) { scrollPosition.scrollTo(y: end) }
        }
        .onDisappear { dictation?.cancel() }
        .routeTestRoot("ask")
        .onStartScreen([.askAnswer, .askConfirm]) { screen in
            send("Who owes me money?")
            if screen == .askConfirm { send("Add ₹600 for a cab, split with Esha and Dev") }
        }
    }

    // MARK: Chrome

    private var header: some View {
        PBModalHeader("Ask Paybak", testIDPrefix: "ask", onClose: router.dismissModal)
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
            .background(alignment: .top) {
                // The chat's scroll-edge fade: white to 60 % of 150 pt from the screen top, then clear
                // (§3.4).
                if isScrolled && !conversation.turns.isEmpty {
                    LinearGradient(stops: [.init(color: PBColor.bgPrimary, location: 0.6),
                                           .init(color: PBColor.bgPrimary.opacity(0), location: 1)],
                                   startPoint: .top, endPoint: .bottom)
                        .frame(height: 150)
                        .offset(y: -62)
                        .allowsHitTesting(false)
                        .transition(.opacity)
                }
            }
    }

    private var composer: some View {
        VStack(spacing: 14) {
            if conversation.turns.isEmpty {
                HStack(spacing: PBSpace.s6) {
                    PBIconView(.lock, size: PBSize.iconSm)
                        .foregroundStyle(PBColor.iconTertiary)
                    Text("Paybak only sees your own data.")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                }
                .accessibilityElement(children: .combine)
            }
            PBComposer(text: $text, placeholder: "Ask or add an expense", isPinned: isComposerFocused, testIDPrefix: "ask",
                       focus: $isComposerFocused, onMic: dictate, onSend: send)
                .padding(.horizontal, isComposerFocused ? 0 : PBLayout.screenMargin)
        }
        .phoneContentWidth()
        .frame(maxWidth: .infinity)
        .background(PBColor.bgPrimary.ignoresSafeArea(edges: .bottom))
    }

    // MARK: Start

    private var start: some View {
        VStack(alignment: .leading, spacing: PBSpace.s32) {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                PBAvatar(.icon(.sparkles), diameter: PBSize.avatarLg)
                VStack(alignment: .leading, spacing: PBSpace.s8) {
                    Text(greeting)
                        .textStyle(.title2)
                        .foregroundStyle(PBColor.textPrimary)
                        .accessibilityAddTraits(.isHeader)
                    Text("Ask about balances and due dates, or add an expense in plain words.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .fixedSize(horizontal: false, vertical: true)
            }
            if !ledgerStore.books.suggestedPrompts().isEmpty {
                VStack(alignment: .leading, spacing: PBSpace.s8) {
                    Text("Try asking")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                    AskSuggestionsCard(onSend: send)
                }
            }
        }
        .padding(.top, 64)
    }

    /// "What can I help with, Arjun?", or without a name "What can I help with?".
    private var greeting: String {
        let name = profileStore.profile.firstName
        return name.isEmpty ? "What can I help with?" : "What can I help with, \(name)?"
    }

    // MARK: Chat

    private var chat: some View {
        VStack(alignment: .leading, spacing: PBSpace.s24) {
            ForEach(conversation.turns) { turn in
                AskTurnView(turn: turn, conversation: conversation, onSend: send)
            }
        }
        .padding(.top, PBSpace.s16)
    }

    // MARK: Actions

    private func send(_ prompt: String) {
        let prompt = prompt.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !prompt.isEmpty else { return }
        dictation?.cancel()
        let upi = profileStore.profile.upiID
        followsNewest = true
        conversation.ask(prompt, books: ledgerStore.books, upi: upi.isEmpty ? nil : upi)
    }

    /// Dictation fills the field and never sends (§3.1); the user taps send.
    private func dictate() {
        dictation?.cancel()
        dictation = Task {
            do {
                try await SpeechInput.dictate { text = $0 }
            } catch is CancellationError {
            } catch {
                router.toast("Dictation isn’t available on this device")
            }
        }
    }
}

/// The suggested prompts card (§3.2): People, Food, Calendar and Bell rows that send in one tap.
struct AskSuggestionsCard: View {
    let onSend: (String) -> Void

    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let suggestions = ledgerStore.books.suggestedPrompts()
        VStack(spacing: 0) {
            ForEach(Array(suggestions.enumerated()), id: \.offset) { index, suggestion in
                PBSettingRow(suggestion.prompt, icon: PBIcon(rawValue: suggestion.icon), showsDivider: index < suggestions.count - 1,
                             titleLineLimit: 2) { onSend(suggestion.prompt) }
                    .accessibilityIdentifier("ask.prompt.\(index)")
            }
        }
        .pbCard(padding: 0)
    }
}

#if DEBUG
#Preview("AskScreen") {
    GroupsPreview(scenarios: Scenario.pro()) {
        AskScreen()
    }
}
#endif
