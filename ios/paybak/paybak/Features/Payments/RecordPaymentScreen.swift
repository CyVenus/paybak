import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Record payment (new, prefilled or editing).
struct RecordPaymentScreen: View {
    let args: RecordPaymentArgs


    var body: some View {
        RoutePlaceholder(route: .recordPayment(args), title: "Record payment", owner: .a, spec: "record-lend-group §2, settle §4")
    }
}
