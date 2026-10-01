package app.paybak.paybak.feature.ask

import androidx.compose.runtime.mutableStateListOf
import app.paybak.paybak.domain.ask.AskAnswer
import java.time.Instant

/**
 * One exchange of the chat: what you asked and the reply. A drafted expense records
 * [savedExpenseId] once saved, and [editStartedAt] while it's open in the Add expense form.
 */
data class ChatTurn(
    val question: String,
    val answer: AskAnswer,
    val savedExpenseId: String? = null,
    val editStartedAt: Instant? = null,
)

/**
 * The conversation, kept for the session (insights §3.6 #8): closing the chat and opening it again
 * shows it as it was; relaunching the app starts afresh. Nothing is stored or sent.
 */
object AskChat {
    val turns = mutableStateListOf<ChatTurn>()
}
