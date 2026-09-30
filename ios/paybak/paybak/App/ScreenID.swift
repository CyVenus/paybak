import SwiftUI

/// Every screen id in flow.md. The ids are also the values of the debug `-startScreen` argument.
/// Some ids are states of one screen: welcome1–3 are the Welcome steps, verifyWrong is Verify in its
/// error state, and the four home ids are Home states.
enum ScreenID: String {
    case splash
    case welcome1
    case welcome2
    case welcome3
    case getStarted
    case signIn
    case verify
    case verifyWrong
    case setup1
    case setup2
    case setup3
    case setup4
    case allSet
    case homeFirstDay
    case homeActive
    case homeAllSettled
    case homeAddSheet
}

extension View {
    /// Marks a screen's root container for UI tests as `screen.<id>` (flow.md "UI tests and test
    /// IDs"). The container keeps its children's own identifiers.
    func screenIdentifier(_ screen: ScreenID) -> some View {
        accessibilityElement(children: .contain)
            .accessibilityIdentifier("screen.\(screen.rawValue)")
    }
}

#if DEBUG
extension ScreenID {
    /// Screens after the sign-in choice. Starting on one of them in a debug build seeds the sample
    /// profile so it renders like Figma.
    var isMidFlow: Bool {
        switch self {
        case .splash, .welcome1, .welcome2, .welcome3, .getStarted: false
        default: true
        }
    }
}
#endif
