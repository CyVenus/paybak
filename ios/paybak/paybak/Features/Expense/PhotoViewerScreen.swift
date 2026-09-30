import SwiftUI

/// A receipt or proof photo, full screen on black (screens-activity §4.3-D, proposal): pinch or
/// double-tap to zoom, ✕ to close.
struct PhotoViewerScreen: View {
    let photo: PhotoRef

    @Environment(AppRouter.self) private var router
    @State private var zoom: CGFloat = 1
    @GestureState private var pinch: CGFloat = 1

    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.black.ignoresSafeArea()
            Group {
                if let image = PhotoFiles.image(photo) {
                    image
                        .resizable()
                        .scaledToFit()
                        .scaleEffect(min(max(zoom * pinch, 1), 4))
                        .gesture(MagnifyGesture().updating($pinch) { value, state, _ in state = value.magnification }
                            .onEnded { zoom = min(max(zoom * $0.magnification, 1), 4) })
                        .onTapGesture(count: 2) {
                            withAnimation(.snappy) { zoom = zoom > 1 ? 1 : 2 }
                        }
                        .accessibilityLabel("Photo")
                } else {
                    Text("This photo isn’t available.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textInverse)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            PBGlassCloseButton(action: router.dismissModal)
                .padding(.horizontal, PBLayout.screenMargin)
                .accessibilityIdentifier("photoViewer.close")
        }
        .routeTestRoot("photoViewer")
    }
}
