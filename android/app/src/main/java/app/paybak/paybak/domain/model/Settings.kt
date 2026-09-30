package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ledger settings and the Pro entitlement (domain.md §1.14). */
@Serializable
data class Settings(
    val keepBalancesPerCurrency: Boolean = false,
    val push: PushSettings = PushSettings(),
    val reminderSchedule: ReminderSchedule = ReminderSchedule(),
    val discovery: Discovery = Discovery(),
    val entitlement: Entitlement = Entitlement(),
)

@Serializable
data class PushSettings(
    val addedToExpense: Boolean = true,
    val paymentsToConfirm: Boolean = true,
    val reminders: Boolean = true,
    val overdueAlerts: Boolean = true,
    val projectUpdates: Boolean = true,
    val monthlySummary: Boolean = true,
)

/** When automatic reminders fire (domain.md §10); [time] is local `HH:mm`. */
@Serializable
data class ReminderSchedule(
    val twoDaysBefore: Boolean = true,
    val onDueDate: Boolean = true,
    val overdueEvery3Days: Boolean = true,
    val time: String = "21:00",
)

@Serializable
data class Discovery(val findMeByContact: Boolean = true, val contactsSync: Boolean = true)

@Serializable
enum class Plan {
    @SerialName("free") Free,
    @SerialName("pro") Pro,
}

@Serializable
enum class PlanPeriod {
    @SerialName("yearly") Yearly,
    @SerialName("monthly") Monthly,
}

/** The (simulated) subscription. A trial is Pro until [trialEndsAt]. */
@Serializable
data class Entitlement(
    val plan: Plan = Plan.Free,
    val period: PlanPeriod? = null,
    val trialEndsAt: Day? = null,
    val since: Moment? = null,
) {
    val isPro: Boolean
        get() = plan == Plan.Pro
}
