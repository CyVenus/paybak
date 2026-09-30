import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// The fewest-payments plan from your side.
struct SettleUpScreen: View {
    let groupId: GroupID?


    var body: some View {
        RoutePlaceholder(route: .settleUp(groupId: groupId), title: "Settle up", owner: .b, spec: "screens-settle §3")
    }
}
