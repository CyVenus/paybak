import SwiftUI

/// Figma documents Pressed and Focused as static variants. Setting this in the environment makes
/// the Paybak controls draw that state without a real touch or keyboard focus, so the debug gallery
/// and SwiftUI previews can show every variant side by side. It is never set in the app's screens.
enum PBPreviewInteraction {
    case pressed
    case focused
}

extension EnvironmentValues {
    @Entry var pbPreviewInteraction: PBPreviewInteraction?
}

extension View {
    /// Draws the Paybak controls inside this view in a static interaction state (gallery/previews).
    func pbPreviewInteraction(_ interaction: PBPreviewInteraction) -> some View {
        environment(\.pbPreviewInteraction, interaction)
    }
}
