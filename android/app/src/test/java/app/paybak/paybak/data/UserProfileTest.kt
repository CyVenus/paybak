package app.paybak.paybak.data

import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.domain.model.SavedPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {
    @Test
    fun initialsUseFirstAndLastName() {
        assertEquals("AM", UserProfile(name = "Arjun Mehta").initials)
        assertEquals("AM", UserProfile(name = "  arjun kumar  mehta ").initials)
    }

    @Test
    fun singleNameGivesOneInitial() {
        assertEquals("A", UserProfile(name = "Arjun").initials)
        assertEquals("", UserProfile(name = " ").initials)
    }

    @Test
    fun firstNameIsTheFirstWord() {
        assertEquals("Arjun", UserProfile(name = " Arjun Mehta").firstName)
    }

    @Test
    fun anM1UpiIdBecomesThePrimaryMethod() {
        val migrated =
            UserProfile(name = "Arjun Mehta", upiId = "arjun@okaxis")
                .normalized(UserProfile(name = "Arjun Mehta", upiId = "arjun@okaxis"))
        assertEquals(
            listOf(
                SavedPaymentMethod(
                    "pm-upi",
                    SavedMethodKind.Upi,
                    value = "arjun@okaxis",
                    primary = true,
                )
            ),
            migrated.paymentMethods,
        )
        assertEquals("arjun", migrated.username)
    }

    @Test
    fun theUpiIdAndThePrimaryMethodStayInStep() {
        val bank =
            SavedPaymentMethod(
                "pm-bank",
                SavedMethodKind.Bank,
                bankName = "HDFC Bank",
                last4 = "4821",
            )
        val before =
            UserProfile(upiId = "arjun@okaxis").normalized().let {
                it.copy(paymentMethods = it.paymentMethods + bank)
            }
        // Setup 3 edits the UPI ID: the primary method follows.
        val edited = before.copy(upiId = "arjun@ybl").normalized(before)
        assertEquals("arjun@ybl", edited.primaryMethod?.value)
        // Settings makes the bank primary: the UPI ID mirror empties.
        val bankPrimary =
            edited.copy(
                paymentMethods =
                    edited.paymentMethods.map { it.copy(primary = it.kind == SavedMethodKind.Bank) }
            )
        assertEquals("", bankPrimary.normalized(edited).upiId)
    }

    @Test
    fun theDefaultCurrencyFallsBackToRupees() {
        assertEquals("INR", UserProfile().defaultCurrency)
        assertEquals("AED", UserProfile(currencyCode = "AED").defaultCurrency)
    }
}
