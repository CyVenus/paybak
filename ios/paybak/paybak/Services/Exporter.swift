import Foundation

// STUB (app-architecture §4): Lane C fills this (CSV built from the ledger, PDF with
// UIGraphicsPDFRenderer), keeping these names.
/// Builds an export file in the cache folder for the share sheet.
enum Exporter {
    enum Format: String {
        case csv
        case pdf
    }

    static func export(_ books: Books, from start: LocalDay, to end: LocalDay, groups: Set<String>, format: Format) throws -> URL {
        throw CocoaError(.featureUnsupported)
    }
}
