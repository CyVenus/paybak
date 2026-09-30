import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// One FAQ answer.
struct HelpAnswerScreen: View {
    let index: Int


    var body: some View {
        RoutePlaceholder(route: .helpAnswer(index: index), title: "Help", owner: .c, spec: "screens-settings §11")
    }
}
