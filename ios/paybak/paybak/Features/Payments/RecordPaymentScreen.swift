import PhotosUI
import SwiftUI
import UIKit

/// Record payment (record-lend-group §2, settle §4): log a payment made outside Paybak. Prefilled
/// from the ＋ sheet (your most recent debt), Settle up, a friend page or a loan repayment; also the
/// edit form of a payment. Save stores it (pending until the receiver confirms) and lands on the
/// payment with "Payment recorded".
struct RecordPaymentScreen: View {
    let args: RecordPaymentArgs

    @Environment(LedgerStore.self) private var store
    @State private var form: PaymentForm?

    var body: some View {
        Group {
            if let form {
                PaymentFormView(form: form)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear {
            if form == nil { form = makeForm() }
        }
    }

    private func makeForm() -> PaymentForm {
        let books = store.books
        if let id = args.editing, let payment = store.ledger.payment(id) {
            let context: PaymentFor = payment.groupId.map { .group($0) } ?? payment.loanId.map { .loan($0) } ?? .direct(expense: payment.expenseId)
            return PaymentForm(editing: id, from: payment.fromId, to: payment.toId, amount: payment.amount, currency: payment.currency,
                               rate: payment.rate, method: payment.method, context: context, date: payment.date, proof: payment.proof)
        }
        var from = args.from
        var to = args.to
        var amount = args.amount
        var context = args.context.map { PaymentFor($0) }
        if from == nil, to == nil, let debt = books.suggestedPayment() {
            from = Person.me
            to = debt.creditor
            amount = debt.amount
            context = PaymentFor(debt)
        }
        let friend = from == Person.me ? to : from
        let resolved = context ?? friend.flatMap { books.paymentContexts(with: $0).first } ?? .direct(expense: nil)
        if amount == nil, let friend {
            let balance = books.openBalance(with: friend, for: resolved)
            amount = balance == 0 ? nil : abs(balance)
        }
        let currency = args.currency ?? books.defaultCurrency
        return PaymentForm(from: from ?? Person.me, to: to, amount: amount, currency: currency, rate: store.todayRate(for: currency),
                           method: args.method ?? .cash, context: resolved, date: books.today)
    }
}

private struct PaymentFormView: View {
    @Bindable var form: PaymentForm

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @State private var requests = (from: RecordID.make(), to: RecordID.make(), currency: RecordID.make(), date: RecordID.make())
    @State private var showsFor = false
    @State private var showsDiscard = false
    @State private var showsPhotoPicker = false
    @State private var pickedPhoto: PhotosPickerItem?
    @State private var error: String?
    @FocusState private var amountFocused: Bool

    private var books: Books { store.books }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                VStack(spacing: PBSpace.s20) {
                    PBPaymentParties(from: party(form.from), to: party(form.to), testIDPrefix: "recordPayment",
                                     onFromTap: { pick(.from) }, onToTap: { pick(.to) })
                    PBAmountField(text: $form.amountText, currency: Currency(code: form.currency), helper: helper,
                                  testIDPrefix: "recordPayment", focus: $amountFocused, onCurrencyTap: openCurrency)
                        .onChange(of: form.amountText) { if amountFocused { form.amountEdited = true } }
                }
                VStack(alignment: .leading, spacing: PBSpace.s16) {
                    methodPicker
                    if form.method == .upi, let upi = payeeUPI {
                        UPIPayeeCard(name: payeeName, avatar: party(form.to).avatar, upi: upi) {
                            UIPasteboard.general.string = upi
                            router.toast("UPI ID copied")
                        }
                        .transition(.opacity)
                    }
                }
                .animation(.easeOut(duration: 0.2), value: form.method)
                VStack(alignment: .leading, spacing: PBSpace.s12) {
                    VStack(spacing: 0) {
                        PBSettingRow("For", value: books.paymentForName(form.context) ?? "No group", icon: .groups) {
                            amountFocused = false
                            showsFor = true
                        }
                        .accessibilityIdentifier("recordPayment.for")
                        PBSettingRow("Date", value: Format.dayWithYear(form.date, today: books.today), icon: .calendar, action: openDate)
                            .accessibilityIdentifier("recordPayment.date")
                        PBSettingRow("Proof", value: form.proof == nil ? "Add photo (optional)" : "1 photo", icon: .camera,
                                     showsDivider: false, action: openProof)
                            .accessibilityIdentifier("recordPayment.proof")
                    }
                    .pbCard(padding: 0)
                    if let summary {
                        Text(summary)
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                            .accessibilityIdentifier("recordPayment.summary")
                    }
                }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s32)
            .phoneContentWidth()
        }
        .scrollDismissesKeyboard(.interactively)
        .pinnedHeader {
            PBModalHeader(form.editing == nil ? "Record payment" : "Edit payment", actionLabel: "Save", isActionEnabled: form.canSave,
                          testIDPrefix: "recordPayment", onClose: close, onAction: save)
        }
        .background(PBColor.bgPrimary)
        .pbSheet(isPresented: $showsFor) {
            PaymentForSheet(contexts: form.friend.map { books.paymentContexts(with: $0) } ?? [.direct(expense: nil)],
                            selected: form.context, onClose: { showsFor = false }) { context in
                showsFor = false
                setContext(context)
            }
        }
        .pbAlert(isPresented: $showsDiscard, title: form.editing == nil ? "Discard this payment?" : "Discard changes?",
                 message: "Your changes won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard",
                 testIDPrefix: "recordPayment.discardAlert", onAction: router.dismissModal)
        .alert("Couldn’t save", isPresented: Binding { error != nil } set: { if !$0 { error = nil } }) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(error ?? "")
        }
        .photosPicker(isPresented: $showsPhotoPicker, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) {
            guard let item = pickedPhoto, let data = try? await item.loadTransferable(type: Data.self) else { return }
            form.proof = PhotoFiles.save(data) ?? form.proof
            pickedPhoto = nil
        }
        .onRouteResult(requests.from) { if case .person(let id) = $0 { form.setFrom(id); refreshAmount() } }
        .onRouteResult(requests.to) { if case .person(let id) = $0 { form.setTo(id); refreshAmount() } }
        .onRouteResult(requests.currency) { result in
            guard case .currency(let code) = result else { return }
            form.currency = code
            form.rate = store.todayRate(for: code)
        }
        .onRouteResult(requests.date) { if case .day(let day?) = $0 { form.date = day } }
        .task {
            if form.amountText.isEmpty, form.editing == nil { amountFocused = true }
        }
        .routeTestRoot("recordPayment")
    }

    // MARK: Pieces

    private var methodPicker: some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            Text("Method")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
            HStack(spacing: PBSpace.s6) {
                ForEach(PaymentMethodKind.allCases, id: \.self) { method in
                    PBCategoryChip(method.label, isSelected: form.method == method) {
                        Haptics.selection()
                        form.method = method
                    }
                    .accessibilityIdentifier("recordPayment.method.\(method.rawValue)")
                }
            }
        }
    }

    private func party(_ id: PersonID?) -> PBPaymentParties.Party {
        guard let id else { return .init(name: "Choose", avatar: .icon(.profile)) }
        if id == Person.me { return .init(name: "You", avatar: profileStore.avatarContent) }
        let person = store.ledger.person(id)
        return .init(name: person?.firstName ?? "Someone", avatar: person?.avatarContent ?? .icon(.profile))
    }

    /// The receiver's UPI ID (yours when someone paid you).
    private var payeeUPI: String? {
        guard let to = form.to else { return nil }
        let upi = to == Person.me ? profileStore.profile.upiID : store.ledger.person(to)?.upi
        return upi?.isEmpty == false ? upi : nil
    }

    private var payeeName: String {
        guard let to = form.to else { return "" }
        return to == Person.me ? profileStore.profile.name : store.ledger.person(to)?.name ?? ""
    }

    /// "You owe Meera ₹450 in Flat 302"; for another currency, the amount at today's rate.
    private var helper: String? {
        if let rate = form.rate, form.amount > 0 {
            return Money.approximateLine(form.amount, currency: form.currency, rate: rate)
        }
        return form.friend.flatMap { books.openBalanceHelper(with: $0, for: form.context) }
    }

    private var summary: String? {
        guard let from = form.from, let to = form.to, from != to, form.amount > 0 else { return nil }
        return books.paymentSummary(from: from, to: to, amount: form.amount, currency: form.currency, method: form.method, context: form.context)
    }

    // MARK: Actions

    private enum Side { case from, to }

    private func pick(_ side: Side) {
        amountFocused = false
        let current = side == .from ? form.from : form.to
        router.open(.pickPeople(PeoplePickRequest(id: side == .from ? requests.from : requests.to, mode: .single,
                                                  selected: current.map { [$0] } ?? [], title: side == .from ? "From" : "To")))
    }

    private func openCurrency() {
        amountFocused = false
        router.open(.pickCurrency(CurrencyPickRequest(id: requests.currency, selected: form.currency)))
    }

    private func openDate() {
        amountFocused = false
        router.open(.pickDate(DatePickRequest(id: requests.date, kind: .date, selected: form.date, latest: books.today)))
    }

    private func openProof() {
        amountFocused = false
        if let proof = form.proof {
            router.open(.photoViewer(.file(proof)))
        } else {
            showsPhotoPicker = true
        }
    }

    /// A new person or context: file it where you have something open, and prefill that balance
    /// unless the amount was typed.
    private func refreshAmount() {
        guard let friend = form.friend else { return }
        let contexts = books.paymentContexts(with: friend)
        if !contexts.contains(form.context) {
            form.context = contexts.first { books.openBalance(with: friend, for: $0) != 0 } ?? contexts.last ?? .direct(expense: nil)
        }
        setContext(form.context)
    }

    private func setContext(_ context: PaymentFor) {
        form.context = context
        guard !form.amountEdited, let friend = form.friend else { return }
        let balance = books.openBalance(with: friend, for: context)
        if balance != 0 { form.amountText = MoneyInput.text(abs(balance), currency: form.currency) }
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
                try store.updatePayment(id, with: draft)
                router.dismissModal()
            } else {
                let id = try store.recordPayment(draft)
                router.didSave(.payment(id), toast: "Payment recorded")
            }
            Haptics.success()
        } catch {
            Haptics.warning()
            self.error = error.localizedDescription
        }
    }
}

extension PaymentFor {
    /// A route's payment context.
    init(_ context: PaymentContext) {
        switch context {
        case .group(let id): self = .group(id)
        case .expense(let id): self = .direct(expense: id)
        case .loan(let id): self = .loan(id)
        }
    }

    /// Where a debt is settled.
    init(_ debt: Obligation) {
        switch debt.kind {
        case .group, .project: self = .group(debt.ref)
        case .direct: self = .direct(expense: debt.ref)
        case .loan: self = .loan(debt.ref)
        }
    }
}
