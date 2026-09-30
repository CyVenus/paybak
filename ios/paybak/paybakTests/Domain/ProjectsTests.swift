import Foundation
import Testing
@testable import paybak

/// verify.py `check_projects`.
struct ProjectsTests {
    @Test func buildADrone() {
        let books = DemoFixture.load()
        let report = books.projectReport("pj-drone")
        #expect(Money.format(report.spent) == "₹52,000")
        #expect(Money.format(report.budget!) == "₹60,000")
        #expect(report.percentUsed == 87)
        #expect(Money.format(report.budget! - report.spent) == "₹8,000")
        #expect("Planned items bring it to \(Money.format(report.projection))" == "Planned items bring it to ₹58,000")
        let rows = Dictionary(uniqueKeysWithValues: report.paid.keys.map {
            (books.firstName($0), [report.paid[$0]! / 100, report.share[$0]! / 100, report.nets[$0]! / 100])
        })
        #expect(rows == ["You": [13000, 13000, 0], "Dev": [25500, 13000, 12500], "Priya": [9000, 13000, -4000], "Rohan": [4500, 13000, -8500]])
        #expect(report.memberOrder.map(books.firstName) == ["Dev", "You", "Priya", "Rohan"])
        let scale = Double(max(report.paid.values.max()!, report.share.values.max()!))
        #expect(Dictionary(uniqueKeysWithValues: report.paid.map { (books.firstName($0.key), Int((Double($0.value) * 100 / scale).rounded())) })
            == ["You": 51, "Dev": 100, "Priya": 35, "Rohan": 18])
        #expect(report.plan.map { "\(books.firstName($0.from)) owes \(books.firstName($0.to)) \(Money.format($0.amount))" }
            == ["Rohan owes Dev ₹8,500", "Priya owes Dev ₹4,000"])
        #expect(report.components.map(\.name)
            == ["GPS module", "Camera", "Transmitter", "ESCs and propellers", "Battery", "Flight controller", "Motors ×4", "Frame"])
        #expect(books.homeTotals().owed == rupees(2900))
    }

    @Test func overBudget() {
        let books = DemoFixture.load("devBuysGps")
        let report = books.projectReport("pj-drone")
        #expect("\(Money.format(report.spent)) of \(Money.format(report.budget!)) · \(Money.format(report.spent - report.budget!)) over budget"
            == "₹61,500 of ₹60,000 · ₹1,500 over budget")
        #expect(books.firstNames(report.nets).mapValues { $0 } == ["You": -2375, "Dev": 19625, "Priya": -6375, "Rohan": -10875])
        #expect(report.nets[Person.me] == -rupees(2375))
    }

    @Test func closeAndArchive() throws {
        var books = DemoFixture.load("closeDrone")
        #expect(books.ledger.group("pj-drone")?.project?.status == .closed)
        #expect(books.projectReport("pj-drone").plan.map { "\(books.firstName($0.from)) pays \(books.firstName($0.to)) \(Money.format($0.amount))" }
            == ["Rohan pays Dev ₹8,500", "Priya pays Dev ₹4,000"])
        #expect("\(Money.format(60_000_00 - books.projectSpent("pj-drone"))) under budget" == "₹8,000 under budget")
        for (id, from, amount) in [("pay-rohan-dev", "p-rohan", 8500.0), ("pay-priya-dev", "p-priya", 4000.0)] {
            try books.recordPayment(PaymentDraft(id: id, fromId: from, toId: "p-dev", amount: rupees(amount), currency: "INR",
                                                 method: .upi, date: books.today, groupId: "pj-drone", recordedBy: from), at: books.now)
            try books.confirmPayment(id, at: books.now)
        }
        books.tick(until: books.now.addingTimeInterval(60))
        #expect(books.ledger.group("pj-drone")?.project?.status == .archived)
    }

    @Test func hackathonKit() {
        let books = DemoFixture.load()
        let report = books.projectReport("pj-hackathon")
        #expect(Money.format(report.spent) == "₹18,400")
        #expect(Int((Double(report.spent) * 100 / Double(report.budget!)).rounded()) == 92)
        #expect(Money.format(report.budget! - report.spent) == "₹1,600")
        #expect(Set(report.nets.values) == [0])
        #expect(Format.short(books.day(of: books.ledger.group("pj-hackathon")!.project!.closedAt!)) == "30 Aug")
    }
}
