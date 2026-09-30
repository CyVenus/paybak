import SwiftUI

/// Figma effect styles. "Liquid Glass only on floating chrome: the tab bar and toolbar buttons.
/// Never on cards." The glass shadow is the only shadow in the app.
enum PBMaterial {
    /// Material/Glass (tab bar): drop shadow 0, 8, blur 32, #0A0A0A @ 10 %.
    case glass
    /// Material/Glass Small (44 pt toolbar buttons): drop shadow 0, 4, blur 16, #0A0A0A @ 8 %.
    case glassSmall
    /// Material/Frosted: drop shadow 0, 8, blur 32, #0A0A0A @ 10 % + background blur 24.
    case frosted

    fileprivate var shadow: (opacity: Double, blur: CGFloat, y: CGFloat) {
        switch self {
        case .glass, .frosted: (0.10, 32, 8)
        case .glassSmall: (0.08, 16, 4)
        }
    }
}

extension View {
    /// Draws the material behind the view in `shape`. Glass uses iOS Liquid Glass (`glassEffect`);
    /// `isInteractive` lets the glass react to touches the way system glass buttons do.
    func pbMaterial(_ material: PBMaterial, in shape: some Shape, isInteractive: Bool = false) -> some View {
        let shadow = material.shadow
        return Group {
            switch material {
            case .glass, .glassSmall:
                glassEffect(.regular.interactive(isInteractive), in: shape)
            case .frosted:
                background(.ultraThinMaterial, in: shape)
            }
        }
        // SwiftUI's shadow radius is about half of Figma's blur.
        .shadow(color: PBPalette.gray900.opacity(shadow.opacity), radius: shadow.blur / 2, y: shadow.y)
    }
}

/// The 40 % scrim behind sheets (`color/bg/scrim`). Covers the whole screen, including the safe
/// areas; tapping it calls `onDismiss`.
struct PBScrim: View {
    let onDismiss: () -> Void

    var body: some View {
        PBColor.bgScrim
            .ignoresSafeArea()
            .contentShape(.rect)
            .onTapGesture(perform: onDismiss)
            .accessibilityElement()
            .accessibilityLabel("Close")
            .accessibilityAddTraits(.isButton)
            .accessibilityAction { onDismiss() }
    }
}

#Preview("Materials") {
    ZStack {
        LinearGradient(colors: [PBColor.bgInverse, PBColor.bgCard], startPoint: .top, endPoint: .bottom)
            .ignoresSafeArea()
        VStack(spacing: PBSpace.s32) {
            Text("Glass").textStyle(.headline)
                .frame(width: 200, height: 62)
                .pbMaterial(.glass, in: .capsule)
            PBIconView(.bell)
                .frame(width: PBSize.tap, height: PBSize.tap)
                .pbMaterial(.glassSmall, in: .circle, isInteractive: true)
            Text("Frosted").textStyle(.headline)
                .frame(width: 200, height: 62)
                .pbMaterial(.frosted, in: .rect(cornerRadius: PBRadius.card))
        }
    }
}
