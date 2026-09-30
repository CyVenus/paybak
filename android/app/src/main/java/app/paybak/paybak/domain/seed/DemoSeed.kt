package app.paybak.paybak.domain.seed

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.clearRecords
import app.paybak.paybak.domain.actions.closeProject
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.flagExpense
import app.paybak.paybak.domain.actions.markNotReceived
import app.paybak.paybak.domain.actions.tick
import app.paybak.paybak.domain.actions.updateComponent
import app.paybak.paybak.domain.actions.withEntitlement
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.Entitlement
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.Loan
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.Plan
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.domain.model.Pronoun
import app.paybak.paybak.domain.model.SavedPaymentMethod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** demo.json's `profile` block (domain.md §7.3), loaded into the ProfileStore. */
@Serializable
data class DemoProfile(
    val name: String,
    val avatar: DemoAvatar = DemoAvatar(),
    val currencyCode: String = "INR",
    val upiID: String = "",
    val username: String = "",
    val pronoun: Pronoun = Pronoun.They,
    val signInMethod: String? = null,
    val contact: String = "",
    val onboardingComplete: Boolean = true,
    val showPaymentToFriends: Boolean = true,
    val paymentMethods: List<SavedPaymentMethod> = emptyList(),
)

@Serializable data class DemoAvatar(val kind: String = "preset", val index: Int = 0)

/** A materialised demo: the ledger, the profile and the clock the scenarios left. */
data class DemoLoad(val ledger: Ledger, val profile: DemoProfile, val now: Instant)

/**
 * The demo dataset (`seed/demo.json`): materialises it for a load day and runs its named scenarios
 * through the store's actions (domain.md §7). A port of verify.py `load` and `apply_scenario`.
 */
class DemoSeed(json: String) {
    private val root: JsonObject = LedgerJson.parseToJsonElement(json).jsonObject
    private val scenarios: JsonObject = root["scenarios"]?.jsonObject ?: JsonObject(emptyMap())

    /** The Figma day and time the demo is drawn at (Wed 30 Sep 2026, 21:15). */
    val figmaDay: LocalDate =
        LocalDate.parse(
            root.getValue("anchor").jsonObject.getValue("figmaDate").jsonPrimitive.content
        )
    val figmaTime: LocalTime =
        LocalTime.parse(
            root.getValue("anchor").jsonObject.getValue("pinnedTime").jsonPrimitive.content
        )

    val scenarioNames: List<String>
        get() = scenarios.keys.toList()

    fun figmaNow(zone: ZoneId): Instant = figmaDay.atTime(figmaTime).atZone(zone).toInstant()

    /**
     * The base records loaded on [anchor] at [now], with `tick(now)` run from the seeded cursor,
     * then [scenarioNames] applied in order.
     */
    fun load(
        anchor: LocalDate,
        now: Instant,
        zone: ZoneId,
        scenarioNames: List<String> = emptyList(),
    ): DemoLoad {
        val resolved = SeedResolver.resolve(root, anchor, now, zone).jsonObject
        val profile = LedgerJson.decodeFromJsonElement<DemoProfile>(resolved.getValue("profile"))
        val ledgerJson = JsonObject(resolved - setOf("anchor", "profile", "scenarios"))
        val base = LedgerJson.decodeFromJsonElement<Ledger>(ledgerJson)
        val ctx = ActionContext(now, zone, profile.currencyCode)
        var state = Run(base.tick(ctx), now)
        for (name in scenarioNames) state = apply(state, name, anchor, zone, profile.currencyCode)
        return DemoLoad(state.ledger, profile, state.now)
    }

    /** Applies one more scenario to a loaded [ledger] (the debug menu's "Apply scenario…"). */
    fun applyScenario(
        ledger: Ledger,
        name: String,
        anchor: LocalDate,
        now: Instant,
        zone: ZoneId,
        defaultCurrency: String,
    ): Pair<Ledger, Instant> =
        apply(Run(ledger, now), name, anchor, zone, defaultCurrency).let { it.ledger to it.now }

    private data class Run(val ledger: Ledger, val now: Instant)

