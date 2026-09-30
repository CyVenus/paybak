import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane A replaces this file with the real screen and keeps
// this initializer.
/// A receipt or proof photo, full screen.
struct PhotoViewerScreen: View {
    let photo: PhotoRef


    var body: some View {
        RoutePlaceholder(route: .photoViewer(photo), title: "Photo", owner: .a, spec: "screens-activity §4")
    }
}
