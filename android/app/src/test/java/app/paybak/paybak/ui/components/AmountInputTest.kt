package app.paybak.paybak.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {
    @Test
    fun keepsDigitsOnly() {
        assertEquals("2800", amountInput("2,800", allowDecimals = false))
        assertEquals("125", amountInput("12.5", allowDecimals = false))
    }

    @Test
    fun dropsLeadingZeros() {
        assertEquals("5", amountInput("05", allowDecimals = false))
        assertEquals("", amountInput("0", allowDecimals = false))
    }

    @Test
    fun keepsOneDecimalPointWithUpToTwoDecimals() {
        assertEquals("12.5", amountInput("12.5", allowDecimals = true))
        assertEquals("12.50", amountInput("12.5.07", allowDecimals = true))
        assertEquals("0.5", amountInput(".5", allowDecimals = true))
        assertEquals("3.", amountInput("3.", allowDecimals = true))
    }
}
