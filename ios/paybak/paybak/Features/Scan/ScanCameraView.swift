import PhotosUI
import SwiftUI

/// The receipt camera (screens-insights-ai §4.2): the dark backdrop with the live image (or the
/// simulated receipt), glass ✕ and flash, the corner guides, the hint pill, Upload photo and the
/// shutter. While a photo is read, the hint turns into "Reading…".
struct ScanCameraView: View {
    let camera: CameraSource
    let isReading: Bool
    @Binding var pickedPhoto: PhotosPickerItem?
    let onClose: () -> Void
    let onCapture: () -> Void

    var body: some View {
        ZStack {
            PBColor.bgCamera.ignoresSafeArea()
            if camera.status == .running && !camera.isSimulated {
                CameraPreview(session: camera.session).ignoresSafeArea()
            }
            VStack(spacing: 0) {
                topBar
                ZStack {
                    if camera.isSimulated {
                        Image("art-receipt-full")
                            .resizable()
                            .frame(width: 257, height: 392.4)
                            .rotationEffect(.degrees(4))
                            .accessibilityLabel("Receipt in view")
                    }
                    ReceiptGuides()
                }
                .frame(width: 300, height: 460)
                .padding(.top, 64)
                hint
                    .padding(.top, PBSpace.s16)
                Spacer(minLength: PBSpace.s16)
                controls
                    .padding(.bottom, PBSpace.s24)
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
        }
        .toolbarVisibility(.hidden, for: .navigationBar)
    }

    private var topBar: some View {
        HStack {
            CameraGlassButton(systemName: "xmark", label: "Close", action: onClose)
                .accessibilityIdentifier("scan.close")
            Spacer()
            CameraGlassButton(systemName: "bolt.fill", label: "Flash", isOn: camera.isTorchOn,
                              action: camera.toggleTorch)
                .accessibilityIdentifier("scan.flash")
        }
        .overlay {
            Text("Scan receipt")
                .textStyle(.headline)
                .foregroundStyle(PBColor.textInverse)
                .accessibilityAddTraits(.isHeader)
        }
        .frame(height: PBSize.tap)
    }

    @ViewBuilder
    private var hint: some View {
        HStack(spacing: PBSpace.s6) {
            if isReading {
                ProgressView()
                    .controlSize(.small)
                    .tint(PBColor.textInverse)
            }
            Text(hintText)
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textInverse)
        }
        .padding(.horizontal, PBSpace.s16)
        .frame(height: 36)
        .background(PBColor.bgScrim, in: .capsule)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("scan.hint")
    }

    private var hintText: String {
        if isReading { return "Reading the receipt…" }
        return camera.status == .denied ? "Allow camera access in Settings to scan receipts." : "Fit the whole receipt in the frame"
    }

    private var controls: some View {
        ZStack {
            HStack {
                PhotosPicker(selection: $pickedPhoto, matching: .images) {
                    HStack(spacing: PBSpace.s4) {
                        PBIconView(.image, size: PBSize.iconSm)
                        Text("Upload photo")
                            .textStyle(.footnote)
                    }
                    .foregroundStyle(PBColor.iconInverse)
                    .padding(.horizontal, PBSpace.s12)
                    .frame(height: 36)
                    .background(PBColor.bgScrim, in: .capsule)
                }
                .accessibilityIdentifier("scan.upload")
                Spacer()
            }
            // The shutter gives its own light haptic, as Android's PbShutterButton does.
            PBShutterButton(action: onCapture)
                .disabled(isReading || camera.status != .running)
                .accessibilityIdentifier("scan.shutter")
        }
    }
}

/// The kit's glass symbol button on the dark camera: a translucent disc with a white symbol.
private struct CameraGlassButton: View {
    let systemName: String
    let label: String
    var isOn = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 25, weight: .medium))
                .foregroundStyle(isOn ? PBColor.iconPrimary : PBColor.iconInverse)
                .frame(width: PBSize.tap, height: PBSize.tap)
                .glassEffect(.regular.tint(PBColor.bgPrimary.opacity(isOn ? 1 : 0.35)).interactive(), in: .circle)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
        .accessibilityAddTraits(isOn ? .isSelected : [])
    }
}

/// Four white corner brackets around the receipt area: 36 pt arms, 4 pt round-capped strokes.
private struct ReceiptGuides: View {
    var body: some View {
        Canvas { context, size in
            let arm: CGFloat = 36
            let radius: CGFloat = 12
            let inset: CGFloat = 2
            var path = Path()
            for (x, y, dx, dy) in [(inset, inset, 1.0, 1.0), (size.width - inset, inset, -1.0, 1.0),
                                   (size.width - inset, size.height - inset, -1.0, -1.0), (inset, size.height - inset, 1.0, -1.0)] {
                path.move(to: CGPoint(x: x, y: y + dy * arm))
                path.addLine(to: CGPoint(x: x, y: y + dy * radius))
                path.addQuadCurve(to: CGPoint(x: x + dx * radius, y: y), control: CGPoint(x: x, y: y))
                path.addLine(to: CGPoint(x: x + dx * arm, y: y))
            }
            context.stroke(path, with: .color(PBColor.iconInverse), style: StrokeStyle(lineWidth: 4, lineCap: .round))
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

#Preview("ScanCameraView") {
    ScanCameraView(camera: CameraSource(), isReading: false, pickedPhoto: .constant(nil), onClose: {}, onCapture: {})
}
