package app.paybak.paybak.debug.menu

import android.content.Intent
import app.paybak.paybak.MainActivity
import app.paybak.paybak.data.ledger.actions.addComment
import app.paybak.paybak.data.ledger.actions.clearRecords
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.data.ledger.actions.flagExpense
import app.paybak.paybak.data.ledger.actions.markNotReceived
import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.data.ledger.actions.removeFlag
import app.paybak.paybak.data.ledger.actions.setPro
import app.paybak.paybak.debug.DebugClock
import app.paybak.paybak.debug.DebugLaunch
import app.paybak.paybak.debug.DemoData
import app.paybak.paybak.debug.ScreenIds
import app.paybak.paybak.domain.actions.addExpense
import app.paybak.paybak.domain.actions.withEntitlement
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxParams
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.navigation.MainState
import java.time.Duration

private const val ESHA = "p-esha"
private const val PRIYA = "p-priya"
private const val MEERA = "p-meera"
private const val SEAFOOD = "e-goa-seafood"
private const val VILLA = "e-goa-villa"

/** Restarts the app with launch [extras], exactly as the adb hooks would. */
fun DebugContext.relaunch(vararg extras: Pair<String, Any>) {
    val intent =
        Intent(activity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    extras.forEach { (key, value) ->
        when (value) {
            is Boolean -> intent.putExtra(key, value)
            else -> intent.putExtra(key, value.toString())
        }
    }
    activity.startActivity(intent)
}

/** M2's sections: data, clock, Pro and the friend's side (app-architecture §3.10). */
internal fun coreSections(context: DebugContext): List<DebugSection> {
    val app = context.app
    val ledger = app.ledger.ledger.value
    val pending =
        ledger.payments.lastOrNull { it.fromId == ME && it.status == PaymentStatus.Pending }
    val payee = pending?.let { ledger.person(it.toId)?.firstName } ?: "Payee"
    return listOf(
        DebugSection(
            "Data",
            listOf(
                DebugAction(
                    "Load demo data at the Figma date",
                    "Wed 30 Sep 2026, 9:15 pm, with Esha’s claim",
                ) {
                    DemoData.load(app, DemoData.Default)
                    navigator.reset(MainState())
                },
                DebugAction(
                    "Load demo data around today",
                    "The same story on today’s dates, real clock",
                ) {
                    DemoData.load(app, DemoData.Default, atFigmaDate = false)
                    navigator.reset(MainState())
                },
                DebugAction("Start an empty account", "Keeps the profile") {
                    app.ledger.clearRecords()
                    navigator.reset(MainState())
                },
                DebugAction("Reset onboarding", "Clears the profile and the ledger") {
                    relaunch("resetOnboarding" to true)
                },
            ),
        ),
        DebugSection(
            "Clock",
            listOf(
                DebugAction("Pin to 30 Sep 2026 21:15") {
                    DebugClock.pin(app, DemoData.seed(app).figmaNow(app.clock.zone))
                },
                DebugAction("Use real time") { DebugClock.pin(app, null) },
                DebugAction("+1 day") {
                    DebugClock.pin(app, app.clock.now().plus(Duration.ofDays(1)))
                },
                DebugAction("Run tick now") { app.ledger.tick() },
            ),
        ),
        DebugSection(
            "Pro",
            listOf(
                DebugAction(
                    "Toggle Pro",
                    "Now: ${if (app.ledger.snapshot.value.isPro) "Pro" else "Free"}",
                ) {
                    app.ledger.setPro(!app.ledger.snapshot.value.isPro)
                },
                DebugAction("Clear the entitlement") {
                    app.ledger.mutate { it.withEntitlement(Entitlement()) }
                },
            ),
        ),
        DebugSection(
            "Friend’s side",
            listOfNotNull(
                DebugAction(
                        "Esha says she paid ₹700",
                        "A pending claim for Dinner at Olive Garden",
                    ) {
                        val claim =
                            PaymentDraft(
                                ESHA,
                                ME,
                                70_000,
                                method = PaymentMethod.Upi,
                                expenseId = "e-olive".takeIf { ledger.expense(it) != null },
                                recordedBy = ESHA,
                            )
                        app.notifications.postPaymentToConfirm(app.ledger.recordPayment(claim))
                    }
                    .takeIf { ledger.person(ESHA) != null },
                pending?.let {
                    DebugAction(
                        "$payee confirms my latest pending payment",
                        Money.format(it.amount, it.currency),
                    ) {
                        app.ledger.confirmPayment(it.id)
                    }
                },
                pending?.let {
                    DebugAction("$payee says Not received") {
                        app.ledger.markNotReceived(
                            it.id,
                            "Hi, I haven’t received it yet. Could you check?",
                        )
                    }
                },
                DebugAction("Esha flags Seafood dinner") {
                        app.ledger.flagExpense(
                            SEAFOOD,
                            "I left before dessert. Can we check the bill?",
                            by = ESHA,
                        )
                    }
                    .takeIf {
                        ledger.expense(SEAFOOD)?.flag == null && ledger.expense(SEAFOOD) != null
                    },
                DebugAction("Esha removes her flag") { app.ledger.removeFlag(SEAFOOD, by = ESHA) }
                    .takeIf { ledger.expense(SEAFOOD)?.flag != null },
                DebugAction("Priya comments on Villa") {
                        app.ledger.addComment(VILLA, "Thanks, that works for me.", by = PRIYA)
                    }
                    .takeIf { ledger.expense(VILLA) != null },
                DebugAction("Meera adds an expense in Flat 302", "Groceries, ₹900") {
                        meeraAddsGroceries()
                    }
                    .takeIf { ledger.group("g-flat302") != null },
            ),
        ),
        DebugSection(
            "Scenarios",
            DemoData.seed(app).scenarioNames.map { name ->
                DebugAction("Apply $name") { DemoData.apply(app, name) }
            },
        ),
        DebugSection(
            "Load screen",
            listOf(
                DebugAction("Component gallery") {
                    relaunch("startScreen" to DebugLaunch.GALLERY_ID)
                }
            ) + ScreenIds.all.map { id -> DebugAction(id) { relaunch("startScreen" to id) } },
        ),
    )
}

/** Meera's own expense in Flat 302, with the inbox item a real sync would bring. */
private fun DebugContext.meeraAddsGroceries() {
    val group = app.ledger.ledger.value.group("g-flat302") ?: return
    val amount = 90_000L
    app.ledger.mutate { ledger ->
        val draft =
            ExpenseDraft.equal(group.memberIds)
                .copy(
                    title = "Groceries",
                    amount = amount,
                    groupId = group.id,
                    category = Category.Food.id,
                    payers = listOf(Payer(MEERA, amount)),
                )
        val (next, id) = ledger.addExpense(draft, app.ledger.context(), by = MEERA)
        val share = next.expense(id)!!.shareOf(ME)
        next.copy(
            inbox =
                next.inbox +
                    InboxItem(
                        id = "n-new-$id",
                        type = InboxType.NewExpenseInGroup,
                        createdAt = app.clock.now(),
                        params =
                            InboxParams(
                                expenseId = id,
                                groupId = group.id,
                                actorId = MEERA,
                                title = "Groceries",
                                total = amount,
                                share = share,
                                currency = group.currency,
                            ),
                    )
        )
    }
}
