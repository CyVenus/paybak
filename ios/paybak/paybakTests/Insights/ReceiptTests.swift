import Foundation
import Testing
import UIKit
@testable import paybak

/// Receipt reading and the scan math (screens-insights-ai §4.6, app-architecture §4.1–§4.2).
struct ReceiptTests {
    private let labels = ["Chicken biryani", "Paneer tikka", "Fish and chips", "Chocolate brownie", "Masala fries", "Fresh lime soda ×3"]

    /// Real OCR on the bundled Leopold Cafe receipt reads the designed items and totals.
    @Test func readsTheBundledReceipt() async throws {
        let image = try #require(UIImage(named: "art-receipt-full"))
        let scan = try await ReceiptReader.read(image, currency: "INR")
        #expect(scan.merchant == "Leopold Cafe")
        #expect(scan.date == DemoFixture.day(2026, 9, 30))
        #expect(scan.time == "1:15 pm")
        #expect(scan.items.map(\.label) == labels)
        #expect(scan.items.map(\.amount) == [430, 370, 450, 240, 240, 270].map { rupees($0) })
        #expect(scan.subtotal == rupees(2000))
        #expect(scan.taxes.map { "\($0.label) \($0.amount)" } == ["GST 5% \(rupees(100))"])
        #expect(scan.tip?.amount == rupees(200))
        #expect(scan.total == rupees(2300))
    }

    /// The iOS recogniser's reading of "₹" as 7, R, $ or {: the receipt's own sums fix it.
    @Test func reconcilesMisreadRupeeSigns() throws {
        let rows: [(String, String?)] = [
            ("Leopold Cafe", nil), ("Wed 30 Sep 2026 • 1:15 pm", nil),
            ("Chicken biryani", "·430"), ("Paneer tikka", "$370"), ("Fish and chips", "$450"), ("Chocolate brownie", "·240"),
            ("Masala fries", "R240"), ("Fresh lime soda ×3", "·270"), ("Subtotal", "72,000"), ("GST 5%", "{100"),
            ("Tip 10%", "7200"), ("Total", "72,300"), ("Thank you. Visit again.", nil),
        ]
        let texts = rows.enumerated().flatMap { index, row in
            let y = Double(index) * 0.05
            let left = ReceiptText(text: row.0, minX: 0.07, maxX: 0.4, minY: y, maxY: y + 0.03)
            return [left] + (row.1.map { [ReceiptText(text: $0, minX: 0.8, maxX: 0.93, minY: y + 0.002, maxY: y + 0.028)] } ?? [])
        }
        let scan = try #require(ReceiptParser.parse(texts, currency: "INR"))
        #expect(scan.items.map(\.amount) == [430, 370, 450, 240, 240, 270].map { rupees($0) })
        #expect(scan.subtotal == rupees(2000))
        #expect(scan.tip?.amount == rupees(200))
        #expect(scan.total == rupees(2300))
    }

    @Test func nothingReadableIsNil() {
        #expect(ReceiptParser.parse([ReceiptText(text: "Blurry", minX: 0, maxX: 1, minY: 0, maxY: 0.1)], currency: "INR") == nil)
        #expect(ReceiptParser.reading("Table 12", currency: "INR") == nil)
    }

    /// Assign items: ₹860 / ₹540 / ₹600 of items become ₹989 / ₹621 / ₹690 with GST and tip.
    @Test func assignmentSharesTaxAndTipInProportion() {
        var review = ReceiptReview(leopold, today: DemoFixture.figmaDay)
        let order = [Person.me, "p-esha", "p-dev"]
        #expect(review.foundLine == "We found 6 items.")
        #expect(review.dateLine == "Wed 30 Sep · 1:15 pm")
        #expect(review.total == rupees(2300))
        #expect(review.unassignedCount == 6)
        review.assignment = [["p-dev"], ["p-esha"], [Person.me], [Person.me], Set(order), Set(order)]
        #expect(review.sharedCaption(4, currency: "INR") == "Shared by 3 · ₹80 each")
        #expect(review.sharedCaption(0, currency: "INR") == nil)
        let shares = review.shares(order: order)
        #expect(order.map { shares[$0] } == [989, 621, 690].map { rupees($0) })
        #expect(ScanAssignPage.chargesNote(review.charges) == "Includes GST and tip")

        let draft = review.expenseDraft(order: order, currency: "INR", receipt: nil)
        #expect(draft.title == "Lunch at Leopold Cafe")
        #expect(draft.category == .food)
        #expect(draft.amount == rupees(2300))
        #expect(draft.splitMode == .itemized)
        #expect(draft.rows.map(\.share) == [989, 621, 690].map { rupees($0) })
    }

    @Test func editsAreCheckedAgainstTheSubtotal() {
        var review = ReceiptReview(leopold, today: DemoFixture.figmaDay)
        review.items[0].amount = rupees(330)
        #expect(review.mismatchLine(currency: "INR") == "Items add up to ₹1,900, the subtotal is ₹2,000.")
        review.addItem("Water", amount: rupees(100))
        #expect(review.addsUp)
        #expect(review.assignment.count == 7)
    }

    /// The form's Split row: the saved assignment comes back, without anyone since taken off the
    /// expense, and its draft leaves the form's title alone.
    @Test func reassignsAScannedExpense() throws {
        let draft = Scenario.leopoldDraft(on: DemoFixture.figmaDay)
        let itemized = try #require(draft.itemized)
        let review = ReceiptReview(reassigning: itemized, among: [Person.me, "p-esha"], on: DemoFixture.figmaDay)
        #expect(review.total == rupees(2300))
        #expect(review.assignment[0].isEmpty)
        #expect(review.assignment[4] == [Person.me, "p-esha"])
        #expect(review.unassignedCount == 1)
        #expect(review.expenseDraft(order: [Person.me, "p-esha"], currency: "INR", receipt: nil).title.isEmpty)
    }

    @Test func aFutureReceiptDateIsToday() {
        var scan = leopold
        scan.date = DemoFixture.day(2027, 1, 5)
        #expect(ReceiptReview(scan, today: DemoFixture.figmaDay).date == DemoFixture.figmaDay)
    }

    @Test func titlesByMealTime() {
        #expect(ReceiptReview.title(merchant: "Cafe X", time: "8:05 am", category: .food) == "Breakfast at Cafe X")
        #expect(ReceiptReview.title(merchant: "Cafe X", time: "5:30 pm", category: .food) == "Snacks at Cafe X")
        #expect(ReceiptReview.title(merchant: "Cafe X", time: "9:40 pm", category: .food) == "Dinner at Cafe X")
        #expect(ReceiptReview.title(merchant: "Hardware Co", time: "1:15 pm", category: .other) == "Hardware Co")
    }

    private var leopold: ReceiptScan {
        ReceiptScan(
            merchant: "Leopold Cafe", date: DemoFixture.day(2026, 9, 30), time: "1:15 pm",
            items: zip(labels, [430, 370, 450, 240, 240, 270]).map { .init(label: $0, amount: rupees(Double($1))) },
            subtotal: rupees(2000), taxes: [.init(label: "GST 5%", amount: rupees(100))],
            tip: .init(label: "Tip 10%", amount: rupees(200)), total: rupees(2300)
        )
    }
}
