package app.paybak.paybak.feature.avatar

import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.domain.avatar.AvatarSlot
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The editor's starting look and unsaved-changes rule (screens-profile §1.7, §3.5). */
class AvatarEditorStateTest {
    @Test
    fun theEditorOpensOnTheSavedCharacterOrAGuessFromThePreset() {
        val saved = AvatarLook(gender = AvatarGender.Girl, boy = BoyLook(hair = "spiky"))
        assertEquals(saved, startingLook(AvatarChoice.Character(saved)))
        assertEquals(AvatarGender.Boy, startingLook(AvatarChoice.Preset(0)).gender)
        assertEquals(AvatarGender.Girl, startingLook(AvatarChoice.Preset(1)).gender)
        assertEquals(AvatarGender.Girl, startingLook(AvatarChoice.Preset(3)).gender)
        assertEquals(AvatarLook(), startingLook(AvatarChoice.None))
        assertEquals(AvatarLook(), startingLook(AvatarChoice.Photo("me.jpg")))
    }

    @Test
    fun switchingGenderAndBackIsNotAChange() {
        val state = AvatarEditorState(AvatarLook(), AvatarLook(), AvatarSlot.Hair)
        state.selectSlot(AvatarSlot.Mouth)
        state.selectGender(AvatarGender.Girl)
        assertEquals(AvatarSlot.Hair, state.slot)
        assertTrue(state.isDirty)
        state.selectGender(AvatarGender.Boy)
        assertFalse(state.isDirty)
    }

    @Test
    fun aPickMakesTheDraftDirtyUntilItIsPickedBack() {
        val state = AvatarEditorState(AvatarLook(), AvatarLook(), AvatarSlot.Hair)
        state.pick("quiff")
        assertTrue(state.isDirty)
        assertEquals("quiff", state.draft.boy.hair)
        state.pick("curly")
        assertFalse(state.isDirty)
    }

    @Test
    fun theDesignedStatesFollowThePrototypeStory() {
        val (girlOutfit, slot) = DesignedEditorStates.getValue("editAvatarGirlOutfit")
        assertEquals(AvatarSlot.Outfit, slot)
        assertEquals(AvatarGender.Girl, girlOutfit.gender)
        assertEquals("striped-tee", girlOutfit.girl.outfit)
        assertEquals("bow", girlOutfit.girl.accessory)
        assertEquals(BoyLook("quiff", "stubble", "square", outfit = "jacket"), girlOutfit.boy)
    }
}
