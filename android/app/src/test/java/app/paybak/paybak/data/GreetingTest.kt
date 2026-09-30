package app.paybak.paybak.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun morningRunsFromFiveToNoon() {
        assertEquals("Good morning, Arjun", greeting("Arjun", hourOfDay = 5))
        assertEquals("Good morning, Arjun", greeting("Arjun", hourOfDay = 11))
    }

    @Test
    fun afternoonRunsFromNoonToFive() {
        assertEquals("Good afternoon, Arjun", greeting("Arjun", hourOfDay = 12))
        assertEquals("Good afternoon, Arjun", greeting("Arjun", hourOfDay = 16))
    }

    @Test
    fun eveningCoversTheRest() {
        assertEquals("Good evening, Arjun", greeting("Arjun", hourOfDay = 17))
        assertEquals("Good evening, Arjun", greeting("Arjun", hourOfDay = 0))
        assertEquals("Good evening, Arjun", greeting("Arjun", hourOfDay = 4))
    }
}
