#if DEBUG
import SwiftUI

// Gallery pages for components-app.md §2.8 and §3: the plan card, Lists & Detail.

struct GalleryPlanPairsPage: View {
    @State private var yearly = true

    var body: some View {
        GalleryPageScroll {
            GallerySection("Card / Plan: Selected True · False (tap)") {
                HStack(spacing: PBSpace.s20) {
                    PBPlanCard(period: "Yearly", price: "₹799/year", detail: "₹67/month", badge: "Save 33%", isSelected: yearly) { yearly = true }
                    PBPlanCard(period: "Monthly", price: "₹99/month", detail: "Billed monthly", isSelected: !yearly) { yearly = false }
                }
            }
            GallerySection("Avatar / Pair: 32 · 56 · white on a card") {
                HStack(spacing: PBSpace.s24) {
                    PBAvatarPair(from: .art(.arjun), to: .art(.kabir))
                    PBAvatarPair(from: .art(.arjun), to: .art(.kabir), diameter: PBSize.avatarLg)
                }
                PBAvatarPair(from: .art(.rohan), to: .art(.dev), isOnCard: true).pbCard()
            }
            GallerySection("Row / Comment") {
                VStack(spacing: 0) {
                    PBCommentRow(name: "Priya", avatar: .art(.priya), date: "27 Sep", text: "Was breakfast included?")
                    PBCommentRow(name: "Kabir", avatar: .art(.kabir), date: "28 Sep", text: "Yes, and the late checkout too. I added the receipt photo.")
                }
            }
            GallerySection("Row / History: Middle · Last") {
                VStack(spacing: 0) {
                    PBHistoryRow(text: "Kabir changed the amount from ₹17,500 to ₹18,000", date: "28 Sep")
                    PBHistoryRow(text: "Kabir changed the amount from ₹17,500 to ₹18,000", date: "28 Sep", isLast: true)
                }
            }
        }
    }
}

struct GalleryPersonRowsPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Row / Person, Size=Regular: all trailing types") {
                VStack(spacing: 0) {
                    ForEach(Array(trailings.enumerated()), id: \.offset) { _, trailing in
                        PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: trailing(.regular))
                    }
                    PBPersonRow(name: "Arjun R", avatar: .initials("AR"), subtitle: "Not on Paybak", tag: "Guest", trailing: .amount("₹700", direction: .owed, label: "Due Sun 4 Oct", overdue: "Overdue 3 days"))
                }
            }
            GallerySection("Size=Compact (inside a card)") {
                VStack(spacing: 0) {
                    ForEach(Array(trailings.enumerated()), id: \.offset) { index, trailing in
                        PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", size: .compact, trailing: trailing(.compact), showsDivider: index < trailings.count - 1)
                    }
                }
                .pbCard(padding: 0)
            }
        }
    }

    private var trailings: [(PBPersonRow.Size) -> PBPersonRow.Trailing] {
        [
            { _ in .amount("₹700", direction: .owed, label: "Due Sun 4 Oct") },
            { _ in .amount("₹700", direction: .owe, label: "Due Sun 4 Oct") },
            { _ in .amount("₹700", label: "Due Sun 4 Oct") },
            { _ in .status("Settled") },
            { _ in .check },
            { _ in .select(true) },
            { _ in .select(false) },
            { _ in .remove {} },
            { $0 == .regular ? .button("Invite") {} : .button("Remind") {} },
            { _ in .none },
        ]
    }
}

struct GalleryTransferHeadersPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Row / Transfer") {
                VStack(spacing: 0) {
                    PBTransferRow(from: .art(.rohan), to: .art(.dev), title: "Rohan owes Dev", amount: "₹8,500")
                    PBTransferRow(from: .art(.priya), to: .art(.arjun), title: "Priya pays you", amount: "₹1,200", showsDivider: false)
                }
                .pbCard(padding: 0)
            }
            GallerySection("Header / Title Row: Tile · Avatar · guest, no members") {
                PBTitleHeader(title: "Goa Trip", leading: .icon(.plane), subtitle: "21–25 Sep · 5 members · ₹39,500 spent", members: [.arjun, .priya, .rohan, .esha])
                PBTitleHeader(title: "Goa Trip", leading: .art(.rohan), subtitle: "21–25 Sep · 5 members · ₹39,500 spent", members: [.arjun, .priya, .rohan, .esha])
                PBTitleHeader(title: "Arjun R", leading: .initials("AR"), tag: "Guest")
            }
            GallerySection("Header / Amount Hero: Icon · Avatar · Pair") {
                PBAmountHero(leading: .avatar(.icon(.food)), title: "Seafood dinner at Britto’s", amount: "₹6,500", meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"])
                PBAmountHero(leading: .avatar(.art(.dev)), title: "Seafood dinner at Britto’s", amount: "₹6,500", meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"])
                PBAmountHero(leading: .pair(from: .art(.arjun), to: .art(.kabir)), title: "Seafood dinner at Britto’s", amount: "₹6,500", meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"], status: "Disputed")
            }
        }
    }
}

