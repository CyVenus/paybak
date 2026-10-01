import SwiftUI

/// Pressed fades the whole control to 50 %: the bare 16–24 pt icons whose 44 pt target overhangs
/// them (a field's clear ✕, a chip's remove ✕, a person row's ✕, the composer's mic) and a receipt
/// line's amount.
struct PBDimButtonStyle: ButtonStyle {
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .opacity(isPressed ? 0.5 : 1)
    }
}
