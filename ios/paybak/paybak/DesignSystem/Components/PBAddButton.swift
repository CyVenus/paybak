import SwiftUI

/// Button / Add (Figma 10:87): the centre ＋ of the tab bar. A 52 pt black circle with the plus drawn
/// at stroke 2 (the only heavier icon). It has no shadow of its own; it sits in the glass tab bar.
struct PBAddButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image("add-button-plus")
                .resizable()
                .frame(width: PBSize.iconLg, height: PBSize.iconLg)
                .accessibilityHidden(true)
        }
        .buttonStyle(PBAddButtonStyle())
        .accessibilityLabel("Add")
    }
}

private struct PBAddButtonStyle: ButtonStyle {
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .foregroundStyle(PBColor.iconInverse)
            .frame(width: PBSize.addButton, height: PBSize.addButton)
            .background(isPressed ? PBColor.bgInversePressed : PBColor.bgInverse, in: .circle)
            .contentShape(.circle)
            .animation(.easeOut(duration: 0.1), value: isPressed)
    }
}

#Preview("PBAddButton") {
    HStack(spacing: PBSpace.s16) {
        PBAddButton {}
        PBAddButton {}.pbPreviewInteraction(.pressed)
    }
}
