package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `PBUserAvatar`: the current user in any avatar circle (screens-profile §1.7): the Setup 1 head,
 * the saved photo, the custom character's head or the initials. [onCard] makes the circle white
 * inside #F5F5F5 cards.
 */
@Composable
fun PbUserAvatar(
    modifier: Modifier = Modifier,
    size: PbAvatarSize = PbAvatarSize.Md,
    onCard: Boolean = false,
    contentDescription: String? = null,
) {
    val store = LocalProfileStore.current
    val profile by store.profile.collectAsState()
    PbAvatar(rememberUserAvatar(profile, store), modifier, size, onCard, contentDescription)
}

/**
 * What an avatar circle shows for the user: the initials are the fallback, also while a photo
 * loads, and the profile icon before a name exists.
 */
@Composable
fun rememberUserAvatar(profile: UserProfile, profileStore: ProfileStore): PbAvatarContent {
    val photo = rememberPhotoImage(profile.avatar as? AvatarChoice.Photo, profileStore)
    return profile.avatar.toAvatarContent(photo, profile.initials)
}

/** Loads a saved [photo] off the main thread; null until it's loaded, or without a photo. */
@Composable
fun rememberPhotoImage(photo: AvatarChoice.Photo?, profileStore: ProfileStore): ImageBitmap? {
    val image by
        produceState<ImageBitmap?>(null, photo) {
            value = photo?.let { profileStore.loadPhoto(it)?.asImageBitmap() }
        }
    return image
}

/**
 * What an avatar circle shows for this choice; [photo] is the photo's image once loaded. The
 * fallback is the [initials], or the profile icon while there are none (no name yet).
 */
fun AvatarChoice.toAvatarContent(photo: ImageBitmap?, initials: String): PbAvatarContent {
    val fallback =
        if (initials.isEmpty()) PbAvatarContent.Symbol(PbIcon.Profile)
        else PbAvatarContent.Initials(initials)
    return when (this) {
        AvatarChoice.None -> fallback
        is AvatarChoice.Preset ->
            PbPeepHead.Presets.getOrNull(index)?.let(PbAvatarContent::Art) ?: fallback
        is AvatarChoice.Photo -> photo?.let(PbAvatarContent::Photo) ?: fallback
        is AvatarChoice.Character -> PbAvatarContent.Character(look, initials)
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarContentPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbAvatar(AvatarChoice.Preset(0).toAvatarContent(null, "AM"))
        PbAvatar(AvatarChoice.None.toAvatarContent(null, "AM"))
        PbAvatar(
            AvatarChoice.Character(AvatarLook.DefaultBoy).toAvatarContent(null, "AM"),
            size = PbAvatarSize.Lg,
        )
    }
}
