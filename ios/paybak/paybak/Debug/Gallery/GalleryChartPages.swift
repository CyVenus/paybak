#if DEBUG
import SwiftUI

// MARK: - Progress and charts

struct GalleryChartsPage: View {
    @State private var progress = 0.3

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Progress Bar: Small · Large × Default · Projected · Over") {
                ForEach([PBProgressBar.Size.small, .large], id: \.self) { size in
                    PBProgressBar(value: 0.6, size: size)
                    PBProgressBar(value: 0.87, projected: 0.97, size: size)
                    PBProgressBar(value: 0.976, overFrom: 0.976, size: size)
                }
                GalleryLabel("Live: changes grow over 0.3 s")
                PBProgressBar(value: progress, projected: min(progress + 0.1, 1), size: .large)
                HStack(spacing: PBSpace.s8) {
                    ForEach([0, 0.3, 0.87, 1], id: \.self) { value in
                        PBButton("\(Int((value * 100).rounded()))%", style: .secondary, size: .small) { progress = value }
                    }
                }
            }
            GallerySection("Show mark (examples)") {
                PBProgressBar(value: 0.6, mark: 0.51)
                PBProgressBar(value: 0.976, overFrom: 0.976, mark: 0.976, size: .large)
            }
            GallerySection("Row / Bar: Icon · Avatar × Neutral · Owed · Owe") {
                VStack(spacing: 0) {
                    ForEach([nil, .owed, .owe] as [PBAmountDirection?], id: \.self) { direction in
                        PBBarRow(leading: .icon(.home), title: "Rent", caption: "51%", amount: "₹12,000", direction: direction, progress: 0.51)
                    }
                }
                VStack(spacing: 0) {
                    ForEach([(nil, 0.51), (.owed, 1), (.owe, 0.35)] as [(PBAmountDirection?, Double)], id: \.1) { direction, fill in
                        PBBarRow(leading: .art(.dev), title: "Rent", caption: "51%", amount: "₹12,000", direction: direction,
                                 progress: fill, mark: 0.51, isOnCard: true)
                    }
                }
                .padding(.horizontal, PBSpace.s16)
                .pbCard(padding: 0)
            }
            GallerySection("Chart / Monthly Bars") {
                PBMonthlyBarChart(months: [
                    .init(label: "Apr", value: 18_400), .init(label: "May", value: 21_950),
                    .init(label: "Jun", value: 19_600), .init(label: "Jul", value: 20_600),
                    .init(label: "Aug", value: 22_150), .init(label: "Sep", value: 23_300),
                ])
                .padding(PBSpace.s20)
                .pbCard(padding: 0)
            }
            GallerySection("Card / Budget: On track · Over budget · Closed") {
                PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .onTrack(projected: 58.0 / 60),
                             percentLabel: "87% used", leftLabel: "₹8,000 left", plannedText: "Planned items bring it to ₹58,000")
                PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 0.976, status: .overBudget(warning: "₹1,500 over budget"),
                             plannedText: "Planned items bring it to ₹58,000")
                PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .closed,
                             percentLabel: "87% used", leftLabel: "₹8,000 left")
                GalleryLabel("Example: over budget (10-03)")
                PBBudgetCard(spent: "₹61,500", budget: "of ₹60,000", progress: 60 / 61.5, status: .overBudget(warning: "₹1,500 over budget"),
                             plannedText: "All planned items are bought.")
            }
            GallerySection("Card / Loan Progress: Active · Paid back") {
                PBLoanProgressCard(original: "₹6,000", paid: "₹0", remaining: "₹6,000", progress: 0, caption: "0% paid back")
                PBLoanProgressCard(original: "₹4,500", paid: "₹4,500", remaining: "₹0", progress: 1, caption: "Paid back on 14 Sep",
                                   isPaidBack: true)
            }
        }
    }
}

// MARK: - Assistant and scan

struct GalleryAssistantPage: View {
    @State private var isSaved = false
    @State private var amounts = ["430", "370", "450"]
    @State private var editing: Int?
    @State private var had: Set<String> = ["you", "esha", "dev"]

