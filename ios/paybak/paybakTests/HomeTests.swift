import Foundation
import Testing
@testable import paybak

/// Where Home's rows lead and which designed Home the data draws (app-architecture §1.2, §2.3).
@MainActor
struct HomeTests {
    private let books = DemoFixture.load()

    @Test func rohanRemindsAboutTheMovieTickets() throws {
        let rohan = try #require(books.dueSoon().first)
        #expect(rohan.title == "Rohan")
        #expect(!rohan.isGroupShare)
        #expect(rohan.route == .friend("p-rohan"))
        #expect(rohan.actionRoute(in: books) == .remind(personId: "p-rohan", context: .expense("e-movie")))
    }

    @Test func goaTripSettlesWithKabirPrefilled() throws {
        let goa = try #require(books.dueSoon().last)
        #expect(goa.title == "Goa Trip")
        #expect(goa.isGroupShare)
        #expect(goa.route == .group("g-goa"))
        // Goa Trip is simplified: you pay Kabir the whole ₹1,400, by UPI (he has an ID).
        #expect(goa.actionRoute(in: books) == .recordPayment(RecordPaymentArgs(
            from: Person.me, to: "p-kabir", amount: 140_000, currency: "INR", method: .upi, context: .group("g-goa")
        )))
    }

    @Test func recentRowsOpenTheirRecords() {
        let recent = books.recentActivity(timeline: books.timeline())
        #expect(recent.map(\.route) == [.expense("e-olive"), .payment("pay-priya-groceries"), .expense("e-flat-elec-09")])
        #expect(recent.map(\.recordID) == ["e-olive", "pay-priya-groceries", "e-flat-elec-09"])
    }

    @Test func screenFollowsTheData() {
        #expect(LedgerSnapshot(books).home.screen == .homeActive)
        #expect(LedgerSnapshot(DemoFixture.load("empty")).home.screen == .homeFirstDay)
        #expect(LedgerSnapshot(DemoFixture.load("allSettled")).home.screen == .homeAllSettled)
        let claim = LedgerSnapshot(DemoFixture.load("eshaClaimsPayment")).home
        #expect(claim.screen == .homeConfirmPayment)
        #expect(claim.stateID == "confirmPayment")
        #expect(LedgerSnapshot(DemoFixture.load("eshaPaymentConfirmed")).home.screen == .homeActive)
    }

    @Test func confirmingEshasClaimUpdatesHome() throws {
        var books = DemoFixture.load("eshaClaimsPayment")
        let claim = try #require(LedgerSnapshot(books).home.pendingClaims.first)
        #expect(claim.title == "Esha says she paid you ₹700")
        #expect(claim.detail == "Dinner at Olive Garden · UPI · 9:12 pm")
        // A pending claim leaves the totals alone.
        #expect(LedgerSnapshot(books).home.totals.owed == 290_000)

        try books.confirmPayment(claim.id, at: DemoFixture.figmaNow)
        let home = LedgerSnapshot(books).home
        #expect(home.pendingClaims.isEmpty)
        #expect(Money.format(home.totals.owed, sign: .signed) == "+₹2,200")
        #expect(home.totals.owedCaption == "from 3 people")
        #expect(home.recent.first?.title == "Esha paid you")
        #expect(home.recent.first?.route == .payment(claim.id))
    }

    @Test func obligationContexts() {
        let direct = Obligation(debtor: "p-rohan", creditor: Person.me, amount: 1, title: "", kind: .direct, ref: "e-movie")
        #expect(direct.reminderContext == .expense("e-movie"))
        #expect(direct.paymentContext == .expense("e-movie"))
        let loan = Obligation(debtor: "p-kabir", creditor: Person.me, amount: 1, title: "", kind: .loan, ref: "l-kabir-bike")
        #expect(loan.reminderContext == .loan("l-kabir-bike"))
        #expect(loan.paymentContext == .loan("l-kabir-bike"))
        let project = Obligation(debtor: Person.me, creditor: "p-dev", amount: 1, title: "", kind: .project, ref: "pj-drone")
        #expect(project.reminderContext == .group("pj-drone"))
        #expect(project.paymentContext == .group("pj-drone"))
    }
}
