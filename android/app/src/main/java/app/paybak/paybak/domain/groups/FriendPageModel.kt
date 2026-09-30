package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.calc.FriendHistoryItem
import app.paybak.paybak.domain.calc.FriendHistoryKind
import app.paybak.paybak.domain.calc.GroupSummary
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.Obligation
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.friendPage
import app.paybak.paybak.domain.calc.paymentFor
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.domain.model.Pronoun
import app.paybak.paybak.domain.model.ReminderContext

/** How many History rows a friend page shows before "See all". */
const val FRIEND_HISTORY_LIMIT = 10

/** The friend page's balance card (screens-groups §6.3). */
data class FriendBalanceCard(
    val standing: Standing,
    /** "Rohan owes you", "You owe Kabir", "Your balance". */
    val label: String,
    /** "+₹800", "−₹1,400", "Settled". */
    val amount: String,
    /** "Movie tickets · Due Sun 27 Sep", or the last payment once settled. */
    val caption: String,
    /** "Overdue 3 days" while the friend owes you past the due date. */
    val overdue: String?,
)

/** What a payment between you is for: the one open item, when the net comes from one. */
data class PaymentContext(
    val groupId: String? = null,
    val expenseId: String? = null,
    val loanId: String? = null,
)

/** A History row: an expense, payment or loan with this friend, newest first. */
data class FriendHistoryRow(
    val item: FriendHistoryItem,
    val title: String,
    val subtitle: String,
    val amount: String,
    /** Still open between you (black amount); settled rows are grey. */
    val open: Boolean,
    val date: String,
    /** A category icon key, or "money-in" / "money-out" for payments and loans. */
    val icon: String,
)

/** A friend's page (screens-groups §6): balance, actions, History, Groups together, reminders. */
data class FriendPageModel(
    val person: Person,
    /** Their UPI ID, else "@username"; none for a guest. */
    val subtitle: String?,
    /** Null until anything is shared: the page shows "No balance yet" instead. */
    val balance: FriendBalanceCard?,
    val net: Long,
    val remindContext: ReminderContext?,
    val paymentContext: PaymentContext,
    /** "Last reminder sent today." while they owe you and one was sent. */
    val lastReminder: String?,
    val history: List<FriendHistoryRow>,
    val moreHistory: Boolean,
    val groups: List<GroupListRow>,
) {
    val firstName: String
        get() = person.firstName

    val guest: Boolean
        get() = person.isGuest
}

fun LedgerView.friendPageModel(personId: String, summaries: List<GroupSummary>): FriendPageModel? {
    val page = friendPage(personId) ?: return null
    val balance = page.balance
    val person = balance.person
    val net = balance.net
    val shared = page.history.isNotEmpty() || page.groupsTogether.isNotEmpty()
    val lead = balance.lead
    val summaryById = summaries.associateBy { it.group.id }
    return FriendPageModel(
        person = person,
        subtitle = if (person.isGuest) null else person.upi ?: person.username?.let { "@$it" },
        balance = if (shared) friendBalanceCard(person, net, lead, balance.overdue, balance.badge) else null,
        net = net,
        remindContext = lead?.reminderContext(),
        paymentContext = balance.items.singleOrNull()?.paymentContext() ?: PaymentContext(),
        lastReminder = page.lastReminder?.takeIf { net > 0 }?.let { "$it." },
        history = page.history.take(FRIEND_HISTORY_LIMIT).map { historyRow(it, person) },
        moreHistory = page.history.size > FRIEND_HISTORY_LIMIT,
        groups =
            page.groupsTogether
                .mapNotNull { summaryById[it.id] }
                .map { groupListRow(it).copy(budget = null) },
    )
}

private fun LedgerView.friendBalanceCard(
    person: Person,
    net: Long,
    lead: Obligation?,
    overdue: Boolean,
    badge: String?,
): FriendBalanceCard {
    val amount = Money.format(net, defaultCurrency, MoneySign.Signed)
    val caption = lead?.let { item -> item.due?.let { "${item.title} · ${Dates.dueLabel(it)}" } ?: item.title }
    return when {
        net > 0 ->
            FriendBalanceCard(
                Standing.Owed,
                "${person.firstName} owes you",
                amount,
                caption.orEmpty(),
                badge?.takeIf { overdue },
            )
        net < 0 ->
            FriendBalanceCard(
                Standing.Owe,
                "You owe ${person.firstName}",
                amount,
                caption.orEmpty(),
                overdue = null,
            )
        else ->
            FriendBalanceCard(
                Standing.Settled,
                "Your balance",
                "Settled",
                lastPaymentCaption(person) ?: "Nothing pending",
                overdue = null,
            )
    }
}

