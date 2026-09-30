#if DEBUG
import SwiftUI

/// Rive illustrations at their Figma slot sizes. The dashed box is the layout slot; the bleed
/// artboards overflow it by 12 pt per side. Tap a character: the tap count and a light haptic come
/// from the file's own trigger (observed, never fired by the app). Allow ~2 s for the watermark pre-roll.
struct GalleryRivePage: View {
    let assets: [PaybakRiveAsset]

    var body: some View {
        GalleryPageScroll {
            ForEach(assets) { asset in
                RiveSample(asset: asset)
            }
        }
    }
}

private struct RiveSample: View {
    let asset: PaybakRiveAsset

    @StateObject private var controller: PaybakRiveController
    @State private var stepIndex = 0
    @State private var lastTrigger: String?

    /// The files' specific triggers, fired together with the tap trigger. Observing them shows
    /// `observe(trigger:)` working and which hit area took the tap.
    private static let specificTriggers: [String: [String]] = [
        PaybakRiveAsset.getStarted.id: ["tapLeft", "tapMiddle", "tapRight"],
        PaybakRiveAsset.allSet.id: ["tapBadge", "tapLeft", "tapMiddle", "tapRight"],
        PaybakRiveAsset.homeFirstDay.id: ["tapCharacter"],
    ]

    init(asset: PaybakRiveAsset) {
        self.asset = asset
        _controller = StateObject(wrappedValue: PaybakRiveController(asset))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            HStack(spacing: PBSpace.s8) {
                VStack(alignment: .leading, spacing: PBSpace.s2) {
                    Text("\(asset.fileName).riv · \(asset.artboard)")
                        .textStyle(.caption1)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(detail)
                        .textStyle(.caption2)
                        .foregroundStyle(PBColor.textTertiary)
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                if asset.id == PaybakRiveAsset.onboarding.id {
                    PBSegmentedControl(options: ["1", "2", "3"], selection: $stepIndex)
                        .frame(width: 120)
                        .onChange(of: stepIndex, initial: true) { _, index in
                            controller.setNumber("step", to: Float(index + 1))
                        }
                } else {
                    PBTextButton("Replay", style: .secondary) {
                        lastTrigger = nil
                        controller.restart()
                    }
                }
            }
            PaybakRiveView(controller: controller)
                .background {
                    Rectangle()
                        .strokeBorder(PBColor.bgIndicator, style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                }
                .sensoryFeedback(.impact(weight: .light), trigger: controller.tapCount)
                .frame(maxWidth: .infinity)
        }
        .onAppear {
            for name in Self.specificTriggers[asset.id] ?? [] {
                controller.observe(trigger: name) { lastTrigger = name }
            }
        }
    }

    private var detail: String {
        let slot = "slot \(Int(asset.slotSize.width))×\(Int(asset.slotSize.height))"
        let view = "view \(Int(asset.viewSize.width))×\(Int(asset.viewSize.height))"
        guard let trigger = asset.tapTrigger else { return "\(slot) · \(view) · no listeners" }
        let last = lastTrigger.map { " · \($0)" } ?? ""
        return "\(slot) · \(view) · \(trigger) ×\(controller.tapCount)\(last)"
    }
}
#endif
