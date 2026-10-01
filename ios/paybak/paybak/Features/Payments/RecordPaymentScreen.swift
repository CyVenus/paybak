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

    /// Edit: the payment as recorded. New: prefilled by the route (a Settle up row, a friend page, a
    /// loan's repayment), else from the debt you owe most recently (record-lend-group §2.4); then the
    /// currency and open amount follow what it's for.
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
        var amountEdited = args.amount != nil
        if from == nil, to == nil, let debt = books.suggestedPayment() {
            let debtContext = PaymentFor(debt)
            let owed = -books.contextBalance(with: debt.creditor, for: debtContext)
            from = Person.me
            to = debt.creditor
            amount = owed > 0 ? owed : debt.amount
            context = debtContext
            amountEdited = true
        }
        // One side is always you: you paid, unless the route says the money came to you.
        let youPaid = from.map { $0 == Person.me } ?? (to != Person.me)
        let other = youPaid ? to : from
        let friend = other == Person.me ? nil : other
        let currency = args.currency ?? books.defaultCurrency
        let form = PaymentForm(from: youPaid ? Person.me : friend, to: youPaid ? friend : Person.me, amount: amount, currency: currency,
                               rate: store.todayRate(for: currency), method: args.method ?? .cash,
                               context: context ?? .direct(expense: nil), date: books.today)
        form.amountEdited = amountEdited
        // An amount the route gives in its own currency stays in that currency.
        if args.amount == nil || args.currency == nil { form.refill(in: store) }
        form.markUnchanged()
        return form
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
                methodPicker
                if form.method == .upi, let upi = payeeUPI {
                    UPIPayeeCard(name: payeeName, avatar: party(form.to).avatar, upi: upi) {
                        UIPasteboard.general.string = upi
                        router.toast("UPI ID copied")
                    }
                    .transition(.opacity)
                }
                VStack(alignment: .leading, spacing: PBSpace.s12) {
                    VStack(spacing: 0) {
                        PBSettingRow("For", value: books.paymentForLabel(form.context) ?? "None", icon: .groups) {
                            amountFocused = false
                            showsFor = true
                        }
                        .accessibilityIdentifier("recordPayment.for")
                        PBSettingRow("Date", value: Format.day(form.date), icon: .calendar, action: openDate)
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
            .animation(.easeOut(duration: 0.2), value: form.method)
            .padding(.bottom, PBSpace.s24)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBModalHeader(form.editing == nil ? "Record payment" : "Edit payment", actionLabel: "Save", isActionEnabled: form.canSave,
                          testIDPrefix: "recordPayment", onClose: close, onAction: save)
        }
        .pbSheet(isPresented: $showsFor) {
            PaymentForSheet(friend: form.friend, selected: form.context, onClose: { showsFor = false }) { context in
                showsFor = false
                setContext(context)
            }
        }
        .pbAlert(isPresented: $showsDiscard, title: form.editing == nil ? "Discard this payment?" : "Discard changes?",
                 message: "Your changes won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard",
                 testIDPrefix: "recordPayment.discardAlert", onAction: router.dismissModal)
        .photosPicker(isPresented: $showsPhotoPicker, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) {
            guard let item = pickedPhoto, let data = try? await item.loadTransferable(type: Data.self) else { return }
            form.proof = PhotoFiles.save(data) ?? form.proof
            pickedPhoto = nil
        }
        .onRouteResult(requests.from) { if case .person(let id) = $0 { picked(id, tappedFrom: true) } }
        .onRouteResult(requests.to) { if case .person(let id) = $0 { picked(id, tappedFrom: false) } }
        .onRouteResult(requests.currency) { result in
            guard case .currency(let code) = result else { return }
            form.currency = code
            form.rate = store.todayRate(for: code)
            // A currency of your own leaves the group or loan, and keeps the amount as typed.
            if case .direct = form.context {} else { form.context = .direct(expense: nil) }
            form.amountEdited = true
        }
        .onRouteResult(requests.date) { if case .day(let day?) = $0 { form.date = day } }
        .task {
            if form.friend == nil, form.editing == nil { amountFocused = true }
        }
        .routeTestRoot("recordPayment")
    }

    // MARK: Pieces

    /// Cash · UPI · Bank · Card · Other, single choice; the row scrolls sideways when it doesn't fit.
    private var methodPicker: some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            Text("Method")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
            ScrollView(.horizontal, showsIndicators: false) {
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
            .scrollBounceBehavior(.basedOnSize, axes: .horizontal)
        }
    }

    private func party(_ id: PersonID?) -> PBPaymentParties.Party {
        guard let id else { return .init(name: "Choose", avatar: .icon(.userAdd)) }
        if id == Person.me { return .init(name: "You", avatar: profileStore.avatarContent) }
        let person = store.ledger.person(id)
        return .init(name: person?.firstName ?? "Someone", avatar: person?.avatarContent ?? .icon(.profile))
    }

    /// The receiver's UPI ID: the friend's when you pay them, yours when they paid you.
    private var payeeUPI: String? {
        guard let to = form.to else { return nil }
        let upi = to == Person.me ? profileStore.profile.upiID : store.ledger.person(to)?.upi
        return upi?.isEmpty == false ? upi : nil
    }

    private var payeeName: String {
        guard let to = form.to else { return "" }
        return to == Person.me ? profileStore.profile.name : store.ledger.person(to)?.name ?? ""
    }

    /// In another currency than what it's for, the amount at today's rate; otherwise what's open
    /// there: "You owe Meera ₹450 in Flat 302", "Dev owes you ₹6,000 for Laptop repair".
    private var helper: String? {
        if form.currency != PaymentForm.currency(of: form.context, in: books), form.amount > 0 {
            return form.rate.map { Money.approximateLine(form.amount, currency: form.currency, rate: $0) }
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
        router.open(.pickPeople(PeoplePickRequest(id: side == .from ? requests.from : requests.to, mode: .single,
                                                  title: "Choose someone")))
    }

    /// Someone picked for From or To: the friend and the direction, then the open amount between you.
    private func picked(_ person: PersonID, tappedFrom: Bool) {
        form.pick(person, tappedFrom: tappedFrom)
        form.refill(in: store)
    }

    private func openCurrency() {
        amountFocused = false
        router.open(.pickCurrency(CurrencyPickRequest(id: requests.currency, selected: form.currency)))
    }

    private func openDate() {
        amountFocused = false
        router.open(.pickDate(DatePickRequest(id: requests.date, kind: .date, selected: form.date)))
    }

    private func openProof() {
        amountFocused = false
        if let proof = form.proof {
            router.open(.photoViewer(.file(proof)))
        } else {
            showsPhotoPicker = true
        }
    }

    /// A group, a loan or nothing (directly between you): its currency, and its open amount unless
    /// the amount was typed.
    private func setContext(_ context: PaymentFor) {
        form.context = context
        form.refill(in: store)
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
            router.toast(error.localizedDescription)
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
