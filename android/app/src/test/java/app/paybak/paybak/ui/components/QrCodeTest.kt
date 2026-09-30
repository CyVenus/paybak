package app.paybak.paybak.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodeTest {
    /** Figma's code for Arjun's invite link: version 4, 33 × 33 modules at level H. */
    @Test
    fun theInviteLinkIsAVersion4Code() {
        val modules = qrModules("https://paybak.app/i/arjun")
        assertEquals(33, modules.size)
        assertTrue(modules.all { it.size == 33 })
    }

    @Test
    fun theFinderPatternsSitInThreeCorners() {
        val modules = qrModules("https://paybak.app/i/arjun")
        val last = modules.lastIndex
        listOf(0 to 0, 0 to last - 6, last - 6 to 0).forEach { (row, column) ->
            assertTrue(modules[row][column] && modules[row + 6][column + 6])
            assertTrue(!modules[row + 1][column + 1] && modules[row + 3][column + 3])
        }
    }
}
