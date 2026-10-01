import SwiftUI

/// Where the top screen's pinned bottom buttons start, so the app toast ("Loan added", "Component
/// added") sits 12 pt above them instead of over them (as on Android). Set by `pinnedFooter()`.
struct PinnedFooterMark: Equatable {
    /// The screen that set it: a screen going away only clears its own mark.
    let owner: UUID
    /// The layer the screen is on (0 = the main stack, n = the nth modal layer); only that layer's
    /// toast follows it.
    let layer: Int
    /// The buttons' top edge in global (window) coordinates.
    var top: CGFloat
}

extension View {
    /// Marks a screen's pinned bottom buttons: while the screen shows, the app toast sits 12 pt above
    /// them (`PinnedFooterMark`). Cleared when the screen goes.
    func pinnedFooter() -> some View {
        modifier(PinnedFooter())
    }
}

extension EnvironmentValues {
    /// The toast layer a screen is on: 0 on the main stack, n inside the nth modal layer.
    @Entry var toastLayer: Int = 0
}

private struct PinnedFooter: ViewModifier {
    @Environment(AppRouter.self) private var router
    @Environment(\.toastLayer) private var layer

    @State private var owner = UUID()
    @State private var top: CGFloat?
    @State private var isShown = false

    func body(content: Content) -> some View {
        content
            .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).minY.rounded() } action: { newTop in
                top = newTop
                if isShown {
                    mark()
                }
            }
            .onAppear {
                isShown = true
                mark()
            }
            .onDisappear {
                isShown = false
                if router.pinnedFooter?.owner == owner {
                    router.pinnedFooter = nil
                }
            }
    }

    private func mark() {
        guard let top else { return }
        let mark = PinnedFooterMark(owner: owner, layer: layer, top: top)
        if router.pinnedFooter != mark {
            router.pinnedFooter = mark
        }
    }
}
