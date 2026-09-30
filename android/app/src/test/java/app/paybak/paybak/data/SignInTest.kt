package app.paybak.paybak.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInTest {
    @Test
    fun aPlausibleEmailIsTrimmed() {
        assertEquals(
            SignInContact(SignInMethod.Email, "arjun@example.com"),
            parseSignInContact("  arjun@example.com "),
        )
    }

    @Test
    fun anEmailNeedsAnAtAndADotWithoutSpaces() {
        listOf("arjun", "arjun@", "arjun@example", "@example.com", "ar jun@example.com", "a@b.")
            .forEach { assertNull(it, parseSignInContact(it)) }
    }

    @Test
    fun aPhoneNumberWithoutCountryCodeGetsPlus91() {
        assertEquals(
            SignInContact(SignInMethod.Phone, "+91 98765 43210"),
            parseSignInContact("98765 43210"),
        )
    }

    @Test
    fun aPhoneNumberWithCountryCodeIsKeptAsTyped() {
        assertEquals(
            SignInContact(SignInMethod.Phone, "+44 20-7946-0958"),
            parseSignInContact(" +44 20-7946-0958"),
        )
    }

    @Test
    fun aPhoneNumberNeedsSevenDigitsAndNothingButSpacesAndDashes() {
        assertEquals(SignInMethod.Phone, parseSignInContact("1234567")?.method)
        listOf("123456", "12-34 56", "98765(43210)", "98+76543210", "+", "").forEach {
            assertNull(it, parseSignInContact(it))
        }
    }

    @Test
    fun onlyTheStubCodeIsCorrect() {
        assertTrue(SignInCode.isCorrect("000000"))
        assertFalse(SignInCode.isCorrect("482917"))
    }
}
