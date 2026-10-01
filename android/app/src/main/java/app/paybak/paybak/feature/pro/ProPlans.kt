package app.paybak.paybak.feature.pro

import androidx.annotation.StringRes
import app.paybak.paybak.R
import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.ui.icons.PbIcon
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
 * Where a Pro member stands, for the Welcome line (§3): a yearly trial ends on a day; a
 * subscription renews a whole number of years or months after it started (or its trial ended).
 */
internal sealed interface ProStatus {
    val day: LocalDate

    data class TrialEnds(override val day: LocalDate) : ProStatus

    data class Renews(override val day: LocalDate, val period: PlanPeriod) : ProStatus
}

/**
 * The status of this entitlement, null on the free plan: the trial's end while it runs, otherwise
 * the next renewal after [today]. A subscription counts from its trial's end, else from [since];
 * [today] stands in for a missing start.
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
