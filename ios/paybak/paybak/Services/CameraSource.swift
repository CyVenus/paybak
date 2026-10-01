import AVFoundation
import Observation
import SwiftUI
import UIKit

/// Where the receipt camera gets its pictures (app-architecture §4): the back camera through
/// AVFoundation, or the bundled receipt art on the simulator and when the debug toggle is on.
@Observable
final class CameraSource {
    enum Status {
        case starting
        case running
        /// Camera access was refused (Upload photo still works).
        case denied
        case unavailable
    }

    /// The debug menu's "Simulated receipt camera" switch.
    static let simulatedFeedKey = "debugSimulatedReceiptCamera"

    /// True on the simulator (no useful camera) and when the debug toggle is on.
    static var usesSimulatedFeed: Bool {
        #if targetEnvironment(simulator)
        true
        #else
        UserDefaults.standard.bool(forKey: simulatedFeedKey)
        #endif
    }

    /// The simulated feed's picture (the Leopold Cafe receipt).
    static var simulatedPhoto: UIImage? { UIImage(named: "art-receipt-full") }

    private(set) var status: Status = .starting
    private(set) var isTorchOn = false
    @ObservationIgnored let session = AVCaptureSession()
    @ObservationIgnored private let output = AVCapturePhotoOutput()
    @ObservationIgnored private var device: AVCaptureDevice?

    var isSimulated: Bool { Self.usesSimulatedFeed }

    /// Asks for camera access the first time, then starts the back camera.
    func start() async {
        guard !isSimulated else {
            status = .running
            return
        }
        guard await AVCaptureDevice.requestAccess(for: .video) else {
            status = .denied
            return
        }
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let input = try? AVCaptureDeviceInput(device: device) else {
            status = .unavailable
            return
        }
        self.device = device
        session.beginConfiguration()
        session.sessionPreset = .photo
        if session.canAddInput(input) { session.addInput(input) }
        if session.canAddOutput(output) { session.addOutput(output) }
        session.commitConfiguration()
        await Self.run(session, start: true)
        status = .running
    }

    func stop() {
        guard !isSimulated else { return }
        Task { await Self.run(session, start: false) }
    }

    /// The torch ("flash") stays on while framing the receipt.
    func toggleTorch() {
        isTorchOn.toggle()
        guard let device, device.hasTorch, (try? device.lockForConfiguration()) != nil else { return }
        device.torchMode = isTorchOn ? .on : .off
        device.unlockForConfiguration()
    }

    /// A still from the camera (or the simulated feed's picture).
    func capture() async throws -> UIImage {
        if isSimulated {
            guard let photo = Self.simulatedPhoto else { throw CaptureError.noPhoto }
            return photo
        }
        let delegate = PhotoCapture()
        output.capturePhoto(with: AVCapturePhotoSettings(), delegate: delegate)
        return try await delegate.photo()
    }

    enum CaptureError: Error {
        case noPhoto
    }

    /// `startRunning` blocks, so it runs off the main thread.
    private nonisolated static func run(_ session: AVCaptureSession, start: Bool) async {
        await Task.detached {
            if start { session.startRunning() } else { session.stopRunning() }
        }.value
    }
}

/// Bridges the photo output's delegate callback to async/await.
private nonisolated final class PhotoCapture: NSObject, AVCapturePhotoCaptureDelegate, @unchecked Sendable {
    private var continuation: CheckedContinuation<UIImage, Error>?
    private var result: Result<UIImage, Error>?
    private let lock = NSLock()

    func photo() async throws -> UIImage {
        try await withCheckedThrowingContinuation { continuation in
            lock.withLock {
                if let result {
                    continuation.resume(with: result)
                } else {
                    self.continuation = continuation
                }
            }
        }
    }

    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        let result: Result<UIImage, Error> = if let error {
            .failure(error)
        } else if let data = photo.fileDataRepresentation(), let image = UIImage(data: data) {
            .success(image)
        } else {
            .failure(CameraSource.CaptureError.noPhoto)
        }
        lock.withLock {
            if let continuation {
                continuation.resume(with: result)
                self.continuation = nil
            } else {
                self.result = result
            }
        }
    }
}

/// The live camera image, filling its frame.
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> PreviewView {
        let view = PreviewView()
        view.previewLayer.session = session
        view.previewLayer.videoGravity = .resizeAspectFill
        return view
    }

    func updateUIView(_ view: PreviewView, context: Context) {}

    final class PreviewView: UIView {
        override class var layerClass: AnyClass { AVCaptureVideoPreviewLayer.self }
        var previewLayer: AVCaptureVideoPreviewLayer { layer as! AVCaptureVideoPreviewLayer }
    }
}
