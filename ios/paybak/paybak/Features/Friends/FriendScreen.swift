import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane B replaces this file with the real screen and keeps
// this initializer.
/// A friend: balance, history, groups together, reminders.
struct FriendScreen: View {
    let personId: PersonID


    var body: some View {
        RoutePlaceholder(route: .friend(personId), title: "Friend", owner: .b, spec: "screens-groups §6")
    }
}
