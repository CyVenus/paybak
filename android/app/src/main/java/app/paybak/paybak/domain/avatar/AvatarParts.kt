package app.paybak.paybak.domain.avatar

import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import app.paybak.paybak.domain.model.GirlLook
import kotlin.random.Random

/**
 * One choice in the avatar editor (screens-profile §1.3): a category chip. Outfit also sets the
 * back outfit layer, and Girl Hair the back hair layer, so the editor never shows "back" layers.
 */
enum class AvatarSlot(val id: String, val label: String) {
    Hair("hair", "Hair"),
    Beard("beard", "Beard"),
    Accessory("accessory", "Accessory"),
    Eyewear("eyewear", "Eyewear"),
    Eyes("eyes", "Eyes"),
    Mouth("mouth", "Mouth"),
    Outfit("outfit", "Outfit");

    /** Outfit tiles show the Bust crop; every other tile the Head crop (§1.5). */
    val showsBust: Boolean
        get() = this == Outfit
}

/** An option of a slot: its kebab-case [id] and the Figma name, which labels the tile. */
data class AvatarOption(val id: String, val name: String)

/**
 * The editor's catalog: each gender's slots and options in the Figma parts-sheet order (the order
 * of `assets/avatar-parts/manifest.json`). "none" options draw nothing.
 */
object AvatarParts {
    const val NONE = "none"

    private val eyes = options("dots" to "Dots", "happy" to "Happy", "wink" to "Wink",
        "squint" to "Squint", "wide" to "Wide", "sleepy" to "Sleepy")
    private val mouths = options("smile" to "Smile", "grin" to "Grin", "neutral" to "Neutral",
        "smirk" to "Smirk", "open" to "Open", "tongue" to "Tongue")

    private val boy: Map<AvatarSlot, List<AvatarOption>> =
        linkedMapOf(
            AvatarSlot.Hair to options("curly" to "Curly", "quiff" to "Quiff",
                "side-part" to "Side part", "spiky" to "Spiky", "man-bun" to "Man bun",
                "crew-cut" to "Crew cut"),
            AvatarSlot.Beard to options(NONE to "None", "stubble" to "Stubble",
                "goatee" to "Goatee", "full" to "Full", "mustache" to "Mustache",
                "handlebar" to "Handlebar"),
            AvatarSlot.Eyewear to options(NONE to "None", "round" to "Round",
                "square" to "Square", "rimless" to "Rimless", "shades" to "Shades",
                "round-shades" to "Round shades"),
            AvatarSlot.Eyes to eyes,
            AvatarSlot.Mouth to mouths,
            AvatarSlot.Outfit to options("hoodie" to "Hoodie", "t-shirt" to "T-shirt",
                "polo" to "Polo", "sweater" to "Sweater", "jacket" to "Jacket",
                "shirt" to "Shirt"),
        )

    private val girl: Map<AvatarSlot, List<AvatarOption>> =
        linkedMapOf(
            AvatarSlot.Hair to options("long-wavy" to "Long wavy",
                "long-straight" to "Long straight", "bob" to "Bob", "top-bun" to "Top bun",
                "ponytail" to "Ponytail", "messy-bun" to "Messy bun"),
            AvatarSlot.Accessory to options(NONE to "None", "headband" to "Headband",
                "bow" to "Bow", "flower-clip" to "Flower clip", "hair-clips" to "Hair clips",
                "beanie" to "Beanie"),
            AvatarSlot.Eyewear to options(NONE to "None", "round" to "Round",
                "square" to "Square", "shades" to "Shades", "heart-shades" to "Heart shades",
                "cat-eye" to "Cat-eye"),
            AvatarSlot.Eyes to eyes,
            AvatarSlot.Mouth to mouths,
            AvatarSlot.Outfit to options("t-shirt" to "T-shirt", "hoodie" to "Hoodie",
                "collar-shirt" to "Collar shirt", "sweater" to "Sweater", "blazer" to "Blazer",
                "striped-tee" to "Striped tee"),
        )

    /** The category chips of [gender], in order. */
    fun slots(gender: AvatarGender): List<AvatarSlot> = catalog(gender).keys.toList()

