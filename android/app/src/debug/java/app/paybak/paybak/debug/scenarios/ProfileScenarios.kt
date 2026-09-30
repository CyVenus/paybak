package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

private fun profile(stack: List<Route> = emptyList()) =
    Scenario(demo(), tab = Tab.Profile, stack = stack, avatar = AvatarLook.DefaultBoy)

/** Profile and the avatar editor (lane C, M8; app-architecture §1.8). */
internal val ProfileScenarios: Map<String, Scenario> =
    mapOf(
        "profile" to profile(),
        "profileSignOut" to profile(),
        "editAvatarBoyHair" to profile(listOf(Route.EditAvatar)),
        "editAvatarBoyBeard" to profile(listOf(Route.EditAvatar)),
        "editAvatarBoyEyewear" to profile(listOf(Route.EditAvatar)),
        "editAvatarBoyOutfit" to profile(listOf(Route.EditAvatar)),
        "editAvatarGirlHair" to profile(listOf(Route.EditAvatar)),
        "editAvatarGirlAccessory" to profile(listOf(Route.EditAvatar)),
        "editAvatarGirlOutfit" to profile(listOf(Route.EditAvatar)),
        "editAvatarDiscard" to profile(listOf(Route.EditAvatar)),
    )
