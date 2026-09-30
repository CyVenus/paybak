import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Pick a group (or none); answers with `.group(id)`.
struct GroupPickerSheet: View {
    let request: GroupPickRequest


    var body: some View {
        RoutePlaceholder(route: .pickGroup(request), title: "Group", owner: .a, spec: "record-lend-group §2")
    }
}
