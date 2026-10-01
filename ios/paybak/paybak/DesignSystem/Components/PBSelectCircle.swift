import SwiftUI

/// The 24 pt multi-select mark of Row / Person (Select On / Off) and Row / Split Person: a black
/// circle with a white 16 pt tick when on, an empty 1.5 pt `border/strong` ring when off. The black
/// fill fades in and out (0.15 s); the tick swaps with it. Decorative; the row carries the selected
/// trait.
struct PBSelectCircle: View {
    let isOn: Bool

    var body: some View {
        ZStack {
            Circle()
                .fill(isOn ? PBColor.bgInverse : .clear)
                .animation(.easeOut(duration: 0.15), value: isOn)
            Circle().strokeBorder(PBColor.borderStrong, lineWidth: 1.5)
            if isOn {
                PBIconView(.check, size: PBSize.iconSm)
                    .foregroundStyle(PBColor.iconInverse)
                    .transition(.identity)
            }
        }
        .frame(width: PBSize.iconLg, height: PBSize.iconLg)
        .accessibilityHidden(true)
    }
}

#Preview("PBSelectCircle") {
    HStack(spacing: PBSpace.s16) {
        PBSelectCircle(isOn: true)
        PBSelectCircle(isOn: false)
    }
}
