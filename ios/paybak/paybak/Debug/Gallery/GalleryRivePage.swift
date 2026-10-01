#if DEBUG
import SwiftUI

/// The six illustrations at their Figma slot sizes (the red outline is the slot; three artboards
/// overhang it by 12 pt). Tap a character: its animation plays, the phone gives a light haptic, and
/// the counter goes up.
struct GalleryRivePage: View {
    private static let assets: [PaybakRiveAsset] = [.onboarding, .getStarted, .notifications, .allSet, .homeFirstDay, .homeAllSquare]

    var body: some View {
        GalleryPageScroll {
            ForEach(Self.assets) { asset in
                RiveSample(asset: asset)
            }
        }
    }
}

private struct RiveSample: View {
    let asset: PaybakRiveAsset

    @StateObject private var controller: PaybakRiveController
    @State private var stepIndex = 0

    /// A trigger that plays each file's tap animation, to check `fire(trigger:)` from code.
    private static let replayTriggers: [String: String] = [
        PaybakRiveAsset.getStarted.id: "tapMiddle",
        PaybakRiveAsset.notifications.id: "bellTapped",
        PaybakRiveAsset.allSet.id: "tapBadge",
        PaybakRiveAsset.homeFirstDay.id: "tapCharacter",
        PaybakRiveAsset.homeAllSquare.id: "tapped",
    ]

    init(asset: PaybakRiveAsset) {
        self.asset = asset
        _controller = StateObject(wrappedValue: PaybakRiveController(asset))
    }

    var body: some View {
        VStack(spacing: PBSpace.s8) {
            GalleryLabel("\(asset.artboard) · slot \(Int(asset.slotSize.width))×\(Int(asset.slotSize.height))"
                         + " · view \(Int(asset.viewSize.width))×\(Int(asset.viewSize.height))")
            PaybakRiveView(controller: controller)
                .overlay {
                    Rectangle()
                        .strokeBorder(PBColor.borderDestructive.opacity(0.4), lineWidth: PBSize.hairline)
                }
                .sensoryFeedback(.impact(weight: .light), trigger: controller.tapCount)
            if asset.id == PaybakRiveAsset.onboarding.id {
                PBSegmentedControl(options: ["Step 1", "Step 2", "Step 3"], selection: $stepIndex)
                    .frame(width: 330)
                    .onChange(of: stepIndex, initial: true) { _, index in
                        controller.setNumber("step", to: Float(index + 1))
                    }
            }
            if let trigger = Self.replayTriggers[asset.id] {
                HStack(spacing: PBSpace.s12) {
                    GalleryLabel("\(asset.tapTrigger ?? "") × \(controller.tapCount)")
                    PBButton("Fire \(trigger)", style: .secondary, size: .small) {
                        controller.fire(trigger: trigger)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
    }
}
#endif
