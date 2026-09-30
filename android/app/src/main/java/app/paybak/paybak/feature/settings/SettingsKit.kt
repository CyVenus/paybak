package app.paybak.paybak.feature.settings

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.MainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.id
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.animatePressColor
import app.paybak.paybak.ui.components.pressable
import app.paybak.paybak.ui.components.rememberPressState
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/**
 * A pushed settings page (screens-settings §0): the push header stays put on white while the
 * sections scroll under it, 24 dp apart. The root is tagged `screen.<routeId>` and the back button
 * "<routeId>.back". [footer] is pinned below the scroll (Export's button).
 */
@Composable
internal fun SettingsPage(
    route: Route,
    title: String,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val navigator = LocalMainNavigator.current
    PbScreenFrame(id = route.id) {
        PbPushHeader(
            title,
            onBack = { navigator.leave(route) },
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            testTag = route.id,
        )
        Column(
            Modifier.fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PbLayout.ScreenMargin)
                .padding(top = PbLayout.SectionGap, bottom = PbSpace.S24),
            verticalArrangement = Arrangement.spacedBy(PbLayout.SectionGap),
            content = content,
        )
        footer()
    }
}

/**
 * Goes back from [route] only while it is the screen on top, so a second tap on a back button that
 * is sliding away doesn't go back again (to Home).
 */
internal fun MainNavigator.leave(route: Route) {
    if (screen.route == route) back()
}

/**
 * Opens [next] (through the paywall when [pro] and not entitled) only while [from] is the screen
 * on top, so a double tap on a row opens one screen, not two.
 */
internal fun MainNavigator.openFrom(from: Route, next: Route, pro: Boolean = false) {
    if (screen.route != from) return
    if (pro) requirePro(next) else open(next)
}

/**
 * A section: its `Row / Section Header` ([title], or a custom [header]), the content 8 dp below and
 * an optional Footnote footer 8 dp under it.
 */
@Composable
internal fun SettingsSection(
    title: String?,
    modifier: Modifier = Modifier,
    footer: String? = null,
    header: @Composable () -> Unit = { if (title != null) PbSectionHeader(title) },
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        header()
        content()
        if (footer != null) SettingsFootnote(footer)
    }
}

/** Footnote text in `text/secondary`: section footers and notes. */
@Composable
internal fun SettingsFootnote(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.fillMaxWidth(),
        style = PbTextStyles.Footnote,
        color = PbColors.Text.Secondary,
    )
}

/** A 16 dp secondary icon beside a Footnote: the "Paybak never moves money" style info line. */
@Composable
internal fun SettingsInfoLine(icon: PbIcon, text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        Box(Modifier.size(PbSize.IconSm, 18.dp), contentAlignment = Alignment.TopCenter) {
            PbIconImage(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(top = 1.dp),
                size = PbSize.IconSm,
                tint = PbColors.Icon.Secondary,
            )
        }
        SettingsFootnote(text, Modifier.weight(1f))
    }
}

/** A settings card of switch rows: the push types, discovery and one-row cards. */
@Composable
internal fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: PbIcon? = null,
    subtitle: String? = null,
    showDivider: Boolean = true,
) {
    PbSettingRow(
        title,
        modifier = modifier,
        trailing = PbSettingTrailing.Toggle(checked, onCheckedChange),
        icon = icon,
        subtitle = subtitle,
        showDivider = showDivider,
    )
}

/**
 * The `Sheet / Action Row` layout without its side padding: a 44 dp icon tile, a Headline title
 * and a Subheadline subtitle. Tappable rows ([onClick]) end in a chevron and fill #F5F5F5 while
 * pressed (payment methods, 64 dp); the paywall's feature list isn't tappable (56 dp).
 */
@Composable
internal fun TileRow(
    icon: PbIcon,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    onClick: (() -> Unit)? = null,
) {
    val press = rememberPressState(interactionSource = null)
    val fill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Card else PbColors.Bg.Card.copy(alpha = 0f),
            label = "Tile row",
        )
    val tileFill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Primary else PbColors.Bg.Card,
            label = "Tile row tile",
        )
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(fill, PbShapes.Tile)
            .then(
                if (onClick == null) Modifier
                else Modifier.pressable(press, enabled = true, onClick = onClick)
            ),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(PbSize.Tap).background(tileFill, PbShapes.Tile),
            contentAlignment = Alignment.Center,
        ) {
            PbIconImage(icon, contentDescription = null)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                title,
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClick != null) {
            PbIconImage(
                PbIcon.ChevronRight,
                contentDescription = null,
                size = PbSize.IconMd,
                tint = PbColors.Icon.Tertiary,
            )
        }
    }
}

/**
 * Copies text to the clipboard with a success haptic and the app toast ([toast], e.g. "UPI ID
 * copied").
 */
@Composable
internal fun rememberCopier(): (text: String, toast: String) -> Unit {
    val clipboard = LocalClipboard.current
    val navigator = LocalMainNavigator.current
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    return { text, toast ->
        scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(toast, text))) }
        haptics.perform(HapticKind.Success)
        navigator.toast(toast)
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun SettingsKitPreview() {
    PaybakTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            SettingsSection("Discovery", footer = "Contacts are only used to find friends.") {
                PbCard {
                    ToggleRow("Find me by phone or email", true, {}, icon = PbIcon.Search)
                    ToggleRow("Contacts sync", false, {}, icon = PbIcon.People, showDivider = false)
                }
            }
            TileRow(PbIcon.Wallet, "arjun@okaxis", "UPI · Primary", height = 64.dp, onClick = {})
            SettingsInfoLine(PbIcon.Lock, "Paybak never moves money.")
        }
    }
}
