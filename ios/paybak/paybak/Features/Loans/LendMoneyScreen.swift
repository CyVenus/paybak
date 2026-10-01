import SwiftUI

/// Lend money (record-lend-group §4): a direct IOU, lent or borrowed, optionally paid back in
/// installments (count, repeats, first due and the schedule preview) or by one due date with quick
/// chips. Save lands on the loan with "Loan added"; editing saves and closes.
struct LendMoneyScreen: View {
    let args: LendMoneyArgs

    @Environment(LedgerStore.self) private var store
    @State private var form: LoanForm?

    var body: some View {
        Group {
            if let form {
                LoanFormView(form: form)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear {
            if form == nil { form = makeForm() }
        }
    }

    private func makeForm() -> LoanForm {
        let books = store.books
        if let id = args.editing, let loan = store.ledger.loan(id) {
            return LoanForm(editing: id, direction: loan.lenderId == Person.me ? .lent : .borrowed, amount: loan.amount,
                            currency: loan.currency, rate: loan.rate, person: loan.friendId, reason: loan.reason ?? "",
                            date: loan.date, installments: loan.installments, dueDate: loan.dueDate)
        }
        return LoanForm(direction: args.direction, amount: nil, currency: books.defaultCurrency, rate: nil, person: args.person,
                        reason: "", date: books.today, installments: nil, dueDate: nil)
    }
}

private enum LoanSheet: String, Identifiable {
    case reason
    case repeats

    var id: String { rawValue }
}

private struct LoanFormView: View {
    @Bindable var form: LoanForm

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @State private var requests = (person: RecordID.make(), currency: RecordID.make(), date: RecordID.make(),
                                   firstDue: RecordID.make(), due: RecordID.make())
    @State private var sheet: LoanSheet?
    @State private var showsDiscard = false
    @State private var error: String?
    @FocusState private var amountFocused: Bool

    private var books: Books { store.books }

    var body: some View {
        ScrollView {
            VStack(spacing: PBSpace.s16) {
                PBSegmentedControl(options: ["I lent", "I borrowed"], selection: Binding {
                    form.isLent ? 0 : 1
                } set: {
                    Haptics.selection()
                    form.direction = $0 == 0 ? .lent : .borrowed
                })
                PBAmountField(text: $form.amountText, currency: Currency(code: form.currency), helper: rateLine,
                              testIDPrefix: "lendMoney", focus: $amountFocused, onCurrencyTap: openCurrency)
                VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                    detailsCard
                    VStack(alignment: .leading, spacing: PBSpace.s12) {
                        repaymentCard
                        if let preview = form.schedulePreview {
                            Text(preview)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textSecondary)
                                .accessibilityIdentifier("lendMoney.schedule")
                        }
                    }
                }
            }
            .padding(.bottom, PBSpace.s32)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBModalHeader(form.editing == nil ? "Lend money" : "Edit loan", actionLabel: "Save", isActionEnabled: form.canSave,
                          testIDPrefix: "lendMoney", onClose: close, onAction: save)
        }
        .pbItemSheet(item: $sheet) { sheet in
            switch sheet {
            case .reason:
                LoanReasonSheet(text: form.reason, onClose: { self.sheet = nil }) { reason in
                    form.reason = reason
                    self.sheet = nil
                }
            case .repeats:
                RepeatsSheet(selected: form.frequency, onClose: { self.sheet = nil }) { frequency in
                    form.frequency = frequency
                    form.pickedFirstDue = nil
                    self.sheet = nil
                }
            }
        }
        .pbAlert(isPresented: $showsDiscard, title: form.editing == nil ? "Discard this loan?" : "Discard changes?",
                 message: "Your changes won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard",
                 testIDPrefix: "lendMoney.discardAlert", onAction: router.dismissModal)
        .alert("Couldn’t save", isPresented: Binding { error != nil } set: { if !$0 { error = nil } }) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(error ?? "")
        }
        .onRouteResult(requests.person) { if case .person(let id) = $0 { form.person = id } }
        .onRouteResult(requests.currency) { result in
            guard case .currency(let code) = result else { return }
            form.currency = code
            form.rate = store.todayRate(for: code)
        }
        .onRouteResult(requests.date) { if case .day(let day?) = $0 { form.date = day } }
        .onRouteResult(requests.firstDue) { if case .day(let day?) = $0 { form.pickedFirstDue = day } }
        .onRouteResult(requests.due) { if case .day(let day) = $0 { form.dueDate = day } }
        .task {
            if form.editing == nil, form.amountText.isEmpty { amountFocused = true }
        }
        .onStartScreen([.lendMoney], perform: applyFigmaPrefill)
        .routeTestRoot("lendMoney")
    }

    private var detailsCard: some View {
        VStack(spacing: 0) {
            PBSettingRow(form.isLent ? "Lent to" : "Borrowed from", value: personName ?? "Choose", icon: .profile, action: pickPerson)
                .accessibilityIdentifier("lendMoney.person")
            PBSettingRow("Reason", value: form.reason.isEmpty ? "Optional" : form.reason, icon: .receipt) { open(.reason) }
                .accessibilityIdentifier("lendMoney.reason")
            PBSettingRow("Date", value: Format.dayWithYear(form.date, today: books.today), icon: .calendar, showsDivider: false, action: openDate)
                .accessibilityIdentifier("lendMoney.date")
        }
        .pbCard(padding: 0)
    }

    private var repaymentCard: some View {
        VStack(spacing: 0) {
            PBSettingRow("Installments", subtitle: "Paid back in parts", icon: .lend,
                         trailing: .toggle($form.hasInstallments.animation(.easeOut(duration: 0.2))))
                .accessibilityIdentifier("lendMoney.installments")
            if form.hasInstallments {
                InstallmentCountRow(count: $form.count)
                PBSettingRow("Repeats", value: form.frequency.title, icon: .repeat) { open(.repeats) }
                    .accessibilityIdentifier("lendMoney.repeats")
                PBSettingRow("First due", value: Format.dayWithYear(form.firstDue, today: books.today), icon: .calendar,
                             showsDivider: false, action: openFirstDue)
                    .accessibilityIdentifier("lendMoney.firstDue")
            } else {
                PBSettingRow("Due", value: form.dueDate.map { Format.dayWithYear($0, today: books.today) } ?? "None",
                             icon: .calendar, showsDivider: false, action: openDue)
                    .accessibilityIdentifier("lendMoney.due")
                PBDueChips(selected: QuickDue.matching(form.dueDate, today: books.today), testIDPrefix: "lendMoney.due",
                           onSelect: { form.dueDate = $0?.day(from: books.today) }, onPickDate: openDue)
            }
        }
        .pbCard(padding: 0)
    }

    private var personName: String? {
        form.person.flatMap { store.ledger.person($0)?.firstName }
    }

    private var rateLine: String? {
        guard let rate = form.rate, form.amount > 0 else { return nil }
        return Money.approximateLine(form.amount, currency: form.currency, rate: rate)
    }

    // MARK: Actions

    private func open(_ local: LoanSheet) {
        amountFocused = false
        sheet = local
    }

    private func pickPerson() {
        amountFocused = false
        router.open(.pickPeople(PeoplePickRequest(id: requests.person, mode: .single, selected: form.person.map { [$0] } ?? [],
                                                  title: form.isLent ? "Lent to" : "Borrowed from", showsYou: false)))
    }

    private func openCurrency() {
        amountFocused = false
        router.open(.pickCurrency(CurrencyPickRequest(id: requests.currency, selected: form.currency)))
    }

    private func openDate() {
        amountFocused = false
        router.open(.pickDate(DatePickRequest(id: requests.date, kind: .date, selected: form.date, latest: books.today)))
    }

    private func openFirstDue() {
        amountFocused = false
        router.open(.pickDate(DatePickRequest(id: requests.firstDue, kind: .dueDate, selected: form.firstDue, earliest: form.date.adding(days: 1))))
    }

    private func openDue() {
        amountFocused = false
        router.open(.pickDate(DatePickRequest(id: requests.due, kind: .dueDate, selected: form.dueDate ?? books.today.adding(days: 1),
                                              allowsNone: true, earliest: books.today)))
    }

    private func close() {
        amountFocused = false
        if form.isDirty { showsDiscard = true } else { router.dismissModal() }
    }

    private func save() {
        guard let draft = form.draft else { return }
        amountFocused = false
        do {
            if let id = form.editing {
                try store.updateLoan(id, with: draft)
                router.dismissModal()
            } else {
                let id = try store.addLoan(draft)
                router.didSave(.loan(id), toast: "Loan added")
            }
            Haptics.success()
        } catch {
            Haptics.warning()
            self.error = error.localizedDescription
        }
    }

    /// The Figma form (§9.3): I lent ₹6,000 to Dev for a laptop repair, 3 monthly installments.
    private func applyFigmaPrefill(_ screen: ScreenID) {
        amountFocused = false
        form.amountText = "6000"
        form.person = "p-dev"
        form.reason = "Laptop repair"
        form.hasInstallments = true
        form.count = 3
        form.frequency = .monthly
    }
}

/// "Number of installments" with the system stepper; the title wraps to two lines as drawn.
private struct InstallmentCountRow: View {
    @Binding var count: Int

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            PBIconView(.split)
                .foregroundStyle(PBColor.iconPrimary)
            Text("Number of installments")
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("\(count)")
                .textStyle(.body)
                .foregroundStyle(PBColor.textSecondary)
            Stepper("Number of installments", value: $count, in: LoanForm.countRange)
                .labelsHidden()
                .accessibilityIdentifier("lendMoney.count")
        }
        .padding(.vertical, PBSpace.s12)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: 56)
        .overlay(alignment: .bottom) { PBDivider().padding(.leading, 52) }
    }
}
