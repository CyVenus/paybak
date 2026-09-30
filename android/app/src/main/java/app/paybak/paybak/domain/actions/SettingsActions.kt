package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.calc.LedgerScheduler
import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.Plan
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.domain.model.Settings

/** Settings, the simulated Pro entitlement (domain.md §1.14), the scheduler and clearing. */
const val TRIAL_DAYS = 7L

/** Starts the free trial (yearly only): Pro until today + 7 days. */
fun Ledger.startTrial(ctx: ActionContext): Ledger =
    withEntitlement(
        Entitlement(Plan.Pro, PlanPeriod.Yearly, ctx.today.plusDays(TRIAL_DAYS), ctx.at)
    )

/** The mock purchase: Pro at once, no trial. */
fun Ledger.subscribe(period: PlanPeriod, ctx: ActionContext): Ledger =
    withEntitlement(Entitlement(Plan.Pro, period, null, ctx.at))

/** Restore purchases: there is no store, so the saved entitlement is all there is. */
fun Ledger.restorePurchases(): Ledger = this

/** The debug toggle. */
fun Ledger.setPro(pro: Boolean, ctx: ActionContext): Ledger =
    withEntitlement(
        if (pro) Entitlement(Plan.Pro, PlanPeriod.Yearly, null, ctx.at) else Entitlement()
    )

fun Ledger.withEntitlement(entitlement: Entitlement): Ledger =
    copy(settings = settings.copy(entitlement = entitlement))

fun Ledger.updateSettings(change: (Settings) -> Settings): Ledger =
    copy(settings = change(settings))

/** Runs the scheduler up to [ActionContext.at] (§10). */
fun Ledger.tick(ctx: ActionContext): Ledger =
    LedgerScheduler.tick(this, ctx.at, ctx.defaultCurrency, ctx.zone)

/**
 * A new, empty account with the same settings (the `empty` scenario and "Start an empty account"):
 * every record goes, the settings, rotation and scheduler stay.
 */
fun Ledger.clearRecords(): Ledger =
    Ledger(settings = settings, rotation = rotation, scheduler = scheduler)
