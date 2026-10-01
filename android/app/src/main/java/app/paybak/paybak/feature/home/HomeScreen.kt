package app.paybak.paybak.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.BuildConfig
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.HomeState
import app.paybak.paybak.domain.calc.HomeSummary
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.feature.PendingClaimStack
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.home.HomeFadeHeight
import app.paybak.paybak.ui.components.home.HomeFadeHeightWithClaim
import app.paybak.paybak.ui.components.home.PbHomeHeader
import app.paybak.paybak.ui.components.home.PbHomeScrollFade
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The `home` tab root (screens-home, screens-home-v2): the header, any pending claims, then First
 * day, the balances with Due soon and Recent activity, or All settled, all from the ledger's
 * snapshot. Content scrolls under the glass tab bar behind a fade. The root is tagged
 * `screen.home<State>` and a hidden `home.state.<state>` names the state for UI tests.
 */
@Composable
fun HomeScreen(route: Route.Home) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val clock = LocalAppClock.current
    val home = snapshot.home
    val state = home.stateId
    val greeting =
        stringResource(
            R.string.home_greeting,
            Dates.partOfDay(clock.now().atZone(clock.zone).hour),
            profile.firstName,
        )
    Box(
        Modifier.fillMaxSize()
            .background(PbColors.Bg.Primary)
            .testTag("screen.home${state.replaceFirstChar(Char::uppercase)}")
    ) {
        Column(
            Modifier.align(Alignment.TopCenter)
                .widthIn(max = PbLayout.MaxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(LocalTabBarPadding.current)
                .padding(horizontal = PbLayout.ScreenMargin)
        ) {
            Box(Modifier.testTag("home.state.$state"))
            PbHomeHeader(
                greeting = greeting,
                unread = snapshot.unreadCount > 0,
                onAssistant = { navigator.requirePro(Route.Ask) },
                onNotifications = { navigator.open(Route.Notifications) },
                onLogoLongPress = { navigator.open(Route.DebugMenu) }.takeIf { BuildConfig.DEBUG },
                testTag = "home",
            )
            Spacer(Modifier.height(PbLayout.SectionGap))
            PendingClaimStack(home.pendingClaims, gap = PbSpace.S12, gapBelow = true)
            when (home.state) {
                HomeState.FirstDay -> FirstDayCard()
                HomeState.AllSettled -> AllSettledCard()
                HomeState.Active -> HomeActiveContent(home, snapshot.view)
            }
        }
        if (home.state == HomeState.Active) {
            PbHomeScrollFade(
                Modifier.align(Alignment.BottomCenter),
                height =
                    if (home.pendingClaims.isEmpty()) HomeFadeHeight else HomeFadeHeightWithClaim,
            )
        }
        // Scrolled content never runs into the status bar (screens-home §1).
        Box(
            Modifier.fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(PbColors.Bg.Primary)
        )
    }
}

/** `home.state.<id>`: a pending claim wins over the data state (home-v2 §3). */
private val HomeSummary.stateId: String
    get() =
        when {
            pendingClaims.isNotEmpty() -> "confirmPayment"
            state == HomeState.FirstDay -> "firstDay"
            state == HomeState.AllSettled -> "allSettled"
            else -> "active"
        }

/** A new account: add the first expense or invite a friend (screens-home §3, home-v2 §2.3). */
@Composable
private fun FirstDayCard() {
    val navigator = LocalMainNavigator.current
    PbEmptyState(
        stringResource(R.string.home_first_day_title),
        stringResource(R.string.home_first_day_body),
        primaryAction =
            PbEmptyAction(
                stringResource(R.string.home_add_expense),
                PbIcon.Plus,
                { navigator.open(Route.AddExpense(AddExpenseArgs())) },
                "home.firstDay.addExpense",
            ),
        secondaryAction =
            PbEmptyAction(
                stringResource(R.string.home_invite_friends),
                PbIcon.UserAdd,
                { navigator.open(Route.AddFriend) },
                "home.firstDay.invite",
            ),
        testTag = "home.firstDay",
    )
}

/** Nothing owed either way: a calm confirmation with no actions (screens-home §4). */
@Composable
private fun AllSettledCard() {
    PbEmptyState(
        stringResource(R.string.home_all_square_title),
        stringResource(R.string.home_all_square_body),
        illustration = PaybakRiveAsset.HomeAllSquare,
        testTag = "home.allSettled",
    )
}
