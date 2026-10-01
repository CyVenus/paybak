package app.paybak.paybak.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.scan.ReceiptSplit
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.ui.components.PbAssignItemRow
import app.paybak.paybak.ui.components.PbAssignee
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbChipLeading
import app.paybak.paybak.ui.components.PbPersonTotal
import app.paybak.paybak.ui.components.PbPersonTotalsCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbScrollEdgeFade
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.rememberUserAvatar
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The people picker's request from Assign items. */
private const val PEOPLE_REQUEST = "scanAssign.people"

/**
 * Assign items (scanAssign; insights §4.4): who had each item, a chip per person on the expense.
 * Shared items split evenly; the pinned card shows each person's total with tax and tip in
 * proportion, and Continue is on once every item has someone. With nobody but you on the expense,
 * "With you and · Add" picks the people first.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ScanAssign(
    state: ScanState,
    onChange: (ScanState) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val store = LocalProfileStore.current
    val profile by store.profile.collectAsState()
    val you = rememberUserAvatar(profile, store)
    val currency = ledger.defaultCurrency
    val scan = state.scan ?: return
    val youLabel = stringResource(R.string.insights_you)
    val people =
        state.people.map { id ->
            if (id == ME) youLabel to you
            else
                snapshot.view.person(id).let { person ->
                    (person?.firstName ?: "?") to
                        (person?.avatarContent() ?: PbAvatarContent.Initials("?"))
                }
        }
    val totals =
        remember(scan, state.assigned, state.people) {
            ReceiptSplit.totals(scan, state.assigned, state.people)
        }
    // Who joins when the expense has only you; scanning from a form with people starts with them.
    val addingPeople = rememberSaveable { state.people.size < 2 }
    RouteResultEffect(PEOPLE_REQUEST) { result ->
        (result as? RouteResult.People)?.let { onChange(state.withPeople(it.personIds)) }
    }
    PbScreenFrame(id = "scanAssign") {
        PbPushHeader(
            stringResource(R.string.insights_scan_assign_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            testTag = "scanAssign",
        )
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PbLayout.ScreenMargin)
                    .padding(top = PbSpace.S8, bottom = PbSpace.S24)
            ) {
                Text(
                    stringResource(R.string.insights_scan_assign_helper),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
                if (addingPeople) {
                    FlowRow(
                        Modifier.padding(top = PbSpace.S12),
                        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.insights_scan_with_you_and),
                            style = PbTextStyles.Subheadline,
                            color = PbColors.Text.Secondary,
                        )
                        people.drop(1).forEach { (name, avatar) ->
                            PbCategoryChip(name, leading = PbChipLeading.Avatar(avatar))
                        }
                        PbCategoryChip(
                            stringResource(R.string.insights_scan_add_people),
                            Modifier.testTag("scanAssign.addPeople"),
                            onClick = {
                                navigator.open(
                                    Route.PickPeople(
                                        PickRequest(PEOPLE_REQUEST),
                                        selected = state.people.filter { it != ME },
                                    )
                                )
                            },
                            leading = PbChipLeading.Icon(PbIcon.Plus),
                        )
                    }
                }
                Column(Modifier.padding(top = PbSpace.S12)) {
                    scan.items.forEachIndexed { index, item ->
                        val who = state.assigned.getOrNull(index).orEmpty()
                        PbAssignItemRow(
                            item = item.label,
                            price = Money.format(item.amount, currency),
                            people =
                                people.mapIndexed { i, (name, avatar) ->
                                    PbAssignee(name, avatar, state.people[i] in who)
                                },
                            onToggle = { i -> onChange(state.toggle(index, state.people[i])) },
                            sharedCaption =
                                who.size
                                    .takeIf { it > 1 }
                                    ?.let { count ->
                                        stringResource(
                                            R.string.insights_scan_shared,
                                            count,
                                            Money.format(
                                                ReceiptSplit.eachShare(item.amount, count),
                                                currency,
                                            ),
                                        )
                                    },
                            showDivider = index < scan.items.lastIndex,
                            testTag = "scanAssign.item.$index",
                        )
                    }
                }
            }
            PbScrollEdgeFade(Modifier.align(Alignment.BottomCenter))
        }
        Column(
            Modifier.padding(horizontal = PbLayout.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        ) {
            PbPersonTotalsCard(
                status =
                    if (totals.unassigned == 0) stringResource(R.string.insights_scan_all_assigned)
                    else
                        pluralStringResource(
                            R.plurals.insights_scan_items_left,
                            totals.unassigned,
                            totals.unassigned,
                        ),
                note = stringResource(R.string.insights_scan_includes_tax),
                totals =
                    people.mapIndexed { i, (name, avatar) ->
                        PbPersonTotal(
                            name,
                            Money.format(totals.totals[state.people[i]] ?: 0, currency),
                            avatar,
                        )
                    },
                modifier = Modifier.testTag("scanAssign.totals"),
                complete = totals.unassigned == 0,
            )
            PbButton(
                stringResource(R.string.insights_scan_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().testTag("scanAssign.continue"),
                enabled = totals.unassigned == 0 && state.people.size > 1,
            )
        }
    }
}
