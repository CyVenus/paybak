import SwiftUI

/// Paybak Pro — Welcome (screens-settings §3): the All set art at 2/3 scale, "You’re on Paybak Pro",
/// the trial or renewal line, and what's now unlocked. Done is the only way out (no close, no swipe).
/// It's a state of the paywall route (`screen.paywall`), marked by the hidden `paywall.state.welcome`.
struct ProWelcomeView: View {
    /// Nil on the free plan, which hides the line.
    let statusLine: String?
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                PaybakRiveIllustration(.allSet)
                    .frame(width: 241.33, height: 200)
                    .frame(maxWidth: .infinity)
                VStack(alignment: .leading, spacing: PBSpace.s12) {
                    Text("You’re on Paybak Pro")
                        .textStyle(.title1)
                        .foregroundStyle(PBColor.textPrimary)
                        .accessibilityAddTraits(.isHeader)
                        .accessibilityIdentifier("proWelcome.title")
                    if let statusLine {
                        Text(statusLine)
                            .textStyle(.body)
                            .foregroundStyle(PBColor.textSecondary)
                            .accessibilityIdentifier("proWelcome.body")
                    }
                }
                VStack(spacing: PBSpace.s8) {
                    PBSectionHeader("Now unlocked")
                    VStack(spacing: 0) {
                        ForEach(ProFeature.all) { feature in
                            PBSettingRow(feature.title, icon: feature.icon, trailing: .check,
                                         showsDivider: feature.id != ProFeature.all.last?.id)
                        }
                    }
                    .pbCard(padding: 0)
                }
            }
            .padding(.top, PBSpace.s24)
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s24)
        }
        .scrollBounceBehavior(.basedOnSize)
        .safeAreaInset(edge: .bottom, spacing: 0) {
            PBButton("Done", fillsWidth: true, action: onDone)
                .accessibilityIdentifier("proWelcome.done")
                .padding(.horizontal, PBLayout.screenMargin)
                .padding(.bottom, PBSpace.s16)
                .background(PBColor.bgPrimary)
        }
        .overlay(alignment: .topLeading) {
            Color.clear
                .frame(width: 1, height: 1)
                .accessibilityElement()
                .accessibilityLabel("welcome")
                .accessibilityIdentifier("paywall.state.welcome")
        }
    }
}

#Preview("ProWelcomeView") {
    ProWelcomeView(statusLine: "Your free trial ends Wed 7 Oct. Then ₹799/year.") {}
}
