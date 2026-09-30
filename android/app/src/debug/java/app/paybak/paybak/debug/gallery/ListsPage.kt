package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.paybak.paybak.ui.components.PbAmountHero
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarPair
import app.paybak.paybak.ui.components.PbAvatarPairSize
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCommentRow
import app.paybak.paybak.ui.components.PbConfirmPaymentCard
import app.paybak.paybak.ui.components.PbGroupBalance
import app.paybak.paybak.ui.components.PbGroupBudget
import app.paybak.paybak.ui.components.PbGroupRow
import app.paybak.paybak.ui.components.PbHeroLeading
import app.paybak.paybak.ui.components.PbHistoryRow
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbNoticeLayout
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbQrCodeCard
import app.paybak.paybak.ui.components.PbTitleHeader
import app.paybak.paybak.ui.components.PbTransferRow
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val Arjun = PbAvatarContent.Art(PbPeepHead.Arjun)
private val Priya = PbAvatarContent.Art(PbPeepHead.Priya)
private val Kabir = PbAvatarContent.Art(PbPeepHead.Kabir)

@Composable
internal fun ListsPage() {
    GalleryPage {
        GallerySection("Avatar / Pair: 32 · 56") {
            PbAvatarPair(Arjun, Kabir)
            PbAvatarPair(Arjun, Kabir, size = PbAvatarPairSize.Large)
        }
        GallerySection("Row / Comment · Row / History: Middle · Last") {
            PbCommentRow("Priya", "27 Sep", "Was breakfast included?", Priya)
            Column {
                PbHistoryRow("Kabir changed the amount from ₹17,500 to ₹18,000", "28 Sep")
                PbHistoryRow(
                    "Kabir changed the amount from ₹17,500 to ₹18,000",
                    "28 Sep",
                    last = true,
                )
            }
        }
        GallerySection("Row / Person: Regular (on white)") {
            Column { PersonRows(PbPersonRowSize.Regular) }
        }
        GallerySection("Row / Person: Compact (in a card)") {
            PbCard { PersonRows(PbPersonRowSize.Compact) }
        }
        GallerySection("Row / Transfer") {
            PbCard {
                PbTransferRow(
                    "Rohan owes Dev",
                    "₹8,500",
                    PbAvatarContent.Art(PbPeepHead.Rohan),
                    PbAvatarContent.Art(PbPeepHead.Dev),
                    showDivider = false,
                )
            }
        }
        GallerySection("Header / Title Row: Tile · Avatar") { TitleHeaders() }
        GallerySection("Header / Amount Hero: Icon · Avatar · Pair") { AmountHeroes() }
        GallerySection("Card / Notice: Leading · Centered × None · One · Two") { Notices() }
        GallerySection("Card / Confirm Payment (tap Confirm)") { ConfirmPayments() }
        GallerySection("Card / QR Code (scans https://paybak.app/i/arjun)") {
            Box(
                Modifier.fillMaxWidth()
                    .background(PbColors.Bg.Card, PbShapes.Card)
                    .padding(PbSpace.S24),
                contentAlignment = Alignment.Center,
            ) {
                PbQrCodeCard("https://paybak.app/i/arjun")
            }
        }
        GallerySection("Row / Group: Group · Project · Archived") { GroupRows() }
    }
}

@Composable
private fun PersonRows(size: PbPersonRowSize) {
    val trailings =
        listOf(
            PbPersonTrailing.Amount("₹700", PbBalance.Owed, "Due Sun 4 Oct"),
            PbPersonTrailing.Amount("₹700", PbBalance.Owe, "Due Sun 4 Oct"),
            PbPersonTrailing.Amount("₹700", label = "Due Sun 4 Oct"),
            PbPersonTrailing.Status("Settled"),
            PbPersonTrailing.Check,
            PbPersonTrailing.Select(selected = true),
            PbPersonTrailing.Select(selected = false),
            PbPersonTrailing.Remove("Remove Priya", onRemove = {}),
            PbPersonTrailing.Action(
                if (size == PbPersonRowSize.Regular) "Invite" else "Remind",
                onClick = {},
            ),
            null,
        )
    trailings.forEach { trailing ->
        PbPersonRow(
            "Priya",
            Priya,
            subtitle = "Dinner at Olive Garden",
            trailing = trailing,
            size = size,
            onClick = {},
        )
    }
    PbPersonRow(
        "Aarav",
        PbAvatarContent.Initials("AR"),
        subtitle = "Dinner at Olive Garden",
        tag = "Guest",
        trailing =
            PbPersonTrailing.Amount("₹700", PbBalance.Owed, "Due Sun 4 Oct", "Overdue 3 days"),
        size = size,
        showDivider = false,
    )
}

