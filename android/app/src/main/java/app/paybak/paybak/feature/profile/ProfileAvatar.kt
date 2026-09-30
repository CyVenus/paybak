package app.paybak.paybak.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbPeepHead

/**
 * The user's avatar as avatar circles draw it: the chosen head, the saved photo, or the initials
 * (the fallback, also shown while the photo loads).
 */
@Composable
fun rememberProfileAvatar(profile: UserProfile, profileStore: ProfileStore): PbAvatarContent {
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

/** What an avatar circle shows for this choice; [photo] is the photo's image once loaded. */
fun AvatarChoice.toAvatarContent(photo: ImageBitmap?, initials: String): PbAvatarContent {
    val fallback = PbAvatarContent.Initials(initials)
    return when (this) {
        AvatarChoice.None -> fallback
        is AvatarChoice.Preset ->
            PbPeepHead.Presets.getOrNull(index)?.let(PbAvatarContent::Art) ?: fallback
        is AvatarChoice.Photo -> photo?.let(PbAvatarContent::Photo) ?: fallback
    }
}
