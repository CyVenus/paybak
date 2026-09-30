package app.paybak.paybak.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Presentation
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.id
import app.paybak.paybak.navigation.presentation
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetDetent
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * Stand-in for a route whose screen a lane builds (app-architecture §6.1): it names the route, its
 * owner, spec and parameters, and gives the route's own way out (back, ✕ or the sheet's close), so
 * every flow can be walked end to end. The root is tagged `screen.<routeId>`. [actions] add the
 * route's main exits (e.g. a picker's Done). The owning lane replaces the file that calls it.
 */
@Composable
fun RoutePlaceholder(
    route: Route,
    title: String = route.id,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val navigator = LocalMainNavigator.current
    when (route.presentation) {
        Presentation.Sheet ->
            PbSheet(
                onDismiss = navigator::dismissSheet,
                title = title,
                detent = PbSheetDetent.Medium,
                testTag = "${route.id}.sheet",
            ) {
                Column(Modifier.testTag("screen.${route.id}")) { PlaceholderBody(route, actions) }
            }

        Presentation.Tab ->
            Column(
                Modifier.fillMaxSize()
                    .background(PbColors.Bg.Primary)
                    .testTag("screen.${route.id}")
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(LocalTabBarPadding.current)
                    .padding(horizontal = PbLayout.ScreenMargin)
            ) {
                Text(title, style = PbTextStyles.Title1, color = PbColors.Text.Primary)
                PlaceholderBody(route, actions)
            }

        // System back is the navigator's (MainHost): the sheet, then the push or the modal.
        Presentation.Push ->
            PbScreen(id = route.id) {
                PbPushHeader(title, onBack = { navigator.back() }, testTag = "${route.id}.header")
                PlaceholderBody(route, actions)
            }

        Presentation.Modal ->
            PbScreen(id = route.id) {
                PbModalHeader(
                    title,
                    onClose = navigator::dismissModal,
                    testTag = "${route.id}.header",
                )
                PlaceholderBody(route, actions)
            }
    }
}

@Composable
private fun PlaceholderBody(route: Route, actions: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        Spacer(Modifier.height(PbSpace.S16))
        PbBadge("Placeholder · ${route.info.owner.label}", style = PbBadgeStyle.Muted)
        Text(route.info.spec, style = PbTextStyles.Headline, color = PbColors.Text.Primary)
        Text(route.toString(), style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary)
        Spacer(Modifier.height(PbSpace.S8))
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            content = actions,
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 400)
@Composable
private fun PlaceholderBodyPreview() {
    PaybakTheme {
        Column(Modifier.padding(PbSpace.S20)) { PlaceholderBody(Route.Friend("p-rohan")) {} }
    }
}
