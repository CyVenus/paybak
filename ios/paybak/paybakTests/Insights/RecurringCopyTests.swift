import Foundation
import Testing
@testable import paybak

/// Recurring rules' copy and the Repeat sheet's lines (screens-insights-ai §5) on the demo.
struct RecurringCopyTests {
    @Test func flat302Page() {
        let page = DemoFixture.load().recurringPage("g-flat302")
        #expect(page.intro == "Paybak adds these to Flat 302 on schedule.")
        #expect(page.drafts.map { "\($0.title) · \($0.subtitle) · \($0.icon)" } == ["Cooking gas · September draft · 28 Sep · flame"])
        #expect(page.rules.map { "\($0.title)|\($0.subtitle)|\($0.detail)|\($0.amount)|\($0.icon)" } == [
            "Rent|Monthly on the 1st\nPaid by you|Next Thu 1 Oct|₹36,000|home",
            "Wi-Fi|Monthly on the 5th\nPaid by Kabir|Next Mon 5 Oct|₹1,200|wi-fi",
            "Cooking gas|Monthly on the 28th\nPaid by you|Next Wed 28 Oct|Varies|flame",
        ])
    }

    @Test func enterAmountPage() throws {
        let page = try #require(DemoFixture.load().draftAmountPage("d-gas-09"))
        #expect(page.title == "Cooking gas")
        #expect(page.meta == "Flat 302 · September")
        #expect(page.paidBy == "You")
        #expect(page.split == "Equally · 3 people")
        #expect(page.date == "Mon 28 Sep")
        #expect(!page.isDone)
    }

    /// Entering the amount adds the expense on the occurrence date and empties "Needs your amount".
    @Test func enteringTheAmountAddsTheExpense() throws {
        var books = DemoFixture.load()
        let id = try books.enterDraftAmount("d-gas-09", amount: rupees(900), at: DemoFixture.figmaNow)
        let expense = try #require(books.ledger.expense(id))
        #expect(expense.date == DemoFixture.day(2026, 9, 28))
        #expect(expense.groupId == "g-flat302")
        #expect(expense.share(of: Person.me) == rupees(300))
        #expect(books.recurringPage("g-flat302").drafts.isEmpty)
        #expect(books.draftAmountPage("d-gas-09")?.isDone == true)
    }

    @Test func repeatSheetLines() {
        let start = DemoFixture.day(2026, 9, 30)
        let gas = RepeatRule(frequency: .monthly, anchorDate: DemoFixture.day(2026, 9, 28), variable: true)
        #expect(gas.helper == "Paybak adds a draft on the 28th and asks you for the amount.")
        #expect(gas.nextLine(after: start) == "Next draft: Wed 28 Oct")
        #expect(gas.dayOfMonthValue == "28th")

        var fixed = gas
        fixed.variable = false
        #expect(fixed.helper == "Paybak adds this expense on the 28th of every month.")
        #expect(fixed.nextLine(after: start) == "Next: Wed 28 Oct")

        let weekly = RepeatRule(frequency: .weekly, anchorDate: DemoFixture.day(2026, 10, 5))
        #expect(weekly.schedule == "Weekly on Mondays")
        #expect(weekly.nextLine(after: start) == "Next: Mon 5 Oct")

        // Custom: every other week (Android's Frequency.Biweekly).
        let biweekly = RepeatRule(frequency: .biweekly, anchorDate: DemoFixture.day(2026, 10, 5))
        #expect(biweekly.schedule == "Every 2 weeks on Mondays")
        #expect(biweekly.helper == "Paybak adds this expense every other Monday.")
        var biweeklyDraft = biweekly
        biweeklyDraft.variable = true
        #expect(biweeklyDraft.helper == "Paybak adds a draft every other Monday and asks you for the amount.")
        // 14 days at a time from the anchor, never the Monday in between.
        #expect(biweekly.nextLine(after: start) == "Next: Mon 5 Oct")
        #expect(biweekly.nextOccurrence(after: DemoFixture.day(2026, 10, 5)) == DemoFixture.day(2026, 10, 19))
        #expect(biweekly.nextOccurrence(after: DemoFixture.day(2026, 10, 12)) == DemoFixture.day(2026, 10, 19))
        #expect(biweekly.nextOccurrence(after: DemoFixture.day(2026, 10, 19)) == DemoFixture.day(2026, 11, 2))

        // The 31st reads as the 31st; a short month still uses its last day.
        let lastDay = RepeatRule(frequency: .monthly, anchorDate: DemoFixture.day(2026, 10, 31))
        #expect(lastDay.dayOfMonthValue == "31st")
        #expect(lastDay.schedule == "Monthly on the 31st")
        #expect(lastDay.nextOccurrence(after: DemoFixture.day(2026, 10, 31)) == DemoFixture.day(2026, 11, 30))

        let yearly = RepeatRule(frequency: .yearly, anchorDate: DemoFixture.day(2026, 9, 28))
        #expect(yearly.schedule == "Yearly on 28 Sep")
        #expect(yearly.nextOccurrence(after: start) == DemoFixture.day(2027, 9, 28))
    }
}
