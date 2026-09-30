#if DEBUG
import SwiftUI

// Gallery pages for components-app.md §4–7: Progress & Charts, Assistant & Scan, the toast, the
// sheet and the receipt art.

struct GalleryProgressPage: View {
    @State private var live = 0.3

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Progress Bar: Small · Large × Default · Projected · Over") {
                VStack(spacing: PBSpace.s16) {
                    PBProgressBar(value: 0.6)
                    PBProgressBar(value: 0.87, projected: 0.97)
                    PBProgressBar(value: 0.976, overFrom: 0.976)
                    PBProgressBar(value: 0.6, size: .large)
                    PBProgressBar(value: 0.87, projected: 0.97, size: .large)
                    PBProgressBar(value: 0.976, overFrom: 0.976, size: .large)
                }
            }
            GallerySection("Show mark=true (examples), then live (tap)") {
                VStack(spacing: PBSpace.s16) {
                    PBProgressBar(value: 0.6, mark: 0.51)
                    PBProgressBar(value: 0.976, overFrom: 0.976, mark: 0.976, size: .large)
                    PBProgressBar(value: live, projected: min(1, live + 0.1), size: .large)
                        .onTapGesture { live = live > 0.9 ? 0.1 : live + 0.2 }
                }
            }
            GallerySection("Row / Bar: Icon · Avatar × Neutral · Owed · Owe") {
                VStack(spacing: 0) {
                    PBBarRow(leading: .icon(.home), title: "Rent", caption: "51%", amount: "₹12,000", progress: 0.51)
                    PBBarRow(leading: .icon(.home), title: "Rent", caption: "51%", amount: "₹12,000", direction: .owed, progress: 0.51)
                    PBBarRow(leading: .icon(.home), title: "Rent", caption: "51%", amount: "₹12,000", direction: .owe, progress: 0.51)
                }
                VStack(spacing: 0) {
                    PBBarRow(leading: .art(.dev), title: "Rent", caption: "51%", amount: "₹12,000", progress: 0.51, mark: 0.51, isOnCard: true)
                    PBBarRow(leading: .art(.dev), title: "Rent", caption: "51%", amount: "₹12,000", direction: .owed, progress: 1, mark: 0.51, isOnCard: true)
                    PBBarRow(leading: .art(.dev), title: "Rent", caption: "51%", amount: "₹12,000", direction: .owe, progress: 0.35, mark: 0.51, isOnCard: true)
                }
                .pbCard()
            }
        }
    }
}

struct GalleryChartCardsPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Chart / Monthly Bars (Apr–Sep 2026)") {
                PBMonthlyBarChart(months: [
                    .init(label: "Apr", value: 18_400), .init(label: "May", value: 21_800),
                    .init(label: "Jun", value: 19_600), .init(label: "Jul", value: 20_500),
                    .init(label: "Aug", value: 22_100), .init(label: "Sep", value: 23_300),
                ])
                .padding(PBSpace.s20)
                .pbCard(padding: 0)
            }
            GallerySection("Card / Budget: On track · Over budget (10-03) · Closed") {
                PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .onTrack(projected: 58.0 / 60), percentLabel: "87% used", leftLabel: "₹8,000 left", plannedText: "Planned items bring it to ₹58,000")
                PBBudgetCard(spent: "₹61,500", budget: "of ₹60,000", progress: 60 / 61.5, status: .overBudget(warning: "₹1,500 over budget"), plannedText: "All planned items are bought.")
                PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .closed, percentLabel: "87% used", leftLabel: "₹8,000 left")
            }
            GallerySection("Card / Loan Progress: Active · Paid back (Kabir)") {
                PBLoanProgressCard(original: "₹6,000", paid: "₹0", remaining: "₹6,000", progress: 0, caption: "0% paid back")
                PBLoanProgressCard(original: "₹4,500", paid: "₹4,500", remaining: "₹0", progress: 1, caption: "Paid back on 14 Sep", isPaidBack: true)
            }
        }
    }
}

struct GalleryAssistantPage: View {
    @State private var isSaved = false

    var body: some View {
        GalleryPageScroll {
            GallerySection("Chat / Bubble: User · Assistant") {
                PBChatBubble(role: .user, text: "Who owes me money?")
                PBChatBubble(role: .assistant, text: "Who owes me money?")
            }
            GallerySection("Chat / Draft Expense: Pending · Saved") {
                draft(isSaved: false)
                draft(isSaved: true)
            }
            GallerySection("Live: Save swaps to Saved (250 ms)") {
                draft(isSaved: isSaved)
                PBButton("Reset", style: .secondary, size: .small) { isSaved = false }
            }
        }
    }

    private func draft(isSaved: Bool) -> PBDraftExpenseCard {
        PBDraftExpenseCard(
            icon: .car, title: "Cab", amount: "₹600", paidLine: "Paid by you · Today",
            splitLine: "Split equally with Esha and Dev", members: [.arjun, .esha, .dev],
            eachLine: "₹200 each", isSaved: isSaved, onSave: { self.isSaved = true }
        )
    }
}

