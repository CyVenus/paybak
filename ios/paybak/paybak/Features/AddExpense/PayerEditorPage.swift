import SwiftUI

/// "Multiple people" (add-expense §6.3): how much each person on the expense paid, on the split
/// editor's Exact rows and Split Total footer. Done is enabled once the amounts add up to the
/// total; one payer left becomes the single payer again.
struct PayerEditorPage: View {
    let form: ExpenseForm

    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @Environment(\.dismiss) private var dismiss
    @State private var texts: [PersonID: String] = [:]
    @State private var paying: Set<PersonID> = []
    @State private var didLoad = false

    private var entered: Int64 {
        paying.reduce(0) { $0 + MoneyInput.minor(texts[$1] ?? "", currency: form.currency) }
    }

    private var isBalanced: Bool { entered == form.amount && !paying.isEmpty }

    var body: some View {
        let status = Splits.exactStatus(total: form.amount, amounts: [entered], currency: form.currency)
        ScrollView {
            VStack(spacing: PBSpace.s16) {
                Text(summary)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity)
                    .lineLimit(1)
                VStack(spacing: 0) {
                    ForEach(form.people, id: \.self) { person in
                        PBSplitRow(
                            name: store.books.firstName(person),
                            avatar: person == Person.me ? profileStore.avatarContent : store.ledger.person(person)?.avatarContent ?? .icon(.profile),
                            mode: .exact(textBinding(person)),
                            amount: "",
                            currency: form.currency,
                            isIncluded: Binding { paying.contains(person) } set: { setPaying(person, $0) },
                            showsDivider: person != form.people.last
                        )
                        .accessibilityIdentifier("payers.row.\(person)")
                    }
                }
                .pbCard(padding: 0)
                Text("Enter how much each person paid.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.bottom, PBSpace.s16)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBDoneHeader(title: "Paid by", isDoneEnabled: isBalanced, testIDPrefix: "payers", onBack: { dismiss() }) {
                apply()
                dismiss()
            }
        }
        .safeAreaInset(edge: .bottom) {
            PBSplitTotalBar(left: status.left, detail: status.detail, isError: !isBalanced)
                .accessibilityIdentifier("payers.total")
                .padding(.horizontal, PBLayout.screenMargin)
                .phoneContentWidth()
                .keyboardGap(PBSpace.s8)
        }
        .navigationBarHiddenKeepingSwipeBack()
        .onAppear(perform: load)
        .routeTestRoot("addExpense")
    }

    private var summary: String {
        let total = Money.format(form.amount, form.currency)
        let title = form.title.trimmingCharacters(in: .whitespaces)
        return title.isEmpty ? total : "\(title) · \(total)"
    }

    private func load() {
        guard !didLoad else { return }
        didLoad = true
        let payers = form.payers.isEmpty ? [Payer(personId: form.payerId, amount: form.amount)] : form.payers
        paying = Set(payers.map(\.personId))
        for person in form.people {
            texts[person] = MoneyInput.text(payers.first { $0.personId == person }?.amount ?? 0, currency: form.currency)
        }
    }

    private func textBinding(_ person: PersonID) -> Binding<String> {
        Binding {
            texts[person] ?? ""
        } set: {
            texts[person] = PBAmountField.sanitize($0, allowsDecimals: Money.info(form.currency).exponent > 0)
        }
    }

    private func setPaying(_ person: PersonID, _ isPaying: Bool) {
        if isPaying {
            paying.insert(person)
        } else {
            paying.remove(person)
            texts[person] = "0"
        }
    }

    private func apply() {
        let payers = form.people.filter(paying.contains).map {
            Payer(personId: $0, amount: MoneyInput.minor(texts[$0] ?? "", currency: form.currency))
        }
        if payers.count == 1 {
            form.payerId = payers[0].personId
            form.payers = []
        } else {
            form.payers = payers
        }
    }
}
