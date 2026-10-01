import SwiftUI

/// Control / Progress Bar (Figma 116:1099): budget, loan and share bars at the available width.
/// Small is 6 pt tall (rows), Large 12 pt (cards). On the gray capsule track (`chart/track`):
/// - the black fill from 0 to `value`;
/// - Projected: a `chart/bar` segment from 0 to `projected` behind the fill;
/// - Over: the red `chart/over` segment from `overFrom` (the budget point) to the end; the fill's
///   right end is square so black meets red with a straight edge;
/// - an optional 2 pt tick at `mark` (fair share, the budget), 3 pt taller than the track at both
///   ends, with a white outline.
/// Values are fractions (0…1) of the width. Changes animate (0.3 s ease-out).
struct PBProgressBar: View {
    enum Size {
        case small
        case large

        var height: CGFloat { self == .small ? 6 : 12 }
    }

    let value: Double
    var projected: Double?
    var overFrom: Double?
    var mark: Double?
    var size: Size = .small
    /// What the bar measures, for VoiceOver ("Budget used").
    var accessibilityLabel = "Progress"

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        GeometryReader { proxy in
            let width = proxy.size.width
            ZStack(alignment: .leading) {
                Capsule().fill(PBColor.chartTrack)
                if let projected {
                    Capsule()
                        .fill(PBColor.chartBar)
                        .frame(width: width * clamped(projected))
                }
                if let overFrom {
                    Rectangle()
                        .fill(PBColor.chartOver)
                        .padding(.leading, width * clamped(overFrom))
                }
                if value > 0 {
                    fillShape
                        .fill(PBColor.chartFill)
                        .frame(width: width * clamped(value))
                }
            }
            .frame(height: size.height)
            .clipShape(.capsule)
            .overlay(alignment: .leading) {
                // Not clipped: the tick overhangs the track by 3 pt at both ends.
                if let mark {
                    Rectangle()
                        .fill(PBColor.chartFill)
                        .frame(width: 2, height: size.height + 6)
                        .padding(PBSize.hairline)
                        .background(PBColor.bgPrimary)
                        .offset(x: width * clamped(mark) - 1 - PBSize.hairline)
                }
            }
        }
        .frame(height: size.height)
        .animation(reduceMotion ? nil : .easeOut(duration: 0.3), value: value)
        .animation(reduceMotion ? nil : .easeOut(duration: 0.3), value: projected)
        .accessibilityElement()
        .accessibilityLabel(accessibilityLabel)
        .accessibilityValue("\(Int((clamped(value) * 100).rounded())) percent")
    }

    /// A capsule, or with the red over segment only the left end is round.
    private var fillShape: AnyShape {
        if overFrom != nil {
            let radius = size.height / 2
            return AnyShape(UnevenRoundedRectangle(topLeadingRadius: radius, bottomLeadingRadius: radius))
        }
        return AnyShape(Capsule())
    }

    private func clamped(_ fraction: Double) -> CGFloat {
        CGFloat(min(max(fraction, 0), 1))
    }
}

#Preview("PBProgressBar") {
    VStack(spacing: PBSpace.s24) {
        PBProgressBar(value: 0.6)
        PBProgressBar(value: 0.87, projected: 0.97)
        PBProgressBar(value: 0.976, overFrom: 0.976)
        PBProgressBar(value: 0.6, size: .large)
        PBProgressBar(value: 0.87, projected: 0.97, size: .large)
        PBProgressBar(value: 0.976, overFrom: 0.976, mark: 0.976, size: .large)
        PBProgressBar(value: 0.6, mark: 0.51)
        PBProgressBar(value: 0, size: .large)
    }
    .padding(PBLayout.screenMargin)
}
