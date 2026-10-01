import SwiftUI

/// "With you and" (or "With" when you aren't on it) + the people on the expense (add-expense §4.3):
/// first-name chips with their avatars, then "Add", scrolling sideways to the screen edge. With
/// nobody picked it's one "Add people" chip. Every chip opens Split with.
struct ExpensePeopleStrip: View {
    let people: [PersonID]
    var includesYou = true
    let onEdit: () -> Void

    @Environment(LedgerStore.self) private var store

    var body: some View {
        if people.isEmpty {
            HStack(spacing: PBSpace.s8) {
                label
                PBCategoryChip("Add people", leading: .icon(.plus), action: onEdit)
                    .accessibilityIdentifier("addExpense.addPeople")
                Spacer(minLength: 0)
            }
        } else {
            ScrollView(.horizontal) {
                HStack(spacing: PBSpace.s8) {
                    label
                    ForEach(people, id: \.self) { id in
                        let person = store.ledger.person(id)
                        PBCategoryChip(person?.firstName ?? "Someone", leading: .avatar(person?.avatarContent ?? .icon(.profile)), action: onEdit)
                            .accessibilityIdentifier("addExpense.person.\(id)")
                    }
                    PBCategoryChip("Add", leading: .icon(.plus), action: onEdit)
                        .accessibilityIdentifier("addExpense.addPeople")
                }
                .padding(.trailing, PBLayout.screenMargin)
            }
            .scrollIndicators(.hidden)
            .padding(.trailing, -PBLayout.screenMargin)
        }
    }

    private var label: some View {
        Text(includesYou ? "With you and" : "With")
            .textStyle(.subheadline)
            .foregroundStyle(PBColor.textSecondary)
            .fixedSize()
    }
}
