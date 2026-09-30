import SwiftUI

/// Scroll edge (fade) (screens-home §2.6, home-v2 §3.8): the white gradient over the bottom of a tab
/// root, above the content and below the glass tab bar, so rows fade out as they pass under it.
/// Transparent → white 85 % at 45 % → white. It ignores touches and VoiceOver.
struct PBScrollEdgeFade: View {
    /// Home draws it 150 tall; 118 while a confirm card pushes the content down.
    var height: CGFloat = 150

    var body: some View {
        LinearGradient(
            stops: [
                .init(color: PBColor.bgPrimary.opacity(0), location: 0),
                .init(color: PBColor.bgPrimary.opacity(0.85), location: 0.45),
                .init(color: PBColor.bgPrimary, location: 1),
            ],
            startPoint: .top,
            endPoint: .bottom
        )
        .frame(height: height)
        .frame(maxWidth: .infinity)
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

#Preview("PBScrollEdgeFade") {
    ZStack(alignment: .bottom) {
        VStack(spacing: PBSpace.s8) {
            ForEach(0..<12) { index in
                Text("Row \(index)")
                    .textStyle(.headline)
                    .frame(maxWidth: .infinity, minHeight: 56)
                    .pbCard(padding: 0)
            }
        }
        .padding(.horizontal, PBLayout.screenMargin)
        PBScrollEdgeFade()
    }
    .ignoresSafeArea(edges: .bottom)
}
