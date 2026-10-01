import Foundation
import Testing
@testable import paybak

/// Ask Paybak's on-device assistant (app-architecture §4.3, domain.md §6.9) on the demo at Figma parity.
struct AssistantTests {
    private let upi = "arjun@okaxis"

    @Test func suggestedPrompts() {
        #expect(DemoFixture.load().suggestedPrompts().map(\.prompt) == [
            "Who owes me money?", "How much did I spend on food this month?", "When is Goa Trip due?", "Draft a reminder for Rohan",
        ])
    }

    @Test func whoOwesMe() throws {
        let reply = DemoFixture.load("eshaClaimsPayment").answer("Who owes me money?", upi: upi)
        #expect(reply.text == "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner.")
        guard case .people(let lines) = try #require(reply.card) else { Issue.record("Expected people"); return }
        #expect(lines.map { "\($0.name) \($0.amount) \($0.overdue ?? "-")" }
            == ["Rohan ₹800 Overdue 3 days", "Priya ₹700 -", "Esha ₹700 -", "Dev ₹700 -"])
        #expect(reply.chips.count == 1)
        guard case .remind(let id, let name, _) = reply.chips[0] else { Issue.record("Expected Remind"); return }
        #expect(id == "p-rohan" && name == "Rohan")
    }

    @Test func spendingOnFood() {
        let reply = DemoFixture.load().answer("How much did I spend on food this month?", upi: upi)
        #expect(reply.text == "You spent ₹3,850 on food in September — 17% of your ₹23,300 share.")
        #expect(reply.chips == [.seeInsights(YearMonth(year: 2026, month: 9))])
        let august = DemoFixture.load().answer("how much did i spend on rent in august", upi: upi)
        #expect(august.text.hasPrefix("You spent ₹12,000 on rent in August"))
    }

    @Test func groupDue() {
        let reply = DemoFixture.load().answer("When is Goa Trip due?", upi: upi)
        #expect(reply.text == "Your Goa Trip share of ₹1,400 is due Fri 2 Oct.")
        #expect(reply.chips.first == .settleUp("g-goa"))
        #expect(DemoFixture.load().answer("When is Narnia due?", upi: upi).text == "I couldn’t find a group called Narnia.")
    }

    @Test func draftedReminder() {
        let reply = DemoFixture.load().answer("Draft a reminder for Rohan", upi: upi)
        #expect(reply.text == "Here’s a reminder for Rohan. Nothing is sent until you tap Send.")
        #expect(reply.more == ["Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."])
        #expect(DemoFixture.load().answer("remind kabir", upi: upi).text == "Kabir doesn’t owe you anything right now.")
    }

    @Test func draftedExpense() throws {
        let reply = DemoFixture.load().answer("Add ₹600 for a cab, split with Esha and Dev", upi: upi)
        #expect(reply.text == "Here’s what I’ll add. Nothing is saved until you tap Save.")
        guard case .draft(let card) = try #require(reply.card) else { Issue.record("Expected a draft"); return }
        #expect(card.title == "Cab")
        #expect(card.amount == "₹600")
        #expect(card.icon == "car")
        #expect(card.paidLine == "Paid by you · Today")
        #expect(card.splitLine == "Split equally with Esha and Dev")
        #expect(card.eachLine == "₹200 each")
        #expect(card.people == [Person.me, "p-esha", "p-dev"])
        #expect(card.draft.category == .travel)
        #expect(card.draft.amount == rupees(600))
    }

    /// Saving the draft is an ordinary expense: Esha and Dev each owe ₹200 more.
    @Test func savingTheDraftUpdatesBalances() throws {
        var books = DemoFixture.load()
        let before = books.friendNets()
        guard case .draft(let card) = books.answer("Add ₹600 for a cab, split with Esha and Dev", upi: upi).card else {
            Issue.record("Expected a draft"); return
        }
        try books.addExpense(card.draft, at: DemoFixture.figmaNow)
        let after = books.friendNets()
        #expect(after["p-esha", default: 0] - before["p-esha", default: 0] == rupees(200))
        #expect(after["p-dev", default: 0] - before["p-dev", default: 0] == rupees(200))
    }

    @Test func unevenSplitsAndUnknownNames() throws {
        let reply = DemoFixture.load().answer("add 1000 for pizza with esha, dev & zed", upi: upi)
        guard case .draft(let card) = try #require(reply.card) else { Issue.record("Expected a draft"); return }
        #expect(card.eachLine == "About ₹333 each")
        #expect(card.draft.category == .food)
        #expect(reply.more == ["I couldn’t find Zed."])
        #expect(DemoFixture.load().answer("Add 600 for a cab", upi: upi).card == nil)
    }

    @Test func fallback() {
        let reply = DemoFixture.load().answer("What’s the weather?", upi: upi)
        #expect(reply.text == "I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”")
        #expect(reply.card == .suggestions)
        #expect(AssistantParser.intent(of: "what happened on 28 sep") == .unknown)
    }

    @Test func parsesTheExpenseGrammar() {
        #expect(AssistantParser.intent(of: "Rs 1,250.50 for the taxi, split with Esha & Dev in Goa Trip") == .expense(.init(
            amount: "1250.50", what: "taxi", names: ["esha", "dev"], group: "goa trip"
        )))
        #expect(AssistantParser.category(for: "Weekend groceries") == .food)
        #expect(AssistantParser.category(for: "wi-fi bill") == .bills)
        #expect(AssistantParser.category(for: "something") == .other)
    }
}
