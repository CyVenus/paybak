package app.paybak.paybak.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.settings.ExportRange
import app.paybak.paybak.domain.settings.exportDefaultSelection
import app.paybak.paybak.domain.settings.exportGroups
import app.paybak.paybak.domain.settings.exportPeriod
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.SystemShare
import app.paybak.paybak.service.export.ExportFormat
import app.paybak.paybak.service.export.Exporter
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.PbTextButtonStyle
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/** Range chip labels and their test ids, in order. */
private val RangeChips =
    listOf(
        Triple(ExportRange.ThisMonth, R.string.settings_export_this_month, "thisMonth"),
        Triple(ExportRange.Last3Months, R.string.settings_export_last_3_months, "last3Months"),
        Triple(ExportRange.AllTime, R.string.settings_export_all_time, "allTime"),
    )

/**
 * The `privacyExport` route (screens-settings §9, Pro): export records as PDF or CSV for a range
 * and a set of groups. Rows start ticked when they have records in the range (again whenever the
 * range changes); Export writes the file on the device and opens the share sheet. If Pro lapses
 * here, Export opens the paywall instead.
 */
@Composable
fun ExportScreen(route: Route.PrivacyExport) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val view = snapshot.view
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val failed = stringResource(R.string.settings_export_failed)
    var format by rememberSaveable { mutableStateOf(ExportFormat.Pdf) }
    var range by rememberSaveable { mutableStateOf(ExportRange.ThisMonth) }
    val period = view.exportPeriod(range)
    val groups = remember(snapshot) { view.exportGroups() }
    var selected by
        rememberSaveable(range) { mutableStateOf(view.exportDefaultSelection(period).toList()) }
    val allSelected = groups.all { it.id in selected }

    SettingsPage(
        route.info.id,
        stringResource(R.string.settings_export_title),
        footer = {
            PbButton(
                stringResource(R.string.settings_export),
                onClick = {
                    if (!snapshot.isPro) {
                        navigator.open(Route.Paywall(continueTo = null))
                    } else {
                        scope.launch {
                            runCatching {
                                    Exporter.export(context, view, period, selected.toSet(), format)
                                }
                                .onSuccess { SystemShare.shareFile(context, it, format.mimeType) }
                                .onFailure { navigator.toast(failed) }
                        }
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = PbLayout.ScreenMargin)
                        .testTag("privacyExport.export"),
                enabled = selected.isNotEmpty(),
            )
        },
    ) {
        SettingsSection(stringResource(R.string.settings_export_format)) {
            PbSegmentedControl(
                options =
                    listOf(
                        stringResource(R.string.settings_export_pdf),
                        stringResource(R.string.settings_export_csv),
                    ),
                selectedIndex = if (format == ExportFormat.Pdf) 0 else 1,
                onSelect = { format = if (it == 0) ExportFormat.Pdf else ExportFormat.Csv },
                modifier = Modifier.fillMaxWidth(),
                segmentTags = listOf("privacyExport.format.pdf", "privacyExport.format.csv"),
            )
        }
        SettingsSection(stringResource(R.string.settings_export_range)) {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                RangeChips.forEach { (chip, label, id) ->
                    PbCategoryChip(
                        stringResource(label),
                        modifier = Modifier.testTag("privacyExport.range.$id"),
                        selected = chip == range,
                        onClick = { range = chip },
                    )
                }
            }
            SettingsFootnote(period.label, Modifier.testTag("privacyExport.rangeLabel"))
        }
        SettingsSection(
            title = null,
            footer = stringResource(R.string.settings_export_note),
            header = {
                GroupsHeader(allSelected) {
                    selected = if (allSelected) emptyList() else groups.map { it.id }
                }
            },
        ) {
            PbCard {
                groups.forEach { group ->
                    val ticked = group.id in selected
                    PbSettingRow(
                        group.name,
                        modifier = Modifier.testTag("privacyExport.group.${group.id}"),
                        trailing = PbSettingTrailing.Check(ticked),
                        onClick = {
                            selected = if (ticked) selected - group.id else selected + group.id
                        },
                        showDivider = group != groups.last(),
                    )
                }
            }
        }
    }
}

/** "Groups" with the Select all / Deselect all action on the right. */
@Composable
private fun GroupsHeader(allSelected: Boolean, onToggleAll: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.settings_export_groups),
            modifier = Modifier.semantics { heading() },
            style = PbTextStyles.Title3,
            color = PbColors.Text.Primary,
        )
        PbTextButton(
            stringResource(
                if (allSelected) R.string.settings_export_deselect_all
                else R.string.settings_export_select_all
            ),
            onClick = onToggleAll,
            modifier = Modifier.wrapContentHeight(unbounded = true).testTag("privacyExport.selectAll"),
            style = PbTextButtonStyle.Secondary,
        )
    }
}
