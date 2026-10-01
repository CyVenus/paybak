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
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbAssignItemRow
import app.paybak.paybak.ui.components.PbAssignee
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbChatBubble
import app.paybak.paybak.ui.components.PbChatRole
import app.paybak.paybak.ui.components.PbDraftExpenseCard
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbPersonTotal
import app.paybak.paybak.ui.components.PbPersonTotalsCard
import app.paybak.paybak.ui.components.PbReceiptLineRow
import app.paybak.paybak.ui.components.PbReceiptSize
import app.paybak.paybak.ui.components.PbReceiptThumbnail
import app.paybak.paybak.ui.components.PbShutterButton
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val People =
    listOf("You" to PbPeepHead.Arjun, "Esha" to PbPeepHead.Esha, "Dev" to PbPeepHead.Dev)

@Composable
internal fun AssistantPage() {
    GalleryPage {
        GallerySection("Chat / Bubble: User · Assistant") {
            PbChatBubble("Who owes me money?", PbChatRole.User)
            PbChatBubble("Who owes me money?", PbChatRole.Assistant)
        }
        GallerySection("Chat / Draft Expense: Pending · Saved (tap Save)") { DraftExpenses() }
        GallerySection("Row / Receipt Line: Default · Total") { ReceiptLines() }
        GallerySection("Row / Assign Item (tap the people)") { AssignItems() }
        GallerySection("Card / Person Totals") {
            PbPersonTotalsCard(
                status = "All items assigned",
                note = "Includes GST and tip",
                totals =
                    listOf("₹989", "₹621", "₹690").zip(People) { amount, (name, head) ->
                        PbPersonTotal(name, amount, PbAvatarContent.Art(head))
                    },
            )
        }
        GallerySection("Control / Shutter: Default · Pressed") {
            Row(
                Modifier.fillMaxWidth()
                    .background(PbColors.Bg.Camera, PbShapes.Card)
                    .padding(PbSpace.S24),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PbShutterButton(onClick = {})
                PbShutterButton(onClick = {}, interactionSource = rememberPressedSource())
            }
        }
        GallerySection("Art / Receipt: Full · Thumb") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.weight(1f)) { PbReceiptThumbnail(size = PbReceiptSize.Full) }
                PbReceiptThumbnail()
            }
        }
    }
}

@Composable
private fun DraftExpenses() {
    PbDraftExpenseCard(
        "Cab",
        "₹600",
        PbIcon.Car,
        paidLine = "Paid by you · Today",
        splitLine = "Split equally with Esha and Dev",
        eachLine = "₹200 each",
        members = People.map { PbAvatarContent.Art(it.second) },
        saved = true,
        onSave = {},
        onEdit = {},
        onView = {},
    )
    var saved by remember { mutableStateOf(false) }
    PbDraftExpenseCard(
        "Cab",
        "₹600",
        PbIcon.Car,
        paidLine = "Paid by you · Today",
        splitLine = "Split equally with Esha and Dev",
        eachLine = "₹200 each",
        members = People.map { PbAvatarContent.Art(it.second) },
        saved = saved,
        onSave = { saved = true },
        onEdit = {},
        onView = { saved = false },
    )
    GalleryLabel("View resets the live card")
}

@Composable
private fun ReceiptLines() {
    PbCard {
        PbReceiptLineRow("Chicken biryani", "₹430")
        PbReceiptLineRow("Chicken biryani", "₹430", total = true)
    }
    GalleryLabel("Editing, live: tap an amount to fix it")
    var amounts by remember { mutableStateOf(listOf("430", "370", "450")) }
    var editing by remember { mutableIntStateOf(-1) }
    PbCard {
        listOf("Chicken biryani", "Paneer tikka", "Fish and chips").forEachIndexed { index, label ->
            val value = amounts[index]
            PbReceiptLineRow(
                label,
                "₹$value",
                editor =
                    if (editing == index) {
                        PbAmountEditor(
                            value,
                            { typed ->
                                amounts = amounts.toMutableList().also { it[index] = typed }
                            },
                            prefix = "₹",
                            onDone = { editing = -1 },
                        )
                    } else {
                        null
                    },
                onEdit = { editing = index },
            )
        }
        PbReceiptLineRow(
            "Total",
            "₹" + groupIndian("${amounts.sumOf { it.toIntOrNull() ?: 0 }}"),
            total = true,
        )
    }
}

@Composable
private fun AssignItems() {
    PbAssignItemRow(
        "Chicken biryani",
        "₹430",
        People.map { (name, head) -> PbAssignee(name, PbAvatarContent.Art(head), name == "Dev") },
        onToggle = {},
    )
    var had by remember { mutableStateOf(setOf(0, 1, 2)) }
    val count = had.size
    PbAssignItemRow(
        "Fresh lime soda ×3",
        "₹270",
        People.mapIndexed { index, (name, head) ->
            PbAssignee(name, PbAvatarContent.Art(head), index in had)
        },
        onToggle = { index -> had = if (index in had) had - index else had + index },
        sharedCaption = if (count > 1) "Shared by $count · ₹${270 / count} each" else null,
    )
}
