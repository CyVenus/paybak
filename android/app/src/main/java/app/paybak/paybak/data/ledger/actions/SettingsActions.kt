package app.paybak.paybak.data.ledger.actions

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.clearRecords
import app.paybak.paybak.domain.actions.restorePurchases
import app.paybak.paybak.domain.actions.setPro
import app.paybak.paybak.domain.actions.startTrial
import app.paybak.paybak.domain.actions.subscribe
import app.paybak.paybak.domain.actions.updateSettings
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.domain.model.Settings

/** Settings and the simulated Pro entitlement (domain.md §1.14, §11). */
fun LedgerRepository.startTrial() = mutate { it.startTrial(context()) }

fun LedgerRepository.subscribe(period: PlanPeriod) = mutate { it.subscribe(period, context()) }

fun LedgerRepository.restorePurchases() = mutate { it.restorePurchases() }

fun LedgerRepository.setPro(pro: Boolean) = mutate { it.setPro(pro, context()) }

fun LedgerRepository.updateSettings(change: (Settings) -> Settings) = mutate {
    it.updateSettings(change)
}

/** Every record goes; settings stay (a new, empty account). */
fun LedgerRepository.clearRecords() = mutate { it.clearRecords() }

/** Reset onboarding and Delete account: nothing is left, not even the settings. */
fun LedgerRepository.clear() = replace(Ledger())
