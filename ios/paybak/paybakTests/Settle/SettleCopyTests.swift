import Foundation
import Testing
@testable import paybak

/// The Settle up copy (screens-settle §1–§8) from the demo at Figma parity: every string the refs show
/// comes out of the records, and the receiver's answers move (or keep) the balances.
struct SettleCopyTests {
    private let books = DemoFixture.load()

    @Test func owedBreakdown() {
        let rows = books.settleRows().get.map(books.breakdownRow)
        #expect(rows.map(\.name) == ["Rohan", "Priya", "Esha", "Dev"])
        #expect(rows.map(\.subtitle) == ["Movie tickets", "Dinner at Olive Garden", "Dinner at Olive Garden", "Dinner at Olive Garden"])
        #expect(rows.map(\.amount) == ["₹800", "₹700", "₹700", "₹700"])
        #expect(rows.map(\.overdue) == ["Overdue 3 days", nil, nil, nil])
        #expect(rows.map(\.dueLabel) == [nil, "Due Sun 4 Oct", "Due Sun 4 Oct", "Due Sun 4 Oct"])
        let totals = books.homeTotals()
        #expect(Money.format(totals.owed, sign: .signed) == "+₹2,900")
        #expect(totals.owedCaption == "from 4 people")
    }

    @Test func oweBreakdown() {
        let rows = books.settleRows().pay.map(books.breakdownRow)
        #expect(rows.map(\.name) == ["Kabir", "Meera"])
        #expect(rows.map(\.subtitle) == ["Goa Trip", "Flat 302"])
        #expect(rows.map(\.amount) == ["₹1,400", "₹450"])
        #expect(rows.map(\.dueLabel) == ["Due Fri 2 Oct", "Due Mon 5 Oct"])
        #expect(rows.allSatisfy { $0.overdue == nil })
        #expect(books.simplifiedDebtsFootnotes(books.simplifiedFootnoteGroups()) == ["Goa Trip uses simplified debts, so you pay Kabir directly."])
        let totals = books.homeTotals()
        #expect(Money.format(-totals.owe, sign: .debit) == "−₹1,850")
        #expect(totals.oweCaption == "across 2 groups")
    }

    @Test func settleUpPlan() {
        let settle = books.settleRows()
        let plan = books.settlePlan(pay: settle.pay, get: settle.get)
        #expect(plan.pay.map(\.name) == ["Kabir", "Meera"])
        #expect(plan.pay.map(\.context) == ["Goa Trip", "Flat 302"])
        #expect(plan.pay.map(\.amount) == ["₹1,400", "₹450"])
        #expect(plan.pay.map(\.badge) == ["Due Fri", "Due Mon"])
        #expect(plan.get.map(\.name) == ["Rohan", "Priya", "Esha", "Dev"])
        #expect(plan.get.map(\.badge) == ["Overdue 3 days", "Due Sun", "Due Sun", "Due Sun"])
        #expect(plan.get.map(\.isOverdue) == [true, false, false, false])
        #expect(plan.pay.allSatisfy { $0.pendingPayment == nil })
        #expect(Books.paymentsHeader(plan.pay.count) == "2 payments to make")
        #expect(Books.owersHeader(plan.get.count) == "4 people owe you")
        #expect(Books.paymentsHeader(1) == "1 payment to make")
        #expect(Books.owersHeader(1) == "1 person owes you")
    }

    /// A payment you recorded keeps its row, marked Pending, and changes no balance until confirmed.
    @Test func pendingPaymentKeepsTheRow() {
        let books = DemoFixture.load("paymentToKabirPending")
        let settle = books.settleRows()
        let plan = books.settlePlan(pay: settle.pay, get: settle.get)
        #expect(plan.pay.map(\.badge) == ["Pending", "Due Mon"])
        #expect(plan.pay.map(\.pendingPayment) == ["pay-me-kabir", nil])
        #expect(plan.pay.first?.amount == "₹1,400")
        #expect(books.homeTotals().owe == rupees(1850))
    }

