package app.paybak.paybak.feature.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addComment
import app.paybak.paybak.data.ledger.actions.deleteExpense
import app.paybak.paybak.data.ledger.actions.resolveFlag
import app.paybak.paybak.data.ledger.actions.updateExpense
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.addrecord.ExpenseDetail
import app.paybak.paybak.domain.addrecord.expenseDetail
import app.paybak.paybak.domain.model.Receipt
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.addexpense.ExpenseForm
import app.paybak.paybak.feature.pickers.rememberLedgerPhoto
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPhotoPicker
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PhotoRef
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAmountHero
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCommentRow
import app.paybak.paybak.ui.components.PbComposer
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbHeroLeading
import app.paybak.paybak.ui.components.PbHistoryRow
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbReceiptThumbnail
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTone
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `expense` route: the Expense detail template (activity §4; `expenseAdded`, add-expense §11).
 * The hero, your share, due date and group balance, the split with the payer first, the receipt,
 * comments, history and the actions. Edit opens Add expense in edit mode.
 */
@Composable
fun ExpenseDetailScreen(route: Route.Expense) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val people = rememberPeopleDirectory()
    val start = rememberDebugStartScreen("expenseDelete", "expenseComment")
    var deleting by rememberSaveable { mutableStateOf(start == "expenseDelete") }
    var flagging by rememberSaveable { mutableStateOf(false) }
    var comment by rememberSaveable {
        mutableStateOf(if (start == "expenseComment") "Thanks, that works for me." else "")
    }
    val detail = snapshot.view.expenseDetail(route.expenseId)
    val deleted = stringResource(R.string.add_toast_expense_deleted)
    val receiptRequest = PickRequest(rememberSaveable { newId() })
    val photo = rememberLedgerPhoto(detail?.expense?.receipt?.photo)
    fun attach(receipt: Receipt) {
        val expense = detail?.expense ?: return
        ledger.updateExpense(expense.id, ExpenseForm.of(expense).copy(receipt = receipt).toDraft())
    }
    val pickPhoto = rememberPhotoPicker {
        attach(Receipt(photo = it, addedAt = ledger.clock.now()))
    }
    RouteResultEffect(receiptRequest.id) { result ->
        (result as? RouteResult.Receipt)?.result?.photo?.let {
            attach(Receipt(photo = it, addedAt = ledger.clock.now()))
        }
    }

    PbPinnedHeaderScreen(
        testTag = "screen.expense",
        header = {
            PbPushHeader(
                stringResource(R.string.add_expense),
                onBack = { navigator.back() },
                action =
                    detail
                        ?.takeIf { it.expense.deletedAt == null }
                        ?.let {
                            PbHeaderAction.Text(
                                stringResource(R.string.add_edit),
                                { edit(navigator::open, it) },
                            )
                        },
                testTag = "expense",
                actionTag = "edit",
            )
        },
        gap = PbSpace.S24,
    ) {
        if (detail == null) {
            Text(
                stringResource(R.string.add_expense_gone),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
            )
            return@PbPinnedHeaderScreen
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbAmountHero(
                detail.title,
                detail.amount,
                detail.meta,
                PbHeroLeading.Single(PbAvatarContent.Symbol(detail.category.pbIcon)),
                Modifier.testTag("expense.hero"),
                tags = detail.tags,
                status = detail.status,
            )
            if (detail.flagTitle != null && detail.flagBody != null) {
                PbNoticeCard(
                    detail.flagBody,
                    PbIcon.Flag,
                    title = detail.flagTitle,
                    primaryAction =
                        PbNoticeAction(
                            stringResource(R.string.add_edit_expense),
                            onClick = { edit(navigator::open, detail) },
                        ),
                    secondaryAction =
                        PbNoticeAction(
                            stringResource(R.string.add_resolve),
                            onClick = { ledger.resolveFlag(detail.expense.id) },
                        ),
                    testTag = "expense.dispute",
                )
            }
        }
        ShareCard(detail) { navigator.open(Route.Group(it)) }
        Section(detail.splitHeader) {
            PbCard {
                detail.split.forEachIndexed { index, line ->
                    PbPersonRow(
                        line.name,
                        people.avatar(line.personId),
                        Modifier.testTag("expense.split.${line.personId}"),
                        subtitle = line.paid,
                        tag = stringResource(R.string.add_guest).takeIf { line.isGuest },
                        trailing = PbPersonTrailing.Amount(line.share),
                        size = PbPersonRowSize.Compact,
                        showDivider = index < detail.split.lastIndex,
                    )
                }
            }
        }
        Section(stringResource(R.string.add_receipt)) {
            val receipt = detail.expense.receipt
            if (receipt != null && detail.receiptLine != null) {
                ReceiptCard(photo, detail.receiptLine) {
                    navigator.open(Route.PhotoViewer(PhotoRef(receipt.photo, receipt.asset)))
                }
            } else {
                PbCard {
                    PbSettingRow(
                        stringResource(R.string.add_add_receipt),
                        Modifier.testTag("expense.addReceipt"),
                        icon = PbIcon.Camera,
                        onClick = {
                            if (snapshot.isPro) navigator.open(Route.ScanReceipt(receiptRequest))
                            else pickPhoto()
                        },
                        showDivider = false,
                    )
                }
            }
        }
        Section(stringResource(R.string.add_comments)) {
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                Column {
                    detail.comments.forEach {
                        PbCommentRow(it.name, it.date, it.text, people.avatar(it.personId))
                    }
                }
                PbComposer(
                    comment,
                    { comment = it },
                    onSend = {
                        ledger.addComment(detail.expense.id, comment)
                        comment = ""
                    },
                    placeholder = stringResource(R.string.add_comment_placeholder),
                    fieldModifier = Modifier.testTag("expense.composer"),
                )
            }
        }
        Section(stringResource(R.string.add_history)) {
            Column {
                detail.history.forEachIndexed { index, line ->
                    PbHistoryRow(line.text, line.date, last = index == detail.history.lastIndex)
                }
            }
        }
        PbCard {
            if (detail.canFlag) {
                PbSettingRow(
                    stringResource(R.string.add_flag_issue),
                    Modifier.testTag("expense.flag"),
                    icon = PbIcon.Flag,
                    onClick = { flagging = true },
                )
            }
            PbSettingRow(
                stringResource(R.string.add_delete_expense),
                Modifier.testTag("expense.delete"),
                icon = PbIcon.Delete,
                tone = PbSettingTone.Destructive,
                trailing = PbSettingTrailing.None,
                onClick = { deleting = true },
                showDivider = false,
            )
        }
    }

    if (detail != null && deleting) {
        val group = detail.tags.takeIf { detail.expense.groupId != null }?.first()
        PbAlert(
            title = stringResource(R.string.add_delete_title),
            message =
                if (group != null) {
                    stringResource(R.string.add_delete_message_group, detail.title, group)
                } else {
                    stringResource(R.string.add_delete_message, detail.title)
                },
            cancelLabel = stringResource(R.string.add_cancel),
            actionLabel = stringResource(R.string.add_delete),
            onCancel = { deleting = false },
            onAction = {
                deleting = false
                ledger.deleteExpense(detail.expense.id)
                navigator.back()
                navigator.toast(deleted)
            },
            testTag = "expense.alert",
        )
    }
    if (detail != null && flagging) {
        FlagSheet(detail.expense.id) { flagging = false }
    }
}

