package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.format.Money

/**
 * Amounts as typed on the decimal pad (add-expense §3.1): digits with at most one "." and the
 * currency's decimals, read as integer minor units and echoed with the symbol and grouping.
 */
object AmountEntry {
    /** ₹99,99,99,999 is the largest amount: nine whole digits. */
    const val MAX_WHOLE_DIGITS = 9

    /** Whether [currency] has a minor unit (JPY has none, so no decimal point). */
    fun allowsDecimals(currency: String): Boolean = Money.currency(currency).exponent > 0

    /** What the amount field keeps of an edit, or null to reject it (too long). */
    fun accept(text: String): String? {
        val whole = text.substringBefore('.')
        return text.takeIf { whole.length <= MAX_WHOLE_DIGITS }
    }

    /** "2800" → 280000 paise; "12.5" → 1250; "" → 0. */
    fun minor(text: String, currency: String): Long {
        if (text.isEmpty()) return 0
        val exponent = Money.currency(currency).exponent
        val whole = text.substringBefore('.').ifEmpty { "0" }.toLongOrNull() ?: return 0
        val decimals = text.substringAfter('.', "").padEnd(exponent, '0').take(exponent)
        return whole * pow10(exponent) + (decimals.toLongOrNull() ?: 0)
    }

    /** The text that edits [minor]: 280000 → "2800", 280050 → "2800.50"; 0 → "". */
    fun text(minor: Long, currency: String): String {
        if (minor <= 0) return ""
        val exponent = Money.currency(currency).exponent
        val unit = pow10(exponent)
        val fraction = minor % unit
        val whole = (minor / unit).toString()
        return if (fraction == 0L) whole
        else "$whole.${fraction.toString().padStart(exponent, '0')}"
    }

    /** The amount as typed, with the symbol and grouping: "₹2,800", "₹2,800.5", "AED 1,200". */
    fun display(text: String, currency: String): String {
        val whole = text.substringBefore('.').ifEmpty { "0" }.toLongOrNull() ?: 0
        val point = if ('.' in text) "." + text.substringAfter('.') else ""
        return prefix(currency) + Money.groupDigits(whole, currency) + point
    }

    /** The empty field's grey "₹0" (or "AED 0"). */
    fun placeholder(currency: String): String = prefix(currency) + "0"

    /** The symbol before an amount: "₹", "$", or the code and a space ("AED "). */
    fun prefix(currency: String): String {
        val symbol = Money.currency(currency).symbol
        return if (symbol.length <= 2) symbol else "$symbol "
    }

    private fun pow10(exponent: Int): Long {
        var result = 1L
        repeat(exponent) { result *= 10 }
        return result
    }
}