    @Test func groupPlan() {
        let plan = books.settlePlan(groupId: "g-goa", pay: [], get: [])
        #expect(plan.pay.map(\.name) == ["Kabir"])
        #expect(plan.pay.map(\.amount) == ["₹1,400"])
        #expect(plan.pay.map(\.badge) == ["Due Fri"])
        #expect(plan.pay.first?.item?.kind == .group)
        #expect(plan.get.isEmpty)
    }

    @Test func reminderMessages() throws {
        let item = try #require(books.reminderItem(for: "p-rohan", context: .expense("e-movie")))
        #expect(item.amount == rupees(800))
        #expect(books.reminderItem(for: "p-rohan", context: nil) == item)
        #expect(books.reminderMessage(item, tone: .friendly, upi: "arjun@okaxis")
            == "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks.")
        #expect(books.reminderMessage(item, tone: .neutral, upi: "arjun@okaxis")
            == "Hi Rohan, this is a reminder that ₹800 for movie tickets (20 Sep) is still due. You can pay me on UPI at arjun@okaxis.")
        #expect(books.reminderMessage(item, tone: .friendly, upi: nil)
            == "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. Thanks.")
        let group = Obligation(debtor: "p-priya", creditor: Person.me, amount: rupees(1400), title: "Goa Trip", kind: .group, ref: "g-goa")
        #expect(books.reminderMessage(group, tone: .friendly, upi: "")
            == "Hi Priya! Just a gentle reminder about ₹1,400 for Goa Trip. Thanks.")
        #expect(books.reminderItem(for: "p-kabir", context: nil) == nil)
    }

    @Test func sendingAReminderLogsIt() throws {
        var books = books
        let item = try #require(books.reminderItem(for: "p-rohan", context: nil))
        let count = books.ledger.reminders.count
        books.sendReminder(about: item, tone: .neutral, message: "Hi", via: .paybak, at: books.now)
        let reminder = try #require(books.ledger.reminders.last)
        #expect(books.ledger.reminders.count == count + 1)
        #expect(reminder.toId == "p-rohan" && reminder.expenseId == "e-movie" && !reminder.automatic)
        #expect(books.homeTotals() == self.books.homeTotals())
    }

    @Test func notReceivedCopy() throws {
        let books = DemoFixture.load("eshaClaimsPayment")
        let payment = try #require(books.ledger.payment("pay-esha-olive"))
        let copy = books.notReceivedCopy(payment)
        #expect(copy.title == "Let Esha know you haven’t received ₹700?")
        #expect(copy.context == "Dinner at Olive Garden · UPI · 9:12 pm")
        #expect(copy.note == "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check your UPI app?")
        #expect(copy.helper == "Esha still owes you ₹700 until a payment is confirmed.")
    }

    /// Confirm settles Esha's ₹700; Not received keeps it owed and removes her claim.
    @Test func receiverAnswers() {
        let confirmed = DemoFixture.load("eshaPaymentConfirmed")
        #expect(confirmed.homeTotals().owed == rupees(2200))
        #expect(confirmed.homeTotals().owedCaption == "from 3 people")
        #expect(confirmed.settleRows().get.map { confirmed.firstName($0.friend) } == ["Rohan", "Priya", "Dev"])

        let refused = DemoFixture.load("eshaPaymentNotReceived")
        #expect(refused.homeTotals().owed == rupees(2900))
        #expect(refused.pendingClaims().isEmpty)
        #expect(refused.ledger.payment("pay-esha-olive")?.status == .notReceived)
    }

    @Test func lowerFirst() {
        #expect(Books.lowerFirst("Movie tickets") == "movie tickets")
        #expect(Books.lowerFirst("UPI top-up") == "UPI top-up")
        #expect(Books.lowerFirst("") == "")
    }
}
