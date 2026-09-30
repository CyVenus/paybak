import SwiftUI

/// The 24 pt multi-select mark of Row / Person (Select On / Off) and Row / Split Person: a black
/// circle with a white 16 pt tick when on, an empty 1.5 pt `border/strong` ring when off.
/// Decorative; the row carries the selected trait.
struct PBSelectCircle: View {
    let isOn: Bool

    var body: some View {
        ZStack {
            if isOn {
                Circle().fill(PBColor.bgInverse)
                PBIconView(.check, size: PBSize.iconSm)
                    .foregroundStyle(PBColor.iconInverse)
            } else {
                Circle().strokeBorder(PBColor.borderStrong, lineWidth: 1.5)
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
