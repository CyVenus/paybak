package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.calc.installmentDue
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Frequency
import java.time.LocalDate

/** Lend money's installment plan (record-lend-group §4.4, §8.2). */
object LoanSchedule {
    const val MIN_COUNT = 2
    const val MAX_COUNT = 24

    /** The first due date by default: the loan date plus one period (Wed 30 Sep → Fri 30 Oct). */
    fun defaultFirstDue(date: LocalDate, frequency: Frequency): LocalDate =
        installmentDue(date, frequency, 1)

    fun dues(firstDue: LocalDate, frequency: Frequency, count: Int): List<LocalDate> =
        (0 until count).map { installmentDue(firstDue, frequency, it) }

    /**
     * "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec"; more than 3: "6 × ₹1,000 · monthly from
     * Fri 30 Oct to Wed 31 Mar". Uneven amounts give the first ones the extra: "About ₹333".
     */
    fun preview(
        amount: Long,
        currency: String,
        count: Int,
        frequency: Frequency,
        firstDue: LocalDate,
    ): String {
        val parts = Splits.installments(amount, count)
        val each =
            if (parts.distinct().size == 1) Money.format(parts.first(), currency)
            else "about ${Money.format(parts.last(), currency)}"
        val dues = dues(firstDue, frequency, count)
        val dates =
            if (count <= 3) {
                dues.dropLast(1).joinToString(", ") { Dates.day(it) } +
                    " and " +
                    Dates.day(dues.last())
            } else {
                "${adverb(frequency)} from ${Dates.day(dues.first())} to ${Dates.day(dues.last())}"
            }
        return "$count × $each · $dates"
    }

    /** The loan detail's section header: "3 monthly installments". */
    fun header(count: Int, frequency: Frequency): String =
        "$count ${adverb(frequency)} installment" + if (count == 1) "" else "s"

    private fun adverb(frequency: Frequency): String =
        when (frequency) {
            Frequency.Weekly -> "weekly"
            Frequency.Biweekly -> "fortnightly"
            Frequency.Monthly -> "monthly"
            Frequency.Yearly -> "yearly"
        }
}
