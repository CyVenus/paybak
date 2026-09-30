import Foundation

// STUB (app-architecture §4): Lane C fills this with Speech + AVAudioEngine, keeping these names.
/// Dictation into Ask Paybak's field.
enum SpeechInput {
    static var isAvailable: Bool { false }

    /// Transcribes until the user stops; the partial text arrives through `onText`.
    static func dictate(onText: @escaping (String) -> Void) async throws {}
}
