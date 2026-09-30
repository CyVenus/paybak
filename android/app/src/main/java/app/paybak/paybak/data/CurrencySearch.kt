package app.paybak.paybak.data

import java.text.Normalizer
import java.util.Locale

/**
 * The currencies in this list that match [query] by ISO code or name, ignoring case and accents
 * (Setup 2 search): the exact code first, then codes starting with the query, names starting with
 * it, and names containing it. Each group keeps the list's order, so pass the list sorted by name.
 * A blank query matches nothing.
 */
fun List<Currency>.search(query: String): List<Currency> {
    val needle = query.foldedForSearch()
    if (needle.isEmpty()) return emptyList()
    return mapNotNull { currency -> currency.matchRank(needle)?.let { rank -> currency to rank } }
        .sortedBy { (_, rank) -> rank }
        .map { (currency, _) -> currency }
}

/** Lower is a better match; null is no match. */
private fun Currency.matchRank(needle: String): Int? {
    val code = code.lowercase(Locale.ROOT)
    val name = name.foldedForSearch()
    return when {
        code == needle -> 0
        code.startsWith(needle) -> 1
        name.startsWith(needle) -> 2
        needle in name -> 3
        else -> null
    }
}

private val CombiningMarks = Regex("""\p{Mn}+""")

/** "São Tomé " → "sao tome": trimmed, accents removed, lower case. */
private fun String.foldedForSearch(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(CombiningMarks, "")
        .lowercase(Locale.ROOT)
