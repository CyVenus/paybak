package app.paybak.paybak.debug.menu

import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.data.ledger.lanes.archiveIfSettled
import app.paybak.paybak.debug.DemoData
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.ProjectStatus

private const val DRONE = "pj-drone"
private const val GPS = "c-drone-gps"
private const val DEV = "p-dev"

/**
 * The debug menu's Projects section, owned by lane B (app-architecture §3.10, screens-projects
 * §11): the other members' side of Build a Drone, so Closed can walk to Archived on one device.
 * Each action does nothing when the demo isn't in that state.
 */
internal val ProjectsDebugActions: List<DebugAction> =
    listOf(
        DebugAction("Dev buys the GPS module", "₹9,500: Build a Drone goes over budget") {
            if (app.ledger.ledger.value.component(GPS)?.status == ComponentStatus.Planned) {
                DemoData.apply(app, "devBuysGps")
            }
        },
        DebugAction("Close Build a Drone") {
            if (app.ledger.ledger.value.group(DRONE)?.project?.status == ProjectStatus.Active) {
                DemoData.apply(app, "closeDrone")
            }
        },
        payDev("p-rohan", "Rohan", "₹8,500"),
        payDev("p-priya", "Priya", "₹4,000"),
        DebugAction("Confirm pending project payments", "As each receiver would") {
            val ledger = app.ledger.ledger.value
            val pending =
                ledger.payments.filter {
                    it.status == PaymentStatus.Pending &&
                        it.groupId?.let(ledger::group)?.isProject == true
                }
            pending.forEach { app.ledger.confirmPayment(it.id) }
            pending.mapNotNull { it.groupId }.distinct().forEach(app.ledger::archiveIfSettled)
        },
    )

/**
 * [name] records paying Dev what Build a Drone's plan says they owe him, pending until Dev
 * confirms; once is enough.
 */
private fun payDev(personId: String, name: String, demoAmount: String) =
    DebugAction("$name pays Dev $demoAmount", "In Build a Drone, for Dev to confirm") {
        val waiting =
            app.ledger.ledger.value.payments.any {
                it.groupId == DRONE && it.fromId == personId && it.status == PaymentStatus.Pending
            }
        if (waiting) return@DebugAction
        val transfer =
            app.ledger.snapshot.value.view.groupPlan(DRONE).firstOrNull {
                it.debtorId == personId && it.creditorId == DEV
            } ?: return@DebugAction
        app.ledger.recordPayment(
            PaymentDraft(personId, DEV, transfer.amount, groupId = DRONE, recordedBy = personId)
        )
    }