    /** The six tiles of [slot] for [gender]. */
    fun options(gender: AvatarGender, slot: AvatarSlot): List<AvatarOption> =
        catalog(gender)[slot].orEmpty()

    private fun catalog(gender: AvatarGender) =
        when (gender) {
            AvatarGender.Boy -> boy
            AvatarGender.Girl -> girl
        }

    private fun options(vararg pairs: Pair<String, String>) =
        pairs.map { (id, name) -> AvatarOption(id, name) }
}

/** The option [slot] has in the shown gender's picks. */
fun AvatarLook.pick(slot: AvatarSlot): String =
    when (gender) {
        AvatarGender.Boy -> boy.pick(slot)
        AvatarGender.Girl -> girl.pick(slot)
    }

/** This look with [slot] of the shown gender set to [option]; the other gender is untouched. */
fun AvatarLook.with(slot: AvatarSlot, option: String): AvatarLook =
    when (gender) {
        AvatarGender.Boy -> copy(boy = boy.with(slot, option))
        AvatarGender.Girl -> copy(girl = girl.with(slot, option))
    }

/**
 * Shuffle (§3.2): a random option in every slot of the shown gender ("None" included), different
 * from this look. The other gender's picks stay.
 */
fun AvatarLook.shuffled(random: Random = Random.Default): AvatarLook {
    while (true) {
        val next =
            AvatarParts.slots(gender).fold(this) { look, slot ->
                look.with(slot, AvatarParts.options(gender, slot).random(random).id)
            }
        if (next != this) return next
    }
}

/** Unknown option ids (after an asset update) fall back to the slot's default (§7). */
fun AvatarLook.normalized(): AvatarLook {
    val defaults = AvatarLook()
    fun AvatarLook.fixed(gender: AvatarGender): AvatarLook {
        val shown = copy(gender = gender)
        return AvatarParts.slots(gender).fold(shown) { look, slot ->
            val known = AvatarParts.options(gender, slot).any { it.id == look.pick(slot) }
            if (known) look else look.with(slot, defaults.copy(gender = gender).pick(slot))
        }
    }
    return fixed(AvatarGender.Boy).fixed(AvatarGender.Girl).copy(gender = gender)
}

private fun BoyLook.pick(slot: AvatarSlot): String =
    when (slot) {
        AvatarSlot.Hair -> hair
        AvatarSlot.Beard -> beard
        AvatarSlot.Eyewear -> eyewear
        AvatarSlot.Eyes -> eyes
        AvatarSlot.Mouth -> mouth
        AvatarSlot.Outfit -> outfit
        AvatarSlot.Accessory -> error("The boy has no accessory")
    }

private fun BoyLook.with(slot: AvatarSlot, option: String): BoyLook =
    when (slot) {
        AvatarSlot.Hair -> copy(hair = option)
        AvatarSlot.Beard -> copy(beard = option)
        AvatarSlot.Eyewear -> copy(eyewear = option)
        AvatarSlot.Eyes -> copy(eyes = option)
        AvatarSlot.Mouth -> copy(mouth = option)
        AvatarSlot.Outfit -> copy(outfit = option)
        AvatarSlot.Accessory -> error("The boy has no accessory")
    }

private fun GirlLook.pick(slot: AvatarSlot): String =
    when (slot) {
        AvatarSlot.Hair -> hair
        AvatarSlot.Accessory -> accessory
        AvatarSlot.Eyewear -> eyewear
        AvatarSlot.Eyes -> eyes
        AvatarSlot.Mouth -> mouth
        AvatarSlot.Outfit -> outfit
        AvatarSlot.Beard -> error("The girl has no beard")
    }

private fun GirlLook.with(slot: AvatarSlot, option: String): GirlLook =
    when (slot) {
        AvatarSlot.Hair -> copy(hair = option)
        AvatarSlot.Accessory -> copy(accessory = option)
        AvatarSlot.Eyewear -> copy(eyewear = option)
        AvatarSlot.Eyes -> copy(eyes = option)
        AvatarSlot.Mouth -> copy(mouth = option)
        AvatarSlot.Outfit -> copy(outfit = option)
        AvatarSlot.Beard -> error("The girl has no beard")
    }
