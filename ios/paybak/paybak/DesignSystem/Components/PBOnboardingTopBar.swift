import SwiftUI

/// Navigation / Onboarding Top Bar (Figma 17:461): an optional back chevron on the left and an
/// optional Skip on the right, 44 pt tall. The bar keeps its height when both are hidden, so nothing
/// below moves (Welcome step 3 hides Skip).
struct PBOnboardingTopBar: View {
    var onBack: (() -> Void)?
    var onSkip: (() -> Void)?
    /// The screen part of the buttons' test ids (flow.md): `<prefix>.back` and `<prefix>.skip`.
    var testIDPrefix: String?

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        HStack(spacing: 0) {
            if let onBack {
                PBIconButton(.chevronLeft, accessibilityLabel: "Back", action: onBack)
                    .accessibilityIdentifier(testID("back"))
            }
            Spacer(minLength: 0)
            if let onSkip {
                PBTextButton("Skip", style: .secondary, action: onSkip)
                    .accessibilityIdentifier(testID("skip"))
                    .transition(.opacity)
            }
        }
        .frame(height: PBSize.tap)
        .animation(reduceMotion ? nil : .easeInOut(duration: 0.2), value: onSkip == nil)
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBOnboardingTopBar") {
    VStack(spacing: PBSpace.s16) {
        PBOnboardingTopBar(onSkip: {})
        PBOnboardingTopBar(onBack: {})
        PBOnboardingTopBar(onBack: {}, onSkip: {})
        PBOnboardingTopBar()
            .border(PBColor.borderSubtle)
    }
    .padding(PBLayout.screenMargin)
}
