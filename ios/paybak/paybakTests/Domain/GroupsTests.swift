import Foundation
import Testing
@testable import paybak

/// verify.py `check_groups`.
struct GroupsTests {
    private let books = DemoFixture.load()

    @Test func goaTrip() throws {
        let (paid, share) = books.groupPaidShare("g-goa")
        let nets = books.groupNets("g-goa")
        #expect(paid.values.reduce(0, +) / 100 == 39_500)
        let rows = Dictionary(uniqueKeysWithValues: nets.keys.map { ($0, [paid[$0]! / 100, share[$0]! / 100, nets[$0]! / 100]) })
        #expect(rows == [Person.me: [6500, 7900, -1400], "p-kabir": [18000, 7900, 10100], "p-priya": [3500, 7900, -4400],
                         "p-esha": [5000, 7900, -2900], "p-dev": [6500, 7900, -1400]])
        let sheet = try #require(books.groupSheet("g-goa"))
        #expect("\(sheet.dateRange!) · \(sheet.group.memberIds.count) members · \(Money.format(sheet.spent)) spent"
            == "21–25 Sep · 5 members · ₹39,500 spent")
        let plan = books.groupPlan("g-goa").map { "\(books.firstName($0.from))→\(books.firstName($0.to)) \($0.amount / 100)" }
        #expect(plan.sorted() == ["Dev→Kabir 1400", "Esha→Kabir 2900", "Priya→Kabir 4400", "You→Kabir 1400"])
        #expect(sheet.simplifyFootnote == "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly.")
        #expect(books.ledger.expense("e-goa-snacks")?.deletedAt != nil)
        #expect(books.ledger.expense("e-goa-villa")!.amount / 5 / 100 == 3600)
        #expect(books.simplifiedFootnoteGroups().map(\.name) == ["Goa Trip"])
    }

    @Test func flat302AndCollege() {
        #expect(books.groupNets("g-flat302") == [Person.me: -45_000, "p-meera": 90_000, "p-kabir": -45_000])
        #expect(books.groupPlan("g-flat302") == [Transfer(from: Person.me, to: "p-meera", amount: 45_000),
                                                   Transfer(from: "p-kabir", to: "p-meera", amount: 45_000)])
        #expect(Set(books.groupNets("g-college").values) == [0])
        #expect(books.ledger.recurringRules.filter { $0.groupId == "g-flat302" && $0.active }.count == 3)
    }

    @Test func dubaiWeekend() throws {
        let (paid, share) = books.groupPaidShare("g-dubai")
        #expect(paid.mapValues { Money.format($0, "AED") } == [Person.me: "AED 540", "p-kabir": "AED 960", "p-meera": "AED 300"])
        #expect(Set(share.values.map { Money.format($0, "AED") }) == ["AED 600"])
        #expect(Set(books.groupNets("g-dubai").values) == [0])
        let sheet = try #require(books.groupSheet("g-dubai"))
        #expect(sheet.expenses.map { Money.approximateLine($0.amount, currency: "AED", rate: $0.rate!) }
            == ["≈ ₹6,870 · ₹22.90 per AED", "≈ ₹12,312 · ₹22.80 per AED", "≈ ₹21,936 · ₹22.85 per AED"])
        #expect("Total \(Money.format(sheet.spent, "AED")) · ≈ \(Money.format(sheet.spentInDefault!)) at saved rates"
            == "Total AED 1,800 · ≈ ₹41,118 at saved rates")
        let last = try #require(books.confirmedPayments()
            .filter { $0.groupId == "g-dubai" && ($0.fromId == Person.me || $0.toId == Person.me) }
            .max { $0.confirmedAt! < $1.confirmedAt! })
        #expect("You paid \(books.firstName(last.toId)) \(Money.format(last.amount, "AED")) on \(Format.short(last.date))"
            == "You paid Kabir AED 60 on 14 Mar")
        // A settled foreign group is exactly ₹0 everywhere.
        #expect(!books.contexts().contains { $0.ref == "g-dubai" })
    }

    @Test func groupsList() {
        let summaries = books.groupSummaries()
        #expect(summaries.map(\.group.name) == ["Goa Trip", "Flat 302", "Build a Drone", "College Gang", "Dubai Weekend", "Hackathon Kit"])
        #expect(["g-goa", "g-flat302", "g-college", "g-dubai"].map { "\(books.ledger.group($0)!.memberIds.count) members" }
            == ["5 members", "3 members", "6 members", "3 members"])
        #expect(summaries.first { $0.id == "pj-drone" }?.isOpen == true)
        #expect(summaries.first { $0.id == "pj-drone" }?.myNet == 0)
    }

    @Test func friendRohan() throws {
        let page = try #require(books.friendPage("p-rohan"))
        let expenses = page.history.compactMap { item -> String? in
            guard case .expense(let expense) = item else { return nil }
            return "\(expense.title) \(Money.format(expense.amount)) \(Format.short(expense.date))"
        }
        #expect(expenses == ["Movie tickets ₹1,600 20 Sep", "Farewell dinner ₹9,000 12 Mar"])
        #expect(page.groupsTogether.map(\.name) == ["College Gang", "Build a Drone"])
        #expect(books.lastReminderText(try #require(page.lastReminder)) == "Last reminder sent today")
        #expect(books.ledger.reminders.filter { $0.toId == "p-rohan" }.map { books.day(of: $0.sentAt).day }.sorted() == [25, 27, 30])
    }
}
