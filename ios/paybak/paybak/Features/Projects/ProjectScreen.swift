import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// A project: budget, components, fair shares, who owes whom.
struct ProjectScreen: View {
    let groupId: GroupID


    var body: some View {
        RoutePlaceholder(route: .project(groupId), title: "Project", owner: .b, spec: "screens-projects §3–8")
    }
}
