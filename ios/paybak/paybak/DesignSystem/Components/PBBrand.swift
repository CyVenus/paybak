import SwiftUI

/// Brand / App Mark (Figma 8:22): the white "P" monogram on a black squircle. Drawn natively so it
/// stays sharp at every size and can be animated: 22.37 % continuous corners (Figma's 60 % corner
/// smoothing), and the glyph as one stroked path with round caps and joins.
struct PBAppMark: View {
    /// Figma sizes: 160 (cover), 96 (splash), 40, 28 (header lockup).
    var size: CGFloat = 96

    var body: some View {
        RoundedRectangle(cornerRadius: 0.2237 * size, style: .continuous)
            .fill(PBColor.bgInverse)
            .overlay {
                PBMonogram()
                    .stroke(
                        PBColor.iconInverse,
                        style: StrokeStyle(lineWidth: 0.109375 * size, lineCap: .round, lineJoin: .round)
                    )
            }
            .frame(width: size, height: size)
            .accessibilityHidden(true)
    }
}

/// The "P" centre line in unit coordinates of the mark (brand/INDEX.md): stem, top bar, a clockwise
/// half-circle bowl, and back to the stem.
private struct PBMonogram: Shape {
    func path(in rect: CGRect) -> Path {
        func point(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
            CGPoint(x: rect.minX + x * rect.width, y: rect.minY + y * rect.height)
        }
        var path = Path()
        path.move(to: point(0.34375, 0.765625))
        path.addLine(to: point(0.34375, 0.234375))
        path.addLine(to: point(0.5078125, 0.234375))
        path.addArc(
            center: point(0.5078125, 0.40625),
            radius: 0.171875 * rect.width,
            startAngle: .degrees(-90),
            endAngle: .degrees(90),
            clockwise: false
        )
        path.addLine(to: point(0.34375, 0.578125))
        return path
    }
}

/// Brand / Logo (Figma 8:31): the mark with the live "Paybak" wordmark in Manrope ExtraBold.
struct PBLogo: View {
    enum Layout {
        /// Mark 28 + Wordmark S, gap 8 (Get Started, Home header).
        case horizontal
        /// Mark 96 over Wordmark L, gap 16 (Splash).
        case stacked
    }

    var layout: Layout = .horizontal

    var body: some View {
        Group {
            switch layout {
            case .horizontal:
                HStack(spacing: PBSpace.s8) {
                    PBAppMark(size: 28)
                    wordmark(.wordmarkS)
                }
            case .stacked:
                VStack(spacing: PBSpace.s16) {
                    PBAppMark(size: 96)
                    wordmark(.wordmarkL)
                }
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Paybak")
    }

    private func wordmark(_ style: PBTextStyle) -> some View {
        Text(verbatim: "Paybak")
            .textStyle(style)
            .foregroundStyle(PBColor.textPrimary)
    }
}

#Preview("Brand") {
    VStack(spacing: PBSpace.s32) {
        HStack(alignment: .bottom, spacing: PBSpace.s16) {
            PBAppMark(size: 160)
            PBAppMark(size: 96)
            PBAppMark(size: 40)
            PBAppMark(size: 28)
        }
        PBLogo(layout: .horizontal)
        PBLogo(layout: .stacked)
    }
}
