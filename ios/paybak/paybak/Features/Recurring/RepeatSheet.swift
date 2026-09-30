import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// How often an expense repeats; answers with `.repeatRule(rule)`.
struct RepeatSheet: View {
    let request: RepeatRuleRequest


    var body: some View {
        RoutePlaceholder(route: .repeatRule(request), title: "Repeat", owner: .c, spec: "screens-insights-ai §5.3")
    }
}
