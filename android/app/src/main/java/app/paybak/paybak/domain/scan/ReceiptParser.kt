package app.paybak.paybak.domain.scan

import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.ScanItem
import app.paybak.paybak.domain.model.ScanTax
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlin.math.abs

/** One line of recognised text with its box in image pixels. */
data class OcrLine(
    val text: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val centerY: Float
        get() = (top + bottom) / 2

    val height: Float
        get() = bottom - top
}

/**
 * Turns recognised receipt lines into a [ReceiptScan] (insights §4.6, app-architecture §4.1): the
 * merchant is the first line; the date and time come from the line that has them; each row pairs
 * its label with the money at its right end; Subtotal, tax lines (GST, CGST, SGST, VAT, Service
 * charge), Tip and Total are recognised by name, and the rows above them are the items. Amounts are
 * in paise.
 *
 * Text recognition reads the rupee sign as "T", "Z", "7" or "2", or drops it. A leading 7 or 2 is
 * therefore ambiguous; the reading kept is the one where the items add up to the subtotal and the
 * subtotal, taxes and tip add up to the total.
 */
object ReceiptParser {
    private val money = Regex("""(\d{1,3}(?:,\d{2,3})+|\d+)(?:\.(\d{1,2}))?""")
    private val percent = Regex("""(\d+(?:\.\d+)?)\s*%""")
    private val dayMonthYear = Regex("""\b(\d{1,2})\s+([A-Za-z]{3,9})\s+(\d{4})\b""")
    private val clock = Regex("""\b(\d{1,2}):(\d{2})\s*([ap]\.?m\.?)?""", RegexOption.IGNORE_CASE)
    private val quantity = Regex("""\s[xX](\d+)$""")
    private val currencyWord = Regex("""^(?:rs\.?|inr)""", RegexOption.IGNORE_CASE)
    private val trailingCurrency = Regex("""\s+(?:rs\.?|inr)$""", RegexOption.IGNORE_CASE)
    private val taxNames = listOf("cgst", "sgst", "igst", "gst", "vat", "service charge", "tax")

    /** What the rupee sign gets read as when it isn't a digit. */
    private const val SIGN_MARKS = "₹TtZzF?%$€£"

    /** Digits the rupee sign gets read as. */
    private const val SIGN_DIGITS = "72"

    private const val PAISE = 100L

    /** Ambiguous amounts tried exhaustively; past this, each keeps its plain reading. */
    private const val MAX_AMBIGUOUS = 12

    fun parse(lines: List<OcrLine>): ReceiptScan? {
        val rows = rows(lines)
        var subtotal: Reading? = null
        var total: Reading? = null
        var tip: Reading? = null
        val taxes = mutableListOf<Pair<String, Reading>>()
        val items = mutableListOf<Pair<String, Reading>>()
        var firstItemRow = -1
        rows.forEachIndexed { index, row ->
            val amount = row.amount ?: return@forEachIndexed
            val name = row.label.lowercase()
            when {
                name.startsWith("subtotal") || name.startsWith("sub total") -> subtotal = amount
                taxNames.any { name.startsWith(it) } -> taxes += row.label to amount
                name.startsWith("tip") -> tip = amount
                name.startsWith("total") || name.startsWith("grand total") -> total = amount
                subtotal == null && total == null && row.label.any(Char::isLetter) -> {
                    if (firstItemRow < 0) firstItemRow = index
                    items += row.label to amount
                }
            }
        }
        if (items.isEmpty()) return null
        val totals = resolveTotals(subtotal, taxes.map { it.second }, tip, total)
        val itemAmounts = resolveItems(items.map { it.second }, totals?.subtotal)
        val header = rows.take(firstItemRow).map { it.text }
        return ReceiptScan(
            merchant = header.firstOrNull()?.trim(),
            date = header.firstNotNullOfOrNull(::dateIn),
            time = header.firstNotNullOfOrNull(::timeIn),
            items = items.zip(itemAmounts) { (label, _), amount -> ScanItem(label, amount) },
            subtotal = totals?.subtotal ?: itemAmounts.sum(),
            taxes =
                taxes.mapIndexed { i, (label, reading) ->
                    ScanTax(label, rateOf(label), totals?.taxes?.get(i) ?: reading.plain)
                },
            tip = totals?.tip ?: tip?.plain,
            total = totals?.total ?: total?.plain,
        )
    }

    /** A money word: [plain] as read, [stripped] without a leading 7 or 2 that may be the sign. */
    private data class Reading(val plain: Long, val stripped: Long?) {
        val options: List<Long>
            get() = listOfNotNull(plain, stripped)
    }

    private data class Totals(
        val subtotal: Long?,
        val taxes: List<Long>,
        val tip: Long?,
        val total: Long?,
    )

