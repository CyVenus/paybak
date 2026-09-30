import SwiftUI

/// The ＋ Add sheet route (screens-home §5): every tab's ＋ opens it; a row dismisses it, then opens
/// its modal.
struct AddSheet: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        PBAddSheet(onClose: router.dismissSheet) { action in
            router.replaceSheet(with: route(for: action))
        }
    }

    private func route(for action: PBAddSheet.Action) -> Route {
        switch action {
        case .expense: .addExpense(.new)
        case .payment: .recordPayment(.new)
        case .lend: .lendMoney(.new)
        case .group: .newGroup(.group)
        }
    }
}
