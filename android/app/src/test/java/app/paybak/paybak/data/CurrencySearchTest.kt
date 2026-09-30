package app.paybak.paybak.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencySearchTest {
    /** Sorted by name, as Currencies.all() returns them. */
    private val currencies =
        listOf(
            Currency("AED", "UAE Dirham", "AED"),
            Currency("EUR", "Euro", "€"),
            Currency("INR", "Indian Rupee", "₹"),
            Currency("IDR", "Indonesian Rupiah", "Rp"),
            Currency("JPY", "Japanese Yen", "¥"),
            Currency("SGD", "Singapore Dollar", "S$"),
            Currency("STN", "São Tomé & Príncipe Dobra", "Db"),
            Currency("USD", "US Dollar", "$"),
        )

    private fun codes(query: String) = currencies.search(query).map(Currency::code)

    @Test
    fun aBlankQueryMatchesNothing() {
        assertEquals(emptyList<String>(), codes("  "))
    }

    @Test
    fun codePrefixThenNamePrefixThenNameContains() {
        // INR's code starts with "in", Indonesian Rupiah's name does, two other names contain it.
        assertEquals(listOf("INR", "IDR", "SGD", "STN"), codes("in"))
    }

    @Test
    fun anExactCodeComesBeforeEarlierNames() {
        val list = listOf(Currency("XLK", "Lek Bond", "XLK"), Currency("LEK", "Zeta Lek", "LEK"))
        assertEquals(listOf("LEK", "XLK"), list.search("lek").map(Currency::code))
    }

    @Test
    fun namesContainingTheQueryKeepTheirNameOrder() {
        assertEquals(listOf("SGD", "USD"), codes("dollar"))
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertEquals(listOf("STN"), codes("SAO TOME"))
        assertEquals(listOf("JPY"), codes(" yen "))
    }
}
