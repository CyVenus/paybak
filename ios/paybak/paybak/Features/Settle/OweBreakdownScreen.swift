import SwiftUI

/// You owe (settle §2): who you pay, and why simplified groups send you to one person.
struct OweBreakdownScreen: View {
    var body: some View {
        BalanceBreakdownView(direction: .owe)
    }
}
