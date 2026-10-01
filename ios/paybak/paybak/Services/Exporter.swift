import Foundation
import UIKit

/// Builds an export file in the cache folder for the share sheet (screens-settings §9): a CSV from
/// the Domain rows, or an A4 PDF with one table per group that has records and its expenses total,
/// laid out as Android's `RecordsPdf`. The file is named after the range ("Paybak records 1 Sep –
/// 30 Sep 2026.pdf"); earlier exports are cleared first.
enum Exporter {
    enum Format: String {
        case csv
        case pdf
    }

    static func export(_ books: Books, from start: LocalDay, to end: LocalDay, groups: Set<String>, format: Format) throws -> URL {
        let records = books.exportRecords(from: start, to: end, groups: groups)
        let rangeLine = paybak.Format.exportRange(start, end)
        let folder = URL.cachesDirectory.appending(path: "exports", directoryHint: .isDirectory)
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
        for old in (try? FileManager.default.contentsOfDirectory(at: folder, includingPropertiesForKeys: nil)) ?? [] {
            try? FileManager.default.removeItem(at: old)
        }
        let url = folder.appending(path: "Paybak records \(rangeLine).\(format.rawValue)")
        switch format {
        case .csv:
            try Data(ExportCSV.make(records, defaultCurrency: books.defaultCurrency).utf8).write(to: url, options: .atomic)
        case .pdf:
            try RecordsPDF(defaultCurrency: books.defaultCurrency).render(rangeLine: rangeLine, records: records)
                .write(to: url, options: .atomic)
        }
        return url
    }
}

/// The PDF export (screens-settings §9, proposal): "Paybak records", the range, then one section per
/// group with a table (Date · Title · Paid by · Amount · Each person's share) and its expenses total,
/// on A4 pages with the footer "Paybak never moves money." Positions are Android's, in points, with
/// `y` as the text baseline.
private struct RecordsPDF {
    let defaultCurrency: String

    private static let page = CGRect(x: 0, y: 0, width: 595, height: 842)
    private static let margin: CGFloat = 40
    private static let footerY: CGFloat = 842 - 24
    private static let line: CGFloat = 15
    /// Table columns: left edges (Amount is right-aligned to its column's end).
    private static let colDate: CGFloat = margin
    private static let colTitle: CGFloat = 96
    private static let colPaidBy: CGFloat = 256
    private static let colAmountEnd: CGFloat = 408
    private static let colShares: CGFloat = 420
    private static let widthTitle: CGFloat = 150
    private static let widthPaidBy: CGFloat = 80
    private static let widthShares: CGFloat = 595 - margin - colShares

    /// `text/primary`, `text/secondary` and the divider, fixed so the page stays black on white.
    private static let ink = UIColor(red: 0x0A / 255, green: 0x0A / 255, blue: 0x0A / 255, alpha: 1)
    private static let secondary = UIColor(red: 0x6B / 255, green: 0x6B / 255, blue: 0x6B / 255, alpha: 1)
    private static let rule = UIColor(red: 0xEB / 255, green: 0xEB / 255, blue: 0xEB / 255, alpha: 1)

    struct Style {
        let font: UIFont
        let color: UIColor
    }

    private static let title = Style(font: PBFont.bold.uiFont(size: 22), color: ink)
    private static let subtitle = Style(font: PBFont.regular.uiFont(size: 12), color: secondary)
    private static let heading = Style(font: PBFont.bold.uiFont(size: 14), color: ink)
    private static let label = Style(font: PBFont.bold.uiFont(size: 9), color: secondary)
    private static let body = Style(font: PBFont.regular.uiFont(size: 10), color: ink)
    private static let total = Style(font: PBFont.bold.uiFont(size: 10), color: ink)

    func render(rangeLine: String, records: [ExportRecord]) -> Data {
        UIGraphicsPDFRenderer(bounds: Self.page).pdfData { context in
            var writer = Writer(context: context)
            writer.newPage()
            writer.text("Paybak records", x: Self.margin, baseline: writer.y + 22, Self.title)
            writer.y += 42
            writer.text(rangeLine, x: Self.margin, baseline: writer.y, Self.subtitle)
            writer.y += 28
            if records.isEmpty { writer.text("No records in this range.", x: Self.margin, baseline: writer.y, Self.body) }
            var sections: [(name: String, rows: [ExportRecord])] = []
            for record in records {
                if let index = sections.firstIndex(where: { $0.name == record.groupName && $0.rows.first?.groupKey == record.groupKey }) {
                    sections[index].rows.append(record)
                } else {
                    sections.append((record.groupName, [record]))
                }
            }
            for section in sections {
                self.section(section.name, rows: section.rows, writer: &writer)
            }
            writer.finishPage()
        }
    }

