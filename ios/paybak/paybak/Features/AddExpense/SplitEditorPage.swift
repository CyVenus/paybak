import SwiftUI

/// The split editor (add-expense §7): Equally · Exact · % · Shares for everyone on the expense, a
/// tick to leave someone out and the live Split Total footer. It edits a copy: Done (or leaving
/// while it adds up) applies it; leaving while it doesn't keeps the last valid split.
struct SplitEditorPage: View {
    /// A field to open with, mid-edit.
    struct Edit: Hashable {
        let personId: PersonID
        let text: String
    }

    let form: ExpenseForm
    var initialEdit: Edit?

    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @Environment(\.dismiss) private var dismiss
    @State private var editor: SplitEditorState?
    @State private var focused: PersonID?

    var body: some View {
        Group {
            if let editor {
                content(editor)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear {
            guard editor == nil else { return }
            let state = SplitEditorState(form: form, books: store.books)
            if let initialEdit {
                state.texts[initialEdit.personId] = initialEdit.text
                focused = initialEdit.personId
            }
            editor = state
        }
    }

    private func content(_ editor: SplitEditorState) -> some View {
        let preview = editor.preview(store.books)
        let footer = preview.footer
        let people = editor.people
        return ScrollView {
            VStack(spacing: PBSpace.s16) {
                Text(summary)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity)
                    .lineLimit(1)
                PBSegmentedControl(options: SplitEditorState.modeTitles, selection: Binding {
                    SplitEditorState.modes.firstIndex(of: editor.mode) ?? 0
                } set: { index in
                    Haptics.selection()
                    focused = nil
                    editor.switchMode(to: SplitEditorState.modes[index], preview: preview)
                })
                VStack(spacing: 0) {
                    ForEach(people, id: \.self) { person in
                        SplitEditorRow(
                            name: store.books.firstName(person),
                            avatar: avatar(person),
                            mode: rowMode(person, editor: editor),
                            amount: Money.format(preview.shares[person, default: 0], form.currency),
                            currencySymbol: Money.info(form.currency).symbol,
                            isIncluded: Binding { editor.isIncluded(person) } set: { editor.setIncluded(person, $0) },
                            showsDivider: person != people.last,
                            personId: person,
                            focused: $focused
                        )
                        .accessibilityIdentifier("split.row.\(person)")
                    }
                }
                .pbCard(padding: 0)
                if focused == nil {
                    Text("Uncheck someone to leave them out.")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier("split.hint")
                }
            }
            .padding(.bottom, PBSpace.s16)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBDoneHeader(title: "Split", isDoneEnabled: preview.isBalanced, testIDPrefix: "split", onBack: { dismiss() }, onDone: { dismiss() })
        }
        .safeAreaInset(edge: .bottom) {
            PBSplitTotalBar(left: footer.left, detail: footer.detail, isError: !preview.isBalanced)
                .accessibilityIdentifier("split.total")
                .padding(.horizontal, PBLayout.screenMargin)
                .phoneContentWidth()
                .keyboardGap(PBSpace.s8)
        }
        .background(PBColor.bgPrimary.onTapGesture { focused = nil })
        .navigationBarHiddenKeepingSwipeBack()
        .onDisappear {
            if preview.isBalanced { editor.apply(to: form) }
        }
        .routeTestRoot("addExpense")
    }

    private var summary: String {
        let total = Money.format(form.amount, form.currency)
        let title = form.title.trimmingCharacters(in: .whitespaces)
        return title.isEmpty ? total : "\(title) · \(total)"
    }

    private func avatar(_ person: PersonID) -> PBAvatar.Content {
        person == Person.me ? profileStore.avatarContent : store.ledger.person(person)?.avatarContent ?? .icon(.profile)
    }

    private func rowMode(_ person: PersonID, editor: SplitEditorState) -> PBSplitRow.Mode {
        switch editor.mode {
        case .equal, .itemized: .equally
        case .exact: .exact(editor.textBinding(person))
        case .percent: .percent(editor.textBinding(person))
        case .shares: .shares(editor.sharesBinding(person))
        }
    }
}

/// One editor row, focusing its field when the page's focused person is this one.
private struct SplitEditorRow: View {
    let name: String
    let avatar: PBAvatar.Content
    let mode: PBSplitRow.Mode
    let amount: String
    let currencySymbol: String
    @Binding var isIncluded: Bool
    let showsDivider: Bool
    let personId: PersonID
    @Binding var focused: PersonID?

    @FocusState private var isFocused: Bool

    var body: some View {
        PBSplitRow(name: name, avatar: avatar, mode: mode, amount: amount, currencySymbol: currencySymbol,
                   isIncluded: $isIncluded, showsDivider: showsDivider, focus: $isFocused)
            .onChange(of: isFocused) { _, now in
                if now { focused = personId } else if focused == personId { focused = nil }
            }
            .onChange(of: focused, initial: true) { _, person in
                isFocused = person == personId
            }
    }
}
