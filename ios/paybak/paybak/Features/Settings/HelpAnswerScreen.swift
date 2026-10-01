import SwiftUI

/// One FAQ answer (screens-settings §11 proposal): the question as the title, the answer in Body.
struct HelpAnswerScreen: View {
    let index: Int

    var body: some View {
        let faq = HelpFAQ.all.indices.contains(index) ? HelpFAQ.all[index] : HelpFAQ.all[0]
        SettingsScaffold(title: faq.question, testIDPrefix: "helpAnswer") {
            Text(faq.answer)
                .textStyle(.body)
                .foregroundStyle(PBColor.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityIdentifier("helpAnswer.body")
        }
    }
}

/// The common questions, verbatim from Figma, with the proposed answers.
struct HelpFAQ {
    /// The list row's title, with a `\n` where Figma wraps a question that would otherwise leave one
    /// word alone on its second line (iOS would move two words down).
    let rowTitle: String
    let answer: String

    var question: String { rowTitle.replacing("\n", with: " ") }

    static let all = [
        HelpFAQ(rowTitle: "Does Paybak move money?",
                answer: "No. Paybak only records who paid and who owes. Friends pay each other in their own apps, by UPI, bank transfer or cash, and Paybak keeps the ledger."),
        HelpFAQ(rowTitle: "How do payment confirmations\nwork?",
                answer: "When a friend records a payment to you, it stays pending until you confirm you got it. Confirm it and the balance updates, or mark it Not received and nothing changes."),
        HelpFAQ(rowTitle: "How does simplify debts work?",
                answer: "Paybak nets out the balances in a group so fewer payments settle everyone. Nobody pays or gets more in total; only who pays whom changes."),
        HelpFAQ(rowTitle: "Can I track more than one\ncurrency?",
                answer: "Yes. Each expense can use its own currency, and the rate is saved with it. By default everything converts to your default currency; turn on Keep balances per currency in Currency to see each currency separately."),
        HelpFAQ(rowTitle: "Why can’t I leave a group?",
                answer: "You can leave a group once your balance in it is settled. Settle up first, then leave."),
    ]
}

#Preview("HelpAnswerScreen") {
    HelpAnswerScreen(index: 1)
        .environment(AppRouter())
}
