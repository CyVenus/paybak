package app.paybak.paybak.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.ScanItem
import app.paybak.paybak.domain.scan.ReceiptSplit
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.service.receipt.ReceiptPhotos
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbReceiptLineRow
import app.paybak.paybak.ui.components.PbReceiptThumbnail
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbScrollEdgeFade
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Check receipt (scanReview; insights §4.3): the photo, the merchant and date, the subtotal, tax,
 * tip and total, and the items, each amount fixable in place. The items must add up to the subtotal
 * before "Looks right" goes on to Assign items; the total follows every edit. When nothing could be
 * read, a notice offers Retake and Attach photo instead.
 */
@Composable
internal fun ScanReview(
    state: ScanState,
    onChange: (ScanState) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onAttachOnly: () -> Unit,
) {
    val currency = LocalLedger.current.defaultCurrency
    val scan = state.scan
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var text by rememberSaveable { mutableStateOf("") }

    fun commit() {
        val key = editing ?: return
        editing = null
        onChange(state.edited(key, AmountEntry.minor(text, currency)))
    }

    fun edit(key: String, amount: Long) {
        commit()
        editing = key
        text = AmountEntry.text(amount, currency)
    }

    @Composable
    fun Line(key: String, label: String, amount: Long, total: Boolean = false) {
        PbReceiptLineRow(
            label,
            Money.format(amount, currency),
            Modifier.testTag("scanReview.$key"),
            total = total,
            editor =
                if (editing == key) {
                    PbAmountEditor(
                        text,
                        onValueChange = { typed -> AmountEntry.accept(typed)?.let { text = it } },
                        prefix = Money.currency(currency).symbol,
                        onDone = ::commit,
                        decimal = AmountEntry.allowsDecimals(currency),
                    )
                } else {
                    null
                },
            onEdit =
                if (total) null
                else {
                    { edit(key, amount) }
                },
        )
    }

    PbScreenFrame(id = "scanReview") {
        PbPushHeader(
            stringResource(R.string.insights_scan_review_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            testTag = "scanReview",
        )
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PbLayout.ScreenMargin)
                    .padding(top = PbSpace.S16, bottom = PbSpace.S24)
            ) {
                Intro(state, scan)
                if (scan == null) {
                    PbNoticeCard(
                        body = stringResource(R.string.insights_scan_unreadable_body),
                        icon = PbIcon.Receipt,
                        modifier = Modifier.padding(top = PbSpace.S24),
                        title = stringResource(R.string.insights_scan_unreadable_title),
                        primaryAction =
                            PbNoticeAction(stringResource(R.string.insights_scan_retake), onBack),
                        secondaryAction =
                            PbNoticeAction(
                                stringResource(R.string.insights_scan_attach),
                                onAttachOnly,
                            ),
                        testTag = "scanReview.unreadable",
                    )
                    return@Column
                }
                Details(scan)
                PbCard(Modifier.padding(top = PbSpace.S16)) {
                    Line(
                        "subtotal",
                        stringResource(R.string.insights_scan_subtotal),
                        scan.subtotal ?: 0,
                    )
                    scan.taxes.forEachIndexed { i, tax -> Line("tax.$i", tax.label, tax.amount) }
                    scan.tip?.let { Line("tip", ReceiptSplit.tipLabel(scan), it) }
                    Line(
                        "total",
                        stringResource(R.string.insights_scan_total),
                        ReceiptSplit.total(scan),
                        total = true,
                    )
                }
                PbSectionHeader(
                    stringResource(R.string.insights_scan_items),
                    Modifier.padding(top = PbSpace.S24),
                    action = stringResource(R.string.insights_scan_add_item),
                    onAction = {
                        commit()
                        val next = state.editScan {
                            it.copy(items = it.items + ScanItem(NEW_ITEM, 0))
                        }
                        onChange(next)
                        editing = "item.${next.scan!!.items.lastIndex}"
                        text = ""
                    },
                )
                PbCard(Modifier.padding(top = PbSpace.S8)) {
                    scan.items.forEachIndexed { i, item ->
                        Line("item.$i", item.label, item.amount)
                    }
                }
                if (!ReceiptSplit.itemsMatch(scan)) {
                    Text(
                        stringResource(
                            R.string.insights_scan_items_mismatch,
                            Money.format(scan.items.sumOf { it.amount }, currency),
                            Money.format(scan.subtotal ?: 0, currency),
                        ),
                        Modifier.padding(top = PbSpace.S8).testTag("scanReview.mismatch"),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Destructive,
                    )
                }
            }
            PbScrollEdgeFade(Modifier.align(Alignment.BottomCenter))
        }
        if (scan != null) {
            PbButton(
                stringResource(R.string.insights_scan_looks_right),
                onClick = {
                    commit()
                    onConfirm()
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = PbLayout.ScreenMargin)
                        .testTag("scanReview.confirm"),
                enabled = state.confirmable(editing, text, currency),
            )
        }
    }
}

