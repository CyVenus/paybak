import SwiftUI

/// Push Header Trailing=Text for editor pages whose action can be disabled (Split with, the split
/// and payer editors): glass back, centred Headline title and a glass "Done" capsule whose label
/// turns `text/tertiary` and stops taking taps while `isDoneEnabled` is false.
/// Test ids: `<prefix>.back`, `<prefix>.done`.
struct PBDoneHeader: View {
    let title: String
    var doneLabel = "Done"
    var isDoneEnabled = true
    let testIDPrefix: String
    let onBack: () -> Void
    let onDone: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            PBIconButton(.chevronLeft, accessibilityLabel: "Back", style: .glass, action: onBack)
                .accessibilityIdentifier("\(testIDPrefix).back")
            Spacer(minLength: 0)
            PBGlassTextButton(doneLabel, action: onDone)
                .disabled(!isDoneEnabled)
                .accessibilityIdentifier("\(testIDPrefix).done")
        }
        .frame(height: PBSize.tap)
        .overlay {
            Text(title)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .frame(width: 200)
                .accessibilityAddTraits(.isHeader)
        }
    }
}

#Preview("PBDoneHeader") {
    VStack(spacing: PBSpace.s24) {
        PBDoneHeader(title: "Split", testIDPrefix: "split", onBack: {}, onDone: {})
        PBDoneHeader(title: "Split", isDoneEnabled: false, testIDPrefix: "split", onBack: {}, onDone: {})
    }
    .padding(PBLayout.screenMargin)
}
