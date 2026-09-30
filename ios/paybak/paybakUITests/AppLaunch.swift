import UIKit
import XCTest

/// The flow.md screen ids: the values of the debug `-startScreen` argument and of the
/// `screen.<id>` identifiers on each screen's root container.
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

extension XCUIApplication {
    /// Launches Paybak with its debug launch hooks (flow.md "Debug-only hooks"): `-resetOnboarding`
    /// clears the saved profile, and `-startScreen` opens a screen directly instead of the splash.
    /// `textSize` sets the app's Dynamic Type size without touching the device's settings.
    static func launchPaybak(
        startScreen: ScreenID? = nil,
        resetOnboarding: Bool = true,
        textSize: UIContentSizeCategory? = nil
    ) -> XCUIApplication {
        let app = XCUIApplication()
        if resetOnboarding {
            app.launchArguments += ["-resetOnboarding", "YES"]
        }
        if let startScreen {
            app.launchArguments += ["-startScreen", startScreen.rawValue]
        }
        if let textSize {
            app.launchArguments += ["-UIPreferredContentSizeCategoryName", textSize.rawValue]
        }
        app.launch()
        return app
    }

    /// The root container of a screen (`screen.<id>`).
    func screen(_ id: ScreenID) -> XCUIElement {
        element("screen.\(id.rawValue)")
    }

    /// The element with a flow.md test id such as `welcome.continue`, whatever its type.
    func element(_ identifier: String) -> XCUIElement {
        descendants(matching: .any).matching(identifier: identifier).firstMatch
    }

    /// The first element labelled `label`, whatever its type (e.g. a toast).
    func element(label: String) -> XCUIElement {
        descendants(matching: .any).matching(NSPredicate(format: "label == %@", label)).firstMatch
    }

    /// The iOS back gesture: a drag from the left screen edge (onboarding screens have no nav bar).
    func swipeBackFromLeftEdge() {
        let start = coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        start.press(forDuration: 0.05, thenDragTo: coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))
    }
}

extension XCUIElement {
    /// Waits until the element's label is `label` (text that animates in changes after a moment).
    @discardableResult
    func waitForLabel(_ label: String, timeout: TimeInterval = 3) -> Bool {
        let predicate = NSPredicate(format: "label == %@", label)
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: self)
        return XCTWaiter().wait(for: [expectation], timeout: timeout) == .completed
    }
}
