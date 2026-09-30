package app.paybak.paybak.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiIdTest {
    @Test
    fun acceptsNameAtBank() {
        listOf("arjun@okaxis", " arjun.mehta-1_x@ybl ", "98765@paytm").forEach {
            assertTrue(it, isPlausibleUpiId(it))
        }
    }

    @Test
    fun rejectsAnythingElse() {
        listOf("", "arjun", "a@okaxis", "arjun@o", "arjun@ok axis", "arjun@ok.axis", "@okaxis")
            .forEach { assertFalse(it, isPlausibleUpiId(it)) }
    }
}
