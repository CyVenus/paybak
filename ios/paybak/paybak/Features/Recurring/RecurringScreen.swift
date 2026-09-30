import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// A group’s recurring rules and drafts that need an amount.
struct RecurringScreen: View {
    let groupId: GroupID


    var body: some View {
        RoutePlaceholder(route: .recurring(groupId), title: "Recurring", owner: .c, spec: "screens-insights-ai §5.2")
    }
}
