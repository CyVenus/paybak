import UIKit

// STUB (app-architecture §4.1): Lane C fills this with Vision text recognition and the receipt parser,
// and keeps these names.
/// What a receipt photo says.
struct ReceiptScan: Hashable {
    struct Line: Hashable {
        var label: String
        var amount: Int64
    }

    var merchant: String?
    var date: LocalDay?
    var time: String?
    var items: [Line] = []
    var subtotal: Int64?
    var taxes: [Line] = []
    var tip: Line?
    var total: Int64?
}

/// Reads a receipt photo on the device. Throws when nothing useful was read.
enum ReceiptReader {
    enum ReadError: Error {
        case unreadable
    }

    static func read(_ image: UIImage) async throws -> ReceiptScan {
        throw ReadError.unreadable
    }
}
