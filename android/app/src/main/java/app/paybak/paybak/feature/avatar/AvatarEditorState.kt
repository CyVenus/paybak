package app.paybak.paybak.feature.avatar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.domain.avatar.AvatarParts
import app.paybak.paybak.domain.avatar.AvatarSlot
import app.paybak.paybak.domain.avatar.normalized
import app.paybak.paybak.domain.avatar.shuffled
import app.paybak.paybak.domain.avatar.with
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import app.paybak.paybak.domain.model.GirlLook
import app.paybak.paybak.domain.model.LedgerJson

/**
 * The editor's draft (screens-profile §3.2): everything on screen renders from [draft], and nothing
 * is saved until Save. [original] is what the editor opened with, so [isDirty] tells whether Back
 * must ask first (§3.5).
 */
@Stable
class AvatarEditorState(val original: AvatarLook, draft: AvatarLook, slot: AvatarSlot) {
    var draft by mutableStateOf(draft)
        private set

    /** The selected category chip. */
    var slot by mutableStateOf(slot)
        private set

    val isDirty: Boolean
        get() = draft != original

    /** Boy | Girl keeps each character's picks; the chips start again at Hair. */
    fun selectGender(gender: AvatarGender) {
        if (gender == draft.gender) return
        draft = draft.copy(gender = gender)
        slot = AvatarSlot.Hair
    }

    fun selectSlot(slot: AvatarSlot) {
        this.slot = slot
    }

    fun pick(option: String) {
        draft = draft.with(slot, option)
    }

    fun shuffle() {
        draft = draft.shuffled()
    }

    companion object {
        /** Saves the looks as JSON and the chip by name, across recreation. */
        val Saver =
            listSaver<AvatarEditorState, String>(
                save = { listOf(it.original.encode(), it.draft.encode(), it.slot.name) },
                restore = { (original, draft, slot) ->
                    AvatarEditorState(
                        decode(original),
                        decode(draft),
                        AvatarSlot.valueOf(slot),
                    )
                },
            )

        private fun AvatarLook.encode() = LedgerJson.encodeToString(AvatarLook.serializer(), this)

        private fun decode(json: String) =
            LedgerJson.decodeFromString(AvatarLook.serializer(), json)
    }
}

/**
 * The editor opened on the user's [avatar] (§1.7): a saved character as it is; otherwise the
 * default looks, showing the gender of the Setup 1 head (Priya's and Esha's are Girl), or Boy.
 */
fun startingLook(avatar: AvatarChoice): AvatarLook =
    when (avatar) {
        is AvatarChoice.Character -> avatar.look.normalized()
        is AvatarChoice.Preset ->
            AvatarLook(gender = if (avatar.index in GIRL_PRESETS) AvatarGender.Girl else AvatarGender.Boy)
        AvatarChoice.None,
        is AvatarChoice.Photo -> AvatarLook()
    }

/** Setup 1 heads 2 and 4 (Priya, Esha). */
private val GIRL_PRESETS = setOf(1, 3)

/**
 * The seven designed editor states (§3.7), for their debug start screens: the prototype story is
 * Quiff → Stubble → Square → Jacket for the Boy, then Ponytail → Bow → Striped tee for the Girl.
 */
internal val DesignedEditorStates: Map<String, Pair<AvatarLook, AvatarSlot>> = run {
    val quiff = BoyLook(hair = "quiff")
    val stubble = quiff.copy(beard = "stubble")
    val square = stubble.copy(eyewear = "square")
    val jacket = square.copy(outfit = "jacket")
    val ponytail = GirlLook(hair = "ponytail")
    val bow = ponytail.copy(accessory = "bow")
    val boy = AvatarGender.Boy
    val girl = AvatarGender.Girl
    mapOf(
        "editAvatarBoyHair" to (AvatarLook(boy, quiff) to AvatarSlot.Hair),
        "editAvatarBoyBeard" to (AvatarLook(boy, stubble) to AvatarSlot.Beard),
        "editAvatarBoyEyewear" to (AvatarLook(boy, square) to AvatarSlot.Eyewear),
        "editAvatarBoyOutfit" to (AvatarLook(boy, jacket) to AvatarSlot.Outfit),
        "editAvatarDiscard" to (AvatarLook(boy, jacket) to AvatarSlot.Outfit),
        "editAvatarGirlHair" to (AvatarLook(girl, jacket, ponytail) to AvatarSlot.Hair),
        "editAvatarGirlAccessory" to (AvatarLook(girl, jacket, bow) to AvatarSlot.Accessory),
        "editAvatarGirlOutfit" to
            (AvatarLook(girl, jacket, bow.copy(outfit = "striped-tee")) to AvatarSlot.Outfit),
    )
}

/**
 * The editor's state for the saved [avatar], kept across recreation. A debug start id [designed]
 * opens one of the designed states instead.
 */
@Composable
fun rememberAvatarEditorState(avatar: AvatarChoice, designed: String?): AvatarEditorState =
    rememberSaveable(saver = AvatarEditorState.Saver) {
        val original = startingLook(avatar)
        val (draft, slot) = DesignedEditorStates[designed] ?: (original to AvatarSlot.Hair)
        AvatarEditorState(original, draft, slot)
    }

/** The chips of the draft's gender, in order. */
val AvatarEditorState.slots: List<AvatarSlot>
    get() = AvatarParts.slots(draft.gender)
