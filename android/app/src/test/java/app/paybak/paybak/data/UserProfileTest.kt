package app.paybak.paybak.data

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
}