/** "New item" ₹0: the line Add item appends, its amount being edited. */
private const val NEW_ITEM = "New item"

/** Whether the items match the subtotal, counting an edit that is still open. */
private fun ScanState.confirmable(editing: String?, text: String, currency: String): Boolean {
    val current = if (editing == null) this else edited(editing, AmountEntry.minor(text, currency))
    return current.scan?.let(ReceiptSplit::itemsMatch) == true
}

/**
 * [state] with the amount at [key] ("subtotal", "tip", "tax.<n>", "item.<n>") set to [amount]; an
 * item set to zero is removed.
 */
private fun ScanState.edited(key: String, amount: Long): ScanState {
    val index = key.substringAfter('.', "").toIntOrNull()
    return when {
        key == "subtotal" -> editScan { it.copy(subtotal = amount) }
        key == "tip" -> editScan { it.copy(tip = amount) }
        key.startsWith("tax.") && index != null ->
            editScan { scan ->
                scan.copy(
                    taxes =
                        scan.taxes.mapIndexed { i, tax ->
                            if (i == index) tax.copy(amount = amount) else tax
                        }
                )
            }
        key.startsWith("item.") && index != null && amount == 0L ->
            editScan { scan -> scan.copy(items = scan.items.filterIndexed { i, _ -> i != index }) }
        key.startsWith("item.") && index != null -> editItem(index) { it.copy(amount = amount) }
        else -> this
    }
}

@Composable
private fun Intro(state: ScanState, scan: ReceiptScan?) {
    val context = LocalContext.current
    val photo by
        produceState<ImageBitmap?>(null, state.photo, state.sample) {
            value =
                state.photo
                    ?.takeIf { !state.sample }
                    ?.let { ReceiptPhotos.load(context, it)?.asImageBitmap() }
        }
    Row(
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbReceiptThumbnail(photo = photo)
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                if (scan == null) stringResource(R.string.insights_scan_found_nothing)
                else
                    pluralStringResource(
                        R.plurals.insights_scan_found,
                        scan.items.size,
                        scan.items.size,
                    ),
                Modifier.testTag("scanReview.found"),
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
            )
            if (scan != null) {
                Text(
                    stringResource(R.string.insights_scan_tap_to_fix),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
        }
    }
}

/** Merchant and date, as read. */
@Composable
private fun Details(scan: ReceiptScan) {
    val notFound = stringResource(R.string.insights_scan_not_found)
    PbCard(Modifier.padding(top = PbSpace.S24)) {
        PbSettingRow(
            stringResource(R.string.insights_scan_merchant),
            Modifier.testTag("scanReview.merchant"),
            trailing = PbSettingTrailing.None,
            icon = PbIcon.Receipt,
            value = scan.merchant ?: notFound,
        )
        PbSettingRow(
            stringResource(R.string.insights_date),
            Modifier.testTag("scanReview.date"),
            trailing = PbSettingTrailing.None,
            icon = PbIcon.Calendar,
            value =
                scan.date?.let { date ->
                    val time = scan.time?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                    Dates.day(date) +
                        (time?.let { " · " + Dates.time(LocalDateTime.of(date, it)) } ?: "")
                } ?: notFound,
            showDivider = false,
        )
    }
}
