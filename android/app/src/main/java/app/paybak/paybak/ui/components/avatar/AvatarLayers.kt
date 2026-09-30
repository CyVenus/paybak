package app.paybak.paybak.ui.components.avatar

import app.paybak.paybak.R
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import app.paybak.paybak.domain.model.GirlLook

/**
 * The part drawables of a look, bottom → top (screens-profile §1.2, manifest `layers`). Every part
 * is a full 772 × 842 rig canvas, so they stack at the same origin with no offsets. "None" options,
 * unknown ids and outfits without a back layer draw nothing.
 */
internal object AvatarLayers {
    fun of(look: AvatarLook): List<Int> =
        when (look.gender) {
            AvatarGender.Boy -> boy(look.boy)
            AvatarGender.Girl -> girl(look.girl)
        }

    private fun boy(look: BoyLook) =
        listOfNotNull(
            boyOutfitBack[look.outfit],
            R.drawable.avatar_part_boy_base_body,
            boyOutfit[look.outfit],
            R.drawable.avatar_part_boy_base_shadow,
            R.drawable.avatar_part_boy_base_face,
            boyBeard[look.beard],
            boyHair[look.hair],
            boyMouth[look.mouth],
            R.drawable.avatar_part_boy_base_nose,
            boyEyes[look.eyes],
            boyEyewear[look.eyewear],
        )

    private fun girl(look: GirlLook) =
        listOfNotNull(
            girlHairBack[look.hair],
            girlOutfitBack[look.outfit],
            R.drawable.avatar_part_girl_base_body,
            girlOutfit[look.outfit],
            R.drawable.avatar_part_girl_base_shadow,
            R.drawable.avatar_part_girl_base_face,
            girlHair[look.hair],
            girlAccessory[look.accessory],
            girlMouth[look.mouth],
            R.drawable.avatar_part_girl_base_nose,
            girlEyes[look.eyes],
            girlEyewear[look.eyewear],
        )

    private val boyHair =
        mapOf(
            "curly" to R.drawable.avatar_part_boy_hair_curly,
            "quiff" to R.drawable.avatar_part_boy_hair_quiff,
            "side-part" to R.drawable.avatar_part_boy_hair_side_part,
            "spiky" to R.drawable.avatar_part_boy_hair_spiky,
            "man-bun" to R.drawable.avatar_part_boy_hair_man_bun,
            "crew-cut" to R.drawable.avatar_part_boy_hair_crew_cut,
        )

    private val boyBeard =
        mapOf(
            "stubble" to R.drawable.avatar_part_boy_beard_stubble,
            "goatee" to R.drawable.avatar_part_boy_beard_goatee,
            "full" to R.drawable.avatar_part_boy_beard_full,
            "mustache" to R.drawable.avatar_part_boy_beard_mustache,
            "handlebar" to R.drawable.avatar_part_boy_beard_handlebar,
        )

    private val boyEyewear =
        mapOf(
            "round" to R.drawable.avatar_part_boy_eyewear_round,
            "square" to R.drawable.avatar_part_boy_eyewear_square,
            "rimless" to R.drawable.avatar_part_boy_eyewear_rimless,
            "shades" to R.drawable.avatar_part_boy_eyewear_shades,
            "round-shades" to R.drawable.avatar_part_boy_eyewear_round_shades,
        )

    private val boyEyes =
        mapOf(
            "dots" to R.drawable.avatar_part_boy_eyes_dots,
            "happy" to R.drawable.avatar_part_boy_eyes_happy,
            "wink" to R.drawable.avatar_part_boy_eyes_wink,
            "squint" to R.drawable.avatar_part_boy_eyes_squint,
            "wide" to R.drawable.avatar_part_boy_eyes_wide,
            "sleepy" to R.drawable.avatar_part_boy_eyes_sleepy,
        )

    private val boyMouth =
        mapOf(
            "smile" to R.drawable.avatar_part_boy_mouth_smile,
            "grin" to R.drawable.avatar_part_boy_mouth_grin,
            "neutral" to R.drawable.avatar_part_boy_mouth_neutral,
            "smirk" to R.drawable.avatar_part_boy_mouth_smirk,
            "open" to R.drawable.avatar_part_boy_mouth_open,
            "tongue" to R.drawable.avatar_part_boy_mouth_tongue,
        )

