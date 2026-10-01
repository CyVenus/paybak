import SwiftUI

extension View {
    /// A route screen's test root, `screen.<routeId>` (app-architecture §2.2).
    func routeTestRoot(_ routeID: String) -> some View {
        accessibilityElement(children: .contain)
            .accessibilityIdentifier("screen.\(routeID)")
    }

    /// A local sheet with the Paybak sheet look (grabber, white fill, 40 pt corners), driven by an
    /// optional item; `detent` picks Medium (fitted) or Large per item.
    func pbItemSheet<Item: Identifiable, Content: View>(
        item: Binding<Item?>,
        detent: @escaping (Item) -> PBSheetDetent = { _ in .fitted },
        @ViewBuilder content: @escaping (Item) -> Content
    ) -> some View {
        sheet(item: item) { item in
            PBSheetPresentation(detent: detent(item)) { content(item) }
        }
    }
}