    private fun apply(
        run: Run,
        name: String,
        anchor: LocalDate,
        zone: ZoneId,
        currency: String,
    ): Run {
        val steps = requireNotNull(scenarios[name]) { "Unknown scenario $name" }.jsonArray
        var state = run
        for (element in steps) {
            val raw = element.jsonObject
            raw["use"]?.let {
                state = apply(state, it.jsonPrimitive.content, anchor, zone, currency)
                continue
            }
            val action = raw.getValue("action").jsonPrimitive.content
            // Scenario moments clamp to now like seeded ones; setClock is the step that moves time.
            val step =
                SeedResolver.resolve(
                        raw,
                        anchor,
                        if (action == "setClock") null else state.now,
                        zone,
                    )
                    .jsonObject
            val at = step["at"]?.jsonPrimitive?.contentOrNull?.let(Instant::parse)
            var ledger = state.ledger
            val cursor = ledger.scheduler.cursor
            if (at != null && (cursor == null || at > cursor)) {
                ledger = ledger.tick(ActionContext(at, zone, currency))
            }
            val ctx = ActionContext(at ?: state.now, zone, currency)
            state =
                when (action) {
                    "setClock" -> Run(ledger, at!!)
                    else -> Run(step(ledger, action, step, ctx, state.now), state.now)
                }
        }
        return state
    }

    private fun step(
        ledger: Ledger,
        action: String,
        step: JsonObject,
        ctx: ActionContext,
        now: Instant,
    ): Ledger {
        fun text(key: String) = step.getValue(key).jsonPrimitive.content
        return when (action) {
            "recordPayment" -> {
                val payment = decode<ScenarioPayment>(step.getValue("payment"))
                ledger.copy(
                    payments =
                        ledger.payments +
                            Payment(
                                id = payment.id,
                                fromId = payment.fromId,
                                toId = payment.toId,
                                amount = payment.amount,
                                currency = payment.currency,
                                method = payment.method,
                                date = LocalDate.parse(payment.date),
                                groupId = payment.groupId,
                                loanId = payment.loanId,
                                expenseId = payment.expenseId,
                                status = PaymentStatus.Pending,
                                recordedBy = payment.recordedBy,
                                createdAt = ctx.at,
                            )
                )
            }
            "confirmPayment" -> ledger.confirmPayment(text("paymentId"), ctx)
            "markNotReceived" -> ledger.markNotReceived(text("paymentId"), text("note"), ctx)
            "flagExpense" -> ledger.flagExpense(text("expenseId"), text("by"), text("note"), ctx)
            "addLoan" -> {
                val loan = decode<Loan>(step.getValue("loan").withCreation(ctx))
                ledger.copy(loans = ledger.loans + loan)
            }
            "updateComponent" ->
                ledger.updateComponent(
                    text("componentId"),
                    ctx,
                    status = decode<ComponentStatus>(step.getValue("status")),
                    actualCost = step.getValue("actualCost").jsonPrimitive.content.toLong(),
                    paidBy = text("paidBy"),
                )
            "closeProject" -> ledger.closeProject(text("projectId"), ctx)
            "createGroup" -> {
                val group = decode<Group>(step.getValue("group").withCreation(ctx))
                ledger.copy(groups = ledger.groups + group)
            }
            "setEntitlement" ->
                ledger.withEntitlement(
                    Entitlement(
                        plan = decode<Plan>(step.getValue("plan")),
                        period =
                            step["period"]?.jsonPrimitive?.contentOrNull?.let {
                                decode<PlanPeriod>(step.getValue("period"))
                            },
                        trialEndsAt =
                            step["trialEndsAt"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?.let(LocalDate::parse),
                        since = now,
                    )
                )
            "clearLedger" -> ledger.clearRecords()
            else -> error("Unknown scenario action $action")
        }
    }

    private inline fun <reified T> decode(element: JsonElement): T =
        LedgerJson.decodeFromJsonElement(element)

    /** A scenario record plus `createdAt` = the step's moment and `createdBy` = you. */
    private fun JsonElement.withCreation(ctx: ActionContext): JsonObject =
        JsonObject(
            jsonObject +
                mapOf(
                    "createdAt" to JsonPrimitive(ctx.at.toString()),
                    "createdBy" to JsonPrimitive(ME),
                )
        )

    @Serializable
    private data class ScenarioPayment(
        val id: String,
        val fromId: String,
        val toId: String,
        val amount: Long,
        val currency: String,
        val method: PaymentMethod,
        val date: String,
        val groupId: String? = null,
        val loanId: String? = null,
        val expenseId: String? = null,
        val recordedBy: String,
    )
}
