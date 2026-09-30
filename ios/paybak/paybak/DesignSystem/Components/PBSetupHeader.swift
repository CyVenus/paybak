import SwiftUI

/// Navigation / Setup Header (Figma 36:666): back, optional Skip, a 4-segment progress bar and
/// "Step N of 4". 82 pt tall. Each setup step is its own pushed screen, so a new header first shows
/// the previous step's fill and grows the new segment from its leading edge while the screen slides
/// in (0.35 s, the Figma push curve); later `step` changes grow or shrink it and roll the number.
/// Both are instant under Reduce Motion. Test ids: `setup.back`, `setup.skip`, `setup.progress`.
struct PBSetupHeader: View {
    static let stepCount = 4

    /// 1…4.
    let step: Int
    let onBack: () -> Void
    /// Shown on the optional steps (Setup 3 and 4).
    var onSkip: (() -> Void)?

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    /// How many segments are filled; starts one short so the entrance can grow the last one.
    @State private var filledSegments: Int

    init(step: Int, onBack: @escaping () -> Void, onSkip: (() -> Void)? = nil) {
        self.step = step
        self.onBack = onBack
        self.onSkip = onSkip
        _filledSegments = State(initialValue: step - 1)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBOnboardingTopBar(onBack: onBack, onSkip: onSkip, testIDPrefix: "setup")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                progress
                Text("Step \(step) of \(Self.stepCount)")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .contentTransition(.numericText(value: Double(step)))
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Step \(step) of \(Self.stepCount)")
            .accessibilityIdentifier("setup.progress")
        }
        .animation(fillAnimation, value: step)
        .task {
            // Only the first appearance animates; coming back to the screen finds it filled.
            guard filledSegments != step else { return }
            if !reduceMotion {
                try? await Task.sleep(for: .seconds(0.1))
            }
            withAnimation(fillAnimation) { filledSegments = step }
        }
        .onChange(of: step) {
            withAnimation(fillAnimation) { filledSegments = step }
        }
    }

    private var fillAnimation: Animation? {
        reduceMotion ? nil : .timingCurve(0.42, 0, 0.58, 1, duration: 0.35)
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
                                .frame(width: index <= filledSegments ? track.size.width : 0)
                        }
                    }
            }
        }
        .frame(height: 4)
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
