import SwiftUI

/// Row / Assign Item (Figma 147:2532): one receipt item on Assign items, on white with no side
/// padding. The Headline item and Amount/Medium price, an optional "Shared by 3 · ₹80 each" caption,
/// then one avatar chip per person, wrapping when there are many; a chip is black when that person
/// had the item and tapping it toggles them. A full-width divider underneath.
struct PBAssignItemRow: View {
    struct Person: Identifiable {
        let id: String
        let name: String
        let avatar: PBAvatar.Content
    }

    let item: String
    let price: String
    let people: [Person]
    let selected: Set<Person.ID>
    /// Shown when several people share the item.
    var sharedCaption: String?
    var showsDivider = true
    let onToggle: (Person.ID) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            HStack(alignment: .top, spacing: PBSpace.s12) {
                VStack(alignment: .leading, spacing: 0) {
                    Text(item)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                    if let sharedCaption {
                        Text(sharedCaption)
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                    }
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                Text(price)
                    .textStyle(.amountMedium)
                    .foregroundStyle(PBColor.textPrimary)
            }
            .accessibilityElement(children: .combine)
            // Many people wrap onto more rows of chips.
            PBFlowLayout {
                ForEach(people) { person in
                    PBCategoryChip(person.name, leading: .avatar(person.avatar), isSelected: selected.contains(person.id)) {
                        onToggle(person.id)
                    }
                    .accessibilityHint("Had \(item)")
                }
            }
        }
        .padding(.vertical, PBSpace.s12)
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider()
            }
        }
    }
}

#Preview("PBAssignItemRow") {
    @Previewable @State var biryani: Set<String> = ["dev"]
    @Previewable @State var soda: Set<String> = ["you", "esha", "dev"]
    let people: [PBAssignItemRow.Person] = [
        .init(id: "you", name: "You", avatar: .art(.arjun)),
        .init(id: "esha", name: "Esha", avatar: .art(.esha)),
        .init(id: "dev", name: "Dev", avatar: .art(.dev)),
    ]
    VStack(spacing: 0) {
        PBAssignItemRow(item: "Chicken biryani", price: "₹430", people: people, selected: biryani) { biryani.formSymmetricDifference([$0]) }
        PBAssignItemRow(item: "Fresh lime soda ×3", price: "₹270", people: people, selected: soda, sharedCaption: "Shared by 3 · ₹90 each") { soda.formSymmetricDifference([$0]) }
    }
    .padding(PBLayout.screenMargin)
}
