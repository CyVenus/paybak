package app.paybak.paybak.data

import android.icu.text.Collator
import android.icu.util.Currency as IcuCurrency
import android.icu.util.ULocale
import java.util.Date
import java.util.Locale

/**
 * An ISO 4217 currency as the app shows it.
 *
 * @property symbol The en-US symbol, e.g. "₹", "$", "S$".
 */
data class Currency(val code: String, val name: String, val symbol: String) {
    /** Text for the round symbol tile: the symbol when it is short, otherwise the ISO code. */
    val tileSymbol: String
        get() = if (symbol.length <= 2) symbol else code
}

/** The region-based suggestion on Setup 2. */
data class SuggestedCurrency(
    val currency: Currency,
    /** False when the device region gave no currency and INR is only the fallback. */
    val fromRegion: Boolean,
)

/** Every currency in use today, plus the Setup 2 suggestion and popular list. */
object Currencies {
    /** "Popular" on Setup 2, in this order (minus the suggested one). */
    val PopularCodes = listOf("USD", "EUR", "GBP", "AED", "SGD")

    const val FALLBACK_CODE = "INR"

    /** Figma's names and symbols for the six designed currencies win over the platform's. */
    private val Designed =
        mapOf(
            "INR" to ("Indian Rupee" to "₹"),
            "USD" to ("US Dollar" to "$"),
            "EUR" to ("Euro" to "€"),
            "GBP" to ("British Pound" to "£"),
            "AED" to ("UAE Dirham" to "AED"),
            "SGD" to ("Singapore Dollar" to "S$"),
        )

    /**
     * Every currency some region uses as legal tender today, sorted by name in [locale]. Asking
     * region by region leaves out the funds and units of account (XUA, XDR, BOV, CHE…) that ICU
     * also lists, like iOS's common ISO currency codes.
     */
    fun all(locale: Locale = Locale.getDefault()): List<Currency> {
        val today = Date()
        val collator = Collator.getInstance(locale)
        return Locale.getISOCountries()
            .flatMap {
                IcuCurrency.getAvailableCurrencyCodes(ULocale("und_$it"), today).orEmpty().asList()
            }
            .distinct()
            .map { currency(it, locale) }
            .sortedWith { a, b -> collator.compare(a.name, b.name) }
    }

    /** The device region's currency, or INR when the region has none. */
    fun suggested(locale: Locale = Locale.getDefault()): SuggestedCurrency {
        val regionCode = runCatching {
            java.util.Currency.getInstance(locale)?.currencyCode
        }
            .getOrNull()
        return if (regionCode != null) {
            SuggestedCurrency(currency(regionCode, locale), fromRegion = true)
        } else {
            SuggestedCurrency(currency(FALLBACK_CODE, locale), fromRegion = false)
        }
    }

    fun popular(excludingCode: String, locale: Locale = Locale.getDefault()): List<Currency> =
        PopularCodes.filter { it != excludingCode }.map { currency(it, locale) }

    /** The currency with the ISO [code], with Figma's name and symbol for the designed six. */
    fun currency(code: String, locale: Locale = Locale.getDefault()): Currency {
        Designed[code]?.let { (name, symbol) ->
            return Currency(code, name, symbol)
        }
        val icu = IcuCurrency.getInstance(code)
        return Currency(
            code = code,
            name = icu.getDisplayName(locale),
            symbol = icu.getSymbol(Locale.US),
        )
    }
}
