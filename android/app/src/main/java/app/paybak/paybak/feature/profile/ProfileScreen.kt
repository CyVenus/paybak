package app.paybak.paybak.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.AvatarCircle
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbNavHeader
import app.paybak.paybak.ui.components.PbNavHeaderInline
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.rememberUserAvatar
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The Profile header's avatar circle (screens-profile §2.1). */
private val ProfileAvatarSize = 120.dp

/**
 * The `profile` tab root (screens-profile §2): the user's avatar, name and handle, the way into the
 * avatar editor, the settings card (Pro first) and Sign out. It scrolls under the tab bar, and the
 * inline title appears once the large one has scrolled away.
 */
@Composable
fun ProfileScreen(route: Route.Profile) {
    val navigator = LocalMainNavigator.current
    val profileStore = LocalProfileStore.current
    val profile by profileStore.profile.collectAsState()
    val isPro = LocalLedger.current.collectSnapshot().value.isPro
    val context = LocalContext.current
    val designed = rememberDebugStartScreen("profileSignOut")
    var signingOut by rememberSaveable { mutableStateOf(designed != null) }
    val scroll = rememberScrollState()
    val titleHeight = with(LocalDensity.current) { PbSize.Tap.roundToPx() }

    Box(
        Modifier.fillMaxSize()
            .background(PbColors.Bg.Primary)
            .testTag("screen.${route.info.id}"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(scroll)
                .statusBarsPadding()
                .padding(LocalTabBarPadding.current)
                .widthIn(max = PbLayout.MaxContentWidth)
                .padding(horizontal = PbLayout.ScreenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PbNavHeader(stringResource(R.string.profile_title))
            Spacer(Modifier.height(PbLayout.SectionGap))
            ProfileHeader(profile, onEditAvatar = { navigator.open(Route.EditAvatar) })
            Spacer(Modifier.height(PbLayout.SectionGap))
            SettingsCard(profile, isPro, open = navigator::open)
            Spacer(Modifier.height(PbLayout.SectionGap))
            PbTextButton(
                stringResource(R.string.profile_sign_out),
                onClick = { signingOut = true },
                modifier = Modifier.testTag("profile.signOut"),
            )
        }
        PbNavHeaderInline(
            stringResource(R.string.profile_title),
            visible = scroll.value > titleHeight,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (signingOut) {
        PbAlert(
            title = stringResource(R.string.profile_sign_out_title),
            message = stringResource(R.string.profile_sign_out_message),
            cancelLabel = stringResource(R.string.profile_cancel),
            actionLabel = stringResource(R.string.profile_sign_out),
            onCancel = { signingOut = false },
            onAction = {
                signingOut = false
                profileStore.update(UserProfile::signedOut)
                context.restartIntoOnboarding()
            },
            testTag = "profile.signOutAlert",
        )
    }
}

/** The avatar circle, name, handle and Edit avatar; the circle opens the editor too. */
@Composable
private fun ProfileHeader(profile: UserProfile, onEditAvatar: () -> Unit) {
    val avatar = rememberUserAvatar(profile, LocalProfileStore.current)
    val avatarLabel = stringResource(R.string.profile_avatar)
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarCircle(
            content = avatar,
            diameter = ProfileAvatarSize,
            fill = PbColors.Bg.Card,
            initialsStyle = PbTextStyles.Title1,
            iconSize = PbSize.IconLg,
            modifier =
                Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onEditAvatar,
                    )
                    .semantics { contentDescription = avatarLabel }
                    .testTag("profile.avatar"),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                profile.name,
                modifier = Modifier.testTag("profile.name"),
                style = PbTextStyles.Title2,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            profile.handle?.let {
                Text(
                    it,
                    modifier = Modifier.testTag("profile.handle"),
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )
            }
        }
        PbButton(
            stringResource(R.string.profile_edit_avatar),
            onClick = onEditAvatar,
            modifier = Modifier.testTag("profile.editAvatar"),
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
    }
}

@Composable
private fun SettingsCard(profile: UserProfile, isPro: Boolean, open: (Route) -> Unit) {
    val currency = Currencies.currency(profile.defaultCurrency)
    PbCard {
        PbSettingRow(
            stringResource(R.string.profile_row_pro),
            modifier = Modifier.testTag("profile.row.pro"),
            onClick = { open(Route.Paywall()) },
            icon = PbIcon.Crown,
            value = if (isPro) stringResource(R.string.profile_pro_active) else null,
            badge = if (isPro) null else stringResource(R.string.profile_try_free),
        )
        PbSettingRow(
            stringResource(R.string.profile_row_payment),
            modifier = Modifier.testTag("profile.row.payment"),
            onClick = { open(Route.PaymentDetails) },
            icon = PbIcon.Wallet,
            value =
                when (profile.primaryMethod?.kind) {
                    SavedMethodKind.Upi -> stringResource(R.string.profile_value_upi)
                    SavedMethodKind.Bank -> stringResource(R.string.profile_value_bank)
                    null -> null
                },
        )
        PbSettingRow(
            stringResource(R.string.profile_row_currency),
            modifier = Modifier.testTag("profile.row.currency"),
            onClick = { open(Route.SettingsCurrency) },
            icon = PbIcon.Exchange,
            value = "${currency.code} ${currency.symbol}",
        )
        PbSettingRow(
            stringResource(R.string.profile_row_notifications),
            modifier = Modifier.testTag("profile.row.notifications"),
            onClick = { open(Route.SettingsNotifications) },
            icon = PbIcon.Bell,
        )
        PbSettingRow(
            stringResource(R.string.profile_row_privacy),
            modifier = Modifier.testTag("profile.row.privacy"),
            onClick = { open(Route.PrivacyData) },
            icon = PbIcon.Lock,
        )
        PbSettingRow(
            stringResource(R.string.profile_row_help),
            modifier = Modifier.testTag("profile.row.help"),
            onClick = { open(Route.HelpFeedback) },
            icon = PbIcon.Help,
            showDivider = false,
        )
    }
}

/**
 * The line under the name (§2.2): the primary UPI ID, else the sign-in contact; null hides it.
 */
private val UserProfile.handle: String?
    get() = upiId.ifEmpty { contact }.ifEmpty { null }
