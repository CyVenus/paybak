package app.paybak.paybak.feature.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.recurring.RepeatCopy
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** The sheet's frequency chips; Custom repeats every other week. */
private enum class Choice(val label: Int, val frequency: Frequency?) {
    Never(R.string.insights_repeat_never, null),
    Weekly(R.string.insights_repeat_weekly, Frequency.Weekly),
    Monthly(R.string.insights_repeat_monthly, Frequency.Monthly),
    Yearly(R.string.insights_repeat_yearly, Frequency.Yearly),
    Custom(R.string.insights_repeat_custom, Frequency.Biweekly);

    companion object {
        fun of(frequency: Frequency?) = entries.first { it.frequency == frequency }
    }
}

/**
 * The `repeatRule` route sheet (recurringRepeat; insights §5.3): how an expense repeats. Never,
 * Weekly, Monthly, Yearly or Custom (every other week); the day it falls on; and "Amount changes
 * each time", which makes the rule add drafts that wait for an amount. The helper and the next date
 * follow the choice. ✕, Done and the scrim all hand the choice back (`RouteResult.Repeat`, null =
 * Never). Tagged `repeatSheet`, parts `repeatSheet.*`.
 */
@Composable
fun RepeatSheet(route: Route.RepeatRule) {
    val navigator = LocalMainNavigator.current
    val haptics = rememberHaptics()
    val today = LocalAppClock.current.today()
    val start = route.startDate ?: today
    var choice by rememberSaveable { mutableStateOf(Choice.of(route.current?.frequency)) }
    var anchor by rememberSaveable { mutableStateOf(route.current?.anchorDate ?: start) }
    var variable by rememberSaveable { mutableStateOf(route.current?.variable ?: false) }
    var picking by rememberSaveable { mutableStateOf(false) }
    val rule = choice.frequency?.let { RepeatRule(it, anchor, variable) }
    PbSheet(
        onDismiss = { navigator.complete(route.request.id, RouteResult.Repeat(rule)) },
        title = stringResource(R.string.insights_repeat_title),
        testTag = "repeatSheet",
    ) { dismiss ->
        Column(Modifier.testTag("screen.repeatRule")) {
            FrequencyChips(choice) {
                haptics.perform(HapticKind.Selection)
                // A yearly rule falls on the expense's own date.
                if (it == Choice.Yearly) anchor = start
                choice = it
                picking = false
            }
            if (rule != null) {
                PbCard(Modifier.padding(top = PbSpace.S16)) {
                    PbSettingRow(
                        RepeatCopy.anchorTitle(rule.frequency),
                        Modifier.testTag("repeatSheet.dayOfMonth"),
                        trailing =
                            if (rule.frequency == Frequency.Yearly) PbSettingTrailing.None
                            else PbSettingTrailing.Chevron,
                        onClick =
                            if (rule.frequency == Frequency.Yearly) null
                            else { { picking = !picking } },
                        icon = PbIcon.Calendar,
                        value = RepeatCopy.anchorValue(rule),
                    )
                    if (picking) {
                        AnchorPicker(rule) {
                            haptics.perform(HapticKind.Selection)
                            anchor = it
                            picking = false
                        }
                    }
                    PbSettingRow(
                        stringResource(R.string.insights_repeat_variable),
                        Modifier.testTag("repeatSheet.variable"),
                        trailing = PbSettingTrailing.Toggle(variable) { variable = it },
                        icon = PbIcon.Wallet,
                        showDivider = false,
                    )
                }
                Text(
                    RepeatCopy.helper(rule),
                    Modifier.padding(top = PbSpace.S12),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
                Row(
                    Modifier.padding(top = PbSpace.S12).testTag("repeatSheet.nextDraft"),
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PbIconImage(
                        PbIcon.Calendar,
                        contentDescription = null,
                        size = PbSize.IconSm,
                        tint = PbColors.Icon.Secondary,
                    )
                    Text(
                        RepeatCopy.nextLine(rule, RepeatCopy.next(rule, start, today)),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Primary,
                    )
                }
            }
            PbButton(
                stringResource(R.string.insights_done),
                onClick = dismiss,
                modifier = Modifier.fillMaxWidth().padding(top = PbSpace.S24).testTag("repeatSheet.done"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencyChips(selected: Choice, onSelect: (Choice) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        Choice.entries.forEach { choice ->
            PbCategoryChip(
                stringResource(choice.label),
                Modifier.testTag("repeatSheet.freq.${choice.name.lowercase()}"),
                selected = choice == selected,
                onClick = { onSelect(choice) },
            )
        }
    }
}

/**
 * The day under the day row: 1–31 for a monthly rule (a shorter month uses its last day), Monday
 * to Sunday for a weekly one. Picking moves the anchor to that day: in the first month from the
 * anchor's that has it, or in the anchor's week.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnchorPicker(rule: RepeatRule, onPick: (LocalDate) -> Unit) {
    val anchor = rule.anchorDate
    FlowRow(
        Modifier.fillMaxWidth().padding(start = PbSpace.S16, end = PbSpace.S16, bottom = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        if (rule.frequency == Frequency.Monthly) {
            (1..LAST_DAY).forEach { day ->
                PbCategoryChip(
                    day.toString(),
                    Modifier.testTag("repeatSheet.day.$day"),
                    selected = day == anchor.dayOfMonth,
                    onClick = {
                        val month =
                            generateSequence(anchor.withDayOfMonth(1)) { it.plusMonths(1) }
                                .first { it.lengthOfMonth() >= day }
                        onPick(month.withDayOfMonth(day))
                    },
                    onCard = true,
                )
            }
        } else {
            DayOfWeek.entries.forEach { weekday ->
                PbCategoryChip(
                    Dates.shortWeekday(weekday),
                    Modifier.testTag("repeatSheet.weekday.${weekday.value}"),
                    selected = weekday == anchor.dayOfWeek,
                    onClick = { onPick(anchor.with(TemporalAdjusters.nextOrSame(weekday))) },
                    onCard = true,
                )
            }
        }
    }
}

private const val LAST_DAY = 31
