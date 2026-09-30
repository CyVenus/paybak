package app.paybak.paybak.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.BuildConfig
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.DueAction
import app.paybak.paybak.domain.calc.HomeState
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.feature.PendingClaimCard
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBalanceSummary
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbIconButtonStyle
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `home` tab root (homeFirstDay, homeActive, homeAllSettled, homeConfirmPayment,
 * settlePaymentConfirmed). PLACEHOLDER owned by lane D: replace this file and keep the signature.
 * It already reads everything from the store, so M1's tests and the debug hooks work: the root is
 * tagged `screen.home<State>` and `home.state.<state>`; the logo's long-press opens the debug menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(route: Route.Home) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val clock = LocalAppClock.current
    val home = snapshot.home
    val state =
        when {
            home.pendingClaims.isNotEmpty() -> "confirmPayment"
            home.state == HomeState.FirstDay -> "firstDay"
            home.state == HomeState.AllSettled -> "allSettled"
            else -> "active"
        }
    Column(
        Modifier.fillMaxSize()
            .background(PbColors.Bg.Primary)
            .testTag("screen.home${state.replaceFirstChar(Char::uppercase)}")
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(LocalTabBarPadding.current)
            .padding(horizontal = PbLayout.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
    ) {
        Box(Modifier.testTag("home.state.$state"))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            Row {
                PbLogo(
                    PbLogoLayout.Horizontal,
                    Modifier.padding(vertical = PbSpace.S8)
                        .testTag("home.logo")
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (BuildConfig.DEBUG) navigator.open(Route.DebugMenu)
                            },
                        ),
                )
                Spacer(Modifier.weight(1f))
                PbIconButton(
                    PbIcon.Sparkles,
                    "Ask Paybak",
                    onClick = { navigator.requirePro(Route.Ask) },
                    modifier = Modifier.testTag("home.assistant"),
                    style = PbIconButtonStyle.Glass,
                )
                Spacer(Modifier.padding(PbSpace.S4))
                PbIconButton(
                    PbIcon.Bell,
                    "Notifications",
                    onClick = { navigator.open(Route.Notifications) },
                    modifier = Modifier.testTag("home.bell"),
                    style = PbIconButtonStyle.Glass,
                    badge = snapshot.unreadCount > 0,
                )
            }
            val hour = clock.now().atZone(clock.zone).hour
            Text(
                "Good ${Dates.partOfDay(hour)}, ${profile.firstName}",
                Modifier.testTag("home.greeting"),
                style = PbTextStyles.Title1,
                color = PbColors.Text.Primary,
            )
            PbBadge("Placeholder · lane D")
        }
        home.pendingClaims.forEach { PendingClaimCard(it) }
        when (home.state) {
            HomeState.FirstDay ->
                PbEmptyState(
                    "Nothing here yet.",
                    "Add your first expense or invite a friend to get started.",
                    primaryAction =
                        PbEmptyAction(
                            "Add expense",
                            PbIcon.Plus,
                            { navigator.open(Route.AddExpense()) },
                            "home.addExpense",
                        ),
                    secondaryAction =
                        PbEmptyAction(
                            "Invite friends",
                            PbIcon.UserAdd,
                            { navigator.open(Route.AddFriend) },
                            "home.inviteFriends",
                        ),
                )
            HomeState.AllSettled ->
                PbEmptyState(
                    "You’re all square.",
                    "No one owes anyone right now.",
                    illustration = PaybakRiveAsset.HomeAllSquare,
                )
            HomeState.Active -> {
                PbBalanceSummary(
                    owedAmount = Money.format(home.totals.owed, sign = MoneySign.Signed),
                    owedCaption = home.totals.owedCaption,
                    oweAmount = Money.format(-home.totals.owe, sign = MoneySign.Signed),
                    oweCaption = home.totals.oweCaption,
                    onOwed = { navigator.open(Route.OwedBreakdown) },
                    onOwe = { navigator.open(Route.OweBreakdown) },
                    onSettleUp = { navigator.open(Route.SettleUp()) },
                    testTag = "home.balance",
                )
                if (home.dueSoon.isNotEmpty()) {
                    Column {
                        PbSectionHeader("Due soon")
                        home.dueSoon.forEach { row ->
                            val item = row.obligation
                            PbActivityRow(
                                leading =
                                    PbAvatarContent.Symbol(
                                        if (row.action == DueAction.Remind) PbIcon.Bell
                                        else PbIcon.Groups
                                    ),
                                title = row.title,
                                subtitle = "${row.detail} · ${row.badge}",
                                action = row.action.name,
                                onAction = {
                                    if (row.action == DueAction.Remind) {
                                        navigator.open(Route.Remind(item.friendId))
                                    } else {
                                        navigator.open(
                                            Route.RecordPayment(
                                                RecordPaymentArgs(
                                                    toId = item.friendId,
                                                    amount = item.amount,
                                                    groupId =
                                                        item.ref.takeIf {
                                                            item.kind.name == "Group"
                                                        },
                                                )
                                            )
                                        )
                                    }
                                },
                                testTag = "home.due.${item.ref}",
                            )
                        }
                    }
                }
            }
        }
        if (home.recent.isNotEmpty()) {
            Column {
                PbSectionHeader(
                    "Recent activity",
                    action = "See all",
                    onAction = { navigator.select(Tab.Activity) },
                )
                home.recent.forEach { row ->
                    val event = row.event
                    PbActivityRow(
                        leading =
                            event.personId?.let { snapshot.view.person(it)?.avatarContent() }
                                ?: PbAvatarContent.Symbol(
                                    Category.of(event.category.orEmpty()).pbIcon
                                ),
                        title = row.title,
                        subtitle = row.subtitle,
                        amount = row.amount,
                        amountPrimary = row.primary,
                        date = row.date,
                        onClick = {
                            navigator.open(
                                if (event.kind == TimelineKind.Payment) Route.Payment(event.ref)
                                else Route.Expense(event.ref)
                            )
                        },
                        testTag = "home.recent.${event.ref}",
                    )
                }
            }
        }
        Spacer(Modifier.height(PbSpace.S8))
    }
}
