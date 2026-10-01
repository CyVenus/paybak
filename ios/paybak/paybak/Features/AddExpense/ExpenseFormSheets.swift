import SwiftUI

/// Paid by (add-expense §6): you and the people on the expense; a tap sets the single payer and
/// closes. "Multiple people" opens the payer editor.
struct PaidBySheet: View {
    let form: ExpenseForm
    let onClose: () -> Void
    let onMultiple: () -> Void

    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        PBSheet(title: "Paid by", testIDPrefix: "paidBy", onClose: onClose) {
            VStack(spacing: 0) {
                ForEach(form.people, id: \.self) { id in
                    PBPersonRow(
                        name: id == Person.me ? "You" : store.ledger.person(id)?.name ?? "Someone",
                        avatar: id == Person.me ? profileStore.avatarContent : store.ledger.person(id)?.avatarContent ?? .icon(.profile),
                        subtitle: id == Person.me ? profileStore.profile.name : nil,
                        trailing: form.payers.isEmpty && form.payerId == id ? .check : .none,
                        showsDivider: id != form.people.last
                    ) {
                        Haptics.selection()
                        form.payers = []
                        form.payerId = id
                        onClose()
                    }
                    .accessibilityIdentifier("paidBy.row.\(id == Person.me ? "you" : id)")
                }
                PBDivider()
                PBSheetRow(title: "Multiple people", subtitle: "Enter how much each person paid", icon: .people, action: onMultiple)
                    .accessibilityIdentifier("paidBy.multiple")
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("paidBy.sheet")
    }
}

/// Category (add-expense §8): the eight categories with their icons, searchable; a tap picks one
/// and closes.
struct CategorySheet: View {
    let selected: ExpenseCategory?
    let onClose: () -> Void
    let onSelect: (ExpenseCategory) -> Void

    @State private var query = ""

    private var matches: [ExpenseCategory] {
        let needle = query.trimmingCharacters(in: .whitespaces)
        guard !needle.isEmpty else { return ExpenseCategory.allCases }
        return ExpenseCategory.allCases.filter {
            $0.name.range(of: needle, options: [.caseInsensitive, .diacriticInsensitive]) != nil
        }
    }

    var body: some View {
        PBSheet(title: "Category", search: $query, searchPrompt: "Search categories", testIDPrefix: "category", onClose: onClose) {
            ScrollView {
                if matches.isEmpty {
                    Text("No categories match “\(query)”")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .frame(maxWidth: .infinity)
                        .padding(.top, PBSpace.s24)
                } else {
                    VStack(spacing: 0) {
                        ForEach(matches, id: \.self) { category in
                            PBSettingRow(category.name, icon: category.pbIcon, trailing: category == selected ? .check : .unchecked,
                                         showsDivider: category != matches.last) {
                                Haptics.selection()
                                onSelect(category)
                            }
                            .accessibilityIdentifier("category.row.\(category.rawValue)")
                        }
                    }
                    .pbCard(padding: 0)
                }
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.immediately)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("category.sheet")
    }
}

/// Notes (add-expense §3.13, proposal): a multi-line field and Done. Up to 500 characters.
struct NotesSheet: View {
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
        PBSheet(title: "Notes", testIDPrefix: "notes", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBTextArea(nil, text: $text, prompt: "Add a note for everyone on this expense", focus: $isFocused)
                    .onChange(of: text) { if text.count > 500 { text = String(text.prefix(500)) } }
                    .accessibilityIdentifier("notes.field")
                PBButton("Done", fillsWidth: true) {
                    onDone(text.trimmingCharacters(in: .whitespacesAndNewlines))
                }
                .accessibilityIdentifier("notes.done")
            }
        }
        .task { isFocused = true }
    }
}