    private val boyOutfit =
        mapOf(
            "hoodie" to R.drawable.avatar_part_boy_outfit_hoodie,
            "t-shirt" to R.drawable.avatar_part_boy_outfit_t_shirt,
            "polo" to R.drawable.avatar_part_boy_outfit_polo,
            "sweater" to R.drawable.avatar_part_boy_outfit_sweater,
            "jacket" to R.drawable.avatar_part_boy_outfit_jacket,
            "shirt" to R.drawable.avatar_part_boy_outfit_shirt,
        )

    private val boyOutfitBack = mapOf("hoodie" to R.drawable.avatar_part_boy_outfit_back_hoodie)

    private val girlHair =
        mapOf(
            "long-wavy" to R.drawable.avatar_part_girl_hair_long_wavy,
            "long-straight" to R.drawable.avatar_part_girl_hair_long_straight,
            "bob" to R.drawable.avatar_part_girl_hair_bob,
            "top-bun" to R.drawable.avatar_part_girl_hair_top_bun,
            "ponytail" to R.drawable.avatar_part_girl_hair_ponytail,
            "messy-bun" to R.drawable.avatar_part_girl_hair_messy_bun,
        )

    private val girlHairBack =
        mapOf(
            "long-wavy" to R.drawable.avatar_part_girl_hair_back_long_wavy,
            "long-straight" to R.drawable.avatar_part_girl_hair_back_long_straight,
            "bob" to R.drawable.avatar_part_girl_hair_back_bob,
            "top-bun" to R.drawable.avatar_part_girl_hair_back_top_bun,
            "ponytail" to R.drawable.avatar_part_girl_hair_back_ponytail,
            "messy-bun" to R.drawable.avatar_part_girl_hair_back_messy_bun,
        )

    private val girlAccessory =
        mapOf(
            "headband" to R.drawable.avatar_part_girl_accessory_headband,
            "bow" to R.drawable.avatar_part_girl_accessory_bow,
            "flower-clip" to R.drawable.avatar_part_girl_accessory_flower_clip,
            "hair-clips" to R.drawable.avatar_part_girl_accessory_hair_clips,
            "beanie" to R.drawable.avatar_part_girl_accessory_beanie,
        )

    private val girlEyewear =
        mapOf(
            "round" to R.drawable.avatar_part_girl_eyewear_round,
            "square" to R.drawable.avatar_part_girl_eyewear_square,
            "shades" to R.drawable.avatar_part_girl_eyewear_shades,
            "heart-shades" to R.drawable.avatar_part_girl_eyewear_heart_shades,
            "cat-eye" to R.drawable.avatar_part_girl_eyewear_cat_eye,
        )

    private val girlEyes =
        mapOf(
            "dots" to R.drawable.avatar_part_girl_eyes_dots,
            "happy" to R.drawable.avatar_part_girl_eyes_happy,
            "wink" to R.drawable.avatar_part_girl_eyes_wink,
            "squint" to R.drawable.avatar_part_girl_eyes_squint,
            "wide" to R.drawable.avatar_part_girl_eyes_wide,
            "sleepy" to R.drawable.avatar_part_girl_eyes_sleepy,
        )

    private val girlMouth =
        mapOf(
            "smile" to R.drawable.avatar_part_girl_mouth_smile,
            "grin" to R.drawable.avatar_part_girl_mouth_grin,
            "neutral" to R.drawable.avatar_part_girl_mouth_neutral,
            "smirk" to R.drawable.avatar_part_girl_mouth_smirk,
            "open" to R.drawable.avatar_part_girl_mouth_open,
            "tongue" to R.drawable.avatar_part_girl_mouth_tongue,
        )

    private val girlOutfit =
        mapOf(
            "t-shirt" to R.drawable.avatar_part_girl_outfit_t_shirt,
            "hoodie" to R.drawable.avatar_part_girl_outfit_hoodie,
            "collar-shirt" to R.drawable.avatar_part_girl_outfit_collar_shirt,
            "sweater" to R.drawable.avatar_part_girl_outfit_sweater,
            "blazer" to R.drawable.avatar_part_girl_outfit_blazer,
            "striped-tee" to R.drawable.avatar_part_girl_outfit_striped_tee,
        )

    private val girlOutfitBack = mapOf("hoodie" to R.drawable.avatar_part_girl_outfit_back_hoodie)
}
