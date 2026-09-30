import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Lend money / I borrowed, with installments.
struct LendMoneyScreen: View {
    let args: LendMoneyArgs


    var body: some View {
        RoutePlaceholder(route: .lendMoney(args), title: "Lend money", owner: .a, spec: "record-lend-group §4")
    }
}
