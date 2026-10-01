import SwiftUI
import UIKit

/// The system share sheet for text, links and files (remind, invite link, QR link, export). Present
/// it with `.systemShare(item:)`; `onComplete` reports whether the user finished sharing.
struct ShareItem: Identifiable {
    let id = UUID()
    var text: String?
    var url: URL?
    var onComplete: (Bool) -> Void = { _ in }

    var activityItems: [Any] { [text as Any?, url as Any?].compactMap(\.self) }
}

extension View {
    /// Presents the share sheet the UIKit way, from the view's own controller, so a sheet that
    /// shares (Remind) stays on screen under it (app-architecture §2.4).
    func systemShare(item: Binding<ShareItem?>) -> some View {
        background(SharePresenter(item: item))
    }
}

private struct SharePresenter: UIViewControllerRepresentable {
    @Binding var item: ShareItem?

    func makeUIViewController(context: Context) -> UIViewController {
        let host = UIViewController()
        host.view.isUserInteractionEnabled = false
        return host
    }

    func updateUIViewController(_ host: UIViewController, context: Context) {
        guard let share = item, host.presentedViewController == nil else { return }
        let controller = UIActivityViewController(activityItems: share.activityItems, applicationActivities: nil)
        controller.completionWithItemsHandler = { _, completed, _, _ in
            item = nil
            share.onComplete(completed)
        }
        controller.popoverPresentationController?.sourceView = host.view
        host.present(controller, animated: true)
    }
}