/** Edit opens Add expense in edit mode, prefilled (activity §4.2). */
private fun edit(open: (Route) -> Unit, detail: ExpenseDetail) =
    open(Route.AddExpense(AddExpenseArgs(editing = detail.expense.id, focusAmount = false)))

/** A titled section: the Title/3 header 8 dp above its body. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbSectionHeader(title)
        content()
    }
}

/** Your share, the due date and your balance in the group (activity §4.3-B). */
@Composable
private fun ShareCard(detail: ExpenseDetail, onGroup: (String) -> Unit) {
    val rows =
        listOfNotNull(
            detail.yourShare?.let { "share" },
            detail.due?.let { "due" },
            detail.groupBalance?.let { "group" },
        )
    if (rows.isEmpty()) return
    PbCard(Modifier.testTag("expense.share")) {
        rows.forEachIndexed { index, row ->
            val last = index == rows.lastIndex
            when (row) {
                "share" ->
                    PbSettingRow(
                        stringResource(R.string.add_your_share),
                        icon = PbIcon.Wallet,
                        value = detail.yourShare,
                        trailing = PbSettingTrailing.None,
                        showDivider = !last,
                    )
                "due" ->
                    PbSettingRow(
                        stringResource(R.string.add_due),
                        icon = PbIcon.Calendar,
                        value = detail.due,
                        trailing = PbSettingTrailing.None,
                        showDivider = !last,
                    )
                else -> {
                    val balance = detail.groupBalance!!
                    PbSettingRow(
                        balance.title,
                        Modifier.testTag("expense.groupBalance"),
                        icon = PbIcon.Groups,
                        value = balance.value,
                        valueColor =
                            when {
                                balance.net > 0 -> PbColors.Text.Primary
                                balance.net < 0 -> PbColors.Text.Secondary
                                else -> PbColors.Text.Tertiary
                            },
                        onClick = { onGroup(balance.groupId) },
                        showDivider = !last,
                    )
                }
            }
        }
    }
}

/** The receipt card: the photo (or the drawn receipt), "Receipt photo", who added it and when. */
@Composable
private fun ReceiptCard(photo: ImageBitmap?, line: String, onClick: () -> Unit) {
    PbCard(Modifier.testTag("expense.receipt")) {
        Row(
            Modifier.fillMaxWidth()
                .clickable(interactionSource = null, indication = null, onClick = onClick)
                .padding(PbSpace.S12),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbReceiptThumbnail(
                photo = photo,
                contentDescription = stringResource(R.string.add_receipt_photo),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    stringResource(R.string.add_receipt_photo),
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                )
                Text(line, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
            }
            PbIconImage(
                PbIcon.ChevronRight,
                contentDescription = null,
                size = PbSize.IconMd,
                tint = PbColors.Icon.Tertiary,
            )
        }
    }
}
