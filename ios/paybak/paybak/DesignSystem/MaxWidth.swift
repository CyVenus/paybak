import SwiftUI

extension View {
    /// Caps the view's width without making it flexible: it gets at most `maxWidth` of the width it
    /// is offered and keeps its own size within that, so a one-line `Text` truncates at the cap and a
    /// short one stays short (a pill beside it follows the text, not the cap).
    func pbMaxWidth(_ maxWidth: CGFloat) -> some View {
        MaxWidthLayout(maxWidth: maxWidth) { self }
    }
}

private struct MaxWidthLayout: Layout {
    let maxWidth: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        guard let subview = subviews.first else { return .zero }
        let size = subview.sizeThatFits(capped(proposal))
        return CGSize(width: min(size.width, maxWidth), height: size.height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        subviews.first?.place(at: bounds.origin, proposal: ProposedViewSize(width: bounds.width, height: bounds.height))
    }

    /// Text baselines pass through, so the capped text still lines up with its neighbours.
    func explicitAlignment(
        of guide: VerticalAlignment,
        in bounds: CGRect,
        proposal: ProposedViewSize,
        subviews: Subviews,
        cache: inout ()
    ) -> CGFloat? {
        guard let subview = subviews.first else { return nil }
        return bounds.minY + subview.dimensions(in: ProposedViewSize(width: bounds.width, height: bounds.height))[guide]
    }

    private func capped(_ proposal: ProposedViewSize) -> ProposedViewSize {
        ProposedViewSize(width: min(proposal.width ?? .infinity, maxWidth), height: proposal.height)
    }
}
