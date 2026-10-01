import SwiftUI

/// Home — Active (screens-home §2, home-v2 §3): a Confirm card per pending claim (newest first), the
/// balance summary with Settle up, Due soon and Recent activity. Every element opens its flow
/// (app-architecture §2.3).
struct HomeActiveContent: View {
    let home: HomeSummary
    /// A claim was confirmed: Home plays the card's Confirmed state before the numbers change.
    let onClaimConfirmed: () -> Void

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let books = ledgerStore.books
        if !home.pendingClaims.isEmpty {
            VStack(spacing: PBSpace.s12) {
                ForEach(home.pendingClaims) { claim in
                    PendingClaimCard(claim: claim, testIDPrefix: "home.confirmCard", onConfirmed: onClaimConfirmed)
                        .accessibilityElement(children: .contain)
                        .accessibilityIdentifier("home.confirmCard")
                        .transition(cardTransition)
                }
            }
            .transition(cardTransition)
        }
        PBBalanceSummary(
            totals: home.totals,
            currency: books.defaultCurrency,
            onOwed: { router.open(.owedBreakdown) },
            onOwe: { router.open(.oweBreakdown) },
            onSettleUp: { router.open(.settleUp(groupId: nil)) }
        )
        // New totals swap in place while the cards slide up after a Confirm.
        .contentTransition(.identity)
        if !home.dueSoon.isEmpty {
            dueSoon(books)
        }
        if !home.recent.isEmpty {
            recentActivity(books)
        }
    }

    /// A confirmed card collapses; with Reduce Motion it only fades, as on Android.
    private var cardTransition: AnyTransition {
        reduceMotion ? .opacity : .collapse
    }

    private func dueSoon(_ books: Books) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            PBSectionHeader("Due soon")
            VStack(spacing: PBSpace.s8) {
                ForEach(home.dueSoon, id: \.self) { row in
                    PBAttentionRow(
                        avatar: row.isGroupShare ? .icon(.groups) : avatar(of: row.obligation.friend, in: books),
                        title: row.title,
                        detail: row.detail,
                        amount: Money.format(row.amount, books.defaultCurrency),
                        badge: row.badge,
                        isOverdue: row.isOverdue,
                        actionTitle: row.action == .remind ? "Remind" : "Settle",
                        testID: "home.due.\(row.isGroupShare ? row.obligation.ref : row.obligation.friend)",
                        onAction: { router.open(row.actionRoute(in: books)) },
                        onTap: { router.open(row.route) }
                    )
                }
            }
        }
    }

    private func recentActivity(_ books: Books) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("Recent activity") {
                router.select(.activity)
                router.activitySegment = .timeline
            }
            .accessibilityIdentifier("home.seeAll")
            ForEach(home.recent) { row in
                Button {
                    router.open(row.route)
                } label: {
                    PBActivityRow(leading: leading(row, in: books), title: row.title, subtitle: row.subtitle,
                                  trailing: .amount(row.amount, date: row.date, isIncoming: row.isIncoming))
                }
                .buttonStyle(PBRowButtonStyle())
                .accessibilityIdentifier("home.activity.\(row.recordID)")
                .transition(.newRow)
            }
        }
    }

    private func leading(_ row: RecentActivityRow, in books: Books) -> PBActivityRow.Leading {
        switch row.kind {
        case .expense(_, let category): .icon(category.pbIcon)
        case .payment(_, let person): .avatar(avatar(of: person, in: books))
        }
    }

    private func avatar(of person: PersonID, in books: Books) -> PBAvatar.Content {
        books.ledger.person(person)?.avatarContent ?? .icon(.profile)
    }
}

/// A confirmed card folds up into its top edge and fades while the content below moves up.
private struct Collapse: ViewModifier {
    let progress: CGFloat

    func body(content: Content) -> some View {
        content
            .scaleEffect(x: 1, y: max(progress, 0.001), anchor: .top)
            .opacity(progress)
    }
}

private extension AnyTransition {
    static var collapse: AnyTransition {
        .asymmetric(insertion: .opacity, removal: .modifier(active: Collapse(progress: 0), identity: Collapse(progress: 1)))
    }

    /// A new activity row (the payment just confirmed) fades in while the rows around it move, in the
    /// 250 ms of the card's collapse, as on Android.
    static var newRow: AnyTransition {
        .asymmetric(insertion: .opacity.animation(.easeOut(duration: 0.25)), removal: .opacity)
    }
}
