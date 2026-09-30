import SwiftUI

/// Who builds a screen (app-architecture §6): each placeholder names its owner.
enum Lane: String {
    case m2 = "M2"
    case a = "Lane A"
    case b = "Lane B"
    case c = "Lane C"
    case d = "Lane D"
}

/// Temporary scaffold for a route the owning lane replaces: the route id, its owner and spec, and
/// its parameters, inside the chrome its presentation uses (tab root, push header with back, modal
/// header with ✕, or a sheet with ✕). The test root is `screen.<routeId>` unless `screenID` says
/// otherwise. Delete this file once the last placeholder is replaced.
struct RoutePlaceholder: View {
    let route: Route
    let title: String
    let owner: Lane
    let spec: String
    /// Overrides the `screen.<routeId>` test root (Home keeps its M1 ids).
    var screenID: String?
    var details: [String] = []
    /// Drawn above a tab root's title (Home's logo).
    var accessory: AnyView?

    @Environment(AppRouter.self) private var router

    var body: some View {
        switch route.presentation {
        case .tab:
            ScrollView {
                VStack(alignment: .leading, spacing: PBSpace.s24) {
                    accessory
                    Text(title)
                        .textStyle(.title1)
                        .foregroundStyle(PBColor.textPrimary)
                        .accessibilityAddTraits(.isHeader)
                    info
                }
                .padding(.horizontal, PBLayout.screenMargin)
                .padding(.bottom, PBTabBar.contentInset)
                .phoneContentWidth()
            }
            .background(PBColor.bgPrimary)
            .testRoot(root)
        case .push:
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                PBPushHeader(title, testIDPrefix: route.routeID, onBack: router.back)
                ScrollView { info }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
            .background(PBColor.bgPrimary)
            .testRoot(root)
        case .modal:
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                PBModalHeader(title, testIDPrefix: route.routeID, onClose: router.dismissModal)
                ScrollView { info }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
            .background(PBColor.bgPrimary)
            .testRoot(root)
        case .sheet:
            PBSheet(title: title, testIDPrefix: route.routeID, onClose: router.dismissSheet) {
                info
            }
            .testRoot(root)
        }
    }

    private var root: String { screenID ?? "screen.\(route.routeID)" }

    private var info: some View {
        PlaceholderInfo(name: route.routeID, owner: owner, spec: spec, details: details + [parameters].compactMap(\.self))
    }

    /// The route's associated values, for checking what a caller passed.
    private var parameters: String? {
        let text = String(describing: route)
        guard let open = text.firstIndex(of: "(") else { return nil }
        return "Parameters: " + text[open...]
    }
}

/// The body of a placeholder: who builds it, the spec, and any details.
struct PlaceholderInfo: View {
    let name: String
    let owner: Lane
    let spec: String
    var details: [String] = []

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            PBBadge("Placeholder · \(owner.rawValue)", style: .inverse)
            Text(name)
                .textStyle(.title3)
                .foregroundStyle(PBColor.textPrimary)
            Text("Spec: \(spec)")
                .textStyle(.body)
                .foregroundStyle(PBColor.textSecondary)
            ForEach(details, id: \.self) { detail in
                Text(detail)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .lineLimit(6)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private extension View {
    func testRoot(_ identifier: String) -> some View {
        accessibilityElement(children: .contain)
            .accessibilityIdentifier(identifier)
    }
}

#Preview("RoutePlaceholder") {
    RoutePlaceholder(route: .expense("e-goa-villa"), title: "Expense", owner: .a, spec: "screens-activity §4")
        .environment(AppRouter())
}
