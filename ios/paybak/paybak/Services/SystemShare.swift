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
    func systemShare(item: Binding<ShareItem?>) -> some View {
        sheet(item: item) { share in
            ActivitySheet(item: share)
                .presentationDetents([.medium, .large])
                .ignoresSafeArea()
        }
    }
}

private struct ActivitySheet: UIViewControllerRepresentable {
    let item: ShareItem

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: item.activityItems, applicationActivities: nil)
        controller.completionWithItemsHandler = { _, completed, _, _ in item.onComplete(completed) }
        return controller
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
