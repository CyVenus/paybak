package app.paybak.paybak.feature.pro

import androidx.annotation.StringRes
import app.paybak.paybak.R
import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.ui.icons.PbIcon
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.PeriodType
import java.time.LocalDate
import java.time.ZoneId

/** What Pro unlocks, in the paywall's order (screens-settings §2). */
internal enum class ProFeature(
    val icon: PbIcon,
    @param:StringRes val title: Int,
    @param:StringRes val detail: Int,
) {
    Assistant(PbIcon.Sparkles, R.string.settings_pro_ai, R.string.settings_pro_ai_detail),
    Scanning(PbIcon.Camera, R.string.settings_pro_scan, R.string.settings_pro_scan_detail),
    Insights(PbIcon.Chart, R.string.settings_pro_insights, R.string.settings_pro_insights_detail),
    Recurring(
        PbIcon.Repeat,
        R.string.settings_pro_recurring,
        R.string.settings_pro_recurring_detail,
    ),
    Export(PbIcon.Download, R.string.settings_pro_export, R.string.settings_pro_export_detail),
}

/**
 * Where a Pro member stands, for the Welcome line (§3): a trial ends on a day; a subscription
 * renews on a day ([period] null when the store doesn't say which plan); a cancelled one ends.
 */
internal sealed interface ProStatus {
    val day: LocalDate

    data class TrialEnds(override val day: LocalDate) : ProStatus

    data class Renews(override val day: LocalDate, val period: PlanPeriod? = null) : ProStatus

    data class Ends(override val day: LocalDate) : ProStatus
}

/**
 * The status of the store's Pro entitlement: its trial's end, its next renewal, or the day it
 * lapses once cancelled. Null when it never expires.
 */
internal fun EntitlementInfo.status(zone: ZoneId): ProStatus? {
    val day = expirationDate?.toInstant()?.atZone(zone)?.toLocalDate() ?: return null
    return when {
        periodType == PeriodType.TRIAL -> ProStatus.TrialEnds(day)
        willRenew -> ProStatus.Renews(day)
        else -> ProStatus.Ends(day)
    }
}

/**
 * The status of this simulated entitlement (debug builds), null on the free plan: the trial's end
 * while it runs, otherwise the next renewal after [today]. A subscription counts from its trial's
 * end, else from [since]; [today] stands in for a missing start.
 */
internal fun Entitlement.status(today: LocalDate, zone: ZoneId): ProStatus? {
    if (!isPro) return null
    trialEndsAt?.takeIf { it >= today }?.let { return ProStatus.TrialEnds(it) }
    val plan = if (period == PlanPeriod.Monthly) PlanPeriod.Monthly else PlanPeriod.Yearly
    val step = if (plan == PlanPeriod.Monthly) 1L else 12L
    val start = trialEndsAt ?: since?.atZone(zone)?.toLocalDate() ?: today
    var months = 0L
    while (start.plusMonths(months) <= today) months += step
    return ProStatus.Renews(start.plusMonths(months), plan)
}
