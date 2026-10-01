import SwiftUI

/// Control / Page Dots (Figma 12:230): the onboarding pager. The active dot is a 24 × 8 black pill,
/// the others 8 × 8 `bg/indicator` circles. Tapping does nothing; swipes and the CTA change the page.
/// Its accessibility value is "Page N of M", so a screen can make it adjustable for VoiceOver.
struct PBPageDots: View {
    var count = 3
    /// 1-based, like the Figma `Active` property.
    let active: Int

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// Makes the accessibility element 44 pt tall (size/tap) without changing the 8 pt layout, so an
    /// adjustable pager is easy to find by touch with VoiceOver.
    private let accessibilitySlop = (PBSize.tap - 8) / 2

    var body: some View {
        ZStack(alignment: .leading) {
            dots
                // Reduce Motion swaps the whole row, so the page changes with a fade, not a grow.
                .id(reduceMotion ? active : 0)
                .transition(.opacity)
        }
        // The active dot grows and the previous one shrinks over 0.3 s on the Figma ease-in-out
        // curve (screens-launch.md §2.5); Reduce Motion fades the row in 0.2 s instead.
        .animation(reduceMotion ? .easeInOut(duration: 0.2) : .timingCurve(0.42, 0, 0.58, 1, duration: 0.3), value: active)
        .padding(.vertical, accessibilitySlop)
        .contentShape(.accessibility, .rect)
        .accessibilityElement()
        .accessibilityValue("Page \(active) of \(count)")
        .padding(.vertical, -accessibilitySlop)
    }

    private var dots: some View {
        HStack(spacing: PBSpace.s6) {
            ForEach(1...count, id: \.self) { index in
                Capsule()
                    .fill(index == active ? PBColor.bgInverse : PBColor.bgIndicator)
                    .frame(width: index == active ? 24 : 8, height: 8)
            }
        }
    }
}

#Preview("PBPageDots") {
    @Previewable @State var active = 1
    VStack(spacing: PBSpace.s24) {
        PBPageDots(active: active)
        PBButton("Next", size: .small) { active = active % 3 + 1 }
    }
}
