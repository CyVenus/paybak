import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// Members, currency, simplify debts, recurring, leave.
struct GroupSettingsScreen: View {
    let groupId: GroupID


    var body: some View {
        RoutePlaceholder(route: .groupSettings(groupId), title: "Group settings", owner: .b, spec: "screens-groups §5")
    }
}
