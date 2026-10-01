import SwiftUI

/// The 2 pt `bg/inverse` caret of controls that draw their own text in place of a visible text
/// field (the amount display, the inline value field). It blinks every 0.5 s and shows again
/// whenever `restartKey` changes (typing moved it); it stays steady with Reduce Motion.
struct PBCaret<Key: Equatable>: View {
    let height: CGFloat
    let restartKey: Key

    @State private var isVisible = true
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    init(height: CGFloat, restartKey: Key) {
        self.height = height
        self.restartKey = restartKey
    }

    var body: some View {
        Rectangle()
            .fill(PBColor.bgInverse)
            .frame(width: 2, height: height)
            .opacity(isVisible ? 1 : 0)
            .task(id: restartKey) {
                isVisible = true
                guard !reduceMotion else { return }
                while !Task.isCancelled {
                    try? await Task.sleep(for: .milliseconds(500))
                    guard !Task.isCancelled else { return }
                    isVisible.toggle()
                }
            }
            .accessibilityHidden(true)
    }
}

extension PBCaret where Key == Int {
    init(height: CGFloat) {
        self.init(height: height, restartKey: 0)
    }
}

#Preview("PBCaret") {
    HStack(spacing: PBSpace.s2) {
        Text("₹2,800").textStyle(.amountDisplay)
        PBCaret(height: 56)
    }
}
