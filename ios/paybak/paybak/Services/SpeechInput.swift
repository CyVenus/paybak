import AVFoundation
import Speech

/// Dictation into Ask Paybak's field (app-architecture §4): Speech on the device where it can, fed by
/// the microphone. It transcribes until the speaker pauses for a moment, or until it's cancelled.
enum SpeechInput {
    enum DictationError: Error {
        case notAllowed
        case unavailable
    }

    /// How long a pause ends the dictation.
    private static let pause: Duration = .seconds(2)

    /// Asks for the microphone and speech permissions the first time, then streams the transcript
    /// into `onText` as it grows.
    static func dictate(onText: @escaping (String) -> Void) async throws {
        guard await speechAllowed(), await AVAudioApplication.requestRecordPermission() else { throw DictationError.notAllowed }
        guard let recognizer = SFSpeechRecognizer(locale: Locale(identifier: "en-IN")) ?? SFSpeechRecognizer(),
              recognizer.isAvailable else { throw DictationError.unavailable }
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.record, mode: .measurement, options: .duckOthers)
        try session.setActive(true, options: .notifyOthersOnDeactivation)
        defer { try? session.setActive(false, options: .notifyOthersOnDeactivation) }

        let recording = try Recording(recognizer: recognizer)
        defer { recording.stop() }
        var lastHeard = ContinuousClock.now
        let watchdog = Task {
            while !Task.isCancelled {
                try await Task.sleep(for: .milliseconds(250))
                if ContinuousClock.now - lastHeard > pause { recording.finish() }
            }
        }
        defer { watchdog.cancel() }
        try await withTaskCancellationHandler {
            for try await text in recording.transcripts {
                lastHeard = .now
                onText(text)
            }
        } onCancel: {
            recording.stop()
        }
    }

    /// Nonisolated: Speech answers on its own queue.
    private nonisolated static func speechAllowed() async -> Bool {
        await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { continuation.resume(returning: $0 == .authorized) }
        }
    }
}

/// The audio engine and the recognition request; their callbacks arrive on audio and Speech queues.
/// The engine and request are only started and stopped through `finish` / `stop`, which are safe to
/// call more than once.
private nonisolated final class Recording: @unchecked Sendable {
    let transcripts: AsyncThrowingStream<String, Error>
    private let engine = AVAudioEngine()
    private let request = SFSpeechAudioBufferRecognitionRequest()
    private let task: SFSpeechRecognitionTask

    init(recognizer: SFSpeechRecognizer) throws {
        request.shouldReportPartialResults = true
        request.requiresOnDeviceRecognition = recognizer.supportsOnDeviceRecognition
        let (stream, continuation) = AsyncThrowingStream<String, Error>.makeStream()
        transcripts = stream
        task = recognizer.recognitionTask(with: request) { result, error in
            if let result {
                continuation.yield(result.bestTranscription.formattedString)
                if result.isFinal { continuation.finish() }
            } else if let error {
                continuation.finish(throwing: error)
            }
        }
        let input = engine.inputNode
        let request = request
        // iOS 27's throwing tap (the SDK doesn't refine its Swift name yet).
        do {
            try input.__installTap(onBus: 0, bufferSize: 1024, format: input.outputFormat(forBus: 0), error: ()) { buffer, _ in
                request.append(buffer)
            }
        } catch {
            task.cancel()
            throw error
        }
        engine.prepare()
        try engine.start()
    }

    /// Stops listening; the recogniser then delivers its final transcript.
    func finish() {
        guard engine.isRunning else { return }
        engine.stop()
        engine.inputNode.removeTap(onBus: 0)
        request.endAudio()
    }

    /// Stops everything at once.
    func stop() {
        finish()
        task.cancel()
    }
}
