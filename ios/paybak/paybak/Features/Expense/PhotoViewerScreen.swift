import SwiftUI

/// A receipt or proof photo, full screen on black (screens-activity §4.3-D, proposal): pinch to zoom
/// (up to 4×), drag to move a zoomed photo, ✕ to close. Demo records show the bundled receipt art.
struct PhotoViewerScreen: View {
    let photo: PhotoRef

    @Environment(AppRouter.self) private var router
    @State private var zoom: CGFloat = 1
    @GestureState private var pinch: CGFloat = 1
    @State private var offset: CGSize = .zero
    @GestureState private var drag: CGSize = .zero

    private var scale: CGFloat { min(max(zoom * pinch, 1), 4) }

    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.black.ignoresSafeArea()
            Group {
                if let image = PhotoFiles.image(photo) {
                    image
                        .resizable()
                        .scaledToFit()
                        // The bundled receipt art sits at the screen margins.
                        .padding(isAsset ? PBLayout.screenMargin : 0)
                        .scaleEffect(scale)
                        .offset(x: offset.width + drag.width, y: offset.height + drag.height)
                        .gesture(zoomGesture.simultaneously(with: panGesture))
                        .accessibilityLabel("Receipt photo")
                } else {
                    Text("This photo isn’t available.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textInverse)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            PBGlassCloseButton(action: router.dismissModal)
                .padding(PBLayout.screenMargin)
                .accessibilityIdentifier("photoViewer.close")
        }
        .routeTestRoot("photoViewer")
    }

    private var isAsset: Bool {
        if case .asset = photo { return true }
        return false
    }

    /// Pinch between 1× and 4×; back at 1× the photo recentres.
    private var zoomGesture: some Gesture {
        MagnifyGesture()
            .updating($pinch) { value, state, _ in state = value.magnification }
            .onEnded { value in
                zoom = min(max(zoom * value.magnification, 1), 4)
                if zoom == 1 { offset = .zero }
            }
    }

    /// Moves the photo while it's zoomed in.
    private var panGesture: some Gesture {
        DragGesture()
            .updating($drag) { value, state, _ in
                if scale > 1 { state = value.translation }
            }
            .onEnded { value in
                guard zoom > 1 else { return }
                offset.width += value.translation.width
                offset.height += value.translation.height
            }
    }
}
