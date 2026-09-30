import Foundation
import Testing
@testable import paybak

/// The Groups & Friends copy (screens-groups §2–§6) from the demo at Figma parity: every string the
/// refs show comes out of the records.
struct GroupsCopyTests {
    private let books = DemoFixture.load()

    @Test func groupsList() {
        let rows = books.groupSummaries().map { summary in (summary.group.name, books.groupRowCopy(summary)) }
        #expect(rows.map(\.0) == ["Goa Trip", "Flat 302", "Build a Drone", "College Gang", "Dubai Weekend", "Hackathon Kit"])
        #expect(rows.map(\.1.subtitle) == ["5 members · Due Fri 2 Oct", "3 members · Due Mon 5 Oct", "Project · 4 members", "6 members",
                                           "3 members · AED", "Project · Closed 30 Aug"])
        #expect(rows.map(\.1.trailing) == [.owe("₹1,400"), .owe("₹450"), .status("You’re settled"), .status("Settled"), .status("Settled"),
                                           .readOnly])
        let budget = rows[2].1.budget
        #expect(budget?.spent == "₹52,000 of ₹60,000")
        #expect(budget?.left == "₹8,000 left")
        #expect(abs((budget?.progress ?? 0) - 52.0 / 60) < 0.0001)
        #expect(rows.filter { $0.0 != "Build a Drone" }.allSatisfy { $0.1.budget == nil })
    }

    @Test func friendsList() {
        let rows = books.friendBalances().map { (books.firstName($0.id), books.friendRowCopy($0)) }
        #expect(rows.map(\.0) == ["Rohan", "Priya", "Esha", "Dev", "Kabir", "Meera", "Ananya"])
        #expect(rows.map(\.1.subtitle) == ["Movie tickets", "Due Sun 4 Oct", "Due Sun 4 Oct", "Due Sun 4 Oct", "Goa Trip · Due Fri 2 Oct",
                                           "Flat 302 · Due Mon 5 Oct", nil])
        #expect(rows.map(\.1.trailing) == [
            .owed("₹800", label: nil, overdue: "Overdue 3 days"),
            .owed("₹700", label: "Owes you", overdue: nil),
            .owed("₹700", label: "Owes you", overdue: nil),
            .owed("₹700", label: "Owes you", overdue: nil),
            .owe("₹1,400", label: "You owe"),
            .owe("₹450", label: "You owe"),
            .status("No balance"),
        ])
    }

    @Test func goaTripDetail() throws {
        let sheet = try #require(books.groupSheet("g-goa"))
        #expect(books.groupHeaderSubtitle(sheet) == "21–25 Sep · 5 members · ₹39,500 spent")
        let balance = books.groupBalanceCopy(sheet)
        #expect(balance == GroupBalanceCopy(tone: .owe, amount: "−₹1,400", caption: "You owe Kabir · Due Fri 2 Oct", showsSettleUp: true,
                                            settle: .pay(Transfer(from: Person.me, to: "p-kabir", amount: rupees(1400)), currency: "INR",
                                                         groupId: "g-goa")))
        let members = books.memberBalances(sheet)
        #expect(members.map(\.name) == ["You", "Kabir", "Priya", "Esha", "Dev"])
        #expect(members.map(\.subtitle) == ["Paid ₹6,500 · Share ₹7,900", "Paid ₹18,000 · Share ₹7,900", "Paid ₹3,500 · Share ₹7,900",
                                            "Paid ₹5,000 · Share ₹7,900", "Paid ₹6,500 · Share ₹7,900"])
        #expect(members.map(\.trailing) == [.owe("₹1,400", label: "You owe"), .owed("₹10,100", label: "Gets back"), .owe("₹4,400", label: "Owes"),
                                            .owe("₹2,900", label: "Owes"), .owe("₹1,400", label: "Owes")])
        #expect(books.groupTotalFootnote(sheet) == nil)
        #expect(sheet.expenses.map { books.groupExpenseSubtitle($0) } == [
            "Dev paid · Your share ₹500", "Dev paid · Your share ₹800", "Esha paid · Your share ₹1,000", "You paid · Your share ₹1,300",
            "Priya paid · Your share ₹700", "Kabir paid · Your share ₹3,600",
        ])
        #expect(sheet.expenses.map(\.title).last == "Villa (3 nights)")
        #expect(books.expenseDayLabel(sheet.expenses[0].date) == "Fri 25 Sep")
    }

    @Test func dubaiWeekendDetail() throws {
        let sheet = try #require(books.groupSheet("g-dubai"))
        #expect(books.groupHeaderSubtitle(sheet) == "6–8 Mar · 3 members · AED")
        #expect(books.groupBalanceCopy(sheet) == GroupBalanceCopy(tone: .settled, amount: "Settled", caption: "You paid Kabir AED 60 on 14 Mar",
                                                                  showsSettleUp: false))
        #expect(books.memberBalances(sheet).map(\.subtitle) == ["Paid AED 540 · Share AED 600", "Paid AED 960 · Share AED 600",
                                                               "Paid AED 300 · Share AED 600"])
        #expect(books.memberBalances(sheet).allSatisfy { $0.trailing == .status("Settled") })
        #expect(books.groupTotalFootnote(sheet) == "Total AED 1,800 · ≈ ₹41,118 at saved rates")
        #expect(sheet.simplifyFootnote == nil)
        #expect(books.expenseDayLabel(sheet.expenses[0].date) == "Sun 8 Mar")
    }

    @Test func newGroupIsEmpty() throws {
        let books = DemoFixture.load("weekendTrek")
        let sheet = try #require(books.groupSheet("g-trek"))
        #expect(books.groupHeaderSubtitle(sheet) == "Trip · 4 members · INR")
        #expect(books.groupBalanceCopy(sheet) == GroupBalanceCopy(tone: .settled, amount: "₹0", caption: nil, showsSettleUp: true))
    }

    @Test func groupSettings() {
        #expect(books.recurringValue("g-goa") == "None")
        #expect(books.recurringValue("g-flat302") == "3 rules")
        #expect(books.leaveCheck("g-goa") == .blocked(
            message: "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave.",
            settle: .pay(Transfer(from: Person.me, to: "p-kabir", amount: rupees(1400)), currency: "INR", groupId: "g-goa")
        ))
        #expect(books.leaveCheck("g-college") == .allowed)
        #expect(books.leaveCheck("g-dubai") == .allowed)
    }

    @Test func friendRohan() throws {
        let page = try #require(books.friendPage("p-rohan"))
        let balance = try #require(books.friendBalanceCopy(page))
        #expect(balance.label == "Rohan owes you")
        #expect(balance.amount == "+₹800")
        #expect(balance.caption == "Movie tickets · Due Sun 27 Sep")
        #expect(balance.overdue == "Overdue 3 days")
        #expect(balance.lead?.ref == "e-movie")
        let history = books.friendHistoryRows(page)
        #expect(history.map(\.title) == ["Movie tickets", "Farewell dinner"])
        #expect(history.map(\.subtitle) == ["You paid · Rohan owes ₹800", "College Gang · Settled"])
        #expect(history.map(\.amount) == ["₹1,600", "₹9,000"])
        #expect(history.map(\.date) == ["20 Sep", "12 Mar"])
        #expect(history.map(\.isOpen) == [true, false])
        let reminder = try #require(page.lastReminder)
        #expect(books.lastReminderText(reminder) + "." == "Last reminder sent today.")
        #expect(books.autoRemindersFootnote("p-rohan") == "Turn off to stop Paybak nudging Rohan.")
    }

    @Test func friendYouOweAndGuest() throws {
        let kabirPage = try #require(books.friendPage("p-kabir"))
        let kabir = try #require(books.friendBalanceCopy(kabirPage))
        #expect(kabir.tone == .owe)
        #expect(kabir.label == "You owe Kabir")
        #expect(kabir.amount == "−₹1,400")
        #expect(kabir.caption == "Goa Trip · Due Fri 2 Oct")
        #expect(kabir.overdue == nil)
        let ananya = try #require(books.friendPage("p-ananya"))
        #expect(books.friendBalanceCopy(ananya) == nil)
    }

    @Test func settledFriendShowsTheLastPayment() throws {
        let books = DemoFixture.load("allSettled")
        let page = try #require(books.friendPage("p-rohan"))
        let rohan = try #require(books.friendBalanceCopy(page))
        #expect(rohan.tone == .settled)
        #expect(rohan.amount == "Settled")
        #expect(rohan.caption == "Rohan paid you ₹800 on 30 Sep")
    }
}
