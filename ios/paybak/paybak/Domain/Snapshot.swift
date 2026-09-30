import Foundation

/// Home's read model (domain.md §6.1).
nonisolated struct HomeSummary: Hashable, Sendable {
    var state: HomeState
    var totals: HomeTotals
    var dueSoon: [DueSoonRow]
    var recent: [RecentActivityRow]
    /// Newest first; each shows as a Confirm card above the balances.
    var pendingClaims: [PendingClaim]
    /// The bell's dot: any unread inbox item.
    var hasUnread: Bool
}

/// Everything the tab roots show, recomputed after every ledger change (app-architecture §3.3).
/// Parametrised reads (a group, a project, a month of Insights) are `Books` queries instead.
nonisolated struct LedgerSnapshot: Sendable {
    var home: HomeSummary
    /// Friends list order.
    var friends: [FriendBalance]
    /// Groups list order (archived last).
    var groups: [GroupSummary]
    /// Settle up: payments to make.
    var settlePay: [SettleRow]
    /// Settle up: people who owe you.
    var settleGet: [SettleRow]
    /// The Owe breakdown footnote's groups.
    var simplifiedGroups: [LedgerGroup]
    var timeline: [TimelineDay]
    var inbox: [InboxRow]
    var unreadCount: Int
    var recentlyDeleted: [DeletedExpenseRow]
    var recentCurrencies: [String]
    var isPro: Bool

    /// The You're owed breakdown rows (§6.2).
    var owedBreakdown: [SettleRow] { settleGet }
    /// The You owe breakdown rows (§6.2).
    var oweBreakdown: [SettleRow] { settlePay }

    init(_ books: Books) {
        let contexts = books.contexts()
        let totals = books.homeTotals(contexts: contexts)
        let claims = books.pendingClaims()
        let events = books.timeline()
        let inbox = books.inbox()
        let settle = books.settleRows(contexts: contexts)
        let unread = inbox.filter { !$0.item.read }.count
        home = HomeSummary(
            state: books.homeState(totals: totals, hasClaims: !claims.isEmpty),
            totals: totals,
            dueSoon: books.dueSoon(contexts: contexts),
            recent: books.recentActivity(timeline: events),
            pendingClaims: claims,
            hasUnread: unread > 0
        )
        friends = books.friendBalances(contexts: contexts)
        groups = books.groupSummaries(contexts: contexts)
        settlePay = settle.pay
        settleGet = settle.get
        simplifiedGroups = books.simplifiedFootnoteGroups()
        timeline = books.timelineDays(events)
        self.inbox = inbox
        unreadCount = unread
        recentlyDeleted = books.recentlyDeleted()
        recentCurrencies = books.recentCurrencies()
        isPro = books.ledger.settings.entitlement.isPro
    }
}
