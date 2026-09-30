package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.icons.PbIcon

/** One choice of the group picker: [id] null is "No group" / "None". */
private data class Choice(val id: String?, val title: String, val icon: PbIcon)

/**
 * The `pickGroup` route sheet (add-expense §3.10, record-lend-group §2.4; proposals): Add expense's
 * Group ("No group", then your active groups) or, with a person, Record payment's For ("None", the
 * groups and projects you share with them, your open loans with them).
 */
@Composable
fun GroupPickerSheet(route: Route.PickGroup) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    var answer by remember { mutableStateOf<RouteResult.Group?>(null) }
    val ledger = snapshot.ledger
    val person = route.personId
    val groups =
        ledger.groups
            .filter { ME in it.memberIds && !it.isArchived }
            .filter { if (person != null) person in it.memberIds else it.kind == GroupKind.Group }
            .map { Choice(it.id, it.name, iconForKey(it.icon)) }
    val loans =
        if (person == null) emptyList()
        else
            ledger.loans
                .filter { it.friendId == person }
                .filter { loan ->
                    snapshot.view.loanContext(loan)?.amount?.let { it != 0L } == true
                }
                .map { Choice(it.id, "Loan · ${it.title}", PbIcon.Lend) }
    val choices =
        listOf(
            Choice(
                null,
                if (person != null) stringResource(R.string.add_none)
                else stringResource(R.string.add_no_group),
                PbIcon.Groups,
            )
        ) + groups + loans
    PbSheet(
        onDismiss = {
            answer?.let { navigator.complete(route.request.id, it) } ?: navigator.dismissSheet()
        },
        title =
            if (person != null) stringResource(R.string.add_for)
            else stringResource(R.string.add_group),
        testTag = "group.sheet",
    ) { dismiss ->
        Column(Modifier.verticalScroll(rememberScrollState()).testTag("screen.pickGroup")) {
            PbCard {
                choices.forEachIndexed { index, choice ->
                    PbSettingRow(
                        choice.title,
                        Modifier.testTag("group.row.${choice.id ?: "none"}"),
                        icon = choice.icon,
                        trailing = PbSettingTrailing.Check(choice.id == route.selected),
                        onClick = {
                            answer = RouteResult.Group(choice.id)
                            dismiss()
                        },
                        showDivider = index < choices.lastIndex,
                    )
                }
            }
        }
    }
}
