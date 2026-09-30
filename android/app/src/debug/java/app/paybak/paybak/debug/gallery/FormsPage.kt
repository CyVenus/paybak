package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbAmountField
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbComposer
import app.paybak.paybak.ui.components.PbPaymentParties
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbPlanCard
import app.paybak.paybak.ui.components.PbSplitMode
import app.paybak.paybak.ui.components.PbSplitRow
import app.paybak.paybak.ui.components.PbSplitTotalBar
import app.paybak.paybak.ui.components.PbTextArea
import app.paybak.paybak.ui.theme.PbSpace

private const val REMINDER =
    "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me " +
        "on UPI at arjun@okaxis. Thanks."

@Composable
internal fun FormsPage() {
    GalleryPage {
        GallerySection("Control / Text Area: Default · Focused · live") { TextAreas() }
        GallerySection("Control / Composer: Empty · Typing · Pinned") { Composers() }
        GallerySection("Control / Amount Display: Empty · Focused · Filled · live") { Amounts() }
        GallerySection("Control / Payment Parties") { Parties() }
        GallerySection("Row / Split Person: Default · Excluded") { SplitRows() }
        GallerySection("Row / Split Person, live: tap a row to exclude, a field to focus") {
            LiveSplit()
        }
        GallerySection("Card / Split Total: Balanced · Error") {
            PbSplitTotalBar("₹0 left", "₹2,800 of ₹2,800")
            PbSplitTotalBar("₹150 left", "₹2,650 of ₹2,800", isError = true)
        }
        GallerySection("Card / Plan (tap to select)") { Plans() }
    }
}

@Composable
private fun TextAreas() {
    PbTextArea(REMINDER, {}, label = "Message", helper = "You can edit this message.")
    PbTextArea(
        REMINDER,
        {},
        label = "Message",
        helper = "You can edit this message.",
        interactionSource = rememberFocusedSource(),
    )
    var note by remember { mutableStateOf("") }
    PbTextArea(note, { note = it }, placeholder = "Add a note (optional)")
}

@Composable
private fun Composers() {
    PbComposer("", {}, onSend = {}, placeholder = "Add a comment", onMic = {})
    PbComposer("Add a comment", {}, onSend = {}, placeholder = "Add a comment")
    GalleryLabel("Pinned: the keyboard bar (full width in a screen)")
    var text by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf("") }
    PbComposer(
        text,
        { text = it },
        onSend = {
            sent = text
            text = ""
        },
        placeholder = "Ask or add an expense",
        pinned = true,
        onMic = {},
    )
    if (sent.isNotEmpty()) GalleryLabel("Sent: $sent")
}

@Composable
private fun Amounts() {
    PbAmountField(
        "",
        {},
        "",
        "₹0",
        "INR",
        onCurrencyClick = {},
        date = "Today",
        interactionSource = rememberFocusedSource(),
    )
    PbAmountField(
        "2800",
        {},
        "₹2,800",
        "₹0",
        "INR",
        onCurrencyClick = {},
        date = "Today",
        interactionSource = rememberFocusedSource(),
    )
    PbAmountField("2800", {}, "₹2,800", "₹0", "INR", onCurrencyClick = {}, date = "Today")
    GalleryLabel("Live, with helper and no date chip (tap the amount)")
    var amount by remember { mutableStateOf("") }
    PbAmountField(
        amount,
        { amount = it },
        "₹" + groupIndian(amount),
        "₹0",
        "INR",
        onCurrencyClick = {},
        helper = "You owe Meera ₹450 in Flat 302",
    )
}

@Composable
private fun Parties() {
    PbPaymentParties(
        fromName = "You",
        fromAvatar = PbAvatarContent.Art(PbPeepHead.Arjun),
        toName = "Meera",
        toAvatar = PbAvatarContent.Art(PbPeepHead.Meera),
        onFromClick = {},
        onToClick = {},
    )
}

@Composable
private fun SplitRows() {
    val priya = PbAvatarContent.Art(PbPeepHead.Priya)
    listOf(true to "Default", false to "Excluded").forEach { (included, state) ->
        GalleryLabel(state)
        val amount = if (included) "₹700" else "₹0"
        PbCard {
            PbSplitRow("Priya", priya, included, {}, PbSplitMode.Equally(amount))
            PbSplitRow(
                "Priya",
                priya,
                included,
                {},
                PbSplitMode.Exact(PbAmountEditor(if (included) "700" else "0", {}, prefix = "₹")),
            )
            PbSplitRow(
                "Priya",
                priya,
                included,
                {},
                PbSplitMode.Percent(
                    PbAmountEditor(if (included) "25" else "0", {}, suffix = "%"),
                    amount,
                ),
            )
            PbSplitRow(
                "Priya",
                priya,
                included,
                {},
                PbSplitMode.Shares(PbAmountEditor(if (included) "1" else "0", {}), amount, {}, {}),
                showDivider = false,
            )
        }
    }
}

@Composable
private fun LiveSplit() {
    val people = listOf(PbPeepHead.Arjun, PbPeepHead.Priya, PbPeepHead.Rohan, PbPeepHead.Esha)
    var included by remember { mutableStateOf(people.toSet()) }
    var shares by remember { mutableStateOf(people.associateWith { 1 }) }
    val total = 2_800
    val totalShares = people.filter { it in included }.sumOf { shares.getValue(it) }
    PbCard {
        people.forEachIndexed { index, head ->
            val count = if (head in included) shares.getValue(head) else 0
            val share = if (totalShares == 0) 0 else total * count / totalShares
            PbSplitRow(
                name = head.name,
                avatar = PbAvatarContent.Art(head),
                included = head in included,
                onIncludedChange = { included = if (it) included + head else included - head },
                mode =
                    PbSplitMode.Shares(
                        PbAmountEditor(
                            "$count",
                            { typed ->
                                shares = shares + (head to (typed.toIntOrNull() ?: 0))
                            },
                        ),
                        amount = "₹" + groupIndian("$share"),
                        onDecrement = { shares = shares + (head to (count - 1).coerceAtLeast(0)) },
                        onIncrement = { shares = shares + (head to count + 1) },
                    ),
                showDivider = index < people.lastIndex,
            )
        }
    }
}

@Composable
private fun Plans() {
    var yearly by remember { mutableIntStateOf(0) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PbSpace.S20)) {
        PbPlanCard(
            "Yearly",
            "₹799/year",
            "₹67/month",
            selected = yearly == 0,
            onClick = { yearly = 0 },
            modifier = Modifier.weight(1f),
            badge = "Save 33%",
        )
        PbPlanCard(
            "Monthly",
            "₹99/month",
            "Billed monthly",
            selected = yearly == 1,
            onClick = { yearly = 1 },
            modifier = Modifier.weight(1f),
        )
    }
}
