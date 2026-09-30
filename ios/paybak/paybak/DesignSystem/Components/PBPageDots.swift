import SwiftUI

/// Control / Page Dots (Figma 12:230): the onboarding pager. The active dot is a 24 × 8 black pill,
/// the others 8 × 8 `bg/indicator` circles. Decorative; swipes and the CTA change the page.
struct PBPageDots: View {
    var count = 3
    /// 1-based, like the Figma `Active` property.
    let active: Int

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        HStack(spacing: PBSpace.s6) {
            ForEach(1...count, id: \.self) { index in
                Capsule()
                    .fill(index == active ? PBColor.bgInverse : PBColor.bgIndicator)
                    .frame(width: index == active ? 24 : 8, height: 8)
            }
        }
        // Figma has instant variant swaps; the 0.25 s grow is our suggestion (components-core.md §4.1).
        .animation(reduceMotion ? nil : .easeInOut(duration: 0.25), value: active)
        .accessibilityElement()
        .accessibilityLabel("Page \(active) of \(count)")
    }
}

#Preview("PBPageDots") {
    @Previewable @State var active = 1
    VStack(spacing: PBSpace.s24) {
        PBPageDots(active: active)
        PBButton("Next", size: .small) { active = active % 3 + 1 }
    }
}
