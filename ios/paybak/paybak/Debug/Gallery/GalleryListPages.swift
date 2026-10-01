#if DEBUG
import SwiftUI

// MARK: - Lists and detail

struct GalleryListsPage: View {
    @State private var isConfirmed = false

    private static let members: [PBPeepHead] = [.arjun, .priya, .rohan, .esha]
    private static let budget = PBGroupRow.Budget(progress: 0.87, spent: "₹52,000 of ₹60,000", left: "₹8,000 left")
    private static let balances: [PBGroupRow.Balance] = [.owe("₹1,400"), .owed("₹1,400"), .settled("Settled")]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Avatar / Pair: 32 · 56") {
                PBAvatarPair(from: .art(.arjun), to: .art(.kabir))
                PBAvatarPair(from: .art(.arjun), to: .art(.kabir), diameter: PBSize.avatarLg)
            }
            GallerySection("Row / Comment · Row / History: Middle · Last") {
                PBCommentRow(name: "Priya", avatar: .art(.priya), date: "27 Sep", text: "Was breakfast included?")
                VStack(spacing: 0) {
                    PBHistoryRow(text: "Kabir changed the amount from ₹17,500 to ₹18,000", date: "28 Sep")
                    PBHistoryRow(text: "Kabir changed the amount from ₹17,500 to ₹18,000", date: "28 Sep", isLast: true)
                }
            }
            GallerySection("Row / Person: Regular (on white)") {
                VStack(spacing: 0) {
                    personRows(.regular)
                }
            }
            GallerySection("Row / Person: Compact (in a card)") {
                VStack(spacing: 0) {
                    personRows(.compact)
                }
                .pbCard(padding: 0)
            }
            GallerySection("Row / Transfer") {
                VStack(spacing: 0) {
                    PBTransferRow(from: .art(.rohan), to: .art(.dev), title: "Rohan owes Dev", amount: "₹8,500", showsDivider: false)
                }
                .pbCard(padding: 0)
            }
            GallerySection("Header / Title Row: Tile · Avatar") {
                PBTitleHeader(title: "Goa Trip", leading: .icon(.plane), subtitle: "21–25 Sep · 5 members · ₹39,500 spent", members: Self.members)
                PBTitleHeader(title: "Goa Trip", leading: .art(.rohan), subtitle: "21–25 Sep · 5 members · ₹39,500 spent", members: Self.members)
                PBTitleHeader(title: "Aarav", leading: .initials("AR"), tag: "Guest")
            }
            GallerySection("Header / Amount Hero: Icon · Avatar · Pair") {
                PBAmountHero(leading: .avatar(.icon(.food)), title: "Seafood dinner at Britto’s", amount: "₹6,500",
                             meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"])
                PBAmountHero(leading: .avatar(.art(.dev)), title: "Seafood dinner at Britto’s", amount: "₹6,500",
                             meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"])
                PBAmountHero(leading: .pair(from: .art(.arjun), to: .art(.kabir)), title: "Seafood dinner at Britto’s", amount: "₹6,500",
                             meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"], status: "Disputed")
            }
            GallerySection("Card / Notice: Leading · Centered × None · One · Two") {
                notices
            }
            GallerySection("Card / Confirm Payment (tap Confirm)") {
                PBConfirmPaymentCard(
                    avatar: .art(.esha),
                    title: "Esha says she paid you ₹700",
                    detail: "Dinner at Olive Garden · UPI · 9:12 pm",
                    confirmedTitle: "Esha paid you ₹700",
                    confirmedDetail: "Dinner at Olive Garden · UPI · Confirmed",
                    isConfirmed: isConfirmed,
                    testIDPrefix: "gallery.confirm",
                    onConfirm: { isConfirmed = true }
                )
                if isConfirmed {
                    PBButton("Reset", style: .secondary, size: .small) { isConfirmed = false }
                }
            }
            GallerySection("Card / QR Code (scans https://paybak.app/i/arjun)") {
                PBQRCodeCard(link: "https://paybak.app/i/arjun")
                    .padding(PBSpace.s24)
                    .frame(maxWidth: .infinity)
                    .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            }
            GallerySection("Row / Group: Group · Project · Archived") {
                VStack(spacing: 0) {
                    ForEach(Self.balances.indices, id: \.self) { index in
                        PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .plane, balance: Self.balances[index]) {}
                    }
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .package, balance: .settled("Settled"),
                               isArchived: true) {}
                    ForEach(Self.balances.indices, id: \.self) { index in
                        PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .drone, balance: Self.balances[index],
                                   budget: Self.budget, showsDivider: index < Self.balances.count - 1) {}
                    }
                }
            }
        }
    }

    /// Every trailing type, then a guest with an overdue amount.
    @ViewBuilder
    private func personRows(_ size: PBPersonRow.Size) -> some View {
        let trailings: [PBPersonRow.Trailing] = [
            .amount("₹700", direction: .owed, label: "Due Sun 4 Oct"),
            .amount("₹700", direction: .owe, label: "Due Sun 4 Oct"),
            .amount("₹700", label: "Due Sun 4 Oct"),
            .status("Settled"),
            .check,
            .select(true),
            .select(false),
            .remove({}),
            .button(size == .regular ? "Invite" : "Remind", {}),
            .none,
        ]
        ForEach(trailings.indices, id: \.self) { index in
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", size: size, trailing: trailings[index],
                        action: {})
        }
        PBPersonRow(name: "Aarav", avatar: .initials("AR"), subtitle: "Dinner at Olive Garden", tag: "Guest", size: size,
                    trailing: .amount("₹700", direction: .owed, label: "Due Sun 4 Oct", overdue: "Overdue 3 days"), showsDivider: false)
    }

    @ViewBuilder
    private var notices: some View {
        ForEach([PBNoticeCard.Layout.leading, .centered], id: \.self) { layout in
            let centered = layout == .centered
            let icon: PBIcon = centered ? .lock : .activity
            PBNoticeCard(icon: icon, title: "Pending confirmation", message: "Waiting for Meera to confirm", layout: layout)
            PBNoticeCard(icon: icon, title: "Pending confirmation", message: "Waiting for Meera to confirm", layout: layout,
                         primary: .init(centered ? "See Pro" : "Send invite") {})
            PBNoticeCard(icon: icon, title: "Pending confirmation", message: "Waiting for Meera to confirm", layout: layout,
                         primary: .init(centered ? "See Pro" : "Edit expense") {}, secondary: .init(centered ? "Not now" : "Resolve") {})
        }
        GalleryLabel("With the Pro badge · without a title")
        PBNoticeCard(icon: .lock, title: "Insights", message: "Insights are part of Pro.", badge: "Pro")
        PBNoticeCard(icon: .shuffle, message: "We simplified 5 debts into 3 payments.")
    }
}
#endif
