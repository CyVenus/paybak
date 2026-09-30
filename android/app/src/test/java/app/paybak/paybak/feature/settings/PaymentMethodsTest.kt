package app.paybak.paybak.feature.settings

import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.domain.model.SavedPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Payment methods (screens-settings §4–5, §12.2). */
class PaymentMethodsTest {
    private val demo =
        UserProfile(
            name = "Arjun Mehta",
            upiId = "arjun@okaxis",
            username = "arjun",
            paymentMethods =
                listOf(
                    SavedPaymentMethod(
                        "pm-upi",
                        SavedMethodKind.Upi,
                        "arjun@okaxis",
                        primary = true,
                    ),
                    SavedPaymentMethod(
                        "pm-hdfc",
                        SavedMethodKind.Bank,
                        bankName = "HDFC Bank",
                        last4 = "4821",
                    ),
                ),
        )

    @Test
    fun theFirstMethodIsPrimaryAndLaterOnesAreNot() {
        val first = UserProfile(name = "Arjun").addingUpi(" arjun@okaxis ").normalized()
        assertTrue(first.paymentMethods.single().primary)
        assertEquals("arjun@okaxis", first.upiId)
        val second = first.addingBank("ICICI Bank", "1234 5678 9012").normalized(first)
        assertFalse(second.paymentMethods.last().primary)
        assertEquals("9012", second.paymentMethods.last().last4)
    }

    @Test
    fun makingABankPrimaryClearsTheMirroredUpiId() {
        val bank = demo.makingPrimary("pm-hdfc").normalized(demo)
        assertEquals("pm-hdfc", bank.primaryMethod?.id)
        assertEquals("", bank.upiId)
    }

    @Test
    fun removingThePrimaryPromotesTheNextMethod() {
        val left = demo.removingMethod("pm-upi")
        assertEquals(listOf("pm-hdfc"), left.paymentMethods.map { it.id })
        assertTrue(left.paymentMethods.single().primary)
        assertTrue(left.removingMethod("pm-hdfc").paymentMethods.isEmpty())
    }

    @Test
    fun aUpiIdNeedsAnAtAndMustBeNew() {
        assertEquals(UpiProblem.Invalid, demo.upiProblem("arjunokhdfcbank"))
        assertEquals(UpiProblem.Duplicate, demo.upiProblem("Arjun@okaxis"))
        assertNull(demo.upiProblem("arjun@okhdfcbank"))
    }

    @Test
    fun theUpiTabSuggestsTheBanksHandle() {
        assertEquals("arjun@okhdfcbank", demo.suggestedUpi())
        assertEquals("", demo.addingUpi("arjun@okhdfcbank").suggestedUpi())
        assertEquals("", UserProfile(name = "Arjun").suggestedUpi())
    }

    @Test
    fun aBankAccountNeedsANameAndFourDigits() {
        assertTrue(isValidBankAccount("HDFC Bank", "4821"))
        assertFalse(isValidBankAccount(" ", "4821"))
        assertFalse(isValidBankAccount("HDFC Bank", "482"))
    }

    @Test
    fun currencyNamesUseTheSettingsCasing() {
        assertEquals("Indian rupee", sentenceCase("Indian Rupee"))
        assertEquals("US dollar", sentenceCase("US Dollar"))
        assertEquals("UAE dirham", sentenceCase("UAE Dirham"))
        assertEquals("Euro", sentenceCase("Euro"))
    }
}
