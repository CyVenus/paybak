#if DEBUG
import SwiftUI

/// The shell's shared pieces (app-architecture §5.1 ★): tab bar, Add sheet rows, nav headers.
struct GalleryShellNavigationPage: View {
    @State private var tab = Tab.home

    var body: some View {
        GalleryPageScroll {
            GallerySection("Navigation / Tab Bar: Active = each tab") {
                ForEach(Tab.allCases, id: \.self) { active in
                    PBTabBar(selection: active, onSelect: { tab = $0 }, onAdd: {})
                }
            }
            GallerySection("Navigation / Tab Bar Item: Active · Inactive") {
                HStack {
                    PBTabItem(tab: .home, isActive: true) {}
                    PBTabItem(tab: .groups, isActive: false) {}
                }
            }
            GallerySection("Sheet / Action Row: Default · Pressed") {
                PBSheetRow(title: "Add expense", subtitle: "Split a bill with friends or a group", icon: .receipt) {}
                PBSheetRow(title: "Record payment", subtitle: "Log money you paid or received", icon: .exchange) {}
                    .pbPreviewInteraction(.pressed)
            }
            GallerySection("Sheet / Action Sheet (body)") {
                PBAddSheet(onClose: {}, onSelect: { _ in })
                    .padding(.bottom, PBSpace.s28)
                    .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.sheet))
                    .overlay(RoundedRectangle(cornerRadius: PBRadius.sheet).stroke(PBColor.borderSubtle))
            }
            GallerySection("Nav Header: Large Title (action / none) · Inline") {
                PBNavHeader(title: "Activity", action: .init(icon: .restore, accessibilityLabel: "Recently deleted") {})
                PBNavHeader(title: "Profile")
                PBInlineNavHeader(title: "Groups")
            }
        }
    }
}

/// Balance cards, activity rows, empty states and the user's avatar.
struct GalleryShellCardsPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Card / Balance Summary · Card / Balance: Settled, badge, action") {
                PBBalanceSummary(totals: HomeTotals(owed: 290_000, owedPeople: 4, owe: 185_000, oweGroups: 2), onSettleUp: {})
                PBBalanceCard(kind: .settled, amount: "₹0", caption: "Nothing pending")
                PBBalanceCard(kind: .owed, amount: "+₹800", caption: "Movie tickets", badge: ("Overdue 3 days", .overdue))
            }
            GallerySection("Row / Activity: Plain · On Card · badge · action · unread") {
                PBActivityRow(leading: .icon(.food), title: "Dinner at Olive Garden", subtitle: "You paid · 4 people",
                              trailing: .amount("₹2,800", date: "Today", isIncoming: true))
                PBActivityRow(leading: .icon(.bolt), title: "Electricity bill", subtitle: "Flat 302 · You owe",
                              trailing: .amount("−₹450", date: "26 Sep", isIncoming: false))
                PBActivityRow(leading: .avatar(.art(.priya)), title: "Priya paid you", subtitle: "UPI",
                              trailing: .amount("₹1,050", date: "Yesterday", isIncoming: true), isUnread: true)
                PBActivityRow(leading: .icon(.flame), title: "Cooking gas draft created", subtitle: "Flat 302 · Needs an amount",
                              trailing: .badge("Draft"))
                PBActivityRow(leading: .icon(.food), title: "Snacks", subtitle: "₹300 · Goa Trip",
                              detail: "Deleted by Priya on 24 Sep · 24 days left", trailing: .action("Restore") {})
            }
            GallerySection("Card / Empty State: First day · All settled") {
                PBEmptyState(primary: .init(title: "Add expense", icon: .plus) {},
                             secondary: .init(title: "Invite friends", icon: .userAdd) {})
                PBEmptyState.allSettled
            }
        }
    }
}
#endif
