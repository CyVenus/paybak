package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import java.time.Instant
import java.time.LocalDate

enum class FriendHistoryKind {
    Expense,
    Payment,
    Loan,
}

/** A History row on a friend's page: shared non-project expenses, payments and loans. */
data class FriendHistoryItem(
    val kind: FriendHistoryKind,
    val ref: String,
    val date: LocalDate,
    val at: Instant,
)

/** A friend's page (§6.5). */
data class FriendPage(
    val balance: FriendBalance,
    /** "Last reminder sent today" / "… yesterday" / "… on Fri 25 Sep". */
    val lastReminder: String?,
    val history: List<FriendHistoryItem>,
    val groupsTogether: List<Group>,
)

fun LedgerView.friendPage(personId: String): FriendPage? {
    val balance = friendBalances().firstOrNull { it.person.id == personId } ?: return null
    val last = ledger.reminders.filter { it.toId == personId }.maxByOrNull { it.sentAt }
    val lastReminder = last?.let {
        val day = localDate(it.sentAt)
        when (day) {
            today -> "Last reminder sent today"
            today.minusDays(1) -> "Last reminder sent yesterday"
            else -> "Last reminder sent on ${Dates.day(day)}"
        }
    }
    val expenses =
        liveExpenses()
            .filter { e -> e.split.rows.any { it.personId == personId } }
            .filter { e -> e.groupId?.let { group(it)?.isProject } != true }
            .map { FriendHistoryItem(FriendHistoryKind.Expense, it.id, it.date, it.createdAt) }
    val payments =
        ledger.payments
            .filter { setOf(it.fromId, it.toId) == setOf(ME, personId) }
            .filter {
                it.status != PaymentStatus.Cancelled &&
                    it.groupId?.let { g -> group(g)?.isProject } != true
            }
            .map { FriendHistoryItem(FriendHistoryKind.Payment, it.id, it.date, it.createdAt) }
    val loans =
        ledger.loans
            .filter { it.friendId == personId }
            .map { FriendHistoryItem(FriendHistoryKind.Loan, it.id, it.date, it.createdAt) }
    return FriendPage(
        balance = balance,
        lastReminder = lastReminder,
        history =
            (expenses + payments + loans).sortedWith(
                compareByDescending<FriendHistoryItem> { it.date }.thenByDescending { it.at }
            ),
        groupsTogether = ledger.groups.filter { personId in it.memberIds && ME in it.memberIds },
    )
}
