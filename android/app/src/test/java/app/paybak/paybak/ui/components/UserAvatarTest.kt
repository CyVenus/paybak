package app.paybak.paybak.ui.components

import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.ui.icons.PbIcon
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAvatarTest {
    @Test
    fun theInitialsAreTheFallback() {
        assertEquals(PbAvatarContent.Initials("AM"), AvatarChoice.None.toAvatarContent(null, "AM"))
        assertEquals(
            PbAvatarContent.Initials("AM"),
            AvatarChoice.Preset(99).toAvatarContent(null, "AM"),
        )
    }

    @Test
    fun beforeANameExistsItIsTheProfileIcon() {
        assertEquals(
            PbAvatarContent.Symbol(PbIcon.Profile),
            AvatarChoice.None.toAvatarContent(null, ""),
        )
        assertEquals(
            PbAvatarContent.Art(PbPeepHead.Arjun),
            AvatarChoice.Preset(0).toAvatarContent(null, ""),
        )
    }
}
