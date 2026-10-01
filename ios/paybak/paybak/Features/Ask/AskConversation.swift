import Foundation
import Observation

/// Ask Paybak's chat for this app session (screens-insights-ai §3.6 #8): reopening the sparkle shows
/// the last conversation; a relaunch starts fresh. Nothing leaves the device.
@Observable
final class AskConversation {
    struct Turn: Identifiable {
        let id: Int
        let prompt: String
        let reply: AssistantReply
        /// The drafted expense once saved (the card shows "Expense added").
        var savedExpense: ExpenseID?
    }

    /// The one conversation of this run.
    static let session = AskConversation()

    private(set) var turns: [Turn] = []
    /// A draft opened in the full form: which turn, and the expenses that existed before, so the
    /// one the form saves can be found.
    @ObservationIgnored private var editing: (turn: Int, known: Set<ExpenseID>)?

    func ask(_ prompt: String, books: Books, upi: String?) {
        turns.append(Turn(id: turns.count, prompt: prompt, reply: books.answer(prompt, upi: upi)))
    }

    func markSaved(_ turn: Turn.ID, expense: ExpenseID) {
        guard turns.indices.contains(turn) else { return }
        turns[turn].savedExpense = expense
    }

    func beginEditing(_ turn: Turn.ID, ledger: Ledger) {
        editing = (turn, Set(ledger.expenses.map(\.id)))
    }

    /// After a ledger change: the expense the full form saved for the draft being edited.
    func noticeSave(in ledger: Ledger) {
        guard let editing, let saved = ledger.expenses.last(where: { !editing.known.contains($0.id) && $0.createdBy == Person.me })
        else { return }
        markSaved(editing.turn, expense: saved.id)
        self.editing = nil
    }
}
