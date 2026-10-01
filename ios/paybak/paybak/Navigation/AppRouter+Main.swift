import SwiftUI

/// The main app's navigation helpers (app-architecture §2.7). Screens call these with route ids; the
/// hosts (`MainView`, `ModalLayers`, `RouteSheetHost`) draw the result.
extension AppRouter {
    /// Opens a route the way its presentation says: a tab switch, a push on the top layer, a new modal
    /// layer, or the top layer's sheet.
    func open(_ route: Route) {
        switch route.presentation {
        case .tab(let tab):
            select(tab)
        case .push:
            push(route)
        case .modal:
            modals.append(ModalLayer(root: route))
        case .sheet:
            setTopSheet(route)
        }
    }

    /// Dismisses every modal and sheet, pops the main stack and selects `tab`.
    func select(_ tab: Tab) {
        modals = []
        mainSheet = nil
        mainPath = []
        selectedTab = tab
    }

    /// Pops the top layer's stack; on a modal's root it dismisses the modal.
    func back() {
        if let top = modals.indices.last {
            if modals[top].path.isEmpty {
                dismissModal()
            } else {
                modals[top].path.removeLast()
            }
        } else if !mainPath.isEmpty {
            mainPath.removeLast()
        }
    }

    func dismissModal() {
        guard !modals.isEmpty else { return }
        modals.removeLast()
    }

    func dismissSheet() {
        setTopSheet(nil)
    }

    /// Pops the top layer back to its root.
    func popToRoot() {
        if let top = modals.indices.last {
            modals[top].path = []
        } else {
            mainPath = []
        }
    }

    /// Dismisses the current sheet, then opens `route` (the Add sheet's rows).
    func replaceSheet(with route: Route) {
        guard topSheet != nil else {
            open(route)
            return
        }
        routeAfterSheet = route
        dismissSheet()
    }

    /// A route sheet finished dismissing: opens the route waiting for it.
    func sheetDidDismiss() {
        guard let route = routeAfterSheet else { return }
        routeAfterSheet = nil
        open(route)
    }

    /// A modal's Save (Add expense, Record payment, Lend money): pushes `detail` without animation on
    /// the layer below, dismisses the modal so it slides away to reveal the detail, then shows the
    /// toast. Edit-mode saves just dismiss.
    func didSave(_ detail: Route, toast text: String) {
        guard let top = modals.indices.last else {
            open(detail)
            toast(text)
            return
        }
        var transaction = Transaction()
        transaction.disablesAnimations = true
        withTransaction(transaction) {
            if top == 0 {
                mainPath.append(detail)
            } else {
                modals[top - 1].path.append(detail)
            }
        }
        modals.removeLast()
        toast(text)
    }

    /// New group's Create: dismisses everything, selects Groups, pushes the new group or project and
    /// shows the toast.
    func didCreateGroup(_ id: GroupID, isProject: Bool) {
        select(.groups)
        groupsSegment = .groups
        var transaction = Transaction()
        transaction.disablesAnimations = true
        withTransaction(transaction) {
            mainPath = [isProject ? .project(id) : .group(id)]
        }
        toast(isProject ? "Project created" : "Group created")
    }

    /// Pro → `route`; free → the paywall, which continues to `route` once Pro unlocks.
    func requirePro(_ route: Route) {
        open(isPro() ? route : .paywall(continueTo: route))
    }

    /// The paywall's Done: dismisses it, then opens where it was going.
    func finishPaywall() {
        guard let index = modals.lastIndex(where: { if case .paywall = $0.root { true } else { false } }) else { return }
        let next: Route? = if case .paywall(let continueTo) = modals[index].root { continueTo } else { nil }
        modals.removeSubrange(index...)
        if let next { open(next) }
    }

    /// Shows a toast above everything (2 s, a new one replaces the current one).
    func toast(_ text: String) {
        toast = PBToastMessage(text)
    }

    // MARK: Results (cross-lane pickers)

    /// A picker's result: stores it for the screen that asked and closes the picker.
    func complete(_ requestID: String, with result: RouteResult) {
        results[requestID] = result
        if topSheet != nil {
            dismissSheet()
        } else {
            back()
        }
    }

    /// Takes (and removes) a result for a request.
    func takeResult(_ requestID: String) -> RouteResult? {
        results.removeValue(forKey: requestID)
    }

    // MARK: Layers

    /// The number of modal layers over the main stack.
    var modalDepth: Int { modals.count }

    /// The sheet of the top layer.
    var topSheet: Route? {
        modals.last.map(\.sheet) ?? mainSheet
    }

    /// Whether the tab shell is showing (nothing pushed, no modal): the tab bar and toasts use it.
    var isOnTabRoot: Bool { modals.isEmpty && mainPath.isEmpty }

    private func push(_ route: Route) {
        if let top = modals.indices.last {
            guard modals[top].path.last != route else { return }
            modals[top].path.append(route)
        } else {
            guard mainPath.last != route else { return }
            mainPath.append(route)
        }
    }

    private func setTopSheet(_ route: Route?) {
        if let top = modals.indices.last {
            modals[top].sheet = route
        } else {
            mainSheet = route
        }
    }
}

extension View {
    /// Receives a picker's result for `requestID` (§2.7): `action` runs once per result.
    func onRouteResult(_ requestID: String, perform action: @escaping (RouteResult) -> Void) -> some View {
        modifier(RouteResultReceiver(requestID: requestID, action: action))
    }
}

private struct RouteResultReceiver: ViewModifier {
    let requestID: String
    let action: (RouteResult) -> Void
    @Environment(AppRouter.self) private var router

    func body(content: Content) -> some View {
        content
            .onChange(of: router.results[requestID], initial: true) { _, result in
                guard result != nil, let taken = router.takeResult(requestID) else { return }
                action(taken)
            }
    }
}
