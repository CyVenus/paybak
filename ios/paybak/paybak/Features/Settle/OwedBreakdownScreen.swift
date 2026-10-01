import SwiftUI

/// You’re owed (settle §1): who owes you, overdue first, behind Home's You’re owed card.
struct OwedBreakdownScreen: View {
    var body: some View {
        BalanceBreakdownView(direction: .owed)
    }
}
