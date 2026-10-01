import Foundation
import os

/// Add & Record's reads on the store (lane A, M3): today's exchange rates, and the groups and
/// currencies the forms offer.
extension LedgerStore {
    /// The bundled "today's rate" from `code` to the default currency; nil for the default itself.
    func todayRate(for code: String) -> Rate? {
        RateTable.bundled.rate(from: code, to: books.defaultCurrency)
    }

    /// Groups (not projects) you’re in, for the expense Group picker.
    var expenseGroups: [LedgerGroup] {
        ledger.groups.filter { !$0.isProject && $0.memberIds.contains(Person.me) }
    }

    /// The currency sheet's Recent list: the default currency, then the (up to 3) other ones used
    /// most recently on expenses and payments. The sheet adds the selected currency after them.
    var recentCurrencyCodes: [String] {
        var codes = [books.defaultCurrency]
        for code in snapshot.recentCurrencies where !codes.contains(code) {
            codes.append(code)
        }
        return codes
    }
}

extension RateTable {
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Rates")

    /// `Resources/Rates/rates.json`; an empty table (no conversions) if it can't be read.
    static let bundled: RateTable = {
        do {
            guard let url = Bundle.main.url(forResource: "rates", withExtension: "json") else {
                throw CocoaError(.fileNoSuchFile)
            }
            return try JSONDecoder().decode(RateTable.self, from: Data(contentsOf: url))
        } catch {
            log.error("Could not read rates.json: \(String(describing: error), privacy: .public)")
            return RateTable(inrPerUnit: [:])
        }
    }()
}
