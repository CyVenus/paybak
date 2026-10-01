package app.paybak.paybak.domain.ask

import app.paybak.paybak.domain.model.Category
import java.math.BigDecimal

/**
 * What a phrase like "Add ₹600 for a cab, split with Esha and Dev" says (insights §3.6.6): the
 * amount in major units ("600", "99.50"), what it was for, the names to split with and the group,
 * all as written. Matching names and groups to records is [AskAssistant]'s job.
 */
data class ExpensePhrase(
    val amount: BigDecimal,
    val what: String?,
    val names: List<String>,
    val group: String?,
) {
    /** "cab" → "Cab". */
    val title: String?
        get() = what?.replaceFirstChar(Char::uppercaseChar)

    /** Guessed from what it was for ("Travel, guessed from “cab”"). */
    val category: Category
        get() = guessCategory(what.orEmpty())

    companion object {
        private val money =
            Regex(
                """(₹\s*|\brs\.?\s*|\binr\s*)?(\d[\d,]*(?:\.\d{1,2})?)(\s*(?:rupees|rs\b|inr\b))?""",
                RegexOption.IGNORE_CASE,
            )
        private val verb = Regex("""^(?:add|log|split)\b\s*""", RegexOption.IGNORE_CASE)
        /** A verb that makes a bare number an expense ("Paid 600 …"). */
        private val expenseVerb =
            Regex("""^(?:add|log|split|record|spent|paid)\s""", RegexOption.IGNORE_CASE)
        /** "for …" or "with …" right after the amount. */
        private val forOrWith = Regex("""^\s+(?:for|with)\s""", RegexOption.IGNORE_CASE)
        private val with =
            Regex(
                """(?:,\s*)?(?:\bsplit\s+)?\bwith\s+(.+?)(?=\s+for\s+|\s+in\s+|$)""",
                RegexOption.IGNORE_CASE,
            )
        private val inGroup = Regex("""\s+in\s+(?:the\s+)?(.+?)$""", RegexOption.IGNORE_CASE)
        private val forWhat =
            Regex(
                """\bfor\s+(?:(?:a|an|the)\s+)?(.+?)(?=\s*,|\s+split\b|\s+with\b|$)""",
                RegexOption.IGNORE_CASE,
            )
        private val nameSeparators = Regex("""\s*(?:,|&|\band\b)\s*""", RegexOption.IGNORE_CASE)

        /**
         * The category keywords of insights §3.6.6, with groceries under Food (domain.md §12 #9).
         */
        private val keywords: Map<Category, Set<String>> =
            linkedMapOf(
                Category.Travel to
                    setOf(
                        "cab",
                        "taxi",
                        "uber",
                        "ola",
                        "auto",
                        "metro",
                        "train",
                        "bus",
                        "flight",
                        "fuel",
                        "petrol",
                        "parking",
                    ),
                Category.Food to
                    setOf(
                        "dinner",
                        "lunch",
                        "breakfast",
                        "food",
                        "coffee",
                        "snack",
                        "pizza",
                        "biryani",
                        "restaurant",
                        "grocery",
                        "groceries",
                    ),
                Category.Rent to setOf("rent"),
                Category.Bills to
                    setOf("electricity", "wifi", "wi-fi", "internet", "gas", "water", "bill"),
                Category.Fun to setOf("movie", "ticket", "concert", "game"),
                Category.Stays to setOf("hotel", "stay", "hostel", "airbnb", "villa"),
                Category.Shopping to setOf("shopping"),
            )

        /** The category whose keywords [text] mentions; Other when none fits. */
        fun guessCategory(text: String): Category {
            val words = text.lowercase().split(Regex("[^a-z-]+")).filter { it.isNotEmpty() }
            return keywords.entries
                .firstOrNull { (_, keys) ->
                    words.any { it in keys || it.removeSuffix("s") in keys }
                }
                ?.key ?: Category.Other
        }

        /**
         * Reads [text] as an expense, or null when it names no amount. A bare number only counts
         * after a verb, with a currency word, or followed by "for" / "with", so "what happened on
         * 28 sep" is not an expense. [isGroup] says whether a trailing "in …" names one of your
         * groups (otherwise it stays part of what it was for).
         */
        fun parse(text: String, isGroup: (String) -> Boolean): ExpensePhrase? {
            val written = text.trim().trimEnd('.', '!', '?')
            val hasVerb = expenseVerb.containsMatchIn(written)
            var rest = written.replace(verb, "")
            val amountMatch = money.find(rest) ?: return null
            val amount =
                amountMatch.groupValues[2].replace(",", "").toBigDecimalOrNull() ?: return null
            if (amount.signum() <= 0) return null
            val hasCurrency = amountMatch.groups[1] != null || amountMatch.groups[3] != null
            val after = rest.substring(amountMatch.range.last + 1)
            if (!hasVerb && !hasCurrency && !forOrWith.containsMatchIn(after)) return null
            rest = rest.removeRange(amountMatch.range).trim()
            val group = inGroup.find(rest)?.takeIf { isGroup(it.groupValues[1]) }
            if (group != null) rest = rest.removeRange(group.range)
            val withMatch = with.find(rest)
            val names =
                withMatch
                    ?.groupValues
                    ?.get(1)
                    ?.split(nameSeparators)
                    ?.map(String::trim)
                    ?.filter(String::isNotEmpty)
                    .orEmpty()
            if (withMatch != null) rest = rest.removeRange(withMatch.range)
            val what =
                forWhat.find(rest)?.groupValues?.get(1)?.trim()?.trimEnd(',')?.takeIf {
                    it.isNotBlank()
                }
            return ExpensePhrase(amount, what, names, group?.groupValues?.get(1)?.trim())
        }
    }
}
