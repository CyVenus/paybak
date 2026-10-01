import Foundation
import Testing
@testable import paybak

/// The Insights report copy (screens-insights-ai §2.6–§2.8) on the demo at Figma parity.
struct InsightsPageTests {
    private let september = YearMonth(year: 2026, month: 9)

    @Test func septemberPage() {
        let page = DemoFixture.load().insightsPage(september)
        #expect(page.title == "September 2026")
        #expect(page.total == "₹23,300")
        #expect(page.trend == "Up 5% from August")
        #expect(page.canGoBack)
        #expect(!page.canGoForward)
        #expect(page.chart.map(\.label) == ["Apr", "May", "Jun", "Jul", "Aug", "Sep"])
        #expect(page.chartLabel.hasPrefix("Your share by month: April ₹18,400, May ₹21,950"))
        #expect(page.categories.map { "\($0.title) \($0.caption) \($0.amount)" }
            == ["Rent 51% ₹12,000", "Food 17% ₹3,850", "Stays 15% ₹3,600", "Fun 8% ₹1,800", "Travel 5% ₹1,200", "Bills 4% ₹850"])
        #expect(page.categories.first?.progress == 0.51)
        #expect(page.groups.map { "\($0.title) \($0.caption) \($0.amount)" }
            == ["Flat 302 55% ₹12,850", "Goa Trip 34% ₹7,900", "Without a group 11% ₹2,550"])
        #expect(page.groups.map(\.leading) == [.icon("home"), .icon("plane"), .icon("people")])
        #expect(page.groups.last?.target == nil)
        #expect(page.loansTitle == "Lent vs borrowed since April")
        #expect(page.lent == "₹4,500")
        #expect(page.borrowed == "₹0")
        #expect(page.loans.map(\.text) == ["Kabir · Bike service"])
        #expect(page.loans.first?.badge == "Paid back")
    }

    /// Friends: each share is spread over the other people on the expense, so the bars add up to
    /// the month's total and the percentages to 100.
    @Test func friendsAddUpToTheTotal() {
        let books = DemoFixture.load()
        let friends = books.insightFriends(september)
        #expect(friends.reduce(0) { $0 + $1.amount } == books.insights(september).total)
        #expect(friends.reduce(0) { $0 + $1.percent } == 100)
        #expect(friends.map(\.amount) == friends.map(\.amount).sorted(by: >))
        #expect(!friends.contains { $0.personId == Person.me })
    }

    @Test func previousMonthComparesWithItsOwnPrevious() {
        let page = DemoFixture.load().insightsPage(YearMonth(year: 2026, month: 8))
        #expect(page.title == "August 2026")
        #expect(page.total == "₹22,200")
        #expect(page.trend == "Up 8% from July")
        #expect(page.canGoForward)
        #expect(page.chart.map(\.label) == ["Mar", "Apr", "May", "Jun", "Jul", "Aug"])
        #expect(page.loansTitle == "Lent vs borrowed since March")
    }

    @Test func emptyMonth() {
        let books = DemoFixture.load()
        let first = books.firstInsightsMonth!
        let page = books.insightsPage(first.adding(months: -1))
        #expect(page.isEmpty)
        #expect(page.total == "₹0")
        #expect(page.trend == nil)
        #expect(!page.canGoBack)
        #expect(page.emptyLine == "No shared expenses in \(first.adding(months: -1).name).")
    }

    @Test func trendWording() {
        let august = YearMonth(year: 2026, month: 8)
        #expect(Books.trend(current: 9_000, previous: 10_000, previousMonth: august) == "Down 10% from August")
        #expect(Books.trend(current: 10_000, previous: 10_000, previousMonth: august) == "Same as August")
        #expect(Books.trend(current: 10_000, previous: 0, previousMonth: august) == nil)
    }
}