struct GalleryNoticesPage: View {
    var body: some View {
        GalleryPageScroll {
            GallerySection("Card / Notice, Layout=Leading: None · One · Two · no title · badge") {
                PBNoticeCard(icon: .activity, title: "Pending confirmation", message: "Waiting for Meera to confirm")
                PBNoticeCard(icon: .activity, title: "Pending confirmation", message: "Waiting for Meera to confirm", primary: .init("Send invite") {})
                PBNoticeCard(icon: .activity, title: "Pending confirmation", message: "Waiting for Meera to confirm", primary: .init("Edit expense") {}, secondary: .init("Resolve") {})
                PBNoticeCard(icon: .shuffle, message: "We simplified 5 debts into 3 payments.")
                PBNoticeCard(icon: .lock, title: "Recurring expenses", message: "Repeat rent and bills every month.", badge: "Pro")
            }
            GallerySection("Layout=Centered: None · One · Two") {
                PBNoticeCard(icon: .lock, title: "Pending confirmation", message: "Waiting for Meera to confirm", layout: .centered)
                PBNoticeCard(icon: .lock, title: "Pending confirmation", message: "Waiting for Meera to confirm", layout: .centered, primary: .init("See Pro") {})
                PBNoticeCard(icon: .lock, title: "Pending confirmation", message: "Waiting for Meera to confirm", badge: "Pro", layout: .centered, primary: .init("See Pro") {}, secondary: .init("Not now") {})
            }
        }
    }
}

struct GalleryConfirmQRPage: View {
    @State private var isConfirmed = false

    var body: some View {
        GalleryPageScroll {
            GallerySection("Card / Confirm Payment: Pending · Confirmed") {
                card(isConfirmed: false)
                card(isConfirmed: true)
            }
            GallerySection("Live: Confirm collapses the card (250 ms)") {
                card(isConfirmed: isConfirmed, testIDPrefix: "gallery.confirm")
                PBButton("Reset", style: .secondary, size: .small) { isConfirmed = false }
            }
            GallerySection("Card / QR Code (scans https://paybak.app/i/arjun)") {
                PBQRCodeCard(link: "https://paybak.app/i/arjun")
                    .padding(PBSpace.s24)
                    .frame(maxWidth: .infinity)
                    .background(PBColor.bgCamera, in: .rect(cornerRadius: PBRadius.card))
            }
        }
    }

    private func card(isConfirmed: Bool, testIDPrefix: String? = nil) -> PBConfirmPaymentCard {
        PBConfirmPaymentCard(
            avatar: .art(.esha),
            title: "Esha says she paid you ₹700",
            detail: "Dinner at Olive Garden · UPI · 9:12 pm",
            confirmedTitle: "Esha paid you ₹700",
            confirmedDetail: "Dinner at Olive Garden · UPI · Confirmed",
            isConfirmed: isConfirmed,
            testIDPrefix: testIDPrefix,
            onConfirm: { self.isConfirmed = true }
        )
    }
}

struct GalleryGroupRowsPage: View {
    private let budget = PBGroupRow.Budget(progress: 52_000.0 / 60_000, spent: "₹52,000 of ₹60,000", left: "₹8,000 left")

    var body: some View {
        GalleryPageScroll {
            GallerySection("Row / Group, Type=Group: Owe · Owed · Settled · Archived") {
                VStack(spacing: 0) {
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .plane, balance: .owe("₹1,400")) {}
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .plane, balance: .owed("₹1,400")) {}
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .plane) {}
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .package, isArchived: true) {}
                }
            }
            GallerySection("Type=Project: Owe · Owed · Settled") {
                VStack(spacing: 0) {
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .drone, balance: .owe("₹1,400"), budget: budget) {}
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .drone, balance: .owed("₹1,400"), budget: budget) {}
                    PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .drone, budget: budget) {}
                }
            }
        }
    }
}
#endif
