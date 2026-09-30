import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// Tell the payer the money didn’t arrive.
struct NotReceivedSheet: View {
    let paymentId: PaymentID


    var body: some View {
        RoutePlaceholder(route: .notReceived(paymentId), title: "Not received", owner: .b, spec: "screens-settle §8")
    }
}