struct GalleryScanPage: View {
    @State private var biryani = "430"
    @State private var editing = false
    @State private var assigned: Set<String> = ["dev"]

    private let people: [PBAssignItemRow.Person] = [
        .init(id: "you", name: "You", avatar: .art(.arjun)),
        .init(id: "esha", name: "Esha", avatar: .art(.esha)),
        .init(id: "dev", name: "Dev", avatar: .art(.dev)),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Row / Receipt Line: Default · Total × Default · Editing (tap ₹430)") {
                VStack(spacing: 0) {
                    PBReceiptLineRow(label: "Chicken biryani", amount: $biryani, isEditing: $editing)
                    PBReceiptLineRow(label: "Chicken biryani", amount: .constant("430"), isEditing: .constant(true))
                        .pbPreviewInteraction(.focused)
                    PBReceiptLineRow(label: "Chicken biryani", amount: .constant("430"), isTotal: true, isEditing: .constant(false))
                    PBReceiptLineRow(label: "Chicken biryani", amount: .constant("430"), isTotal: true, isEditing: .constant(true))
                        .pbPreviewInteraction(.focused)
                }
                .pbCard(padding: 0)
            }
            GallerySection("Row / Assign Item: Shared False (tap chips) · True") {
                VStack(spacing: 0) {
                    PBAssignItemRow(item: "Chicken biryani", price: "₹430", people: people, selected: assigned, sharedCaption: assigned.count > 1 ? "Shared by \(assigned.count)" : nil) {
                        assigned.formSymmetricDifference([$0])
                    }
                    PBAssignItemRow(item: "Chicken biryani", price: "₹430", people: people, selected: ["you", "esha", "dev"], sharedCaption: "Shared by 3 · ₹80 each") { _ in }
                }
            }
            GallerySection("Card / Person Totals") {
                PBPersonTotalsCard(status: "All items assigned", note: "Includes GST and tip", totals: [
                    .init(id: "you", name: "You", avatar: .art(.arjun), amount: "₹989"),
                    .init(id: "esha", name: "Esha", avatar: .art(.esha), amount: "₹621"),
                    .init(id: "dev", name: "Dev", avatar: .art(.dev), amount: "₹690"),
                ])
            }
            GallerySection("Control / Shutter: Default · Pressed · Art / Receipt Thumb") {
                HStack(spacing: PBSpace.s32) {
                    PBShutterButton {}
                    PBShutterButton {}.pbPreviewInteraction(.pressed)
                    PBReceiptThumbnail()
                }
                .padding(PBSpace.s24)
                .frame(maxWidth: .infinity)
                .background(PBColor.bgCamera, in: .rect(cornerRadius: PBRadius.card))
            }
            GallerySection("Art / Receipt: Full") {
                PBReceiptThumbnail(size: .full)
                    .frame(maxWidth: .infinity)
            }
        }
    }
}

struct GallerySheetToastPage: View {
    @State private var toast: PBToastMessage?
    @State private var showsSheet = false
    @State private var query = ""
    @State private var category = "Food"

    var body: some View {
        GalleryPageScroll {
            GallerySection("Overlay / Toast: icon · no icon (tap for a live one)") {
                PBToast("Expense added")
                PBToast("Payment recorded", icon: nil)
                PBButton("Show a toast", style: .secondary, size: .small) { toast = PBToastMessage("Expense added") }
            }
            GallerySection("Sheet / Container example: Category (tap to present)") {
                sheet
                    .allowsHitTesting(false)
                    .padding(.bottom, PBSpace.s28)
                    .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.sheet))
                    .overlay(alignment: .top) {
                        Capsule().fill(PBColor.bgIndicator).frame(width: 60, height: 4).padding(.top, PBSpace.s8)
                    }
                    .padding(PBSpace.s8)
                    .background(PBColor.bgScrim, in: .rect(cornerRadius: PBRadius.card))
                    .contentShape(.rect)
                    .onTapGesture { showsSheet = true }
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("Present the category sheet")
                    .accessibilityAddTraits(.isButton)
                    .accessibilityIdentifier("gallery.sheetPreview")
            }
        }
        .pbToast($toast, bottomPadding: 50)
        .pbSheet(isPresented: $showsSheet) { sheet(testIDPrefix: "gallery.sheet") }
    }

    private var sheet: some View { sheet(testIDPrefix: nil) }

    private func sheet(testIDPrefix: String?) -> some View {
        PBSheet(title: "Category", search: $query, searchPrompt: "Search categories", testIDPrefix: testIDPrefix, onClose: { showsSheet = false }) {
            VStack(spacing: 0) {
                ForEach([("Food", PBIcon.food), ("Travel", .car), ("Stays", .bed)], id: \.0) { name, icon in
                    PBSettingRow(name, icon: icon, trailing: category == name ? .check : .unchecked, showsDivider: name != "Stays") {
                        category = name
                    }
                    .accessibilityIdentifier(testIDPrefix.map { "\($0).\(name)" } ?? "")
                }
            }
            .pbCard(padding: 0)
        }
    }
}
#endif
