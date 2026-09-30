package app.paybak.paybak.domain

import app.paybak.paybak.domain.avatar.AvatarParts
import app.paybak.paybak.domain.avatar.AvatarSlot
import app.paybak.paybak.domain.avatar.normalized
import app.paybak.paybak.domain.avatar.pick
import app.paybak.paybak.domain.avatar.shuffled
import app.paybak.paybak.domain.avatar.with
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The avatar catalog and draft edits (screens-profile §1.3, §3.2). */
class AvatarPartsTest {
    @Test
    fun eachGenderHasSixChipsOfSixOptionsInFigmaOrder() {
        assertEquals(
            listOf("hair", "beard", "eyewear", "eyes", "mouth", "outfit"),
            AvatarParts.slots(AvatarGender.Boy).map { it.id },
        )
        assertEquals(
            listOf("hair", "accessory", "eyewear", "eyes", "mouth", "outfit"),
            AvatarParts.slots(AvatarGender.Girl).map { it.id },
        )
        for (gender in AvatarGender.entries) {
            AvatarParts.slots(gender).forEach {
                assertEquals(6, AvatarParts.options(gender, it).size)
            }
        }
        assertEquals(
            listOf("None", "Round", "Square", "Rimless", "Shades", "Round shades"),
            AvatarParts.options(AvatarGender.Boy, AvatarSlot.Eyewear).map { it.name },
        )
    }

    @Test
    fun theDefaultsAreTodaysLooks() {
        val look = AvatarLook()
        assertEquals("curly", look.pick(AvatarSlot.Hair))
        assertEquals("none", look.pick(AvatarSlot.Beard))
        assertEquals("hoodie", look.pick(AvatarSlot.Outfit))
        val girl = look.copy(gender = AvatarGender.Girl)
        assertEquals("long-wavy", girl.pick(AvatarSlot.Hair))
        assertEquals("t-shirt", girl.pick(AvatarSlot.Outfit))
    }

    @Test
    fun switchingGenderKeepsEachCharactersPicks() {
        val quiff = AvatarLook().with(AvatarSlot.Hair, "quiff")
        val ponytail = quiff.copy(gender = AvatarGender.Girl).with(AvatarSlot.Hair, "ponytail")
        val back = ponytail.copy(gender = AvatarGender.Boy)
        assertEquals("quiff", back.pick(AvatarSlot.Hair))
        assertEquals("ponytail", back.girl.hair)
    }

    @Test
    fun shuffleChangesOnlyTheShownGenderAndAlwaysChangesIt() {
        val start = AvatarLook(boy = BoyLook(hair = "quiff"))
        val random = Random(7)
        repeat(50) {
            val next = start.shuffled(random)
            assertNotEquals(start, next)
            assertEquals(start.girl, next.girl)
            assertEquals(AvatarGender.Boy, next.gender)
            AvatarParts.slots(AvatarGender.Boy).forEach { slot ->
                assertTrue(
                    AvatarParts.options(AvatarGender.Boy, slot).any { it.id == next.pick(slot) }
                )
            }
        }
    }

    @Test
    fun unknownOptionsFallBackToTheDefaults() {
        val look =
            AvatarLook(gender = AvatarGender.Girl, boy = BoyLook(hair = "mohawk", beard = "stubble"))
                .with(AvatarSlot.Accessory, "crown")
                .normalized()
        assertEquals("curly", look.boy.hair)
        assertEquals("stubble", look.boy.beard)
        assertEquals("none", look.girl.accessory)
        assertEquals(AvatarGender.Girl, look.gender)
    }
}
