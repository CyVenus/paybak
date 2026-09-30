import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// Pick a date or due date; answers with `.day(day)`.
struct DatePickerSheet: View {
    let request: DatePickRequest


    var body: some View {
        RoutePlaceholder(route: .pickDate(request), title: "Date", owner: .a, spec: "add-expense §10")
    }
}
