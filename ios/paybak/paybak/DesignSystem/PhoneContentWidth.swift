import SwiftUI

extension View {
    /// Keeps a phone layout centred at `PBLayout.maxContentWidth` on wider screens (Pro Max, iPad),
    /// per flow.md "Resolved decisions". Apply it to a screen's content, not to the app root, so
    /// full-bleed layers (the screen background, `PBScrim`, a sheet's backdrop) and the edge-swipe
    /// back gesture still reach the screen edges.
    func phoneContentWidth() -> some View {
        frame(maxWidth: PBLayout.maxContentWidth)
            .frame(maxWidth: .infinity)
    }
}

#Preview("phoneContentWidth") {
    ZStack {
        PBScrim {}
        Text("Content capped at \(Int(PBLayout.maxContentWidth)) pt")
            .textStyle(.headline)
            .foregroundStyle(PBColor.textPrimary)
            .frame(maxWidth: .infinity, minHeight: 200)
            .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.card))
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
    }
}
