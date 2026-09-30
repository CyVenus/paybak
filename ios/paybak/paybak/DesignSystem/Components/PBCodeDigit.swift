import SwiftUI

/// Control / Code Digit (Figma 36:675): one 48 × 56 box of the 6-digit code.
struct PBCodeDigit: View {
    enum DigitState {
        case empty
        /// The next box to fill: black ring and a blinking caret.
        case focused
        case filled
        /// Red ring; the digit stays black.
        case error
    }

    let digit: Character?
    let state: DigitState

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: PBRadius.input)
                .fill(PBColor.bgCard)
            if let ring {
                RoundedRectangle(cornerRadius: PBRadius.input)
                    .strokeBorder(ring, lineWidth: 1.5)
            }
            switch state {
            case .focused:
                caret
            case .filled, .error:
                if let digit {
                    Text(String(digit))
                        .textStyle(.title2)
                        .foregroundStyle(PBColor.textPrimary)
                }
            case .empty:
                EmptyView()
            }
        }
        .frame(maxWidth: 48)
        .frame(height: 56)
        .animation(.easeOut(duration: 0.12), value: state)
    }

    private var ring: Color? {
        switch state {
        case .focused: PBColor.borderStrong
        case .error: PBColor.borderDestructive
        case .empty, .filled: nil
        }
    }

    /// A 2 × 24 bar blinking with a 1 s period (not designed in Figma; steady under Reduce Motion).
    private var caret: some View {
        TimelineView(.periodic(from: .now, by: 0.5)) { context in
            let isVisible = reduceMotion || Int(context.date.timeIntervalSinceReferenceDate * 2) % 2 == 0
            Rectangle()
                .fill(PBColor.bgInverse)
                .frame(width: 2, height: 24)
                .opacity(isVisible ? 1 : 0)
        }
    }
}

#Preview("PBCodeDigit") {
    HStack(spacing: PBSpace.s12) {
        PBCodeDigit(digit: nil, state: .empty)
        PBCodeDigit(digit: nil, state: .focused)
        PBCodeDigit(digit: "4", state: .filled)
        PBCodeDigit(digit: "7", state: .error)
    }
    .padding(PBLayout.screenMargin)
}
