package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.DeletedRow
import app.paybak.paybak.domain.calc.FriendBalance
import app.paybak.paybak.domain.calc.GroupSummary
import app.paybak.paybak.domain.calc.HomeSummary
import app.paybak.paybak.domain.calc.InboxRow
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.SettlePlan
import app.paybak.paybak.domain.calc.SettleRow
import app.paybak.paybak.domain.calc.TimelineDay
import app.paybak.paybak.domain.calc.friendBalances
import app.paybak.paybak.domain.calc.groupSummaries
import app.paybak.paybak.domain.calc.homeSummary
import app.paybak.paybak.domain.calc.inboxRows
import app.paybak.paybak.domain.calc.recentCurrencies
import app.paybak.paybak.domain.calc.recentlyDeleted
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.calc.simplifiedFootnoteGroups
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.domain.calc.timelineDays
import app.paybak.paybak.domain.model.Ledger
import java.time.Instant
import java.time.ZoneId

/**
 * Every read model the tab roots show, recomputed after each change (app-architecture §3.3).
 * Parametrised queries (a group sheet, a friend page, a project report, Insights for a month …) are
 * functions of [view], which has the same data and caches.
 */
class LedgerSnapshot private constructor(val view: LedgerView) {
    val ledger: Ledger
        get() = view.ledger

    val isPro: Boolean
        get() = ledger.settings.entitlement.isPro

    private val events = view.timeline()

    val home: HomeSummary = view.homeSummary(events)
    val friends: List<FriendBalance> = view.friendBalances()
    val groups: List<GroupSummary> = view.groupSummaries()
    val settlePlan: SettlePlan = view.settlePlan()
    val timeline: List<TimelineDay> = view.timelineDays(events)
    val inbox: List<InboxRow> = view.inboxRows()
    val unreadCount: Int = ledger.inbox.count { !it.read }
    val recentlyDeleted: List<DeletedRow> = view.recentlyDeleted()
    val recentCurrencies: List<String> = view.recentCurrencies()

    /** You’re owed breakdown: one row per friend who owes you (§6.2). */
    val owedBreakdown: List<SettleRow>
        get() = settlePlan.get

    /** You owe breakdown. */
    val oweBreakdown: List<SettleRow>
        get() = settlePlan.pay

    /** Groups whose You owe breakdown explains simplified debts (§6.2). */
    val simplifiedFootnoteGroups: List<String> by lazy { view.simplifiedFootnoteGroups() }

    companion object {
        fun of(
            ledger: Ledger,
            defaultCurrency: String,
            now: Instant,
            zone: ZoneId,
        ): LedgerSnapshot = LedgerSnapshot(LedgerView(ledger, defaultCurrency, now, zone))
    }
}
