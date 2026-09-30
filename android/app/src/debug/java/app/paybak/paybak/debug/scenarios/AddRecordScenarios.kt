package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.NewGroupMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import java.time.LocalDate

/** The picker request the debug start screens open pickers with. */
internal val DebugRequest = PickRequest("debug")

private val addExpense = Route.AddExpense()

/** The Olive Garden bill as 06-02 shows it, ready to save (add-expense §12). */
private val oliveGarden =
    ExpenseDraft.equal(listOf(ME, "p-priya", "p-esha", "p-dev"))
        .copy(
            title = "Dinner at Olive Garden",
            amount = 280_000,
            category = Category.Food.id,
            dueDate = LocalDate.of(2026, 10, 4),
        )

private val addExpenseFilled =
    Route.AddExpense(AddExpenseArgs(draft = oliveGarden, focusAmount = false))

/** Add & Record (lane A, M3; app-architecture §1.3). */
internal val AddRecordScenarios: Map<String, Scenario> =
    mapOf(
        "addExpenseEmpty" to Scenario(demo(), stack = listOf(addExpense)),
        "addExpenseFilled" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpenseSplitWith" to
            Scenario(
                demo(),
                stack =
                    listOf(
                        addExpenseFilled,
                        Route.PickPeople(
                            DebugRequest,
                            selected = listOf(ME, "p-priya", "p-esha", "p-dev"),
                        ),
                    ),
            ),
        "addExpensePaidBy" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpensePayers" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpenseSplitEqually" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpenseSplitExactError" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpenseCategory" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "addExpenseCurrency" to
            Scenario(
                demo(),
                stack = listOf(addExpenseFilled),
                sheet = Route.PickCurrency(DebugRequest, selected = "INR"),
            ),
        "addExpenseDueDate" to
            Scenario(
                demo(),
                stack = listOf(addExpenseFilled),
                sheet =
                    Route.PickDate(
                        DebugRequest,
                        DateKind.DueDate,
                        selected = LocalDate.of(2026, 10, 4),
                        allowsNone = true,
                    ),
            ),
        "addExpenseDate" to
            Scenario(
                demo(),
                stack = listOf(addExpenseFilled),
                sheet =
                    Route.PickDate(
                        DebugRequest,
                        DateKind.Date,
                        selected = LocalDate.of(2026, 9, 30),
                    ),
            ),
        "addExpenseDiscard" to Scenario(demo(), stack = listOf(addExpenseFilled)),
        "expenseAdded" to
            Scenario(
                demo(),
                stack = listOf(Route.Expense("e-olive", "Expense added")),
                toast = "Expense added",
            ),
        "recordPayment" to
            Scenario(
                demo(),
                stack =
                    listOf(
                        Route.RecordPayment(
                            RecordPaymentArgs(
                                fromId = ME,
                                toId = "p-meera",
                                amount = 45_000,
                                groupId = "g-flat302",
                            )
                        )
                    ),
            ),
        "settleRecordKabir" to
            Scenario(
                demo(),
                stack =
                    listOf(
                        Route.SettleUp(),
                        Route.RecordPayment(
                            RecordPaymentArgs(
                                fromId = ME,
                                toId = "p-kabir",
                                amount = 140_000,
                                method = PaymentMethod.Upi,
                                groupId = "g-goa",
                            )
                        ),
                    ),
            ),
        "paymentRecorded" to
            Scenario(
                demo("paymentToMeeraPending"),
                stack = listOf(Route.Payment("pay-me-meera")),
                toast = "Payment recorded",
            ),
        "settlePaymentPending" to
            Scenario(
                demo("paymentToKabirPending"),
                stack = listOf(Route.SettleUp(), Route.Payment("pay-me-kabir")),
                toast = "Payment recorded",
            ),
        "paymentCancelAlert" to
            Scenario(demo("paymentToMeeraPending"), stack = listOf(Route.Payment("pay-me-meera"))),
        "lendMoney" to Scenario(demo(), stack = listOf(Route.LendMoney())),
        "loanAdded" to
            Scenario(
                demo("lendDev"),
                stack = listOf(Route.Loan("l-dev-laptop")),
                toast = "Loan added",
            ),
        "loanPaidBack" to Scenario(demo(), stack = listOf(Route.Loan("l-kabir-bike"))),
        "loanOverdue" to
            Scenario(demo("lendDevOverdue"), stack = listOf(Route.Loan("l-dev-laptop"))),
        "newGroup" to Scenario(demo(), stack = listOf(Route.NewGroup(NewGroupMode.Group))),
        "newGroupProject" to Scenario(demo(), stack = listOf(Route.NewGroup(NewGroupMode.Project))),
        "newGroupCreated" to
            Scenario(
                demo("weekendTrek"),
                tab = Tab.Groups,
                stack = listOf(Route.Group("g-trek")),
                toast = "Group created",
            ),
    )
