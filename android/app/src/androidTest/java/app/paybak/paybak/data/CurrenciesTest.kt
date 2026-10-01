package app.paybak.paybak.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The currency pickers list spendable currencies only, from the device's ICU data. */
@RunWith(AndroidJUnit4::class)
class CurrenciesTest {
    private val codes = Currencies.all(Locale.US).map { it.code }.toSet()

    @Test
    fun listsEveryRegionsTender() {
        listOf("INR", "USD", "EUR", "GBP", "AED", "SGD", "JPY", "XAF", "XOF").forEach {
            assertTrue("$it is missing", it in codes)
        }
    }

    @Test
    fun leavesOutFundsUnitsAndMetals() {
        listOf("XUA", "XDR", "XSU", "BOV", "CHE", "CHW", "USN", "XAU", "XTS").forEach {
            assertFalse("$it is listed", it in codes)
        }
    }
}
