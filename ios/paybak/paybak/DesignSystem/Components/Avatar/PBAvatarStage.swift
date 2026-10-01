import SwiftUI

/// Avatar / Stage (Figma 64:4189): the editor's live preview. A 300 pt tall `bg/card` card with 20 pt
/// corners showing the look in the Stage crop (the rig's bottom on the card's bottom, centred), and
/// the glass Shuffle button 12 pt in from the bottom-right corner. The look cross-fades as it changes.
/// Test ids: `<prefix>.stage`, `<prefix>.shuffle`.
struct PBAvatarStage: View {
    let look: AvatarLook
    var testIDPrefix: String?
    let onShuffle: () -> Void

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        ZStack {
            AvatarCharacterView(look: look, crop: .stage)
                .id(look)
                .transition(.opacity)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 300)
        .background(PBColor.bgCard)
        .clipShape(.rect(cornerRadius: PBRadius.card))
        .animation(reduceMotion ? nil : .easeOut(duration: 0.15), value: look)
        .accessibilityElement()
        .accessibilityLabel("Avatar preview")
        .accessibilityAddTraits(.isImage)
        .accessibilityIdentifier(testID("stage"))
        .overlay(alignment: .bottomTrailing) {
            PBIconButton(.shuffle, accessibilityLabel: "Shuffle avatar", style: .glass, action: onShuffle)
                .padding(PBSpace.s12)
                .accessibilityIdentifier(testID("shuffle"))
        }
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBAvatarStage") {
    @Previewable @State var look = AvatarLook.defaultBoy
    PBAvatarStage(look: look) {
        look = look.setting(["quiff", "spiky", "man-bun", "curly"].randomElement()!, for: "hair")
    }
    .padding(PBLayout.screenMargin)
}
