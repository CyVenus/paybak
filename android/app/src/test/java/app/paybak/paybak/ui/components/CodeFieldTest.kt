package app.paybak.paybak.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class CodeFieldTest {
    @Test
    fun keepsDigitsOnlyUpToSix() {
        assertEquals("123", nextCode(code = "12", input = "123", isError = false))
        assertEquals("123456", nextCode(code = "", input = "12-34 5678", isError = false))
    }

    @Test
    fun aSeventhDigitIsIgnoredWithoutAnError() {
        assertEquals("123456", nextCode(code = "123456", input = "1234567", isError = false))
    }

    @Test
    fun typingAfterAWrongCodeStartsOverFromBoxOne() {
        assertEquals("7", nextCode(code = "123456", input = "1234567", isError = true))
    }

    @Test
    fun backspaceAfterAWrongCodeDeletesTheLastDigit() {
        assertEquals("12345", nextCode(code = "123456", input = "12345", isError = true))
    }

    @Test
    fun aCodePastedOrAutofilledAfterAWrongCodeReplacesIt() {
        assertEquals("000000", nextCode(code = "123456", input = "123456000000", isError = true))
        assertEquals("000000", nextCode(code = "123456", input = "000000", isError = true))
    }
}