    /**
     * The readings of the totals block where subtotal + taxes + tip = total (the lowest such
     * subtotal when several fit); the plain readings when nothing fits or a part is missing.
     */
    private fun resolveTotals(
        subtotal: Reading?,
        taxes: List<Reading>,
        tip: Reading?,
        total: Reading?,
    ): Totals? {
        if (subtotal == null && total == null) return null
        val extras = taxes + listOfNotNull(tip)
        val fitting =
            combinations(listOfNotNull(subtotal, total) + extras)
                .filter { picked ->
                    if (subtotal == null || total == null) return@filter false
                    picked[0] + picked.drop(2).sum() == picked[1]
                }
                .minByOrNull { it[0] }
        if (fitting == null) {
            return Totals(subtotal?.plain, taxes.map { it.plain }, tip?.plain, total?.plain)
        }
        val parts = fitting.drop(2)
        return Totals(fitting[0], parts.take(taxes.size), tip?.let { parts.last() }, fitting[1])
    }

    /** The item readings that add up to [subtotal]; the plain readings when none do. */
    private fun resolveItems(items: List<Reading>, subtotal: Long?): List<Long> {
        val plain = items.map { it.plain }
        if (subtotal == null || items.count { it.stripped != null } > MAX_AMBIGUOUS) return plain
        return combinations(items).firstOrNull { it.sum() == subtotal } ?: plain
    }

    /** Every way of reading [readings], stripped readings first. */
    private fun combinations(readings: List<Reading>): Sequence<List<Long>> =
        readings.fold(sequenceOf(emptyList())) { acc, reading ->
            acc.flatMap { picked -> reading.options.reversed().asSequence().map { picked + it } }
        }

    /** A recognised row: its words left to right, and the money at its right end. */
    private data class Row(val label: String, val amount: Reading?, val text: String)

    /**
     * Joins lines that sit side by side (vertical centres within half a line height) into rows, top
     * to bottom. A row's amount is its last word when that is money ("₹430", "T430", "Rs 2,300");
     * the words before it are the label ("Fresh lime soda x3" reads as "×3").
     */
    private fun rows(lines: List<OcrLine>): List<Row> {
        val sorted = lines.filter { it.text.isNotBlank() }.sortedBy { it.centerY }
        val groups = mutableListOf<MutableList<OcrLine>>()
        for (line in sorted) {
            val group = groups.lastOrNull()
            val reference = group?.first()
            if (reference != null && abs(line.centerY - reference.centerY) < reference.height / 2) {
                group += line
            } else {
                groups += mutableListOf(line)
            }
        }
        return groups.map { group ->
            val text = group.sortedBy { it.left }.joinToString(" ") { it.text.trim() }
            val words = text.split(' ').filter(String::isNotEmpty)
            val amount = words.lastOrNull()?.let(::readingOf)
            val label =
                if (amount == null) text
                else words.dropLast(1).joinToString(" ").replace(trailingCurrency, "").trim()
            Row(label.trimEnd('.', ':', '-').trim().replace(quantity, " ×$1"), amount, text)
        }
    }

    /** "₹2,300", "T430" → one reading; "7370" → 7,370 or 370; anything else → null. */
    private fun readingOf(word: String): Reading? {
        val signed = word.firstOrNull()?.let { it in SIGN_MARKS } == true
        val digits = word.replace(currencyWord, "").trimStart(*SIGN_MARKS.toCharArray())
        val plain = amountOf(digits) ?: return null
        val stripped =
            digits
                .takeIf { !signed && it.length > 1 && it[0] in SIGN_DIGITS && it[1].isDigit() }
                ?.let { amountOf(it.drop(1)) }
        return Reading(plain, stripped)
    }

    private fun amountOf(text: String): Long? {
        val match = money.matchEntire(text) ?: return null
        val whole = match.groupValues[1].replace(",", "").toLongOrNull() ?: return null
        return whole * PAISE + match.groupValues[2].padEnd(2, '0').toLong()
    }

    /** "GST 5%" → 500 basis points. */
    private fun rateOf(label: String): Long? =
        percent.find(label)?.groupValues?.get(1)?.toBigDecimalOrNull()?.movePointRight(2)?.toLong()

    private fun dateIn(text: String): LocalDate? {
        val match = dayMonthYear.find(text) ?: return null
        val month =
            Month.entries.firstOrNull {
                it.name.startsWith(match.groupValues[2].uppercase().take(3))
            } ?: return null
        val year = match.groupValues[3].toInt()
        val day = match.groupValues[1].toInt()
        return if (day in 1..month.length(Year.isLeap(year.toLong())))
            LocalDate.of(year, month, day)
        else null
    }

    /** "1:15 pm" → "13:15". */
    private fun timeIn(text: String): String? {
        val match = clock.find(text) ?: return null
        var hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()
        val meridiem = match.groupValues[3].lowercase().replace(".", "")
        if (meridiem == "pm" && hour < 12) hour += 12
        if (meridiem == "am" && hour == 12) hour = 0
        if (hour !in 0..23 || minute !in 0..59) return null
        return "%02d:%02d".format(hour, minute)
    }
}
