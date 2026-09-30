package app.paybak.paybak.service.speech

import android.content.Intent
import android.speech.RecognizerIntent

/**
 * Dictation for Ask Paybak's mic: the system speech recognizer, which records in its own UI (no
 * RECORD_AUDIO permission; app-architecture §4). Lane C (M9) owns it.
 */
object SpeechInput {
    /** Launch with an ActivityResult launcher; the text is in `RecognizerIntent.EXTRA_RESULTS`. */
    fun intent(prompt: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
}
