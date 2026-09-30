import Foundation
import os

/// `ledger.json` in Application Support (app-architecture §3.2, §3.5). The store encodes on the main
/// actor and hands the bytes over; writes run in order on a serial queue, atomically. A file that
/// can't be decoded is moved aside as `ledger.corrupt-<timestamp>.json` and the app starts empty.
nonisolated final class LedgerFile: Sendable {
    let url: URL
    private let queue = DispatchQueue(label: "app.paybak.paybak.ledger-file")
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "LedgerFile")

    init(url: URL) {
        self.url = url
    }

    static var standard: LedgerFile {
        LedgerFile(url: URL.applicationSupportDirectory.appending(path: "ledger.json"))
    }

    static func encoder() -> JSONEncoder {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        encoder.dateEncodingStrategy = .iso8601
        return encoder
    }

    static func decoder() -> JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return decoder
    }

    /// The saved ledger, nil when there's none (or it was unreadable and moved aside).
    func load() -> Ledger? {
        queue.sync {
            guard let data = try? Data(contentsOf: url) else { return nil }
            do {
                return try Self.decoder().decode(Ledger.self, from: data)
            } catch {
                Self.log.error("Could not read the ledger, starting empty: \(String(describing: error), privacy: .public)")
                let corrupt = url.deletingLastPathComponent().appending(path: "ledger.corrupt-\(Int(Date().timeIntervalSince1970)).json")
                try? FileManager.default.moveItem(at: url, to: corrupt)
                return nil
            }
        }
    }

    /// Writes `data` in order after earlier writes.
    func write(_ data: Data) {
        let url = url
        queue.async {
            do {
                try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
                try data.write(to: url, options: .atomic)
            } catch {
                Self.log.error("Could not save the ledger: \(String(describing: error), privacy: .public)")
            }
        }
    }

    func delete() {
        let url = url
        queue.async {
            try? FileManager.default.removeItem(at: url)
        }
    }

    /// Waits for pending writes (tests, and before the app is suspended).
    func flush() {
        queue.sync {}
    }
}