    private func section(_ name: String, rows: [ExportRecord], writer: inout Writer) {
        writer.ensureSpace(Self.line * 4)
        writer.text(name, x: Self.margin, baseline: writer.y, Self.heading)
        writer.y += 20
        writer.columnLabels()
        for record in rows {
            row(record, writer: &writer)
        }
        let spent = rows.filter { $0.kind == .expense }.reduce(0) { $0 + $1.defaultAmount }
        writer.ensureSpace(Self.line)
        writer.text("Expenses total", x: Self.colTitle, baseline: writer.y, Self.total)
        writer.rightText(Money.format(spent, defaultCurrency), end: Self.colAmountEnd, baseline: writer.y, Self.total)
        writer.y += Self.line * 2
    }

    private func row(_ record: ExportRecord, writer: inout Writer) {
        let shares = record.shares.map { "\($0.name) \(Money.format($0.amount, record.currency))" }
        let height = Self.line * CGFloat(max(shares.count, 1))
        if writer.ensureSpace(height) { writer.columnLabels() }
        writer.text(paybak.Format.short(record.date), x: Self.colDate, baseline: writer.y, Self.body)
        writer.text(Writer.fit(record.title, width: Self.widthTitle, Self.body), x: Self.colTitle, baseline: writer.y, Self.body)
        writer.text(Writer.fit(record.paidBy, width: Self.widthPaidBy, Self.body), x: Self.colPaidBy, baseline: writer.y, Self.body)
        writer.rightText(Money.format(record.amount, record.currency), end: Self.colAmountEnd, baseline: writer.y, Self.body)
        for (index, share) in shares.enumerated() {
            writer.text(Writer.fit(share, width: Self.widthShares, Self.body), x: Self.colShares,
                        baseline: writer.y + CGFloat(index) * Self.line, Self.body)
        }
        writer.y += height + 4
    }

    /// The page being drawn and the current baseline.
    struct Writer {
        let context: UIGraphicsPDFRendererContext
        var y: CGFloat = 0
        private var isOpen = false

        init(context: UIGraphicsPDFRendererContext) {
            self.context = context
        }

        mutating func newPage() {
            context.beginPage()
            isOpen = true
            y = RecordsPDF.margin
        }

        /// The footer on the page being finished.
        mutating func finishPage() {
            guard isOpen else { return }
            text("Paybak never moves money.", x: RecordsPDF.margin, baseline: RecordsPDF.footerY, RecordsPDF.subtitle)
            isOpen = false
        }

        /// Starts a new page when `height` doesn't fit on this one; true if it did.
        @discardableResult
        mutating func ensureSpace(_ height: CGFloat) -> Bool {
            if y + height <= RecordsPDF.footerY - RecordsPDF.line * 2 { return false }
            finishPage()
            newPage()
            return true
        }

        mutating func columnLabels() {
            text("DATE", x: RecordsPDF.colDate, baseline: y, RecordsPDF.label)
            text("TITLE", x: RecordsPDF.colTitle, baseline: y, RecordsPDF.label)
            text("PAID BY", x: RecordsPDF.colPaidBy, baseline: y, RecordsPDF.label)
            rightText("AMOUNT", end: RecordsPDF.colAmountEnd, baseline: y, RecordsPDF.label)
            text("EACH PERSON’S SHARE", x: RecordsPDF.colShares, baseline: y, RecordsPDF.label)
            y += 6
            let path = UIBezierPath()
            path.move(to: CGPoint(x: RecordsPDF.margin, y: y))
            path.addLine(to: CGPoint(x: RecordsPDF.page.width - RecordsPDF.margin, y: y))
            path.lineWidth = 1
            RecordsPDF.rule.setStroke()
            path.stroke()
            y += RecordsPDF.line
        }

        func text(_ text: String, x: CGFloat, baseline: CGFloat, _ style: Style) {
            (text as NSString).draw(at: CGPoint(x: x, y: baseline - style.font.ascender), withAttributes: Self.attributes(style))
        }

        func rightText(_ text: String, end: CGFloat, baseline: CGFloat, _ style: Style) {
            let width = (text as NSString).size(withAttributes: Self.attributes(style)).width
            self.text(text, x: end - width, baseline: baseline, style)
        }

        /// `text` cut with an ellipsis to fit `width`.
        static func fit(_ text: String, width: CGFloat, _ style: Style) -> String {
            func measure(_ string: String) -> CGFloat { (string as NSString).size(withAttributes: attributes(style)).width }
            guard measure(text) > width else { return text }
            var cut = text
            while !cut.isEmpty, measure(cut + "…") > width { cut.removeLast() }
            return cut.trimmingCharacters(in: .whitespaces) + "…"
        }

        private static func attributes(_ style: Style) -> [NSAttributedString.Key: Any] {
            [.font: style.font, .foregroundColor: style.color]
        }
    }
}
