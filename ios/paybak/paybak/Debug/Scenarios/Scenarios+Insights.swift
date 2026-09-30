#if DEBUG
extension Scenario {
    /// Insights, Ask Paybak, receipt scanning and recurring ids (app-architecture §1.9).
    static let insights: [ScreenID: Scenario] = {
        let form = Layer(root: .addExpense(AddExpenseArgs(focusAmount: false)))
        let scan = Layer(root: .scanReceipt(ScanRequest(people: [Person.me, "p-esha", "p-dev"])))
        let figma = DemoSeed.figmaDay
        return [
            .insightsSeptember: Scenario(seeds: pro(), tab: .activity, activitySegment: .insights),
            .insightsScrolled: Scenario(seeds: pro(), tab: .activity, activitySegment: .insights),
            .insightsLocked: Scenario(seeds: demo, tab: .activity, activitySegment: .insights),
            .askStart: Scenario(seeds: pro(), modals: [Layer(root: .ask)]),
            .askAnswer: Scenario(seeds: pro(), modals: [Layer(root: .ask)]),
            .askConfirm: Scenario(seeds: pro(), modals: [Layer(root: .ask)]),
            .scanCamera: Scenario(seeds: pro(), modals: [form, scan]),
            .scanReview: Scenario(seeds: pro(), modals: [form, scan]),
            .scanAssign: Scenario(seeds: pro(), modals: [form, scan]),
            .scanAddExpense: Scenario(seeds: pro(), modals: [Layer(root: .addExpense(AddExpenseArgs(draft: leopoldDraft(on: figma), focusAmount: false)))]),
            .recurringFlat302: Scenario(seeds: pro(), tab: .groups, stack: [.group("g-flat302"), .recurring("g-flat302")]),
            .recurringRepeat: Scenario(seeds: pro(), modals: [Layer(
                root: .addExpense(AddExpenseArgs(draft: cookingGasDraft(on: figma), focusAmount: false)),
                sheet: .repeatRule(RepeatRuleRequest(current: RepeatRule(frequency: .monthly, anchorDate: figma), startDate: figma))
            )]),
            .recurringEnterAmount: Scenario(seeds: pro(), modals: [Layer(root: .enterDraftAmount("d-gas-09"))]),
        ]
    }()

    /// The Leopold Cafe receipt read and assigned (insights §4): ₹2,300 itemized, You ₹989 · Esha ₹621 · Dev ₹690.
    static func leopoldDraft(on day: LocalDay) -> ExpenseDraft {
        let items: [Itemized.Item] = [
            .init(label: "Chicken biryani", amount: 43_000, personIds: ["p-dev"]),
            .init(label: "Paneer tikka", amount: 37_000, personIds: ["p-esha"]),
            .init(label: "Fish and chips", amount: 45_000, personIds: [Person.me]),
            .init(label: "Chocolate brownie", amount: 24_000, personIds: [Person.me]),
            .init(label: "Masala fries", amount: 24_000, personIds: [Person.me, "p-esha", "p-dev"]),
            .init(label: "Fresh lime soda ×3", amount: 27_000, personIds: [Person.me, "p-esha", "p-dev"]),
        ]
        let order = [Person.me, "p-esha", "p-dev"]
        let shares = Splits.itemized(items: items, total: 230_000, order: order).shares
        return ExpenseDraft(
            title: "Leopold Cafe", category: .food, amount: 230_000, currency: "INR", date: day,
            payers: [Payer(personId: Person.me, amount: 230_000)], splitMode: .itemized,
            rows: order.map { SplitRow(personId: $0, included: true, value: shares[$0], share: shares[$0] ?? 0) },
            itemized: Itemized(items: items, lines: [.init(label: "GST 5%", amount: 10_000), .init(label: "Tip 10%", amount: 20_000)],
                               subtotal: 200_000)
        )
    }

    /// Cooking gas for Flat 302, the Repeat sheet's example.
    static func cookingGasDraft(on day: LocalDay) -> ExpenseDraft {
        ExpenseDraft(groupId: "g-flat302", title: "Cooking gas", category: .bills, currency: "INR", date: day,
                     rows: [Person.me, "p-meera", "p-kabir"].map { SplitRow(personId: $0) })
    }
}
#endif
