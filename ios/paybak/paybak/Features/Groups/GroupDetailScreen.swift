import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// A group: balances, paid vs share, expenses (empty = Group created).
struct GroupDetailScreen: View {
    let groupId: GroupID


    var body: some View {
        RoutePlaceholder(route: .group(groupId), title: "Group", owner: .b, spec: "screens-groups §4, record-lend-group §7")
    }
}
