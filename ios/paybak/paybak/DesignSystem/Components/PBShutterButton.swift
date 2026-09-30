import SwiftUI

/// Control / Shutter (Figma 117:1001): the camera shutter on Scan receipt, always on the dark
/// `bg/camera` backdrop. A 76 pt white 4 pt ring around a 62 pt white disc; pressed shrinks the disc
/// to 56 pt in `bg/card-pressed` (0.1 s). Tapping captures, with a light haptic.
struct PBShutterButton: View {
    let action: () -> Void

    @State private var captures = 0

    var body: some View {
        Button {
            captures += 1
            action()
        } label: {
            EmptyView()
        }
        .buttonStyle(ShutterStyle())
        .sensoryFeedback(.impact(weight: .light), trigger: captures)
        .accessibilityLabel("Take photo")
    }
}

private struct ShutterStyle: ButtonStyle {
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        ZStack {
            Circle().strokeBorder(PBColor.bgPrimary, lineWidth: 4)
            Circle()
                .fill(isPressed ? PBColor.bgCardPressed : PBColor.bgPrimary)
                .frame(width: isPressed ? 56 : 62, height: isPressed ? 56 : 62)
        }
        .frame(width: 76, height: 76)
        .contentShape(.circle)
        .animation(.easeOut(duration: 0.1), value: isPressed)
    }
}

#Preview("PBShutterButton") {
    HStack(spacing: PBSpace.s32) {
        PBShutterButton {}
        PBShutterButton {}.pbPreviewInteraction(.pressed)
    }
    .padding(PBSpace.s32)
    .background(PBColor.bgCamera)
}
