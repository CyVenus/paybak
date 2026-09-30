import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// The Remind sheet: tone, message, Send in Paybak, Share….
struct RemindSheet: View {
    let personId: PersonID
    let context: ReminderContext?


    var body: some View {
        RoutePlaceholder(route: .remind(personId: personId, context: context), title: "Remind", owner: .b, spec: "screens-settle §6–7")
    }
}
