package app.paybak.paybak.feature.pickers

import java.text.Normalizer
import java.util.Locale

private val CombiningMarks = Regex("""\p{Mn}+""")

/** "Café " → "cafe": trimmed, accents removed, lower case. */
internal fun String.folded(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(CombiningMarks, "")
        .lowercase(Locale.ROOT)

/** Whether any of [texts] contains [query], ignoring case and accents; a blank query matches. */
fun matchesSearch(vararg texts: String?, query: String): Boolean {
    val needle = query.folded()
    return needle.isEmpty() || texts.any { it != null && needle in it.folded() }
}

/**
 * The people picker's search: a name, username or contact containing [query]. A leading "@"
 * ("@priya") searches usernames, which are kept without it.
 */
fun matchesPerson(name: String?, username: String?, contact: String?, query: String): Boolean =
    matchesSearch(name, username, contact, query = query.trim().removePrefix("@"))
