import UIKit

/// App haptics (app-architecture §7.2): selection on chips, segments and tiles; success on Save and
/// Confirm; warning on validation errors; light impact on Rive taps. In views prefer
/// `.sensoryFeedback`; use these from actions.
enum Haptics {
    static func selection() {
        UISelectionFeedbackGenerator().selectionChanged()
    }

    static func success() {
        UINotificationFeedbackGenerator().notificationOccurred(.success)
    }

    static func warning() {
        UINotificationFeedbackGenerator().notificationOccurred(.warning)
    }

    static func lightImpact() {
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
}
