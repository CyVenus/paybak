package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.Loan
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.Rate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The balances of a [ledger] from the user's side at [now] (domain.md §5): a straight port of
 * verify.py's `Ledger`. Functions that take `asOf` see only the records that existed then (§5.1);
 * null means [now], whose results are cached. Read models built on it live in the files next to
 * this one. [defaultCurrency] is the profile currency.
 */
class LedgerView(
    val ledger: Ledger,
    val defaultCurrency: String,
    val now: Instant,
    val zone: ZoneId,
) {
    val today: LocalDate = localDate(now)

    private val peopleById = ledger.people.associateBy { it.id }
    private val groupsById = ledger.groups.associateBy { it.id }
    private val expensesById = ledger.expenses.associateBy { it.id }
    private val loansById = ledger.loans.associateBy { it.id }

    fun localDate(moment: Instant): LocalDate = moment.atZone(zone).toLocalDate()

    fun localDateTime(moment: Instant): LocalDateTime = moment.atZone(zone).toLocalDateTime()

    fun person(id: String) = peopleById[id]

    fun group(id: String) = groupsById[id]

    fun expense(id: String) = expensesById[id]

    fun loan(id: String) = loansById[id]

    /** "You" for the user, otherwise the first name. */
    fun first(personId: String): String =
        if (personId == ME) "You" else peopleById[personId]?.firstName ?: "Someone"

    // ---- Records that count (§5.1) --------------------------------------------------------

    private val liveNow by lazy { liveExpensesAt(now) }
    private val confirmedNow by lazy { confirmedPaymentsAt(now) }

    fun liveExpenses(asOf: Instant? = null): List<Expense> = asOf?.let(::liveExpensesAt) ?: liveNow

    fun confirmedPayments(asOf: Instant? = null): List<Payment> =
        asOf?.let(::confirmedPaymentsAt) ?: confirmedNow

    private fun liveExpensesAt(asOf: Instant) =
        ledger.expenses.filter {
            it.createdAt <= asOf && !(it.deletedAt != null && it.deletedAt <= asOf)
        }

    private fun confirmedPaymentsAt(asOf: Instant) =
        ledger.payments.filter {
            it.status == PaymentStatus.Confirmed && it.confirmedAt != null && it.confirmedAt <= asOf
        }

    // ---- Money in the default currency (§5.4) ---------------------------------------------

    fun toDefault(minor: Long, currency: String, rate: Rate?): Long =
        if (currency == defaultCurrency || rate == null) minor
        else Money.convert(minor, rate.value, currency, rate.to)

    fun myShare(expense: Expense): Long = expense.shareOf(ME)

    // ---- Groups and projects (§5.2–5.4, §8) -----------------------------------------------

    /** Each member's net in the group currency; they sum to 0. */
    fun groupNets(groupId: String, asOf: Instant? = null): Map<String, Long> {
        val group = groupsById[groupId] ?: return emptyMap()
        val nets = LinkedHashMap<String, Long>()
        group.memberIds.forEach { nets[it] = 0 }
        if (group.kind == GroupKind.Project) {
            val (paid, share) = projectPaidShare(groupId)
            group.memberIds.forEach { nets[it] = (paid[it] ?: 0) - (share[it] ?: 0) }
        } else {
            for (expense in liveExpenses(asOf)) {
                if (expense.groupId != groupId) continue
                expense.payers.forEach { nets.add(it.personId, it.amount) }
                expense.split.rows.forEach { nets.add(it.personId, -it.share) }
            }
        }
        for (payment in confirmedPayments(asOf)) {
            if (payment.groupId != groupId) continue
            nets.add(payment.fromId, payment.amount)
            nets.add(payment.toId, -payment.amount)
        }
        return nets
    }

    /** Paid and share per member over the live expenses (the group sheet's "paid vs share"). */
    fun groupPaidShare(groupId: String): Pair<Map<String, Long>, Map<String, Long>> {
        val group = groupsById[groupId] ?: return emptyMap<String, Long>() to emptyMap()
        val paid = LinkedHashMap<String, Long>()
        val share = LinkedHashMap<String, Long>()
        group.memberIds.forEach {
            paid[it] = 0
            share[it] = 0
        }
        for (expense in liveExpenses()) {
            if (expense.groupId != groupId) continue
            expense.payers.forEach { paid.add(it.personId, it.amount) }
            expense.split.rows.forEach { share.add(it.personId, it.share) }
        }
        return paid to share
    }

    /** Who pays whom in the group (simplified, or pairwise when Simplify debts is off). */
    fun groupPlan(groupId: String, asOf: Instant? = null): List<Transfer> {
        val group = groupsById[groupId] ?: return emptyList()
        return if (group.simplifyDebts || group.kind == GroupKind.Project) {
            simplify(groupNets(groupId, asOf), group.memberIds)
        } else {
            pairwisePlan(groupId, asOf)
        }
    }

    /** Simplify off: each participant owes each payer pro rata, netted per pair (§5.3). */
    fun pairwisePlan(groupId: String, asOf: Instant? = null): List<Transfer> {
        val pairs = LinkedHashMap<Pair<String, String>, Long>()
        fun add(debtor: String, creditor: String, amount: Long) {
            val key = if (debtor <= creditor) debtor to creditor else creditor to debtor
            pairs[key] = (pairs[key] ?: 0) + if (debtor == key.first) amount else -amount
        }
        for (expense in liveExpenses(asOf)) {
            if (expense.groupId != groupId) continue
            for (row in expense.split.rows) {
                for (payer in expense.payers) {
                    if (payer.personId != row.personId) {
                        add(row.personId, payer.personId, row.share * payer.amount / expense.amount)
                    }
                }
            }
        }
        for (payment in confirmedPayments(asOf)) {
            if (payment.groupId == groupId) add(payment.toId, payment.fromId, payment.amount)
        }
        return pairs.mapNotNull { (pair, amount) ->
            when {
                amount > 0 -> Transfer(pair.first, pair.second, amount)
                amount < 0 -> Transfer(pair.second, pair.first, -amount)
                else -> null
            }
        }
    }

    /**
     * The saved rate of a foreign group's latest record: converts its open balance into the default
     * currency (§5.4). Null for a group in the default currency.
     */
    fun groupRate(groupId: String): Rate? {
        val group = groupsById[groupId] ?: return null
        if (group.currency == defaultCurrency) return null
        val expenses =
            liveExpenses()
                .filter { it.groupId == groupId && it.rate != null }
                .map { it.createdAt to it.rate }
        val payments =
            confirmedPayments()
                .filter { it.groupId == groupId && it.rate != null }
                .map {
                    it.createdAt to it.rate
                }
        return (expenses + payments).maxByOrNull { it.first }?.second
    }

    fun projectParts(projectId: String) = ledger.components.filter { it.projectId == projectId }

    /** Σ actual cost of bought and done parts (§8.1). */
    fun projectSpent(projectId: String): Long =
        projectParts(projectId).filter { it.counts }.sumOf { it.actualCost ?: 0 }

    /** What each member paid for parts and their fair share of the spending (§8.2). */
    fun projectPaidShare(projectId: String): Pair<Map<String, Long>, Map<String, Long>> {
        val group = groupsById[projectId] ?: return emptyMap<String, Long>() to emptyMap()
        val members = group.memberIds
        val paid = LinkedHashMap<String, Long>()
        members.forEach { paid[it] = 0 }
        projectParts(projectId)
            .filter { it.status != ComponentStatus.Planned }
            .forEach {
                paid.add(it.paidBy, it.actualCost ?: 0)
            }
        val spent = paid.values.sum()
        val contribution = group.project?.contribution
        val counter = ledger.rotation[projectId] ?: 0
        val share =
            when (contribution?.rule ?: ContributionRule.Equal) {
                ContributionRule.Equal -> Splits.equal(spent, members, counter).shares
                else -> Splits.weighted(spent, contribution!!.values, members, counter).shares
            }
        return paid to share
    }

    // ---- Everything between you and each friend (§5.5) ------------------------------------

    private val contextsNow by lazy { contextsAt(null) }
    private val friendNetsNow by lazy { friendNetsAt(null) }
    private val openItemsNow by lazy { openItemsAt(null) }

    fun contexts(asOf: Instant? = null): List<BalanceContext> =
        if (asOf == null) contextsNow else contextsAt(asOf)

    fun friendNets(asOf: Instant? = null): Map<String, Long> =
        if (asOf == null) friendNetsNow else friendNetsAt(asOf)

    /** Obligations in the direction of each friend's overall net. */
    fun openItems(asOf: Instant? = null): List<Obligation> =
        if (asOf == null) openItemsNow else openItemsAt(asOf)

    private fun contextsAt(asOf: Instant?): List<BalanceContext> {
        val result = mutableListOf<BalanceContext>()
        for (group in ledger.groups) {
            if (ME !in group.memberIds) continue
            val rate = groupRate(group.id)
            for (transfer in groupPlan(group.id, asOf)) {
                if (ME != transfer.debtorId && ME != transfer.creditorId) continue
                val value = toDefault(transfer.amount, group.currency, rate)
                val friend = if (transfer.debtorId == ME) transfer.creditorId else transfer.debtorId
                val kind = if (group.isProject) ObligationKind.Project else ObligationKind.Group
                val due = if (group.kind == GroupKind.Group) group.settleBy else null
                val obligation =
                    Obligation(
                        transfer.debtorId,
                        transfer.creditorId,
                        value,
                        due,
                        group.name,
                        kind,
                        group.id,
                    )
                val amount = if (transfer.creditorId == ME) value else -value
                result +=
                    BalanceContext(friend, kind, group.id, group.name, amount, listOf(obligation))
            }
        }
        for (person in ledger.people) directContext(person.id, asOf)?.let(result::add)
        for (loan in ledger.loans) loanContext(loan, asOf)?.let(result::add)
        return result
    }

    /**
     * Expenses outside any group plus direct payments with [friendId]. Payments pay the oldest
     * debts first, so the open items are the newest ones.
     */
    fun directContext(friendId: String, asOf: Instant? = null): BalanceContext? {
        val debts = mutableListOf<Obligation>()
        var net = 0L
        val expenses = liveExpenses(asOf).sortedWith(compareBy({ it.date }, { it.createdAt }))
        for (expense in expenses) {
            if (expense.groupId != null) continue
            val amount = pairDebt(expense, friendId, ME)
            if (amount == 0L) continue
            val value = toDefault(kotlin.math.abs(amount), expense.currency, expense.rate)
            val (debtor, creditor) = if (amount > 0) friendId to ME else ME to friendId
            debts +=
                Obligation(
                    debtor,
                    creditor,
                    value,
                    expense.dueDate,
                    expense.title,
                    ObligationKind.Direct,
                    expense.id,
                )
            net += if (amount > 0) value else -value
        }
        for (payment in confirmedPayments(asOf)) {
            if (payment.groupId != null || payment.loanId != null) continue
            if (setOf(payment.fromId, payment.toId) != setOf(ME, friendId)) continue
            val value = toDefault(payment.amount, payment.currency, payment.rate)
            net += if (payment.fromId == friendId) -value else value
        }
        if (debts.isEmpty() && net == 0L) return null
        val items = mutableListOf<Obligation>()
        var left = kotlin.math.abs(net)
        for (obligation in debts.asReversed()) {
            if (left == 0L) break
            if ((obligation.creditorId == ME) == (net > 0)) {
                val take = minOf(left, obligation.amount)
                items += obligation.copy(amount = take)
                left -= take
            }
        }
        return BalanceContext(friendId, ObligationKind.Direct, friendId, "", net, items)
    }

    fun loanContext(loan: Loan, asOf: Instant? = null): BalanceContext? {
        if ((asOf ?: now) < loan.createdAt) return null
        val paid = confirmedPayments(asOf).filter { it.loanId == loan.id }.sumOf { it.amount }
        val remaining = loan.amount - paid
        val items =
            installments(loan, asOf).mapIndexedNotNull { index, installment ->
                if (installment.paidOn != null) null
                else
                    Obligation(
                        loan.borrowerId,
                        loan.lenderId,
                        installment.amount,
                        installment.due,
                        loan.title,
                        ObligationKind.Loan,
                        loan.id,
                        index + 1,
                    )
            }
        val sign = if (loan.lenderId == ME) 1 else -1
        return BalanceContext(
            loan.friendId,
            ObligationKind.Loan,
            loan.id,
            loan.title,
            sign * remaining,
            items,
        )
    }

    /** Due, amount and paid date per installment; repayments fill them in due order (§9). */
    fun installments(loan: Loan, asOf: Instant? = null): List<Installment> {
        val plan = loan.installments
        val amounts: List<Long>
        val dues: List<LocalDate?>
        if (plan != null) {
            amounts = Splits.installments(loan.amount, plan.count)
            dues = (0 until plan.count).map { installmentDue(plan.firstDue, plan.frequency, it) }
        } else {
            amounts = listOf(loan.amount)
            dues = listOf(loan.dueDate)
        }
        val queue =
            ArrayDeque(confirmedPayments(asOf).filter { it.loanId == loan.id }.sortedBy { it.date })
        val result = mutableListOf<Installment>()
        var pool = 0L
        for (i in amounts.indices) {
            val need = amounts[i]
            var paidOn: LocalDate? = null
            while (pool < need && queue.isNotEmpty()) {
                val payment = queue.removeFirst()
                pool += payment.amount
                paidOn = payment.date
            }
            if (pool >= need) {
                pool -= need
                result += Installment(dues[i], need, paidOn ?: result.last().paidOn)
            } else {
                result += Installment(dues[i], need, null)
            }
        }
        return result
    }

    private fun friendNetsAt(asOf: Instant?): Map<String, Long> {
        val nets = LinkedHashMap<String, Long>()
        ledger.people.forEach { nets[it.id] = 0 }
        contexts(asOf).forEach { nets.add(it.friendId, it.amount) }
        return nets
    }

    private fun openItemsAt(asOf: Instant?): List<Obligation> {
        val nets = friendNets(asOf)
        return contexts(asOf).flatMap { context ->
            val net = nets[context.friendId] ?: 0
            context.items.filter { (net > 0 && it.owedToMe) || (net < 0 && !it.owedToMe) }
        }
    }

    /** Home totals (§6.1). */
    fun homeTotals(asOf: Instant? = null): HomeTotals {
        val nets = friendNets(asOf)
        val groups = mutableSetOf<String>()
        val people = mutableSetOf<String>()
        for (item in openItems(asOf)) {
            if (item.debtorId != ME) continue
            if (item.kind == ObligationKind.Group || item.kind == ObligationKind.Project) {
                groups += item.ref
            } else {
                people += item.friendId
            }
        }
        return HomeTotals(
            owed = nets.values.filter { it > 0 }.sum(),
            owedPeople = nets.values.count { it > 0 },
            owe = -nets.values.filter { it < 0 }.sum(),
            oweGroups = groups.size,
            owePeople = people.size,
        )
    }

    /** "You owe" when the payer is someone you currently pay; otherwise "Your share" (§6.6). */
    fun iOwePayer(expense: Expense): Boolean {
        val payer = expense.payerId
        val groupId =
            expense.groupId ?: return openItems().any { it.debtorId == ME && it.ref == expense.id }
        return groupPlan(groupId).any { it.debtorId == ME && it.creditorId == payer }
    }

    fun groupOf(expense: Expense): Group? = expense.groupId?.let(groupsById::get)

    companion object {
        /**
         * What [a] owes [b] on one expense (negative: [b] owes [a]); several payers count pro rata.
         */
        fun pairDebt(expense: Expense, a: String, b: String): Long {
            val total = expense.amount
            if (total == 0L) return 0
            val aOwes = expense.shareOf(a) * expense.paidBy(b) / total
            val bOwes = expense.shareOf(b) * expense.paidBy(a) / total
            return aOwes - bOwes
        }
    }
}

private fun MutableMap<String, Long>.add(key: String, amount: Long) {
    this[key] = (this[key] ?: 0) + amount
}
