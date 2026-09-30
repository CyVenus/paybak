package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.ME
import java.time.LocalDate

enum class ObligationKind {
    Direct,
    Group,
    Project,
    Loan,
}

/**
 * One open debt between the user and a friend (§5.5), in the default currency. [ref] is the
 * expense, group or loan id; [title] the expense title, group name or loan reason.
 */
data class Obligation(
    val debtorId: String,
    val creditorId: String,
    val amount: Long,
    val due: LocalDate?,
    val title: String,
    val kind: ObligationKind,
    val ref: String,
    val installment: Int? = null,
) {
    val friendId: String
        get() = if (debtorId == ME) creditorId else debtorId

    val owedToMe: Boolean
        get() = creditorId == ME
}

/** Everything between the user and one friend in one place; [amount] > 0: the friend owes you. */
data class BalanceContext(
    val friendId: String,
    val kind: ObligationKind,
    val ref: String,
    val title: String,
    val amount: Long,
    val items: List<Obligation>,
)

data class Installment(val due: LocalDate?, val amount: Long, val paidOn: LocalDate?)

/** Home's two totals (§6.1). */
data class HomeTotals(
    val owed: Long,
    val owedPeople: Int,
    val owe: Long,
    val oweGroups: Int,
    val owePeople: Int,
) {
    /** "from 4 people", "from 1 person". */
    val owedCaption: String
        get() = "from $owedPeople " + if (owedPeople == 1) "person" else "people"

    /** "across 2 groups", "to 1 person", "across 1 group and 2 people". */
    val oweCaption: String
        get() {
            val groups = "$oweGroups group" + if (oweGroups == 1) "" else "s"
            val people = "$owePeople " + if (owePeople == 1) "person" else "people"
            return when {
                oweGroups > 0 && owePeople > 0 -> "across $groups and $people"
                oweGroups > 0 -> "across $groups"
                else -> "to $people"
            }
        }
}

/** Installment i's due date (0-based): weekly +7 d, biweekly +14 d, monthly same day (§9). */
fun installmentDue(first: LocalDate, frequency: Frequency, index: Int): LocalDate =
    when (frequency) {
        Frequency.Weekly -> first.plusWeeks(index.toLong())
        Frequency.Biweekly -> first.plusWeeks(2L * index)
        Frequency.Monthly -> Dates.addMonths(first, index.toLong(), first.dayOfMonth)
        Frequency.Yearly -> first.plusYears(index.toLong())
    }
