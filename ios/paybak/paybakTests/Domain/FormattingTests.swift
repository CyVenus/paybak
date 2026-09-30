import Foundation
import Testing
@testable import paybak

/// verify.py `check_formatting`.
struct FormattingTests {
    private let today = DemoFixture.figmaDay

    @Test func money() {
        #expect(Money.format(rupees(2900), sign: .signed) == "+₹2,900")
        #expect(Money.format(-rupees(1850), sign: .signed) == "−₹1,850")
        #expect(Money.format(rupees(100_000)) == "₹1,00,000")
        #expect(Money.format(rupees(9_999_999.5)) == "₹99,99,999.50")
        #expect(Money.format(rupees(1800), "AED") == "AED 1,800")
        #expect(Money.format(rupees(1200), "EUR") == "€1,200")
        #expect(Money.format(1234, "JPY") == "¥1,234")
        #expect(Money.format(-rupees(450), sign: .debit) == "−₹450")
        #expect(Money.format(rupees(450), sign: .debit) == "₹450")
        #expect(Money.format(33_334) == "₹333.34")
    }

    @Test func conversion() {
        #expect(Money.convert(rupees(960), rate: Decimal(string: "22.85")!, from: "AED", to: "INR") == rupees(21936))
        #expect(Money.approximateLine(rupees(300), currency: "AED", rate: Rate(value: "22.90", to: "INR")) == "≈ ₹6,870 · ₹22.90 per AED")
    }

    @Test func dates() {
        #expect(Format.rowDate(DemoFixture.day(2026, 9, 26), today: today) == "26 Sep")
        #expect(Format.rowDate(DemoFixture.day(2025, 9, 26), today: today) == "26 Sep 2025")
        #expect(Format.rowDate(today, today: today) == "Today")
        #expect(Format.rowDate(DemoFixture.day(2026, 9, 29), today: today) == "Yesterday")
        #expect(Format.dayHeader(DemoFixture.day(2026, 9, 28), today: today) == "Mon 28 Sep")
        #expect(Format.dueBadge(DemoFixture.day(2026, 10, 2), today: today) == "Due Fri")
        #expect(Format.dueBadge(DemoFixture.day(2026, 9, 27), today: today) == "Overdue 3 days")
        #expect(Format.dueBadge(DemoFixture.day(2026, 9, 29), today: today) == "Overdue 1 day")
        #expect(Format.dueBadge(DemoFixture.day(2026, 10, 12), today: today) == "Due 12 Oct")
        #expect(Format.time(DemoFixture.moment(2026, 9, 30, 21, 12), calendar: DemoFixture.calendar) == "9:12 pm")
        #expect(Format.time(DemoFixture.moment(2026, 9, 30, 0, 5), calendar: DemoFixture.calendar) == "12:05 am")
        #expect(Format.dateRange(DemoFixture.day(2026, 9, 21), DemoFixture.day(2026, 9, 25)) == "21–25 Sep")
        #expect(Format.dateRange(DemoFixture.day(2026, 9, 28), DemoFixture.day(2026, 10, 2)) == "28 Sep – 2 Oct")
        #expect(Format.loanMetaDate(DemoFixture.day(2026, 6, 12), today: today) == "12 Jun")
        #expect(Format.loanMetaDate(today, today: DemoFixture.day(2026, 11, 3)) == "Wed 30 Sep")
        #expect(Format.fullRange(DemoFixture.day(2026, 9, 1), DemoFixture.day(2026, 9, 30)) == "1 Sep – 30 Sep 2026")
    }

    @Test func dayArithmetic() {
        #expect(DemoFixture.day(2026, 1, 31).adding(months: 1) == DemoFixture.day(2026, 2, 28))
        #expect(DemoFixture.day(2026, 1, 31).adding(months: 2, day: 31) == DemoFixture.day(2026, 3, 31))
        #expect(DemoFixture.day(2024, 2, 29).replacing(year: 2025) == DemoFixture.day(2025, 2, 28))
        #expect(today.weekday == 2)  // Wednesday
        #expect(today.adding(days: 34) == DemoFixture.day(2026, 11, 3))
        #expect(DemoFixture.day(2026, 12, 31).adding(days: 1) == DemoFixture.day(2027, 1, 1))
        #expect(LocalDay(string: "2026-09-30") == today)
        #expect(DemoFixture.day(2026, 9, 1).lastOfMonth == today)
    }
}
