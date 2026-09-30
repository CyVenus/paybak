import Foundation
import Testing
@testable import paybak

/// verify.py `check_activity_and_inbox`.
struct ActivityAndInboxTests {
    @Test func claimCardAndTimeline() {
        let books = DemoFixture.load("eshaClaimsPayment")
        #expect(books.pendingClaims().map { [$0.title, $0.detail] }
            == [["Esha says she paid you ₹700", "Dinner at Olive Garden · UPI · 9:12 pm"]])
        #expect(books.homeTotals().owed == rupees(2900))
        let since = DemoFixture.day(2026, 9, 25)
        let rows = books.timeline()
            .filter { books.day(of: $0.at) >= since }
            .map { [Format.dayHeader(books.day(of: $0.at), today: books.today), $0.title, $0.subtitle, $0.amount ?? "–"] }
        #expect(rows == [
            ["Today", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", "–"],
            ["Today", "You added Dinner at Olive Garden", "You paid · 4 people", "₹2,800"],
            ["Yesterday", "Priya paid you", "Weekend groceries · UPI · Confirmed", "₹1,050"],
            ["Mon 28 Sep", "Kabir changed Villa (3 nights)", "Goa Trip · Was ₹17,500", "₹18,000"],
            ["Mon 28 Sep", "Cooking gas draft created", "Flat 302 · Needs an amount", "–"],
            ["Sun 27 Sep", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", "–"],
            ["Sat 26 Sep", "Meera added Electricity bill", "Flat 302 · You owe ₹450", "₹1,350"],
            ["Fri 25 Sep", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", "–"],
            ["Fri 25 Sep", "Dev added Fuel", "Goa Trip · Your share ₹500", "₹2,500"],
        ])
    }

    @Test func inbox() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let inbox = books.inbox()
        #expect(inbox.filter(\.isToday).map { [$0.title, $0.body, $0.time, "\($0.item.read)"] } == [
            ["Payment reminder", "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.", "9:00 pm", "false"],
            ["Monthly summary", "September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900.", "8:00 pm", "false"],
        ])
        #expect(inbox.filter { !$0.isToday }.prefix(3).map { [$0.title, $0.body, "\($0.item.read)"] } == [
            ["Payment confirmed", "Priya paid you ₹1,050 for Weekend groceries by UPI.", "true"],
            ["Payment overdue", "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep.", "true"],
            ["New expense in Flat 302", "Meera added Electricity bill, ₹1,350. Your share is ₹450.", "true"],
        ])
        #expect(LedgerSnapshot(books).unreadCount == 2)
    }

    @Test func recentlyDeleted() throws {
        let books = DemoFixture.load("eshaClaimsPayment")
        let row = try #require(books.recentlyDeleted().first)
        #expect("\(Money.format(row.expense.amount)) · \(books.groupName(row.expense.groupId)) | \(row.detail)"
            == "₹300 · Goa Trip | Deleted by Priya on 24 Sep · 24 days left")
    }

    @Test func confirmAndNotReceived() {
        let confirmed = DemoFixture.load("eshaPaymentConfirmed")
        let totals = confirmed.homeTotals()
        #expect(Money.format(totals.owed, sign: .signed) == "+₹2,200")
        #expect(totals.owedCaption == "from 3 people")
        let first = confirmed.recentActivity(timeline: confirmed.timeline())[0]
        #expect([first.title, first.subtitle, first.amount, first.date] == ["Esha paid you", "UPI", "₹700", "Today"])
        #expect(confirmed.pendingClaims().isEmpty)
        let rejected = DemoFixture.load("eshaPaymentNotReceived")
        #expect(rejected.homeTotals().owed == rupees(2900))
        #expect(rejected.pendingClaims().isEmpty)
    }
}
