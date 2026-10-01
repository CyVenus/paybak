import SwiftUI

/// Enter amount (screens-insights-ai §5.4): a variable rule's draft becomes a real expense, dated the
/// occurrence and split as the rule says. The amount is focused on open; Add stays disabled until
/// it's above zero, then lands back on the list with "Expense added".
struct EnterAmountScreen: View {
    let draftId: DraftID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    /// Read once, so the screen keeps its content while it slides away after Add.
    @State private var page: DraftAmountPage?
    @State private var amountText = ""
    @FocusState private var isAmountFocused: Bool

    var body: some View {
        Group {
            if let page {
                content(page)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear(perform: load)
        .routeTestRoot("enterDraftAmount")
    }

    /// A draft entered elsewhere (or gone) leaves nothing to do here.
    private func load() {
        guard page == nil else { return }
        if let page = ledgerStore.books.draftAmountPage(draftId), !page.isDone {
            self.page = page
        } else {
            router.dismissModal()
        }
    }

    private func content(_ page: DraftAmountPage) -> some View {
        let amount = MoneyInput.minor(amountText, currency: page.currency)
        return ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                HStack(spacing: PBSpace.s8) {
                    PBBadge("Draft")
                    Text(page.meta)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .padding(.top, PBSpace.s8)
                PBAmountField(
                    text: $amountText, currency: Currency(code: page.currency),
                    helper: "Drafts don’t count until you add the amount.", testIDPrefix: "enterAmount", focus: $isAmountFocused
                )
                VStack(spacing: 0) {
                    PBSettingRow("Paid by", value: page.paidBy, icon: .wallet, trailing: .none)
                    PBSettingRow("Split", value: page.split, icon: .split, trailing: .none)
                    PBSettingRow("Date", value: page.date, icon: .calendar, trailing: .none, showsDivider: false)
                }
                .pbCard(padding: 0)
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .phoneContentWidth()
        }
        .scrollBounceBehavior(.basedOnSize)
        .pbPinnedHeader {
            PBModalHeader(page.title, actionLabel: "Add", isActionEnabled: amount > 0, testIDPrefix: "enterAmount",
                          onClose: router.dismissModal) { add(amount) }
        }
        .onAppear { isAmountFocused = true }
    }

    private func add(_ amount: Int64) {
        guard (try? ledgerStore.enterDraftAmount(draftId, amount: amount)) != nil else { return }
        Haptics.success()
        router.dismissModal()
        router.toast("Expense added")
    }
}

#if DEBUG
#Preview("EnterAmountScreen") {
    GroupsPreview(scenarios: Scenario.pro()) {
        EnterAmountScreen(draftId: "d-gas-09")
    }
}
#endif
