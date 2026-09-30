import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// New group or project; Create calls `router.didCreateGroup`.
struct NewGroupScreen: View {
    let mode: NewGroupMode


    var body: some View {
        RoutePlaceholder(route: .newGroup(mode), title: "New group", owner: .a, spec: "record-lend-group §6")
    }
}
