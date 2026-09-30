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
