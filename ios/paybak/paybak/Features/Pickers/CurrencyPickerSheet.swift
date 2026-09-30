import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Pick a currency; answers with `.currency(code)`.
struct CurrencyPickerSheet: View {
    let request: CurrencyPickRequest


    var body: some View {
        RoutePlaceholder(route: .pickCurrency(request), title: "Currency", owner: .a, spec: "add-expense §9")
    }
}
