package app.paybak.paybak.feature.signin

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownTest {
    @Test
    fun formatsMinutesAndTwoDigitSeconds() {
        assertEquals("0:30", formatCountdown(30))
        assertEquals("0:05", formatCountdown(5))
        assertEquals("1:00", formatCountdown(60))
    }
}