    private static let people: [PBAssignItemRow.Person] = [
        .init(id: "you", name: "You", avatar: .art(.arjun)),
        .init(id: "esha", name: "Esha", avatar: .art(.esha)),
        .init(id: "dev", name: "Dev", avatar: .art(.dev)),
    ]
    private static let lines = ["Chicken biryani", "Paneer tikka", "Fish and chips"]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Chat / Bubble: User · Assistant") {
                PBChatBubble(role: .user, text: "Who owes me money?")
                PBChatBubble(role: .assistant, text: "Who owes me money?")
            }
            GallerySection("Chat / Draft Expense: Pending · Saved (tap Save)") {
                draft(isSaved: true)
                draft(isSaved: isSaved, onSave: { isSaved = true }, onView: { isSaved = false })
                GalleryLabel("View resets the live card")
            }
            GallerySection("Row / Receipt Line: Default · Total") {
                VStack(spacing: 0) {
                    PBReceiptLineRow(label: "Chicken biryani", amount: .constant("430"), isEditing: .constant(false))
                    PBReceiptLineRow(label: "Chicken biryani", amount: .constant("430"), isTotal: true, isEditing: .constant(false))
                }
                .pbCard(padding: 0)
                GalleryLabel("Editing, live: tap an amount to fix it")
                VStack(spacing: 0) {
                    ForEach(Self.lines.indices, id: \.self) { index in
                        PBReceiptLineRow(label: Self.lines[index], amount: $amounts[index], isEditing: isEditing(index))
                    }
                    PBReceiptLineRow(label: "Total", amount: .constant(galleryGroupIndian(String(total))), isTotal: true,
                                     isEditing: .constant(false))
                }
                .pbCard(padding: 0)
            }
            GallerySection("Row / Assign Item (tap the people)") {
                PBAssignItemRow(item: "Chicken biryani", price: "₹430", people: Self.people, selected: ["dev"]) { _ in }
                PBAssignItemRow(item: "Fresh lime soda ×3", price: "₹270", people: Self.people, selected: had,
                                sharedCaption: had.count > 1 ? "Shared by \(had.count) · ₹\(270 / had.count) each" : nil) {
                    had.formSymmetricDifference([$0])
                }
            }
            GallerySection("Card / Person Totals") {
                PBPersonTotalsCard(status: "All items assigned", note: "Includes GST and tip", totals: [
                    .init(id: "you", name: "You", avatar: .art(.arjun), amount: "₹989"),
                    .init(id: "esha", name: "Esha", avatar: .art(.esha), amount: "₹621"),
                    .init(id: "dev", name: "Dev", avatar: .art(.dev), amount: "₹690"),
                ])
            }
            GallerySection("Control / Shutter: Default · Pressed") {
                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    PBShutterButton {}
                    Spacer(minLength: 0)
                    PBShutterButton {}
                        .pbPreviewInteraction(.pressed)
                    Spacer(minLength: 0)
                }
                .padding(PBSpace.s24)
                .frame(maxWidth: .infinity)
                .background(PBColor.bgCamera, in: .rect(cornerRadius: PBRadius.card))
            }
            GallerySection("Art / Receipt: Full · Thumb") {
                HStack(alignment: .top, spacing: PBSpace.s16) {
                    PBReceiptThumbnail(size: .full)
                        .frame(maxWidth: .infinity)
                    PBReceiptThumbnail()
                }
            }
        }
    }

    private var total: Int {
        amounts.map { Int($0) ?? 0 }.reduce(0, +)
    }

    private func isEditing(_ index: Int) -> Binding<Bool> {
        Binding {
            editing == index
        } set: { isOn in
            if isOn {
                editing = index
            } else if editing == index {
                editing = nil
            }
        }
    }

    private func draft(isSaved: Bool, onSave: @escaping () -> Void = {}, onView: @escaping () -> Void = {}) -> PBDraftExpenseCard {
        PBDraftExpenseCard(
            icon: .car, title: "Cab", amount: "₹600", paidLine: "Paid by you · Today",
            splitLine: "Split equally with Esha and Dev", members: [.arjun, .esha, .dev],
            eachLine: "₹200 each", isSaved: isSaved, onSave: onSave, onView: onView
        )
    }
}
#endif
