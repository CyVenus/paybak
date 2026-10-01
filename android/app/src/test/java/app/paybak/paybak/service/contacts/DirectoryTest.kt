package app.paybak.paybak.service.contacts

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.feature.friends.ScannedCode
import app.paybak.paybak.feature.friends.scannedCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Add friend's lists and lookups over the simulated directory (screens-groups §7). */
class DirectoryTest {
    private val people = Demo.load().ledger.people

    private val accounts =
        listOf(
            DirectoryAccount("p-kabir", "Kabir Singh", "kabir", "+91 98100 22305", "avatar-6"),
            DirectoryAccount("p-meera", "Meera Iyer", "meera", "+91 98100 22306", "avatar-7"),
            DirectoryAccount("p-tara", "Tara Nair", "tara", "tara@example.com"),
        )

    private val contacts =
        listOf(
            DeviceContact("Kabir Singh", "+91 98100 22305"),
            DeviceContact("Meera Iyer", "+91 98100 22306"),
            DeviceContact("Ananya Rao", "+91 98765 43210"),
        )

    private val directory = Directory(people, accounts)

    @Test
    fun contactsSplitIntoOnPaybakAndInvite() {
        val lists = directory.addFriendLists(contacts, "")
        assertEquals(
            listOf("Kabir Singh" to "@kabir", "Meera Iyer" to "@meera"),
            lists.onPaybak.map { it.person.name to it.handle },
        )
        assertTrue(lists.onPaybak.all { it.added })
        assertEquals(listOf("Ananya Rao"), lists.invite.map { it.name })
        assertEquals("AR", lists.invite.single().initials)
    }

    @Test
    fun searchFiltersAndFindsUsernames() {
        assertEquals(
            listOf("Meera Iyer"),
            directory.addFriendLists(contacts, "mee").onPaybak.map { it.person.name },
        )
        val tara = directory.addFriendLists(contacts, "@tara").onPaybak.single()
        assertEquals("Tara Nair", tara.person.name)
        assertEquals(false, tara.added)
        assertEquals(
            listOf("Ananya Rao"),
            directory.addFriendLists(contacts, "98765").invite.map { it.name },
        )
    }

    @Test
    fun anUnknownNumberBecomesAnInviteAndNothingMatchesOtherText() {
        val lists = directory.addFriendLists(contacts, "+91 90000 11111")
        assertEquals(listOf("+91 90000 11111"), lists.invite.map { it.contact })
        assertTrue(directory.addFriendLists(contacts, "zzz").isEmpty)
    }

    @Test
    fun lookups() {
        assertEquals("p-rohan", directory.byUsername("@rohan")?.id)
        assertEquals("p-tara", directory.byUsername("tara")?.id)
        assertNull(directory.byUsername("nobody"))
        assertEquals("p-ananya", directory.byContact("+919876543210")?.id)
        assertEquals("9876543210", contactKey("+91 98765-43210"))
        assertNull(contactKey("Ananya"))
    }

    @Test
    fun aScannedCodeIsSomeoneYourOwnOrNotPaybak() {
        val meera = directory.scannedCode("https://paybak.app/i/meera", "arjun")
        assertEquals("p-meera", (meera as ScannedCode.User).person.id)
        assertEquals(ScannedCode.Own, directory.scannedCode("https://paybak.app/i/arjun", "arjun"))
        assertEquals(ScannedCode.NotPaybak, directory.scannedCode("hello", "arjun"))
    }
}
