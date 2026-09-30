package app.paybak.paybak.feature.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbUserAvatar

/**
 * The `profile` tab root (profile, profileSignOut). PLACEHOLDER owned by lane C (M8): replace this
 * file and keep the signature.
 */
@Composable
fun ProfileScreen(route: Route.Profile) {
    val navigator = LocalMainNavigator.current
    RoutePlaceholder(route, title = "Profile") {
        PbUserAvatar(size = PbAvatarSize.Lg)
        PbButton(
            "Paybak Pro",
            onClick = { navigator.open(Route.Paywall()) },
            Modifier.fillMaxWidth().testTag("profile.pro"),
        )
        PbButton(
            "Export records",
            onClick = { navigator.requirePro(Route.PrivacyExport) },
            Modifier.fillMaxWidth().testTag("profile.export"),
            style = PbButtonStyle.Secondary,
        )
    }
}
