#if DEBUG
extension Scenario {
    /// Add & Record ids (app-architecture §1.3). Form states are applied by the forms themselves.
    static let addRecord: [ScreenID: Scenario] = {
        let form = Layer(root: .addExpense(.new))
        let figma = DemoSeed.figmaDay
        var table: [ScreenID: Scenario] = [
            .addExpenseEmpty: Scenario(seeds: demo, modals: [form]),
            .addExpenseFilled: Scenario(seeds: demo, modals: [Layer(root: .addExpense(AddExpenseArgs(focusAmount: false)))]),
            .addExpenseSplitWith: Scenario(seeds: demo, modals: [Layer(
                root: .addExpense(AddExpenseArgs(focusAmount: false)),
                path: [.pickPeople(PeoplePickRequest(selected: [Person.me, "p-priya", "p-esha", "p-dev"]))]
            )]),
            .addExpenseCurrency: Scenario(seeds: demo, modals: [Layer(
                root: .addExpense(AddExpenseArgs(focusAmount: false)), sheet: .pickCurrency(CurrencyPickRequest(selected: "INR"))
            )]),
            .addExpenseDueDate: Scenario(seeds: demo, modals: [Layer(
                root: .addExpense(AddExpenseArgs(focusAmount: false)),
                sheet: .pickDate(DatePickRequest(kind: .dueDate, selected: figma.adding(days: 4), allowsNone: true, earliest: figma))
            )]),
            .addExpenseDate: Scenario(seeds: demo, modals: [Layer(
                root: .addExpense(AddExpenseArgs(focusAmount: false)),
                sheet: .pickDate(DatePickRequest(kind: .date, selected: figma, latest: figma))
            )]),
            .expenseAdded: Scenario(seeds: demo, stack: [.expense("e-olive")], toast: "Expense added"),
            .recordPayment: Scenario(seeds: demo, modals: [Layer(root: .recordPayment(RecordPaymentArgs(
                from: Person.me, to: "p-meera", amount: 45_000, currency: "INR", context: .group("g-flat302")
            )))]),
            .settleRecordKabir: Scenario(seeds: demo, stack: [.settleUp(groupId: nil)], modals: [Layer(root: .recordPayment(RecordPaymentArgs(
                from: Person.me, to: "p-kabir", amount: 140_000, currency: "INR", method: .upi, context: .group("g-goa")
            )))]),
            .paymentRecorded: Scenario(seeds: demo + ["paymentToMeeraPending"], stack: [.payment("pay-me-meera")], toast: "Payment recorded"),
            .settlePaymentPending: Scenario(seeds: demo + ["paymentToKabirPending"], stack: [.settleUp(groupId: nil), .payment("pay-me-kabir")],
                                            toast: "Payment recorded"),
            .paymentCancelAlert: Scenario(seeds: demo + ["paymentToMeeraPending"], stack: [.payment("pay-me-meera")]),
            .lendMoney: Scenario(seeds: demo, modals: [Layer(root: .lendMoney(.new))]),
            .loanAdded: Scenario(seeds: demo + ["lendDev"], stack: [.loan("l-dev-laptop")], toast: "Loan added"),
            .loanPaidBack: Scenario(seeds: demo, stack: [.loan("l-kabir-bike")]),
            .loanOverdue: Scenario(seeds: demo + ["lendDevOverdue"], stack: [.loan("l-dev-laptop")]),
            .newGroup: Scenario(seeds: demo, modals: [Layer(root: .newGroup(.group))]),
            .newGroupProject: Scenario(seeds: demo, modals: [Layer(root: .newGroup(.project))]),
            .newGroupCreated: Scenario(seeds: demo + ["weekendTrek"], tab: .groups, stack: [.group("g-trek")], toast: "Group created"),
        ]
        for id in [ScreenID.addExpensePaidBy, .addExpensePayers, .addExpenseSplitEqually, .addExpenseSplitExactError,
                   .addExpenseCategory, .addExpenseDiscard] {
            table[id] = Scenario(seeds: demo, modals: [Layer(root: .addExpense(AddExpenseArgs(focusAmount: false)))])
        }
        return table
    }()
}
#endif
