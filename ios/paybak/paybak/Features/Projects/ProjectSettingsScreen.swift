import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// Contribution, members, budget, close project.
struct ProjectSettingsScreen: View {
    let groupId: GroupID


    var body: some View {
        RoutePlaceholder(route: .projectSettings(groupId), title: "Project settings", owner: .b, spec: "screens-projects §6")
    }
}
