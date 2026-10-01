import Foundation
import UIKit

/// Builds an export file in the cache folder for the share sheet (screens-settings §9): a CSV from
/// the Domain rows, or an A4 PDF with one table per ticked group and its total. The file is named
/// after the range ("Paybak records 1 Sep – 30 Sep 2026.pdf").
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
        let url = folder.appending(path: "Paybak records \(rangeLine).\(format.rawValue)")
        switch format {
        case .csv:
            try Data(ExportCSV.make(records, defaultCurrency: books.defaultCurrency).utf8).write(to: url, options: .atomic)
        case .pdf:
            let rows = books.exportGroups(from: start, to: end).filter { groups.contains($0.id) }
            try pdf(records, groups: rows, rangeLine: rangeLine, defaultCurrency: books.defaultCurrency).write(to: url, options: .atomic)
        }
        return url
    }

    // MARK: PDF

    private static let page = CGRect(x: 0, y: 0, width: 595, height: 842)
    /// `text/primary` and `text/secondary`, fixed so the page stays black on white in Dark Mode.
    private static let primaryText = UIColor(red: 0x0A / 255, green: 0x0A / 255, blue: 0x0A / 255, alpha: 1)
    private static let secondaryText = UIColor(red: 0x6B / 255, green: 0x6B / 255, blue: 0x6B / 255, alpha: 1)
    private static let margin: CGFloat = 40
    /// Date · Title · Paid by · Amount, as fractions of the text width.
    private static let columns: [CGFloat] = [0.16, 0.46, 0.18, 0.20]

    private static func pdf(_ records: [ExportRecord], groups: [ExportGroupRow], rangeLine: String, defaultCurrency: String) -> Data {
        UIGraphicsPDFRenderer(bounds: page).pdfData { context in
            var y = margin
            let width = page.width - margin * 2

            func newPageIfNeeded(_ height: CGFloat) {
                if y + height > page.height - margin - 24 {
                    footer()
                    context.beginPage()
                    y = margin
                }
            }

            func footer() {
                draw("Paybak never moves money.", font: .medium, size: 9, color: secondaryText,
                     in: CGRect(x: margin, y: page.height - margin, width: width, height: 14))
            }

            context.beginPage()
            y += draw("Paybak records", font: .extraBold, size: 24, in: CGRect(x: margin, y: y, width: width, height: 32))
            y += draw(rangeLine, font: .medium, size: 12, color: secondaryText, in: CGRect(x: margin, y: y, width: width, height: 18)) + 16
            for group in groups {
                let groupRecords = records.filter { $0.groupKey == group.id }
                newPageIfNeeded(60)
                y += draw(group.name, font: .bold, size: 15, in: CGRect(x: margin, y: y, width: width, height: 22)) + 4
                y += row(["Date", "Title", "Paid by", "Amount"], font: .semiBold, color: secondaryText, y: y, width: width) + 4
                if groupRecords.isEmpty {
                    y += draw("Nothing in this range.", font: .regular, size: 10, color: secondaryText,
                              in: CGRect(x: margin, y: y, width: width, height: 16))
                }
                for record in groupRecords {
                    let shares = record.shares.map { "\($0.name) \(Money.format($0.amount, record.currency))" }.joined(separator: " · ")
                    newPageIfNeeded(shares.isEmpty ? 18 : 32)
                    y += row([paybak.Format.short(record.date), "\(record.kind.rawValue): \(record.title)", record.paidBy,
                              Money.format(record.amount, record.currency)], font: .regular, color: primaryText, y: y, width: width)
                    if !shares.isEmpty {
                        y += draw(shares, font: .regular, size: 8, color: secondaryText,
                                  in: CGRect(x: margin + width * columns[0], y: y, width: width * (1 - columns[0]), height: 14))
                    }
                    y += 2
                }
                let total = groupRecords.filter { $0.kind != .payment }.reduce(0) { $0 + $1.defaultAmount }
                y += row(["", "Total", "", Money.format(total, defaultCurrency)], font: .bold, color: primaryText, y: y, width: width) + 16
            }
            footer()
        }
    }

    /// One table row in the four columns; returns its height.
    private static func row(_ cells: [String], font: PBFont, color: UIColor, y: CGFloat, width: CGFloat) -> CGFloat {
        var x = margin
        for (index, cell) in cells.enumerated() {
            let cellWidth = width * columns[index]
            draw(cell, font: font, size: 10, color: color, alignment: index == cells.count - 1 ? .right : .left,
                 in: CGRect(x: x, y: y, width: cellWidth - 6, height: 16))
            x += cellWidth
        }
        return 16
    }

    /// Draws one line of text (truncating) and returns the rect's height.
    @discardableResult
    private static func draw(_ text: String, font: PBFont, size: CGFloat, color: UIColor = primaryText,
                             alignment: NSTextAlignment = .left, in rect: CGRect) -> CGFloat {
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = alignment
        paragraph.lineBreakMode = .byTruncatingTail
        let attributes: [NSAttributedString.Key: Any] = [.font: font.uiFont(size: size), .foregroundColor: color, .paragraphStyle: paragraph]
        (text as NSString).draw(with: rect, options: [.usesLineFragmentOrigin, .truncatesLastVisibleLine], attributes: attributes, context: nil)
        return rect.height
    }
}
