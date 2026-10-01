#if DEBUG
import SwiftUI

/// The app shell's shared pieces (M2): tab bar, Add sheet, headers, Home cards and rows.
struct GalleryShellPage: View {
    @State private var tab = Tab.home
    @State private var segment = 0

    var body: some View {
        GalleryPageScroll {
            GallerySection("Navigation / Tab Bar (tap to select)") {
                PBTabBar(selection: tab, onSelect: { tab = $0 }, onAdd: {})
                    .padding(.vertical, PBSpace.s20)
                    .frame(maxWidth: .infinity)
                    .background(GalleryStripes())
            }
            GallerySection("Sheet / Action Sheet") {
                GallerySheetContainer {
                    PBAddSheet(onClose: {}, onSelect: { _ in })
                }
                .padding(PBSpace.s8)
                .background(PBColor.bgCard)
            }
            GallerySection("Nav Header: Large Title · Inline") {
                PBNavHeader(title: "Groups", action: .init(icon: .plus, accessibilityLabel: "New group") {})
                PBInlineNavHeader(title: "Groups")
                // The Activity header: the title with Restore, then Timeline · Insights 16 below.
                VStack(spacing: PBSpace.s16) {
                    PBNavHeader(title: "Activity", action: .init(icon: .restore, accessibilityLabel: "Recently deleted") {})
                    PBSegmentedControl(options: ["Timeline", "Insights"], selection: $segment)
                }
            }
            GallerySection("Card / Balance Summary · Balance: Settled · with badge") {
                PBBalanceSummary(totals: HomeTotals(owed: 290_000, owedPeople: 4, owe: 185_000, oweGroups: 2), onSettleUp: {})
                PBBalanceCard(kind: .settled, amount: "₹0", caption: "Nothing pending", onTap: {})
                PBBalanceCard(kind: .owed, amount: "+₹800", caption: "Movie tickets", badge: ("Overdue 3 days", .overdue))
            }
            GallerySection("Row / Activity: Plain · On Card") {
                VStack(spacing: 0) {
                    PBActivityRow(leading: .icon(.food), title: "Dinner at Olive Garden", subtitle: "You paid · 4 people",
                                  trailing: .amount("₹2,800", date: "Today", isIncoming: true))
                    PBActivityRow(leading: .icon(.bolt), title: "Electricity bill", subtitle: "Flat 302 · You owe",
                                  trailing: .amount("−₹450", date: "26 Sep", isIncoming: false))
                    PBActivityRow(leading: .avatar(.art(.priya)), title: "Priya paid you", subtitle: "UPI",
                                  trailing: .amount("₹1,050", date: "Yesterday", isIncoming: true), isUnread: true)
                    PBActivityRow(leading: .icon(.flame), title: "Cooking gas draft created", subtitle: "Flat 302 · Needs an amount",
                                  trailing: .badge("Draft"))
                }
                VStack(spacing: 0) {
                    PBActivityRow(leading: .avatar(.art(.rohan)), title: "You paid Rohan", subtitle: "UPI · Goa Trip",
                                  trailing: .amount("−₹1,400", date: "Today", isIncoming: false), surface: .onCard, showsDivider: true)
                    PBActivityRow(leading: .icon(.food), title: "Snacks", subtitle: "₹300 · Goa Trip",
                                  detail: "Deleted by Priya on 24 Sep · 24 days left", trailing: .action("Restore") {}, surface: .onCard)
                }
                .padding(.horizontal, PBSpace.s16)
                .pbCard(padding: 0)
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
