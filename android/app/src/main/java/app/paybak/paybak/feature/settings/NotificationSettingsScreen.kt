package app.paybak.paybak.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.updateSettings
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.PushSettings
import app.paybak.paybak.domain.model.ReminderSchedule
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.icons.PbIcon

/** One push-type switch: its title, test id and field (screens-settings §7). */
private class PushType(
    val title: Int,
    val id: String,
    val isOn: (PushSettings) -> Boolean,
    val set: (PushSettings, Boolean) -> PushSettings,
)

private val PushTypes =
    listOf(
        PushType(R.string.settings_push_added, "addedToExpense", { it.addedToExpense }) { p, on ->
            p.copy(addedToExpense = on)
        },
        PushType(R.string.settings_push_confirm, "paymentsToConfirm", { it.paymentsToConfirm }) {
            p,
            on ->
            p.copy(paymentsToConfirm = on)
        },
        PushType(R.string.settings_push_reminders, "reminders", { it.reminders }) { p, on ->
            p.copy(reminders = on)
        },
        PushType(R.string.settings_push_overdue, "overdueAlerts", { it.overdueAlerts }) { p, on ->
            p.copy(overdueAlerts = on)
        },
        PushType(R.string.settings_push_projects, "projectUpdates", { it.projectUpdates }) { p, on ->
            p.copy(projectUpdates = on)
        },
        PushType(R.string.settings_push_summary, "monthlySummary", { it.monthlySummary }) { p, on ->
            p.copy(monthlySummary = on)
        },
    )

/** One reminder-schedule check (independent checks, all on by default). */
private class ScheduleStep(
    val title: Int,
    val id: String,
    val isOn: (ReminderSchedule) -> Boolean,
    val set: (ReminderSchedule, Boolean) -> ReminderSchedule,
)

private val ScheduleSteps =
    listOf(
        ScheduleStep(R.string.settings_schedule_two_days, "twoDaysBefore", { it.twoDaysBefore }) {
            s,
            on ->
            s.copy(twoDaysBefore = on)
        },
        ScheduleStep(R.string.settings_schedule_due_date, "onDueDate", { it.onDueDate }) { s, on ->
            s.copy(onDueDate = on)
        },
        ScheduleStep(
            R.string.settings_schedule_overdue,
            "overdueEvery3Days",
            { it.overdueEvery3Days },
        ) { s, on ->
            s.copy(overdueEvery3Days = on)
        },
    )

/**
 * The `settingsNotifications` route (screens-settings §7): a switch per push type, the default
 * reminder schedule for anything with a due date (the scheduler and the local notifications follow
 * it), and the friends muted from automatic reminders.
 */
@Composable
fun NotificationSettingsScreen(route: Route.SettingsNotifications) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot = ledger.collectSnapshot().value
    val settings = snapshot.ledger.settings
    val muted = snapshot.ledger.people.count { it.remindersMuted }

    SettingsPage(route.info.id, stringResource(R.string.settings_notifications_title)) {
        SettingsSection(stringResource(R.string.settings_notifications_push)) {
            PbCard {
                PushTypes.forEach { type ->
                    ToggleRow(
                        stringResource(type.title),
                        checked = type.isOn(settings.push),
                        onCheckedChange = { on ->
                            ledger.updateSettings { it.copy(push = type.set(it.push, on)) }
                        },
                        modifier = Modifier.testTag("settingsNotifications.push.${type.id}"),
                        showDivider = type != PushTypes.last(),
                    )
                }
            }
        }
        SettingsSection(
            stringResource(R.string.settings_schedule),
            footer = stringResource(R.string.settings_schedule_footer),
        ) {
            PbCard {
                ScheduleSteps.forEach { step ->
                    val on = step.isOn(settings.reminderSchedule)
                    PbSettingRow(
                        stringResource(step.title),
                        modifier = Modifier.testTag("settingsNotifications.schedule.${step.id}"),
                        trailing = PbSettingTrailing.Check(on),
                        onClick = {
                            ledger.updateSettings {
                                it.copy(reminderSchedule = step.set(it.reminderSchedule, !on))
                            }
                        },
                        showDivider = step != ScheduleSteps.last(),
                    )
                }
            }
        }
        SettingsSection(title = null, footer = stringResource(R.string.settings_muted_footer)) {
            PbCard {
                PbSettingRow(
                    stringResource(R.string.settings_muted),
                    modifier = Modifier.testTag("settingsNotifications.muted"),
                    onClick = { navigator.open(Route.MutedFriends) },
                    icon = PbIcon.Bell,
                    value =
                        if (muted == 0) stringResource(R.string.settings_muted_none)
                        else pluralStringResource(R.plurals.settings_muted_count, muted, muted),
                    showDivider = false,
                )
            }
        }
    }
}