@Composable
private fun TitleHeaders() {
    val members = listOf(PbPeepHead.Arjun, PbPeepHead.Priya, PbPeepHead.Rohan, PbPeepHead.Esha)
    PbTitleHeader(
        "Goa Trip",
        PbAvatarContent.Symbol(PbIcon.Plane),
        subtitle = "21–25 Sep · 5 members · ₹39,500 spent",
        members = members,
    )
    PbTitleHeader(
        "Goa Trip",
        PbAvatarContent.Art(PbPeepHead.Rohan),
        subtitle = "21–25 Sep · 5 members · ₹39,500 spent",
        members = members,
    )
    PbTitleHeader("Aarav", PbAvatarContent.Initials("AR"), tag = "Guest")
}

@Composable
private fun AmountHeroes() {
    val leadings =
        listOf(
            PbHeroLeading.Single(PbAvatarContent.Symbol(PbIcon.Food)),
            PbHeroLeading.Single(PbAvatarContent.Art(PbPeepHead.Dev)),
            PbHeroLeading.FromTo(Arjun, Kabir),
        )
    leadings.forEachIndexed { index, leading ->
        PbAmountHero(
            title = "Seafood dinner at Britto’s",
            amount = "₹6,500",
            meta = "Paid by you · 22 Sep",
            leading = leading,
            tags = listOf("Goa Trip", "Food"),
            status = if (index == 2) "Disputed" else null,
        )
    }
}

@Composable
private fun Notices() {
    PbNoticeLayout.entries.forEach { layout ->
        val centered = layout == PbNoticeLayout.Centered
        val primary = PbNoticeAction(if (centered) "See Pro" else "Send invite", onClick = {})
        val two = PbNoticeAction(if (centered) "See Pro" else "Edit expense", onClick = {})
        val secondary = PbNoticeAction(if (centered) "Not now" else "Resolve", onClick = {})
        val icon = if (centered) PbIcon.Lock else PbIcon.Activity
        PbNoticeCard(
            "Waiting for Meera to confirm",
            icon,
            title = "Pending confirmation",
            layout = layout,
        )
        PbNoticeCard(
            "Waiting for Meera to confirm",
            icon,
            title = "Pending confirmation",
            layout = layout,
            primaryAction = primary,
        )
        PbNoticeCard(
            "Waiting for Meera to confirm",
            icon,
            title = "Pending confirmation",
            layout = layout,
            primaryAction = two,
            secondaryAction = secondary,
        )
    }
    GalleryLabel("With the Pro badge · without a title")
    PbNoticeCard("Insights are part of Pro.", PbIcon.Lock, title = "Insights", badge = "Pro")
    PbNoticeCard("We simplified 5 debts into 3 payments.", PbIcon.Shuffle)
}

@Composable
private fun ConfirmPayments() {
    var confirmed by remember { mutableStateOf(false) }
    PbConfirmPaymentCard(
        avatar = PbAvatarContent.Art(PbPeepHead.Esha),
        title = "Esha says she paid you ₹700",
        detail = "Dinner at Olive Garden · UPI · 9:12 pm",
        confirmedTitle = "Esha paid you ₹700",
        confirmedDetail = "Dinner at Olive Garden · UPI · Confirmed",
        confirmed = confirmed,
        onConfirm = { confirmed = true },
        onNotReceived = {},
    )
    if (confirmed) {
        PbButton(
            "Reset",
            onClick = { confirmed = false },
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
    }
}

@Composable
private fun GroupRows() {
    val budget = PbGroupBudget(0.87f, "₹52,000 of ₹60,000", "₹8,000 left")
    val balances =
        listOf(
            PbGroupBalance.Owe("₹1,400"),
            PbGroupBalance.Owed("₹1,400"),
            PbGroupBalance.Settled("Settled"),
        )
    Column {
        balances.forEach { balance ->
            PbGroupRow("Goa Trip", "5 members · Due Fri 2 Oct", PbIcon.Plane, balance, onClick = {})
        }
        PbGroupRow(
            "Goa Trip",
            "5 members · Due Fri 2 Oct",
            PbIcon.Package,
            PbGroupBalance.Settled("Settled"),
            archived = true,
            onClick = {},
        )
        balances.forEachIndexed { index, balance ->
            PbGroupRow(
                "Goa Trip",
                "5 members · Due Fri 2 Oct",
                PbIcon.Drone,
                balance,
                budget = budget,
                showDivider = index < balances.lastIndex,
                onClick = {},
            )
        }
    }
}