/** "You paid Kabir ₹450 on 7 Sep" / "Priya paid you ₹1,050 on 29 Sep": the latest confirmed one. */
private fun LedgerView.lastPaymentCaption(person: Person): String? {
    val last =
        confirmedPayments()
            .filter { setOf(it.fromId, it.toId) == setOf(ME, person.id) }
            .maxByOrNull { it.confirmedAt!! } ?: return null
    val amount = Money.format(last.amount, last.currency)
    val on = Dates.short(last.date)
    return if (last.fromId == ME) "You paid ${person.firstName} $amount on $on"
    else "${person.firstName} paid you $amount on $on"
}

private fun Obligation.reminderContext(): ReminderContext =
    when (kind) {
        ObligationKind.Direct -> ReminderContext(expenseId = ref)
        ObligationKind.Group, ObligationKind.Project -> ReminderContext(groupId = ref)
        ObligationKind.Loan -> ReminderContext(loanId = ref, installment = installment)
    }

private fun Obligation.paymentContext(): PaymentContext =
    when (kind) {
        ObligationKind.Direct -> PaymentContext(expenseId = ref)
        ObligationKind.Group, ObligationKind.Project -> PaymentContext(groupId = ref)
        ObligationKind.Loan -> PaymentContext(loanId = ref)
    }

private fun LedgerView.historyRow(item: FriendHistoryItem, person: Person): FriendHistoryRow {
    val date = Dates.rowDate(item.date, today)
    return when (item.kind) {
        FriendHistoryKind.Expense -> expenseRow(item, person, date)
        FriendHistoryKind.Payment -> {
            val payment = ledger.payments.first { it.id == item.ref }
            val toMe = payment.toId == ME
            val state =
                when (payment.status) {
                    PaymentStatus.Pending -> " · Pending"
                    PaymentStatus.NotReceived -> " · Not received"
                    else -> ""
                }
            FriendHistoryRow(
                item,
                title = if (toMe) "${person.firstName} paid you" else "You paid ${person.firstName}",
                subtitle = "${paymentFor(payment)} · ${payment.method.label}$state",
                amount = Money.format(payment.amount, payment.currency),
                open = toMe,
                date = date,
                icon = if (toMe) "money-in" else "money-out",
            )
        }
        FriendHistoryKind.Loan -> {
            val loan = loan(item.ref)!!
            val remaining = loanContext(loan)?.amount?.let(Math::abs) ?: 0
            val lent = loan.lenderId == ME
            val who = if (lent) "You lent" else "${person.firstName} lent you"
            val owes = if (lent) "${person.firstName} owes" else "You owe"
            FriendHistoryRow(
                item,
                title = loan.title,
                subtitle =
                    if (remaining == 0L) "$who · Paid back"
                    else "$who · $owes ${Money.format(remaining, loan.currency)}",
                amount = Money.format(loan.amount, loan.currency),
                open = remaining != 0L,
                date = date,
                icon = if (lent) "money-out" else "money-in",
            )
        }
    }
}

/**
 * "You paid · Rohan owes ₹800" while a direct expense is open, "College Gang · Settled" for a group
 * where you're square, "Goa Trip · Your share ₹3,600" while the group is open.
 */
private fun LedgerView.expenseRow(
    item: FriendHistoryItem,
    person: Person,
    date: String,
): FriendHistoryRow {
    val expense = expense(item.ref)!!
    val group = groupOf(expense)
    val payer = if (expense.payerId == ME) "You paid" else "${first(expense.payerId)} paid"
    val (subtitle, open) =
        if (group != null) {
            val square = (groupNets(group.id)[ME] ?: 0) == 0L
            when {
                square -> "${group.name} · Settled" to false
                expense.payerId == ME -> "${group.name} · You paid" to true
                else -> "${group.name} · Your share ${Money.format(myShare(expense), expense.currency)}" to true
            }
        } else {
            val openItem =
                openItems().firstOrNull { it.ref == expense.id && it.friendId == person.id }
            if (openItem == null) {
                "$payer · Settled" to false
            } else {
                val debtor = if (openItem.debtorId == ME) "You owe" else "${person.firstName} owes"
                "$payer · $debtor ${Money.format(openItem.amount, defaultCurrency)}" to true
            }
        }
    return FriendHistoryRow(
        item,
        title = expense.title,
        subtitle = subtitle,
        amount = Money.format(expense.amount, expense.currency),
        open = open,
        date = date,
        icon = Category.of(expense.category).icon,
    )
}

/**
 * The guest invite notice (§6.7): "Ananya isn’t on Paybak yet. You can still split with her. When
 * she joins with the same phone or email, her history moves to her account."
 */
fun inviteNotice(person: Person): Pair<String, String> {
    val name = person.firstName
    val (subject, objectForm, possessive) =
        when (person.pronoun) {
            Pronoun.She -> Triple("she", "her", "her")
            Pronoun.He -> Triple("he", "him", "his")
            Pronoun.They -> Triple("they", "them", "their")
        }
    val joins = if (person.pronoun == Pronoun.They) "join" else "joins"
    return "Invite $name to Paybak" to
        "$name isn’t on Paybak yet. You can still split with $objectForm. When $subject $joins " +
            "with the same phone or email, $possessive history moves to $possessive account."
}
