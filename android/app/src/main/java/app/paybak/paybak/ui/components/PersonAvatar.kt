package app.paybak.paybak.ui.components

import app.paybak.paybak.domain.model.Person

/**
 * A friend's avatar circle content: their Peep head (`avatar-2` … `avatar-7`), otherwise their
 * initials. The user's own avatar is [PbUserAvatar].
 */
fun Person.avatarContent(): PbAvatarContent =
    avatar
        ?.removePrefix("avatar-")
        ?.toIntOrNull()
        ?.let { PbPeepHead.entries.getOrNull(it - 1) }
        ?.let(PbAvatarContent::Art) ?: PbAvatarContent.Initials(initials)
