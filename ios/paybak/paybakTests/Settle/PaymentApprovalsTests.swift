import Foundation
import Testing
@testable import paybak

/// The payer's side of a confirm (`PaymentApprovals`): which changes count as a payment of yours just
/// approved or just recorded, and the payment-approved scene's headline.
struct PaymentApprovalsTests {
    private let now = DemoFixture.figmaNow

    @Test func confirmingYourPaymentApprovesIt() throws {
        let before = DemoFixture.load("paymentToMeeraPending")
        var after = before
        try after.confirmPayment("pay-me-meera", at: now)
        let approved = PaymentApprovals.approved(from: before.ledger, to: after.ledger)
        #expect(approved.map(\.id) == ["pay-me-meera"])
        #expect(PaymentApprovals.headline(for: approved, in: after.ledger) == "Meera confirmed ₹450")
    }

    @Test func confirmingAClaimToYouIsNoApproval() throws {
        let before = DemoFixture.load("eshaClaimsPayment")
        var after = before
        try after.confirmPayment("pay-esha-olive", at: now)
        #expect(PaymentApprovals.approved(from: before.ledger, to: after.ledger).isEmpty)
    }

    @Test func otherChangesAreNoApprovals() throws {
        let pending = DemoFixture.load("paymentToMeeraPending")
        var cancelled = pending
        try cancelled.cancelPayment("pay-me-meera")
        #expect(PaymentApprovals.approved(from: pending.ledger, to: cancelled.ledger).isEmpty)
        var notReceived = DemoFixture.load("paymentToMeeraPending")
        try notReceived.markNotReceived("pay-me-meera", note: "Not yet", at: now)
        #expect(PaymentApprovals.approved(from: pending.ledger, to: notReceived.ledger).isEmpty)

        let confirmed = DemoFixture.load("paymentToMeeraConfirmed").ledger
        // Confirmed before the change, or never pending (a whole ledger loaded at once).
        #expect(PaymentApprovals.approved(from: confirmed, to: confirmed).isEmpty)
        #expect(PaymentApprovals.approved(from: Ledger(), to: confirmed).isEmpty)
    }

    @Test func recordingAPaymentToAFriend() throws {
        let before = DemoFixture.load()
        var paid = before
        let id = try paid.recordPayment(
            PaymentDraft(fromId: Person.me, toId: "p-meera", amount: 45_000, currency: "INR", date: DemoFixture.figmaDay),
            at: now
        )
        #expect(PaymentApprovals.recorded(from: before.ledger, to: paid.ledger).map(\.id) == [id])
        #expect(PaymentApprovals.recorded(from: paid.ledger, to: paid.ledger).isEmpty)

        // A claim a friend records is for you to confirm, not to wait on.
        var claimed = before
        _ = try claimed.recordPayment(
            PaymentDraft(fromId: "p-esha", toId: Person.me, amount: 70_000, currency: "INR", date: DemoFixture.figmaDay,
                         recordedBy: "p-esha"),
            at: now
        )
        #expect(PaymentApprovals.recorded(from: before.ledger, to: claimed.ledger).isEmpty)
    }

    @Test func headlineForSeveralPayments() throws {
        let ledger = DemoFixture.load("paymentToMeeraPending", "paymentToKabirPending").ledger
        let meera = try #require(ledger.payment("pay-me-meera"))
        let kabir = try #require(ledger.payment("pay-me-kabir"))
        var meeraAgain = meera
        meeraAgain.id = "pay-me-meera-2"
        meeraAgain.amount = 20_000

        #expect(PaymentApprovals.headline(for: [meera, meeraAgain], in: ledger) == "Meera confirmed ₹650")
        #expect(PaymentApprovals.headline(for: [meera, kabir], in: ledger) == "Meera and Kabir confirmed your payments")
        var meeraInEuros = meeraAgain
        meeraInEuros.currency = "EUR"
        #expect(PaymentApprovals.headline(for: [meera, meeraInEuros], in: ledger) == "Meera confirmed your payments")
    }
}
