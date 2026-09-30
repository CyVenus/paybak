package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SavedMethodKind {
    @SerialName("upi") Upi,
    @SerialName("bank") Bank,
}

/**
 * A way friends can pay the user (domain.md §1.1): a UPI ID in [value], or a bank shown as "HDFC
 * Bank ···· 4821". Exactly one is primary when there are any.
 */
@Serializable
data class SavedPaymentMethod(
    val id: String,
    val kind: SavedMethodKind,
    val value: String? = null,
    val bankName: String? = null,
    val last4: String? = null,
    val primary: Boolean = false,
)

@Serializable
enum class AvatarGender {
    @SerialName("boy") Boy,
    @SerialName("girl") Girl,
}

/** The Boy character's picks: option ids from screens-profile §1.3 (★ = the defaults here). */
@Serializable
data class BoyLook(
    val hair: String = "curly",
    val beard: String = "none",
    val eyewear: String = "round",
    val eyes: String = "dots",
    val mouth: String = "smile",
    val outfit: String = "hoodie",
)

/** The Girl character's picks (screens-profile §1.3). */
@Serializable
data class GirlLook(
    val hair: String = "long-wavy",
    val accessory: String = "none",
    val eyewear: String = "round",
    val eyes: String = "dots",
    val mouth: String = "smile",
    val outfit: String = "t-shirt",
)

/**
 * A custom character from Edit avatar (screens-profile §1.7). Both genders' picks are kept, so
 * switching back restores them; [gender] is the one shown.
 */
@Serializable
data class AvatarLook(
    val gender: AvatarGender = AvatarGender.Boy,
    val boy: BoyLook = BoyLook(),
    val girl: GirlLook = GirlLook(),
) {
    companion object {
        /** "Curly, Beard none, Round, Dots, Smile, Hoodie": the Profile ref's look. */
        val DefaultBoy = AvatarLook()
    }
}
