package app.paybak.paybak.debug.menu

import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.debug.DemoData
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.navigation.MainState

/**
 * The debug menu's Home section (lane D): switch Home between its designed states by loading the
 * demo at the Figma date (flow.md's "switch Home state"), and stack a second claim to check the
 * several-claims proposal (home-v2 §3.11).
 */
internal val HomeDebugActions: List<DebugAction> =
    listOf(
        homeState("Home: First day", "An empty account", listOf("empty")),
        homeState("Home: Active", "The demo without Esha’s claim", emptyList()),
        homeState("Home: Confirm payment", "The demo with Esha’s claim", DemoData.Default),
        homeState("Home: All settled", "Everyone has paid", listOf("allSettled")),
        DebugAction("Dev says he paid ₹700", "Another pending claim, for Dinner at Olive Garden") {
            val claim =
                PaymentDraft(
                    DEV,
                    ME,
                    70_000,
                    method = PaymentMethod.Upi,
                    expenseId = OLIVE.takeIf { app.ledger.ledger.value.expense(it) != null },
                    recordedBy = DEV,
                )
            app.notifications.postPaymentToConfirm(app.ledger.recordPayment(claim))
        },
    )

private const val DEV = "p-dev"
private const val OLIVE = "e-olive"

private fun homeState(title: String, subtitle: String, scenarios: List<String>) =
    DebugAction(title, subtitle) {
        DemoData.load(app, scenarios)
        navigator.reset(MainState())
    }
