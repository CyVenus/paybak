package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAlertAction
import app.paybak.paybak.ui.components.PbAlertCard
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbChipLeading
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTone
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetContainer
import app.paybak.paybak.ui.components.PbSheetDetent
import app.paybak.paybak.ui.components.PbSheetSearch
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

@Composable
internal fun NavigationPage() {
    GalleryPage {
        GallerySection("Navigation / Push Header: Text · Icon · None · Wide Text") { PushHeaders() }
        GallerySection("Navigation / Modal Header: Enabled · Disabled · None") { ModalHeaders() }
        GallerySection("Overlay / Alert: Destructive · Primary") { Alerts() }
        GallerySection("Sheet / Container") { Sheets() }
        GallerySection("Control / Category Chip (tap to select)") { Chips() }
        GallerySection("Row / Setting: Default · Destructive") { SettingRows() }
    }
}

@Composable
private fun PushHeaders() {
    PbPushHeader("Edit avatar", onBack = {}, action = PbHeaderAction.Text("Save", onClick = {}))
    PbPushHeader(
        "Edit avatar",
        onBack = {},
        action = PbHeaderAction.Icon(PbIcon.Settings, "Settings", onClick = {}),
    )
    PbPushHeader("Edit avatar", onBack = {})
    PbPushHeader(
        "Edit avatar",
        onBack = {},
        action = PbHeaderAction.Text("Save", onClick = {}, wide = true),
    )
    GalleryLabel("Wide Text truncates the title and a long action")
    PbPushHeader(
        "Notifications and reminders",
        onBack = {},
        action = PbHeaderAction.Text("Mark all read", onClick = {}, wide = true),
    )
}

@Composable
private fun ModalHeaders() {
    PbModalHeader("Add expense", onClose = {}, action = "Save")
    PbModalHeader("Add expense", onClose = {}, action = "Save", actionEnabled = false)
    PbModalHeader("Add expense", onClose = {})
}

