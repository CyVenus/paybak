import Foundation
import Testing
@testable import paybak

/// Export records (screens-settings §9, domain.md §6.5) on the demo at Figma parity.
struct ExportRecordsTests {
    private let books = DemoFixture.load()

    @Test func rangeLabels() {
        #expect(books.exportRangeLabel(.thisMonth) == "1 Sep – 30 Sep 2026")
        #expect(books.exportRangeLabel(.last3Months) == "1 Jul – 30 Sep 2026")
        #expect(books.exportRangeLabel(.allTime) == "6 Mar – 30 Sep 2026")
        #expect(Format.exportRange(DemoFixture.day(2025, 12, 12), DemoFixture.day(2026, 1, 5)) == "12 Dec 2025 – 5 Jan 2026")
    }

    @Test func septemberTicksGroupsWithRecords() {
        let (start, end) = books.exportInterval(.thisMonth)
        let rows = books.exportGroups(from: start, to: end)
        #expect(rows.map(\.name) == ["Goa Trip", "Flat 302", "College Gang", "Dubai Weekend", "Build a Drone", "Hackathon Kit",
                                     "Without a group"])
        #expect(rows.filter(\.hasRecords).map(\.id) == ["g-goa", "g-flat302", "pj-drone", ExportGroupRow.withoutGroupID])
    }

    @Test func allTimeTicksEveryGroup() {
        let (start, end) = books.exportInterval(.allTime)
        let rows = books.exportGroups(from: start, to: end)
        #expect(rows.filter { !$0.hasRecords }.isEmpty)
    }

    @Test func withoutAGroupInSeptember() {
        let (start, end) = books.exportInterval(.thisMonth)
        let records = books.exportRecords(from: start, to: end, groups: [ExportGroupRow.withoutGroupID])
        let titles = records.map(\.title)
        #expect(titles.contains("Dinner at Olive Garden"))
        #expect(titles.contains("Movie tickets"))
        #expect(titles.contains("Priya Sharma paid you"))
        #expect(records.allSatisfy { $0.groupName == "Without a group" })
        #expect(records.map(\.date) == records.map(\.date).sorted())
        let dinner = records.first { $0.title == "Dinner at Olive Garden" }
        #expect(dinner?.shares.contains(ExportRecord.Share(name: "You", amount: rupees(700))) == true)
    }

    @Test func csvRows() {
        let (start, end) = books.exportInterval(.thisMonth)
        let records = books.exportRecords(from: start, to: end, groups: ["g-goa"])
        let csv = ExportCSV.make(records, defaultCurrency: "INR")
        let lines = csv.dropFirst().split(separator: "\r\n")
        #expect(csv.hasPrefix("\u{FEFF}Date,Group,Type,Title,Category,Paid by,Amount,Currency,Rate,Amount (INR),Shares"))
        #expect(lines.count == records.count + 1)
        #expect(lines.dropFirst().allSatisfy { $0.contains(",Goa Trip,") })
    }

    @Test func plainAmounts() {
        #expect(ExportCSV.plain(rupees(700), "INR") == "700.00")
        #expect(ExportCSV.plain(123_456, "INR") == "1234.56")
        #expect(ExportCSV.plain(-5, "INR") == "-0.05")
        #expect(ExportCSV.plain(1500, "JPY") == "1500.00")
    }

    @Test @MainActor func deleteBlockedMessage() {
        let totals = LedgerSnapshot(books).home.totals
        #expect(PrivacyScreen.blockedMessage(owe: totals.owe, owed: totals.owed, currency: "INR")
            == "You still owe ₹1,850 and are owed ₹2,900. Settle every balance before deleting your account.")
        #expect(PrivacyScreen.blockedMessage(owe: 0, owed: rupees(2900), currency: "INR")
            == "You’re still owed ₹2,900. Settle every balance before deleting your account.")
    }

    @Test @MainActor func exportFilesAreWritten() throws {
        let (start, end) = books.exportInterval(.thisMonth)
        let groups = Set(books.exportGroups(from: start, to: end).filter(\.hasRecords).map(\.id))
        // Each export clears the earlier files first, so read the PDF before writing the CSV.
        let pdf = try Exporter.export(books, from: start, to: end, groups: groups, format: .pdf)
        #expect(pdf.lastPathComponent == "Paybak records 1 Sep – 30 Sep 2026.pdf")
        #expect(try Data(contentsOf: pdf).starts(with: Data("%PDF".utf8)))
        let csv = try Exporter.export(books, from: start, to: end, groups: groups, format: .csv)
        #expect(try String(contentsOf: csv, encoding: .utf8).contains("Dinner at Olive Garden"))
    }
}
