import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Pick friends (or guests); answers with `router.complete(request.id, with: .people(ids))`.
struct PeoplePickerScreen: View {
    let request: PeoplePickRequest


    var body: some View {
        RoutePlaceholder(route: .pickPeople(request), title: "Split with", owner: .a, spec: "add-expense §5")
    }
}
