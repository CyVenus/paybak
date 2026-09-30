import SwiftUI

extension View {
    /// Pins a pushed screen's header (screens-groups §1.6): the Push Header at the screen margins from
    /// the top safe area, on a solid white band that covers the status bar, so the content scrolls
    /// under it. Apply it to the screen's `ScrollView`.
    func pbPinnedHeader(@ViewBuilder _ header: () -> some View) -> some View {
        safeAreaInset(edge: .top, spacing: 0) {
            header()
                .padding(.horizontal, PBLayout.screenMargin)
                .phoneContentWidth()
                .background(PBColor.bgPrimary.ignoresSafeArea(edges: .top))
        }
        .background(PBColor.bgPrimary)
    }

    /// A pushed screen's scroll content: at the screen margins, starting 16 below the pinned header.
    func pbPushContent() -> some View {
        padding(.horizontal, PBLayout.screenMargin)
            .padding(.top, PBSpace.s16)
            .phoneContentWidth()
    }

    /// Row / Person Regular on a white list: Figma places its avatar at the content edge and its
    /// trailing flush with the right margin (screens-groups §1.4), so the row's own 16 pt side padding
    /// hangs outside the column, and its divider stops at the margin.
    func pbFlushRow() -> some View {
        padding(.horizontal, -PBSpace.s16)
            .clipped()
    }
}

#Preview("pbPinnedHeader") {
    ScrollView {
        VStack(alignment: .leading, spacing: PBSpace.s16) {
            ForEach(0..<20) { index in
                Text("Row \(index)").textStyle(.headline)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .pbPushContent()
    }
    .pbPinnedHeader {
        PBPushHeader("Group settings") {}
    }
}
