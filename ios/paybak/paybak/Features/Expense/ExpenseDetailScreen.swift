import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// An expense: hero, your share, split, receipt, comments, history, flag and delete.
struct ExpenseDetailScreen: View {
    let expenseId: ExpenseID
    let toast: String?


    var body: some View {
        RoutePlaceholder(route: .expense(expenseId, toast: toast), title: "Expense", owner: .a, spec: "screens-activity §4, add-expense §11")
    }
}
