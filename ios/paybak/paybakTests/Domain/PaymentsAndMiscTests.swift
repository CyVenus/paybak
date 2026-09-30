import Foundation
import Testing
@testable import paybak

/// verify.py `check_payments` and `check_recurring_and_misc`.
struct PaymentsAndMiscTests {
    @Test func payments() {
        #expect(Money.format(-DemoFixture.load("paymentToMeeraPending").homeTotals().owe, sign: .signed) == "−₹1,850")
        let done = DemoFixture.load("paymentToMeeraConfirmed").homeTotals()
        #expect(Money.format(-done.owe, sign: .signed) == "−₹1,400")
        #expect(done.oweCaption == "across 1 group")
        #expect(DemoFixture.load("paymentToKabirPending").homeTotals().owe == rupees(1850))
        #expect(DemoFixture.load("allSettled").homeTotals() == HomeTotals())
    }

    @Test func flagAndEmptyAndTrek() throws {
        let flagged = DemoFixture.load("eshaFlagsSeafood")
        #expect(flagged.ledger.expense("e-goa-seafood")?.flag?.note == "I left before dessert. Can we check the bill?")
        #expect(flagged.groupNets("g-goa")[Person.me] == -rupees(1400))
        let empty = DemoFixture.load("empty")
        #expect(empty.homeTotals().owed == 0)
        #expect(empty.timeline().isEmpty)
        let trek = DemoFixture.load("weekendTrek")
        let group = try #require(trek.ledger.group("g-trek"))
        #expect("Trip · \(group.memberIds.count) members · \(group.currency)" == "Trip · 4 members · INR")
        #expect(Set(trek.groupNets("g-trek").values) == [0])
    }

    @Test func recurring() throws {
        let books = DemoFixture.load()
        let next = Dictionary(uniqueKeysWithValues: books.ledger.recurringRules.map { ($0.title, Format.day(Books.nextOccurrence($0, after: books.today))) })
        #expect(next == ["Rent": "Thu 1 Oct", "Wi-Fi": "Mon 5 Oct", "Cooking gas": "Wed 28 Oct"])
        let draft = try #require(books.ledger.drafts.first)
        #expect("\(Format.month(draft.occurrenceDate.month)) draft · \(Format.short(draft.occurrenceDate))" == "September draft · 28 Sep")
        #expect(Format.day(books.today.adding(days: 7)) == "Wed 7 Oct")
        #expect(DemoFixture.load("pro").ledger.settings.entitlement.plan == .pro)
    }

    @Test func exportTicks() {
        let books = DemoFixture.load()
        let (start, end) = (DemoFixture.day(2026, 9, 1), DemoFixture.day(2026, 9, 30))
        var ticked = Set<String>()
        for expense in books.liveExpenses() where (start...end).contains(expense.date) {
            ticked.insert(expense.groupId.map(books.groupName) ?? Books.withoutAGroup)
        }
        for payment in books.confirmedPayments()
        where (start...end).contains(payment.date) && (payment.fromId == Person.me || payment.toId == Person.me) {
            ticked.insert(payment.groupId.map(books.groupName) ?? Books.withoutAGroup)
        }
        for part in books.ledger.components where (start...end).contains(books.day(of: part.statusChangedAt)) {
            ticked.insert(books.groupName(part.projectId))
        }
        #expect(ticked == ["Goa Trip", "Flat 302", "Build a Drone", "Without a group"])
    }

    @Test func tickIsIdempotent() {
        var books = DemoFixture.load()
        let counts = [books.ledger.reminders.count, books.ledger.inbox.count, books.ledger.expenses.count, books.ledger.drafts.count]
        books.tick(until: books.now)
        #expect([books.ledger.reminders.count, books.ledger.inbox.count, books.ledger.expenses.count, books.ledger.drafts.count] == counts)
    }

    @Test func tickAddsTheNextRent() throws {
        let later = DemoFixture.load(now: DemoFixture.moment(2026, 10, 1, 10, 0))
        #expect(try #require(later.ledger.expense("e-r-rent-2026-10-01")).amount == rupees(36000))
        #expect(later.groupNets("g-flat302")[Person.me] == rupees(-450 + 24000))
    }

    @Test func anotherLoadDay() {
        let shifted = DemoFixture.load(now: DemoFixture.moment(2026, 10, 1, 9, 0), anchor: DemoFixture.day(2026, 10, 1))
        let totals = shifted.homeTotals()
        #expect(totals.owed == rupees(2900))
        #expect(totals.owe == rupees(1850))
        #expect(shifted.dueSoon().map(\.badge) == ["Overdue 3 days", "Due Sat"])
        #expect(!shifted.ledger.reminders.contains { shifted.day(of: $0.sentAt) == shifted.today })
    }
}
