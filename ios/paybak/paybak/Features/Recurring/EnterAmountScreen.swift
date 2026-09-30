import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// Enter a variable recurring expense’s amount.
struct EnterAmountScreen: View {
    let draftId: DraftID


    var body: some View {
        RoutePlaceholder(route: .enterDraftAmount(draftId), title: "Enter amount", owner: .c, spec: "screens-insights-ai §5.4")
    }
}
