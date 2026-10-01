import SwiftUI

/// The main app (app-architecture §2.1, §2.8): one `NavigationStack` over the tab shell, its route
/// sheet, then the modal layers, each a full-screen cover with its own stack and sheet. Every layer
/// shows the app toast while it's on top.
struct MainView: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        @Bindable var router = router
        NavigationStack(path: $router.mainPath) {
            TabShell()
                .navigationBarHiddenKeepingSwipeBack()
                .navigationDestination(for: Route.self) { route in
                    RouteView(route: route)
                        .navigationBarHiddenKeepingSwipeBack()
                }
        }
        .routeSheet($router.mainSheet, onDismiss: router.sheetDidDismiss)
        .toastHost(level: 0)
        .modalLayers(from: 0)
        .task {
            if let link = router.pendingLink {
                router.pendingLink = nil
                router.open(link)
            }
        }
    }
}

/// Home · Groups · ＋ · Activity · Profile. Each tab root is created on its first visit and keeps its
/// state while the app runs; the glass tab bar floats over them.
private struct TabShell: View {
    @Environment(AppRouter.self) private var router
    @State private var visited: Set<Tab> = []

    var body: some View {
        ZStack(alignment: .bottom) {
            ForEach(Tab.allCases, id: \.self) { tab in
                if visited.contains(tab) || tab == router.selectedTab {
                    let isSelected = tab == router.selectedTab
                    RouteView(route: tab.route)
                        .opacity(isSelected ? 1 : 0)
                        .allowsHitTesting(isSelected)
                        .accessibilityHidden(!isSelected)
                }
            }
            PBTabBar(selection: router.selectedTab, onSelect: router.select, onAdd: { router.open(.addSheet) })
                .padding(.horizontal, PBLayout.screenMargin)
                .padding(.bottom, PBTabBar.bottomOffset)
                .phoneContentWidth()
                .ignoresSafeArea(.container, edges: .bottom)
        }
        .background(PBColor.bgPrimary)
        .onChange(of: router.selectedTab, initial: true) { _, tab in
            visited.insert(tab)
        }
    }
}

extension View {
    /// Presents the modal layers from `level` up as nested full-screen covers.
    func modalLayers(from level: Int) -> some View {
        modifier(ModalLayers(level: level))
    }

    /// Presents a layer's route sheet with the sheet look its route asks for.
    fileprivate func routeSheet(_ route: Binding<Route?>, onDismiss: @escaping () -> Void) -> some View {
        sheet(item: route, onDismiss: onDismiss) { route in
            let detent: PBSheetDetent = if case .sheet(let detent) = route.presentation { detent } else { .fitted }
            PBSheetPresentation(detent: detent) {
                RouteView(route: route)
            }
        }
        .pbSheetScrim(isShown: route.wrappedValue != nil)
    }
}

private struct ModalLayers: ViewModifier {
    let level: Int
    @Environment(AppRouter.self) private var router

    func body(content: Content) -> some View {
        content.fullScreenCover(item: layer) { _ in
            ModalLayerView(level: level)
        }
    }

    private var layer: Binding<ModalLayer?> {
        Binding {
            router.modals.indices.contains(level) ? router.modals[level] : nil
        } set: { newValue in
            if newValue == nil, router.modals.count > level {
                router.modals.removeSubrange(level...)
            }
        }
    }
}

/// One modal layer: its root route, its push stack and its sheet, with the next layer above it.
private struct ModalLayerView: View {
    let level: Int
    @Environment(AppRouter.self) private var router

    var body: some View {
        if router.modals.indices.contains(level) {
            NavigationStack(path: path) {
                RouteView(route: router.modals[level].root)
                    .navigationBarHiddenKeepingSwipeBack()
                    .navigationDestination(for: Route.self) { route in
                        RouteView(route: route)
                            .navigationBarHiddenKeepingSwipeBack()
                    }
            }
            .routeSheet(sheet, onDismiss: router.sheetDidDismiss)
            .toastHost(level: level + 1)
            .modalLayers(from: level + 1)
        }
    }

    private var path: Binding<[Route]> {
        Binding {
            router.modals.indices.contains(level) ? router.modals[level].path : []
        } set: { newValue in
            if router.modals.indices.contains(level) { router.modals[level].path = newValue }
        }
    }

    private var sheet: Binding<Route?> {
        Binding {
            router.modals.indices.contains(level) ? router.modals[level].sheet : nil
        } set: { newValue in
            if router.modals.indices.contains(level) { router.modals[level].sheet = newValue }
        }
    }
}

extension View {
    /// The app toast on layer `level` (0 = main), shown only while that layer is on top: 16 above the
    /// tab bar on tab roots, 50 above the bottom edge elsewhere (app-architecture §7.2).
    fileprivate func toastHost(level: Int) -> some View {
        modifier(ToastHost(level: level))
    }
}

private struct ToastHost: ViewModifier {
    let level: Int
    @Environment(AppRouter.self) private var router

    func body(content: Content) -> some View {
        @Bindable var router = router
        let isTop = router.modals.count == level
        let bottom: CGFloat = level == 0 && router.mainPath.isEmpty
            ? PBTabBar.bottomOffset + PBSize.tabbar + PBSpace.s16
            : 50
        content.overlay {
            if isTop {
                Color.clear
                    .allowsHitTesting(false)
                    .pbToast($router.toast, bottomPadding: bottom)
                    .ignoresSafeArea(.container, edges: .bottom)
            }
        }
    }
}
