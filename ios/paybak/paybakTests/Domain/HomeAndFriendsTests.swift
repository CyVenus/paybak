import Foundation
import Testing
@testable import paybak

/// verify.py `check_home_and_friends`.
struct HomeAndFriendsTests {
    private let books = DemoFixture.load()

    @Test func homeTotals() {
        let totals = books.homeTotals()
        #expect(Money.format(totals.owed, sign: .signed) == "+₹2,900")
        #expect(totals.owedCaption == "from 4 people")
        #expect(Money.format(-totals.owe, sign: .signed) == "−₹1,850")
        #expect(totals.oweCaption == "across 2 groups")
        #expect("You still owe \(Money.format(totals.owe)) and are owed \(Money.format(totals.owed))."
            == "You still owe ₹1,850 and are owed ₹2,900.")
    }

    @Test func friendNets() {
        #expect(books.firstNames(books.friendNets())
            == ["Rohan": 800, "Priya": 700, "Esha": 700, "Dev": 700, "Kabir": -1400, "Meera": -450, "Ananya": 0])
    }

    @Test func dueSoon() {
        let rows = books.dueSoon().map { ($0.title, $0.detail, $0.amount / 100, $0.badge, $0.action) }
        #expect(rows.count == 2)
        #expect(rows[0] == ("Rohan", "Movie tickets", 800, "Overdue 3 days", .remind))
        #expect(rows[1] == ("Goa Trip", "Your share", 1400, "Due Fri", .settle))
    }

    @Test func recentActivity() {
        let recent = books.recentActivity(timeline: books.timeline())
        #expect(recent.map(\.title) == ["Dinner at Olive Garden", "Priya paid you", "Electricity bill"])
        #expect(recent.map(\.date) == ["Today", "Yesterday", "26 Sep"])
        #expect(recent[0].subtitle == "You paid · 4 people")
        #expect(recent[0].amount == "₹2,800")
        #expect(recent[1].subtitle == "UPI")
        #expect(recent[1].amount == "₹1,050")
        #expect(recent[2].subtitle == "Flat 302 · You owe")
        #expect(recent[2].amount == "−₹450")
        #expect(books.iOwePayer(books.ledger.expense("e-flat-elec-09")!))
    }

    @Test func homeState() {
        let snapshot = LedgerSnapshot(books)
        #expect(snapshot.home.state == .active)
        #expect(snapshot.home.pendingClaims.isEmpty)
        #expect(LedgerSnapshot(DemoFixture.load("allSettled")).home.state == .allSettled)
        #expect(LedgerSnapshot(DemoFixture.load("empty")).home.state == .firstDay)
        #expect(LedgerSnapshot(DemoFixture.load("eshaClaimsPayment")).home.pendingClaims.count == 1)
    }

    @Test func friendsOrder() {
        #expect(books.friendBalances().map(\.person.firstName) == ["Rohan", "Priya", "Esha", "Dev", "Kabir", "Meera", "Ananya"])
    }

    @Test func settleUp() {
        let (pay, get) = books.settleRows()
        let today = books.today
        #expect(pay.map { [books.firstName($0.friend), "\($0.amount / 100)", $0.context, Format.dueBadge($0.due!, today: today)] }
            == [["Kabir", "1400", "Goa Trip", "Due Fri"], ["Meera", "450", "Flat 302", "Due Mon"]])
        #expect(get.map { [books.firstName($0.friend), "\($0.amount / 100)", $0.context, Format.dueBadge($0.due!, today: today)] }
            == [["Rohan", "800", "Movie tickets", "Overdue 3 days"], ["Priya", "700", "Dinner at Olive Garden", "Due Sun"],
                ["Esha", "700", "Dinner at Olive Garden", "Due Sun"], ["Dev", "700", "Dinner at Olive Garden", "Due Sun"]])
        #expect(pay.map { Format.day($0.due!) } == ["Fri 2 Oct", "Mon 5 Oct"])
        #expect(books.homeTotals().oweCaption.replacingOccurrences(of: "across ", with: "") == "2 groups")
    }

    @Test func askPaybak() {
        #expect(books.whoOwesAnswer()
            == "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner.")
        let goa = books.openItems().first { $0.kind == .group && $0.ref == "g-goa" }!
        #expect("Your Goa Trip share of \(Money.format(goa.amount)) is due \(Format.day(goa.due!))."
            == "Your Goa Trip share of ₹1,400 is due Fri 2 Oct.")
    }
}
