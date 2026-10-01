import SwiftUI

/// One FAQ answer (screens-settings §11 proposal): the whole question as a Title/3 heading (a long
/// one would truncate in the push header), the answer in Body.
struct HelpAnswerScreen: View {
    let index: Int

    var body: some View {
        let faq = HelpFAQ.all[min(max(index, 0), HelpFAQ.all.count - 1)]
        SettingsScaffold(title: "Help", testIDPrefix: "helpAnswer") {
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text(faq.question)
                    .textStyle(.title3)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("helpAnswer.question")
                Text(faq.answer)
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityIdentifier("helpAnswer.body")
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

/// The common questions, verbatim from Figma, with the proposed answers.
struct HelpFAQ {
    let question: String
    let answer: String

    static let all = [
        HelpFAQ(question: "Does Paybak move money?",
                answer: "No. Paybak only records who paid and who owes. Friends pay each other in their own apps, by UPI, bank transfer or cash, and Paybak keeps the ledger."),
        HelpFAQ(question: "How do payment confirmations work?",
                answer: "When a friend records a payment to you, it stays pending until you confirm you got it. Confirm it and the balance updates, or mark it Not received and nothing changes."),
        HelpFAQ(question: "How does simplify debts work?",
                answer: "Paybak nets out the balances in a group so fewer payments settle everyone. Nobody pays or gets more in total; only who pays whom changes."),
        HelpFAQ(question: "Can I track more than one currency?",
                answer: "Yes. Each expense can use its own currency, and the rate is saved with it. By default everything converts to your default currency; turn on Keep balances per currency in Currency to see each currency separately."),
        HelpFAQ(question: "Why can’t I leave a group?",
                answer: "You can leave a group once your balance in it is settled. Settle up first, then leave."),
    ]
}

#Preview("HelpAnswerScreen") {
    HelpAnswerScreen(index: 1)
        .environment(AppRouter())
}
