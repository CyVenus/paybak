package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.paybak.paybak.feature.activity.ActivityHeader
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAddSheetRows
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalanceCard
import app.paybak.paybak.ui.components.PbBalanceSummary
import app.paybak.paybak.ui.components.PbBalanceType
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbNavAction
import app.paybak.paybak.ui.components.PbNavHeader
import app.paybak.paybak.ui.components.PbNavHeaderInline
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbRowSurface
import app.paybak.paybak.ui.components.PbSheetContainer
import app.paybak.paybak.ui.components.PbTabBar
import app.paybak.paybak.ui.components.PbTabItemSpec
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace

/** The app shell's shared pieces (M2): tab bar, Add sheet, headers, Home cards and rows. */
@Composable
internal fun ShellPage() {
    GalleryPage {
        GallerySection("Navigation / Tab Bar (tap to select)") { TabBars() }
        GallerySection("Sheet / Action Sheet") {
            PbSheetContainer(
                Modifier.background(PbColors.Bg.Card).padding(PbSpace.S8),
                title = "Add",
                onClose = {},
            ) {
                PbAddSheetRows(onAction = {})
            }
        }
        GallerySection("Nav Header: Large Title · Inline") {
            PbNavHeader("Groups", action = PbNavAction(PbIcon.Plus, "New group", {}))
            PbNavHeaderInline("Groups", visible = true)
            var segment by remember { mutableStateOf(ActivitySegment.Timeline) }
            ActivityHeader(segment, { segment = it }, showRestore = true, onRestore = {})
        }
        GallerySection("Card / Balance Summary · Balance: Settled · with badge") {
            PbBalanceSummary(
                "+₹2,900",
                "from 4 people",
                "−₹1,850",
                "across 2 groups",
                {},
                {},
                onSettleUp = {},
            )
            PbBalanceCard(
                PbBalanceType.Settled,
                "₹0",
                "Nothing pending",
                Modifier.fillMaxWidth(),
                onClick = {},
            )
            PbBalanceCard(
                PbBalanceType.Owed,
                "+₹800",
                "Movie tickets",
                Modifier.fillMaxWidth(),
                badge = "Overdue 3 days",
            )
        }
        GallerySection("Row / Activity: Plain · On Card") { ActivityRows() }
        GallerySection("Card / Empty State: First day · All settled") {
            PbEmptyState(
                "Nothing here yet.",
                "Add your first expense or invite a friend to get started.",
                primaryAction = PbEmptyAction("Add expense", PbIcon.Plus, {}),
                secondaryAction = PbEmptyAction("Invite friends", PbIcon.UserAdd, {}),
            )
            PbEmptyState(
                "You’re all square.",
                "No one owes anyone right now.",
                illustration = PaybakRiveAsset.HomeAllSquare,
            )
        }
    }
}

@Composable
private fun TabBars() {
    var selected by remember { mutableIntStateOf(0) }
    val items =
        listOf(
            PbTabItemSpec("Home", PbIcon.Home, "g.home"),
            PbTabItemSpec("Groups", PbIcon.Groups, "g.groups"),
            PbTabItemSpec("Activity", PbIcon.Activity, "g.activity"),
            PbTabItemSpec("Profile", PbIcon.Profile, "g.profile"),
        )
    Column(Modifier.stripes().padding(vertical = 20.dp)) {
        PbTabBar(items, selected, onSelect = { selected = it }, onAdd = {})
    }
}

@Composable
private fun ActivityRows() {
    Column {
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Food),
            "Dinner at Olive Garden",
            subtitle = "You paid · 4 people",
            amount = "₹2,800",
            date = "Today",
        )
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Bolt),
            "Electricity bill",
            subtitle = "Flat 302 · You owe",
            amount = "−₹450",
            amountPrimary = false,
            date = "26 Sep",
        )
        PbActivityRow(
            PbAvatarContent.Art(PbPeepHead.Priya),
            "Priya paid you",
            subtitle = "UPI",
            amount = "₹1,050",
            date = "Yesterday",
            unread = true,
        )
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Flame),
            "Cooking gas draft created",
            subtitle = "Flat 302 · Needs an amount",
            badge = "Draft",
        )
    }
    PbCard {
        Column(Modifier.padding(horizontal = PbSpace.S16)) { OnCardRows() }
    }
}

@Composable
private fun OnCardRows() {
    PbActivityRow(
        PbAvatarContent.Art(PbPeepHead.Rohan),
        "You paid Rohan",
        subtitle = "UPI · Goa Trip",
        amount = "−₹1,400",
        amountPrimary = false,
        date = "Today",
        surface = PbRowSurface.OnCard,
        showDivider = true,
    )
    PbActivityRow(
        PbAvatarContent.Symbol(PbIcon.Food),
        "Snacks",
        subtitle = "₹300 · Goa Trip",
        detail = "Deleted by Priya on 24 Sep · 24 days left",
        action = "Restore",
        surface = PbRowSurface.OnCard,
    )
}
