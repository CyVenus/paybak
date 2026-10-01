import SwiftUI

/// Assign items (screens-insights-ai §4.4): tap who had each item; shared items split evenly, and the
/// live totals add tax and tip in proportion. Continue waits until every item has someone and
/// someone besides you is on the expense. Scanned with nobody but you, "With you and · Add" picks
/// the people first.
struct ScanAssignPage: View {
    @Binding var review: ReceiptReview?
    /// You first, then the people on the expense.
    let people: [PersonID]
    /// Shows "With you and", the people and "Add" (the expense started with only you).
    var addsPeople = false
    var onAddPeople: () -> Void = {}
    let onBack: () -> Void
    let onContinue: () -> Void

    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    private var currency: String { ledgerStore.books.defaultCurrency }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Tap who had each item. Tax and tip are split in proportion.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .padding(.top, PBSpace.s8)
                if addsPeople {
                    peopleRow
                        .padding(.top, PBSpace.s12)
                }
                if let review {
                    VStack(spacing: 0) {
                        ForEach(review.items.indices, id: \.self) { index in
                            row(index, in: review)
                        }
                    }
                    .padding(.top, PBSpace.s12)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, 200)
            .phoneContentWidth()
        }
        .pbPinnedHeader {
            PBPushHeader("Assign items", testIDPrefix: "scanAssign", onBack: onBack)
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if let review {
                ScanBottomBar {
                    totals(review)
                    PBButton("Continue", fillsWidth: true, action: onContinue)
                        .disabled(review.unassignedCount > 0 || people.count < 2)
                        .accessibilityIdentifier("scanAssign.continue")
                }
            }
        }
        .toolbarVisibility(.hidden, for: .navigationBar)
        .scanState("assign")
    }

    /// "With you and" + the others' chips + "Add" (opens Split with).
    private var peopleRow: some View {
        PBFlowLayout {
            Text("With you and")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
            ForEach(chipPeople.dropFirst()) { person in
                PBCategoryChip(person.name, leading: .avatar(person.avatar)) {}
                    .allowsHitTesting(false)
            }
            PBCategoryChip("Add", leading: .icon(.plus), action: onAddPeople)
                .accessibilityIdentifier("scanAssign.addPeople")
        }
    }

    private func row(_ index: Int, in review: ReceiptReview) -> some View {
        let item = review.items[index]
        return PBAssignItemRow(
            item: item.label, price: Money.format(item.amount, currency), people: chipPeople,
            selected: review.assignment[index], sharedCaption: sharedCaption(item, count: review.assignment[index].count),
            showsDivider: index < review.items.count - 1
        ) { person in
            self.review?.toggle(person, onItem: index)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("scanAssign.item.\(index)")
    }

    private func totals(_ review: ReceiptReview) -> some View {
        let shares = review.shares(order: people)
        let left = review.unassignedCount
        return PBPersonTotalsCard(
            status: left == 0 ? "All items assigned" : "\(left) item\(left == 1 ? "" : "s") left",
            isComplete: left == 0,
            note: Self.chargesNote(review.charges),
            totals: chipPeople.map { person in
                PBPersonTotalsCard.Total(id: person.id, name: person.name, avatar: person.avatar,
                                         amount: Money.format(shares[person.id] ?? 0, currency))
            }
        )
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("scanAssign.status")
    }

    /// "Shared by 3 · ₹80 each" for an item two or more people had (each share rounded down).
    private func sharedCaption(_ item: ReceiptScan.Line, count: Int) -> String? {
        guard count > 1 else { return nil }
        return "Shared by \(count) · \(Money.format(item.amount / Int64(count), currency)) each"
    }

    private var chipPeople: [PBAssignItemRow.Person] {
        people.map { id in
            if id == Person.me {
                return PBAssignItemRow.Person(id: id, name: "You", avatar: profileStore.avatarContent)
            }
            let person = ledgerStore.ledger.person(id)
            return PBAssignItemRow.Person(id: id, name: person?.firstName ?? "", avatar: person?.avatarContent ?? .icon(.profile))
        }
    }

    /// "Includes GST and tip": each charge's name without its rate; acronyms keep their capitals.
    static func chargesNote(_ charges: [ReceiptScan.Line]) -> String? {
        let names = charges.map { charge in
            let name = charge.label.replacing(/\s*\d+(\.\d+)?\s*%$/, with: "")
            return name == name.uppercased() ? name : name.lowercased()
        }
        return names.isEmpty ? nil : "Includes \(Format.joinedNames(names))"
    }
}
