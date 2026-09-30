package app.paybak.paybak.feature.pro

import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.Plan
import app.paybak.paybak.domain.model.PlanPeriod
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** What the Welcome says about a Pro member (screens-settings §3, §12.1). */
class ProStatusTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val today = LocalDate.of(2026, 9, 30)
    private val since = ZonedDateTime.of(2026, 9, 30, 21, 15, 0, 0, zone).toInstant()

    @Test
    fun theFreePlanHasNoStatus() {
        assertNull(Entitlement().status(today, zone))
    }

    @Test
    fun aTrialEndsAWeekAfterItStarts() {
        val trial = Entitlement(Plan.Pro, PlanPeriod.Yearly, today.plusDays(7), since)
        assertEquals(ProStatus.TrialEnds(LocalDate.of(2026, 10, 7)), trial.status(today, zone))
    }

    @Test
    fun subscriptionsRenewAfterTheirPeriod() {
        val monthly = Entitlement(Plan.Pro, PlanPeriod.Monthly, null, since)
        assertEquals(
            ProStatus.Renews(LocalDate.of(2026, 10, 30), PlanPeriod.Monthly),
            monthly.status(today, zone),
        )
        val yearly = Entitlement(Plan.Pro, PlanPeriod.Yearly, null, since)
        assertEquals(
            ProStatus.Renews(LocalDate.of(2027, 9, 30), PlanPeriod.Yearly),
            yearly.status(today, zone),
        )
    }
}
