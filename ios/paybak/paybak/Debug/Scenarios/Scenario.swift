#if DEBUG
import Foundation

/// What a debug start screen sets up (app-architecture §3.10): the demo data (base records plus
/// seed scenarios, at Figma parity), Pro, the avatar, then the tab, stacks, modal layers and sheets.
/// In-screen state (a prefilled form, an open local sheet, an alert) is applied by the owning screen
/// through `onStartScreen`.
struct Scenario {
    /// A modal layer to present: its root, its pushes and its sheet.
    struct Layer {
        var root: Route
        var path: [Route] = []
        var sheet: Route?
    }

    /// Seed scenarios applied after the base records (`E` = ["empty"], `D` = ["eshaClaimsPayment"],
    /// `D−claim` = []).
    var seeds: [String]
    var tab: Tab = .home
    var stack: [Route] = []
    var sheet: Route?
    var modals: [Layer] = []
    var avatar: AvatarLook?
    var toast: String?
    var groupsSegment: GroupsSegment?
    var activitySegment: ActivitySegment?

    /// The Seed column shorthands.
    static let empty = ["empty"]
    static let demo = ["eshaClaimsPayment"]
    static let base: [String] = []

    /// `D P`: the demo with Pro.
    static func pro(_ seeds: [String] = demo) -> [String] { seeds + ["pro"] }

    /// Every main-app screen id's scenario, gathered from the module tables.
    static let all: [ScreenID: Scenario] = [Scenario.home, Scenario.addRecord, Scenario.groups, Scenario.settle, Scenario.activity,
                                            Scenario.projects, Scenario.profile, Scenario.settings, Scenario.insights]
        .reduce(into: [:]) { result, table in result.merge(table) { first, _ in first } }
}
#endif
