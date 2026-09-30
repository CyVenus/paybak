import Foundation
import Testing
@testable import paybak

/// verify.py `check_insights` and `check_loans`.
struct InsightsAndLoansTests {
    private let september = YearMonth(year: 2026, month: 9)

    @Test func septemberInsights() {
        let books = DemoFixture.load()
        let report = books.insights(september)
        #expect(Money.format(report.total) == "₹23,300")
        let categories = report.rankedCategories.map { row in
            "\(row.category.name) \(row.percent)% \(Money.format(row.amount)) \((310 * Double(row.percent) / 100 * 10).rounded() / 10)"
        }
        #expect(categories == ["Rent 51% ₹12,000 158.1", "Food 17% ₹3,850 52.7", "Stays 15% ₹3,600 46.5",
                               "Fun 8% ₹1,800 24.8", "Travel 5% ₹1,200 15.5", "Bills 4% ₹850 12.4"])
        #expect(report.rankedGroups.map { "\($0.name) \($0.percent)% \(Money.format($0.amount))" }
            == ["Flat 302 55% ₹12,850", "Goa Trip 34% ₹7,900", "Without a group 11% ₹2,550"])
        let months = books.monthlyTotals(endingAt: september)
        let totals = months.map(\.total)
        #expect(totals.map { $0 / 100 } == [18400, 21950, 19600, 20600, 22200, 23300])
        #expect(totals.map { Int((120 * Double($0) / Double(totals.max()!)).rounded()) } == [95, 113, 101, 106, 114, 120])
        #expect(Books.trend(current: totals[5], previous: totals[4], previousMonth: months[4].month) == "Up 5% from August")
        let window = books.lentAndBorrowed(since: DemoFixture.day(2026, 4, 1))
        #expect(Money.format(window.lent) == "₹4,500")
        #expect(Money.format(window.borrowed) == "₹0")
        let food = report.categories.first { $0.category == .food }!.amount
        #expect("You spent \(Money.format(food)) on food in September — \(report.percent(of: .food))% of your \(Money.format(report.total)) share."
            == "You spent ₹3,850 on food in September — 17% of your ₹23,300 share.")
    }

    @Test func kabirLoan() throws {
        let books = DemoFixture.load()
        let kabir = try #require(books.ledger.loan("l-kabir-bike"))
        let rows = books.installments(of: kabir)
        let labels = rows.map { row -> String in
            let late = row.due!.days(to: row.paidOn!)
            return "Paid \(Format.short(row.paidOn!))" + (late > 0 ? " · \(late) day\(late == 1 ? "" : "s") late" : "")
        }
        #expect(labels == ["Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late"])
        #expect("\(kabir.reason!) · \(Format.loanMetaDate(kabir.date, today: books.today))" == "Bike service · 12 Jun")
        #expect("Paid back on \(Format.short(rows.last!.paidOn!))" == "Paid back on 14 Sep")
    }

    @Test func devLoan() throws {
        let books = DemoFixture.load("lendDev")
        let dev = try #require(books.ledger.loan("l-dev-laptop"))
        #expect(books.installments(of: dev).map { "\(Format.day($0.due!)) \(Money.format($0.amount))" }
            == ["Fri 30 Oct ₹2,000", "Mon 30 Nov ₹2,000", "Wed 30 Dec ₹2,000"])
        #expect("\(dev.reason!) · \(Format.loanMetaDate(dev.date, today: books.today))" == "Laptop repair · Today")
        #expect(books.friendNets()["p-dev"] == rupees(6700))
    }

    @Test func devLoanOverdue() throws {
        let books = DemoFixture.load("lendDevOverdue")
        #expect(books.now == DemoFixture.moment(2026, 11, 3, 10, 0))
        let dev = try #require(books.ledger.loan("l-dev-laptop"))
        #expect(Format.dueBadge(books.installments(of: dev)[0].due!, today: books.today) == "Overdue 4 days")
        let sent = books.ledger.reminders.filter { $0.loanId == "l-dev-laptop" }.map(\.sentAt).sorted()
        #expect(sent.map { Format.day(books.day(of: $0)) } == ["Wed 28 Oct", "Fri 30 Oct", "Mon 2 Nov"])
        #expect(books.loanReminderText(try #require(books.lastReminder(forLoan: dev.id))) == "Last reminder sent Mon 2 Nov")
        #expect("\(dev.reason!) · \(Format.loanMetaDate(dev.date, today: books.today))" == "Laptop repair · Wed 30 Sep")
        #expect(books.ledger.expense("e-goa-snacks") == nil)
    }
}
