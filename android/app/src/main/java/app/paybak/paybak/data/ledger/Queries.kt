package app.paybak.paybak.data.ledger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import app.paybak.paybak.domain.LedgerSnapshot
import app.paybak.paybak.domain.calc.LedgerView

/**
 * Reading the ledger from Compose. The read models are in [LedgerSnapshot]; parametrised queries
 * (`groupSheet(id)`, `friendPage(id)`, `projectReport(id)`, `loanDetail(id)`, `insights(month)`,
 * `recurring(groupId)` …) are functions of [LedgerSnapshot.view] in `domain/calc`.
 */
@Composable
fun LedgerRepository.collectSnapshot(): State<LedgerSnapshot> = snapshot.collectAsState()

/** The current calculator, for one-off queries outside composition. */
val LedgerRepository.view: LedgerView
    get() = snapshot.value.view

/** Pro is on (a trial counts). */
val LedgerRepository.isPro: Boolean
    get() = snapshot.value.isPro
