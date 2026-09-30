package app.paybak.paybak.domain.format

import java.math.BigDecimal
import java.math.RoundingMode

/** A currency as the ledger formats it (domain.md §1.13). */
data class CurrencyInfo(val code: String, val symbol: String, val exponent: Int, val name: String)

/** How a signed amount is written (domain.md §2.2). */
enum class MoneySign {
    /** Unsigned (timeline, settle rows, chat). */
    None,

    /** `+₹2,900` / `−₹1,850`. */
    Signed,

    /** `−₹450` for negatives only. */
    Debit,
}

/** U+2212, the minus sign in copy. */
const val MINUS = "−"

object Money {
    private val known =
        listOf(
                CurrencyInfo("INR", "₹", 2, "Indian rupee"),
                CurrencyInfo("AED", "AED", 2, "UAE dirham"),
                CurrencyInfo("USD", "$", 2, "US dollar"),
                CurrencyInfo("EUR", "€", 2, "Euro"),
                CurrencyInfo("GBP", "£", 2, "British pound"),
                CurrencyInfo("SGD", "S$", 2, "Singapore dollar"),
                CurrencyInfo("AUD", "A$", 2, "Australian dollar"),
                CurrencyInfo("CAD", "C$", 2, "Canadian dollar"),
                CurrencyInfo("JPY", "¥", 0, "Japanese yen"),
            )
            .associateBy { it.code }

    /** The designed currencies; any other ISO code uses its code and JVM exponent. */
    fun currency(code: String): CurrencyInfo =
        known[code]
            ?: CurrencyInfo(
                code = code,
                symbol = code,
                exponent =
                    runCatching { java.util.Currency.getInstance(code).defaultFractionDigits }
                        .getOrDefault(2)
                        .coerceAtLeast(0),
                name = code,
            )

    /** "₹2,900", "₹1,00,000", "₹1,234.50", "AED 1,800", "+₹2,900", "−₹1,850" (domain.md §2.2). */
    fun format(minor: Long, code: String = "INR", sign: MoneySign = MoneySign.None): String {
        val currency = currency(code)
        val value = kotlin.math.abs(minor)
        val unit = pow10(currency.exponent)
        val whole = value / unit
        val fraction = value % unit
        var text = groupDigits(whole, code)
        if (fraction != 0L) text += "." + fraction.toString().padStart(currency.exponent, '0')
        val body =
            if (currency.symbol.length <= 2) currency.symbol + text else "${currency.symbol} $text"
        return when {
            sign == MoneySign.Signed && minor > 0 -> "+$body"
            sign != MoneySign.None && minor < 0 -> MINUS + body
            else -> body
        }
    }

    /** Indian grouping (last three digits, then pairs) for INR; thousands otherwise. */
    fun groupDigits(whole: Long, code: String): String {
        val digits = whole.toString()
        if (code != "INR" || digits.length <= 3) return "%,d".format(java.util.Locale.US, whole)
        var head = digits.dropLast(3)
        val tail = digits.takeLast(3)
        val pairs = ArrayDeque<String>()
        while (head.length > 2) {
            pairs.addFirst(head.takeLast(2))
            head = head.dropLast(2)
        }
        return (listOf(head) + pairs + tail).joinToString(",")
    }

    /**
     * [minor] of [from] in [to]'s minor units at a saved decimal [rate], rounded half up (domain.md
     * §2.1): AED 960 at 22.85 → ₹21,936.
     */
    fun convert(minor: Long, rate: String, from: String, to: String): Long {
        val shift = currency(to).exponent - currency(from).exponent
        val value = BigDecimal(minor).multiply(BigDecimal(rate)).movePointRight(shift)
        return value.setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    /** The "≈ ₹21,936 · ₹22.85 per AED" line of a foreign amount (whole rupees). */
    fun approxLine(minor: Long, rate: String, from: String, to: String): String {
        val converted = convert(minor, rate, from, to)
        val unit = pow10(currency(to).exponent)
        val whole =
            BigDecimal(converted).divide(BigDecimal(unit)).setScale(0, RoundingMode.HALF_EVEN)
        val rateText = BigDecimal(rate).setScale(2, RoundingMode.HALF_UP).toPlainString()
        return "≈ ${format(whole.toLong() * unit, to)} · ${currency(to).symbol}$rateText per $from"
    }

    private fun pow10(exponent: Int): Long {
        var result = 1L
        repeat(exponent) { result *= 10 }
        return result
    }
}
