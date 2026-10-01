package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.Itemized
import app.paybak.paybak.domain.model.ItemizedItem
import app.paybak.paybak.domain.model.ItemizedLine
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.SplitRow
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import java.time.LocalDate

/** The Leopold Cafe receipt read and assigned (insights §4.5): ₹2,300 itemized among 3 people. */
private val leopoldDraft =
    ExpenseDraft(
        title = "Leopold Cafe",
        amount = 230_000,
        category = Category.Food.id,
        split = Split(SplitMode.Itemized, listOf(ME, "p-esha", "p-dev").map { SplitRow(it) }),
        itemized =
            Itemized(
                items =
                    listOf(
                        ItemizedItem("Chicken biryani", 43_000, listOf("p-dev")),
                        ItemizedItem("Paneer tikka", 37_000, listOf("p-esha")),
                        ItemizedItem("Fish and chips", 45_000, listOf(ME)),
                        ItemizedItem("Chocolate brownie", 24_000, listOf(ME)),
                        ItemizedItem("Masala fries", 24_000, listOf(ME, "p-esha", "p-dev")),
                        ItemizedItem("Fresh lime soda ×3", 27_000, listOf(ME, "p-esha", "p-dev")),
                    ),
                lines = listOf(ItemizedLine("GST 5%", 10_000), ItemizedLine("Tip 10%", 20_000)),
                subtotal = 200_000,
            ),
    )

/** Cooking gas repeating monthly on the 28th, waiting for its amount each time (insights §5.3). */
private val cookingGasRepeat = RepeatRule(Frequency.Monthly, LocalDate.of(2026, 9, 28), variable = true)

/** The Cooking gas draft of Flat 302 the Repeat sheet opens over. */
private val cookingGasDraft =
    ExpenseDraft.equal(listOf(ME, "p-meera", "p-kabir"))
        .copy(
            title = "Cooking gas",
            groupId = "g-flat302",
            category = Category.Bills.id,
            repeat = cookingGasRepeat,
        )

/** Insights and AI (lane C, M9; app-architecture §1.9). */
internal val InsightsScenarios: Map<String, Scenario> =
    mapOf(
        "insightsSeptember" to
            Scenario(demo().pro(), tab = Tab.Activity, activitySegment = ActivitySegment.Insights),
        "insightsScrolled" to
            Scenario(demo().pro(), tab = Tab.Activity, activitySegment = ActivitySegment.Insights),
        "insightsLocked" to
            Scenario(demo(), tab = Tab.Activity, activitySegment = ActivitySegment.Insights),
        "askStart" to Scenario(demo().pro(), stack = listOf(Route.Ask)),
        "askAnswer" to Scenario(demo().pro(), stack = listOf(Route.Ask)),
        "askConfirm" to Scenario(demo().pro(), stack = listOf(Route.Ask)),
        "scanCamera" to
            Scenario(
                demo().pro(),
                stack = listOf(Route.AddExpense(), Route.ScanReceipt(DebugRequest)),
            ),
        "scanReview" to
            Scenario(
                demo().pro(),
                stack = listOf(Route.AddExpense(), Route.ScanReceipt(DebugRequest)),
            ),
        "scanAssign" to
            Scenario(
                demo().pro(),
                stack = listOf(Route.AddExpense(), Route.ScanReceipt(DebugRequest)),
            ),
        "scanAddExpense" to
            Scenario(
                demo().pro(),
                stack =
                    listOf(
                        Route.AddExpense(AddExpenseArgs(draft = leopoldDraft, focusAmount = false))
                    ),
            ),
        "recurringFlat302" to
            Scenario(
                demo().pro(),
                tab = Tab.Groups,
                stack = listOf(Route.Group("g-flat302"), Route.Recurring("g-flat302")),
            ),
        "recurringRepeat" to
            Scenario(
                demo().pro(),
                stack =
                    listOf(
                        Route.AddExpense(
                            AddExpenseArgs(draft = cookingGasDraft, focusAmount = false)
                        )
                    ),
                sheet =
                    Route.RepeatRule(
                        DebugRequest,
                        current = cookingGasRepeat,
                        startDate = LocalDate.of(2026, 9, 28),
                    ),
            ),
        "recurringEnterAmount" to
            Scenario(demo().pro(), stack = listOf(Route.EnterDraftAmount("d-gas-09"))),
    )
