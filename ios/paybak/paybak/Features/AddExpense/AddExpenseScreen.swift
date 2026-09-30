import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// The Add expense form (new, prefilled or editing).
struct AddExpenseScreen: View {
    let args: AddExpenseArgs


    var body: some View {
        RoutePlaceholder(route: .addExpense(args), title: "Add expense", owner: .a, spec: "screens-add-expense §4–12")
    }
}
