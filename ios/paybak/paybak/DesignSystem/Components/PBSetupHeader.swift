import SwiftUI

/// Navigation / Setup Header (Figma 36:666): back, optional Skip, a 4-segment progress bar and
/// "Step N of 4". 82 pt tall. Changing `step` grows or shrinks the segment fill from its leading edge
/// (0.35 s, the Figma push curve) and rolls the step number; both are instant under Reduce Motion.
struct PBSetupHeader: View {
    static let stepCount = 4

    /// 1…4.
    let step: Int
    let onBack: () -> Void
    /// Shown on the optional steps (Setup 3 and 4).
    var onSkip: (() -> Void)?

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBOnboardingTopBar(onBack: onBack, onSkip: onSkip)
            progress
            Text("Step \(step) of \(Self.stepCount)")
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
                .contentTransition(.numericText(value: Double(step)))
        }
        .animation(reduceMotion ? nil : .timingCurve(0.42, 0, 0.58, 1, duration: 0.35), value: step)
    }

    private var progress: some View {
        HStack(spacing: PBSpace.s4) {
            ForEach(1...Self.stepCount, id: \.self) { index in
                Capsule()
                    .fill(PBColor.bgIndicator)
                    .overlay(alignment: .leading) {
                        GeometryReader { track in
                            Capsule()
                                .fill(PBColor.bgInverse)
                                .frame(width: index <= step ? track.size.width : 0)
                        }
                    }
            }
        }
        .frame(height: 4)
        .accessibilityHidden(true)
    }
}

#Preview("PBSetupHeader") {
    @Previewable @State var step = 1
    VStack(spacing: PBSpace.s24) {
        PBSetupHeader(step: step, onBack: { step = max(1, step - 1) }, onSkip: step >= 3 ? {} : nil)
        PBButton("Next step", size: .small) { step = step % 4 + 1 }
    }
    .padding(PBLayout.screenMargin)
}