@Composable
private fun Alerts() {
    var shown by remember { mutableStateOf<PbAlertAction?>(null) }
    Box(
        Modifier.fillMaxWidth().background(PbColors.Bg.Scrim, PbShapes.Card).padding(PbSpace.S16),
        contentAlignment = Alignment.Center,
    ) {
        PbAlertCard(
            "Discard changes?",
            "Your avatar edits won’t be saved.",
            cancelLabel = "Keep editing",
            actionLabel = "Discard",
            onCancel = {},
            onAction = {},
        )
    }
    Box(
        Modifier.fillMaxWidth().background(PbColors.Bg.Scrim, PbShapes.Card).padding(PbSpace.S16),
        contentAlignment = Alignment.Center,
    ) {
        PbAlertCard(
            "Discard changes?",
            "Your avatar edits won’t be saved.",
            cancelLabel = "Not now",
            actionLabel = "Settle up",
            onCancel = {},
            onAction = {},
            action = PbAlertAction.Primary,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbButton(
            "Show Destructive",
            onClick = { shown = PbAlertAction.Destructive },
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
        PbButton(
            "Show Primary",
            onClick = { shown = PbAlertAction.Primary },
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
    }
    shown?.let { action ->
        val destructive = action == PbAlertAction.Destructive
        PbAlert(
            title = if (destructive) "Discard changes?" else "Settle up with Rohan?",
            message =
                if (destructive) "Your avatar edits won’t be saved."
                else "This records ₹800 as paid.",
            cancelLabel = if (destructive) "Keep editing" else "Not now",
            actionLabel = if (destructive) "Discard" else "Settle up",
            onCancel = { shown = null },
            onAction = { shown = null },
            action = action,
        )
    }
}

@Composable
private fun Sheets() {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableIntStateOf(0) }
    var open by remember { mutableStateOf<PbSheetDetent?>(null) }
    GalleryLabel("Detent=Medium with search (the picker pattern)")
    Box(Modifier.fillMaxWidth().background(PbColors.Bg.Scrim, PbShapes.Card).padding(8.dp)) {
        PbSheetContainer(
            title = "Category",
            onClose = {},
            search = PbSheetSearch(query, { query = it }, "Search categories"),
        ) {
            CategoryRows(category, onSelect = { category = it })
        }
    }
    GalleryLabel("No title and no close: the content starts 20 dp down")
    Box(Modifier.fillMaxWidth().background(PbColors.Bg.Scrim, PbShapes.Card).padding(8.dp)) {
        PbSheetContainer {
            PbButton("Not received", onClick = {}, Modifier.fillMaxWidth())
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbButton(
            "Open Medium",
            onClick = { open = PbSheetDetent.Medium },
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
        PbButton(
            "Open Large",
            onClick = { open = PbSheetDetent.Large },
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
        )
    }
    open?.let { detent ->
        PbSheet(
            onDismiss = { open = null },
            title = if (detent == PbSheetDetent.Medium) "Category" else "Paid by",
            detent = detent,
            search = PbSheetSearch(query, { query = it }, "Search categories"),
            testTag = "gallery.sheet",
        ) { dismiss ->
            CategoryRows(
                category,
                onSelect = {
                    category = it
                    dismiss()
                },
            )
        }
    }
}

private val Categories =
    listOf("Food" to PbIcon.Food, "Travel" to PbIcon.Car, "Stays" to PbIcon.Bed)

@Composable
private fun CategoryRows(selected: Int, onSelect: (Int) -> Unit) {
    PbCard {
        Categories.forEachIndexed { index, (name, icon) ->
            PbSettingRow(
                name,
                icon = icon,
                trailing = PbSettingTrailing.Check(selected == index),
                onClick = { onSelect(index) },
                showDivider = index < Categories.lastIndex,
            )
        }
    }
}

@Composable
private fun Chips() {
    var selected by remember { mutableStateOf(setOf("Food")) }
    var people by remember { mutableStateOf(listOf(PbPeepHead.Priya, PbPeepHead.Rohan)) }
    val toggle = { label: String ->
        selected = if (label in selected) selected - label else selected + label
    }
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        listOf("Hair", "Food").forEach { label ->
            PbCategoryChip(label, selected = label in selected, onClick = { toggle(label) })
        }
        PbCategoryChip(
            "Add",
            selected = "Add" in selected,
            onClick = { toggle("Add") },
            leading = PbChipLeading.Icon(PbIcon.Plus),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        listOf("Hair", "Priya").forEach { label ->
            PbCategoryChip(
                label,
                selected = label in selected,
                onClick = { toggle(label) },
                leading = PbChipLeading.Avatar(PbAvatarContent.Art(PbPeepHead.Priya)),
            )
        }
    }
    GalleryLabel("Show remove: ✕ removes the chip")
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        people.forEach { head ->
            PbCategoryChip(
                head.name,
                leading = PbChipLeading.Avatar(PbAvatarContent.Art(head)),
                onRemove = { people = people - head },
            )
        }
    }
}

@Composable
private fun SettingRows() {
    var toggle by remember { mutableStateOf(true) }
    var count by remember { mutableIntStateOf(2) }
    PbSettingTone.entries.forEach { tone ->
        PbCard {
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                value = "UPI",
                tone = tone,
                onClick = {},
            )
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                trailing = PbSettingTrailing.Toggle(toggle) { toggle = it },
                tone = tone,
            )
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                value = "$count",
                trailing =
                    PbSettingTrailing.Stepper(
                        onDecrement = { count-- },
                        onIncrement = { count++ },
                        canDecrement = count > 0,
                    ),
                tone = tone,
            )
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                trailing = PbSettingTrailing.Check(true),
                tone = tone,
                onClick = {},
            )
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                trailing = PbSettingTrailing.Check(false),
                tone = tone,
                onClick = {},
            )
            PbSettingRow(
                "Payment details",
                icon = PbIcon.Wallet,
                value = "UPI",
                trailing = PbSettingTrailing.None,
                tone = tone,
                showDivider = false,
            )
        }
    }
    GalleryLabel("Subtitle, badge, no icon")
    PbCard {
        PbSettingRow(
            "Loan to Dev",
            subtitle = "Paid back in parts",
            badge = "Pro",
            onClick = {},
        )
        PbSettingRow(
            "Insights",
            icon = PbIcon.Chart,
            badge = "Try free",
            onClick = {},
            showDivider = false,
        )
    }
}
