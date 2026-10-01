package app.paybak.paybak.feature.pickers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The people picker's search (add-expense §5): names, usernames and contacts. */
class PeopleSearchTest {
    private fun priya(query: String) =
        matchesPerson("Priya Sharma", "priya", "priya.s@example.com", query)

    @Test
    fun aLeadingAtSearchesUsernames() {
        assertTrue(priya("@priya"))
        assertTrue(priya(" @Pri "))
        assertFalse(priya("@kabir"))
        assertTrue(priya("@"))
    }

    @Test
    fun namesAndContactsStillMatch() {
        assertTrue(priya("sharma"))
        assertTrue(priya("priya.s@example.com"))
        assertTrue(priya(""))
        assertFalse(matchesPerson("Kabir", null, null, "priya"))
    }
}
