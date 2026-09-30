import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// A recorded payment: pending / confirmed / not received, Edit, Cancel payment.
struct PaymentDetailScreen: View {
    let paymentId: PaymentID


    var body: some View {
        RoutePlaceholder(route: .payment(paymentId), title: "Payment", owner: .a, spec: "record-lend-group §3, settle §5")
    }
}
