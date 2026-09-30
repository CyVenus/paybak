import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// A loan: progress card, installments, Remind and Record repayment.
struct LoanScreen: View {
    let loanId: LoanID


    var body: some View {
        RoutePlaceholder(route: .loan(loanId), title: "Loan", owner: .a, spec: "record-lend-group §5")
    }
}
