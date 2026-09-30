import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// The timeline filtered to a person, group, project or category and month.
struct ActivityLogScreen: View {
    let filter: ActivityFilter


    var body: some View {
        RoutePlaceholder(route: .activityLog(filter), title: "History", owner: .a, spec: "screens-activity §3.9")
    }
}
