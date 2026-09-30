import Foundation
import Testing
@testable import paybak

/// verify.py `check_splits`.
struct SplitsTests {
    @Test func equalWithRotation() {
        let (four, _) = Splits.equal(rupees(2800), among: ["me", "priya", "esha", "dev"])
        #expect(Set(four.values) == [rupees(700)])
        let (first, counter) = Splits.equal(rupees(1000), among: ["me", "priya", "esha"], counter: 0)
        #expect(first == ["me": 33_334, "priya": 33_333, "esha": 33_333])
        let (second, _) = Splits.equal(rupees(1000), among: ["me", "priya", "esha"], counter: counter)
        #expect(second == ["me": 33_333, "priya": 33_334, "esha": 33_333])
    }

    @Test func exactFooter() {
        let status = Splits.exactStatus(total: rupees(2800), amounts: [rupees(700), rupees(700), rupees(700), rupees(550)])
        #expect(status.remaining == rupees(150))
        #expect(status.left == "₹150 left")
        #expect(status.detail == "₹2,650 of ₹2,800")
        #expect(Splits.exactStatus(total: rupees(100), amounts: [rupees(120)]).left == "₹20 over")
    }

    @Test func weighted() {
        let (percent, _) = Splits.weighted(rupees(1000), weights: ["a": 3333, "b": 3333, "c": 3334], order: ["a", "b", "c"])
        #expect(percent.values.reduce(0, +) == rupees(1000))
        let (shares, _) = Splits.weighted(rupees(2800), weights: ["a": 2, "b": 1, "c": 1], order: ["a", "b", "c"])
        #expect(shares == ["a": rupees(1400), "b": rupees(700), "c": rupees(700)])
    }

    @Test func receipt() {
        let items: [Itemized.Item] = [
            .init(label: "Chicken biryani", amount: rupees(430), personIds: ["dev"]),
            .init(label: "Paneer tikka", amount: rupees(370), personIds: ["esha"]),
            .init(label: "Fish and chips", amount: rupees(450), personIds: ["me"]),
            .init(label: "Chocolate brownie", amount: rupees(240), personIds: ["me"]),
            .init(label: "Masala fries", amount: rupees(240), personIds: ["me", "esha", "dev"]),
            .init(label: "Fresh lime soda ×3", amount: rupees(270), personIds: ["me", "esha", "dev"]),
        ]
        let result = Splits.itemized(items: items, total: rupees(2300), order: ["me", "esha", "dev"])
        #expect(result.subtotals == ["me": rupees(860), "esha": rupees(540), "dev": rupees(600)])
        #expect(result.shares == ["me": rupees(989), "esha": rupees(621), "dev": rupees(690)])
    }

    @Test func installmentsGiveTheLeftoverToTheEarliest() {
        #expect(Splits.installments(rupees(6000), count: 3) == [rupees(2000), rupees(2000), rupees(2000)])
        #expect(Splits.installments(100, count: 3) == [34, 33, 33])
    }
}
