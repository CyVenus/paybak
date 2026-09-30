package app.paybak.paybak.debug.menu

import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.domain.model.AvatarLook

/** The debug menu's Profile section (lane C): switch the user's avatar kind. */
internal val ProfileDebugActions: List<DebugAction> =
    listOf(
        DebugAction("Use the preset avatar", "Arjun’s Setup 1 head, as the demo") {
            app.profileStore.update { it.copy(avatar = AvatarChoice.Preset(0)) }
        },
        DebugAction("Use the custom avatar", "The default Boy character") {
            app.profileStore.update { it.copy(avatar = AvatarChoice.Character(AvatarLook.DefaultBoy)) }
        },
    )
