import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// Plans, mock trial / purchase, Welcome; Done calls `router.finishPaywall()`.
struct PaywallScreen: View {
    let continueTo: Route?


    var body: some View {
        RoutePlaceholder(route: .paywall(continueTo: continueTo), title: "Paybak Pro", owner: .c, spec: "screens-settings §2–3")
    }
}
