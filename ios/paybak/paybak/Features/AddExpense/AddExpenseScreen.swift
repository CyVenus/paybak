import PhotosUI
import SwiftUI

/// Add expense (add-expense §3–§4): the amount-first full-screen form, new, prefilled or editing.
/// People, currency, dates and the group come back from the picker routes; Paid by, Category and
/// Notes are local sheets; the split editor and the payer editor push inside the modal. Save lands
/// on the expense with "Expense added" (edits just close).
struct AddExpenseScreen: View {
    let args: AddExpenseArgs

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @State private var form: ExpenseForm?

    var body: some View {
        Group {
            if let form {
                ExpenseFormView(form: form, focusAmount: args.focusAmount && args.editing == nil)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear {
            if form == nil { form = makeForm() }
        }
    }

    private func makeForm() -> ExpenseForm {
        let books = store.books
        if let id = args.editing, let expense = store.ledger.expense(id) {
            return ExpenseForm(draft: ExpenseDraft(expense), editing: id, categoryChosen: true)
        }
        var draft = args.draft ?? ExpenseDraft(currency: books.defaultCurrency, date: books.today)
        if draft.rows.isEmpty {
            draft.rows = [SplitRow(personId: Person.me)]
        }
        if draft.currency != books.defaultCurrency, draft.rate == nil {
            draft.rate = store.todayRate(for: draft.currency)
        }
        // A drafted amount comes with its category, even Other.
        return ExpenseForm(draft: draft, categoryChosen: args.draft.map { $0.amount > 0 || $0.category != .other } ?? false)
    }
}

/// The ids the form's picker routes answer to (§2.7 result channel).
private struct FormRequests {
    var people = RecordID.make()
    var currency = RecordID.make()
    var date = RecordID.make()
    var due = RecordID.make()
    var group = RecordID.make()
    var repeatRule = RecordID.make()
    var receipt = RecordID.make()
}

private enum LocalSheet: String, Identifiable {
    case paidBy
    case category
    case notes

    var id: String { rawValue }
}

private struct ExpenseFormView: View {
    @Bindable var form: ExpenseForm
    let focusAmount: Bool

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @State private var requests = FormRequests()
    @State private var sheet: LocalSheet?
    @State private var showsSplitEditor = false
    /// A field the split editor opens with, mid-edit (the Exact error start screen).
    @State private var splitEditorEdit: SplitEditorPage.Edit?
    @State private var showsPayerEditor = false
    @State private var showsDiscard = false
    @State private var showsPhotoPicker = false
    @State private var pickedPhoto: PhotosPickerItem?
    @FocusState private var amountFocused: Bool
    @FocusState private var titleFocused: Bool

    private var books: Books { store.books }
    private var split: SplitPreview { books.previewSplit(form.draft) }

    var body: some View {
        ScrollView {
            VStack(spacing: PBSpace.s16) {
                PBAmountField(
                    text: $form.amountText,
                    currency: Currency(code: form.currency),
                    date: Format.dateChip(form.date, today: books.today),
                    helper: rateLine ?? receiptLine,
                    testIDPrefix: "addExpense",
                    focus: $amountFocused,
                    onCurrencyTap: openCurrency,
                    onDateTap: openDate
                )
                ExpensePeopleStrip(people: form.others, includesYou: form.people.contains(Person.me), onEdit: openPeople)
                PBTextField(nil, text: $form.title, prompt: "What was it for?", focus: $titleFocused)
                    .submitLabel(.done)
                    .onChange(of: form.title) { if form.title.count > 60 { form.title = String(form.title.prefix(60)) } }
                    .accessibilityIdentifier("addExpense.title")
                ExpenseFormCard(form: form, split: split, isPro: store.isPro, actions: cardActions)
            }
            .padding(.bottom, PBSpace.s32)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBModalHeader(
                form.isEditing ? "Edit expense" : "Add expense",
                actionLabel: "Save",
                isActionEnabled: form.canSave(split: split),
                testIDPrefix: "addExpense",
                onClose: close,
                onAction: save
            )
        }
        .navigationDestination(isPresented: $showsSplitEditor) {
            SplitEditorPage(form: form, initialEdit: splitEditorEdit)
        }
        .navigationDestination(isPresented: $showsPayerEditor) {
            PayerEditorPage(form: form)
        }
        .pbItemSheet(item: $sheet, detent: { $0 == .category ? .large : .fitted }) { sheet in
            switch sheet {
            case .paidBy:
                PaidBySheet(form: form, onClose: { self.sheet = nil }) {
                    self.sheet = nil
                    showsPayerEditor = true
                }
            case .category:
                CategorySheet(selected: form.category, onClose: { self.sheet = nil }) { category in
                    form.category = category
                    self.sheet = nil
                }
            case .notes:
                NotesSheet(text: form.notes, onClose: { self.sheet = nil }) { notes in
                    form.notes = notes
                    self.sheet = nil
                }
            }
        }
        .pbAlert(
            isPresented: $showsDiscard,
            title: form.isEditing ? "Discard changes?" : "Discard this expense?",
            message: "Your changes won’t be saved.",
            cancelLabel: "Keep editing",
            actionLabel: "Discard",
            testIDPrefix: "addExpense.discardAlert",
            onAction: router.dismissModal
        )
        .photosPicker(isPresented: $showsPhotoPicker, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) { await attach(pickedPhoto) }
        .onRouteResult(requests.people) { result in
            if case .people(let ids) = result { form.setPeople(ids) }
        }
        .onRouteResult(requests.currency) { result in
            if case .currency(let code) = result {
                form.setCurrency(code, rate: store.todayRate(for: code), group: form.groupId.flatMap { store.ledger.group($0) })
            }
        }
        .onRouteResult(requests.date) { result in
            if case .day(let day?) = result { form.date = day }
        }
        .onRouteResult(requests.due) { result in
            if case .day(let day) = result { form.dueDate = day }
        }
        .onRouteResult(requests.group) { result in
            if case .group(let id) = result {
                form.setGroup(id.flatMap { store.ledger.group($0) }, rate: store.todayRate(for:))
            }
        }
        .onRouteResult(requests.repeatRule) { result in
            if case .repeatRule(let rule) = result { form.repeatRule = rule }
        }
        .onRouteResult(requests.receipt) { result in
            if case .receipt(let receipt) = result { form.apply(receipt) }
        }
        .task {
            if focusAmount { amountFocused = true }
        }
        .onStartScreen(Self.startScreens, perform: applyStartScreen)
        .routeTestRoot("addExpense")
    }

    /// "From receipt · 6 items" on an itemized expense (insights §4.5), under the rate line if any.
    private var receiptLine: String? {
        guard form.splitMode == .itemized, let count = form.itemized?.items.count else { return nil }
        return "From receipt · \(count) item\(count == 1 ? "" : "s")"
    }

    private var rateLine: String? {
        guard let rate = form.rate, form.amount > 0 else { return nil }
        return Money.approximateLine(form.amount, currency: form.currency, rate: rate)
    }

    private var cardActions: ExpenseFormCard.Actions {
        ExpenseFormCard.Actions(
            category: { open(.category) },
            paidBy: { open(.paidBy) },
            split: openSplit,
            group: openGroup,
            due: openDue,
            quickDue: { chip in
                Haptics.selection()
                form.dueDate = chip?.day(from: books.today)
            },
            repeatRule: openRepeat,
            receipt: openReceipt,
            notes: { open(.notes) },
            today: books.today
        )
    }

    // MARK: Navigation

    private func open(_ local: LocalSheet) {
        dismissKeyboard()
        sheet = local
    }

    private func openPeople() {
        dismissKeyboard()
        router.open(.pickPeople(PeoplePickRequest(id: requests.people, selected: form.people)))
    }

    private func openCurrency() {
        dismissKeyboard()
        router.open(.pickCurrency(CurrencyPickRequest(id: requests.currency, selected: form.currency)))
    }

    private func openDate() {
        dismissKeyboard()
        router.open(.pickDate(DatePickRequest(id: requests.date, kind: .date, selected: form.date, latest: books.today)))
    }

    /// The sheet opens on the due date, or tomorrow, and starts from tomorrow.
    private func openDue() {
        dismissKeyboard()
        router.open(.pickDate(DatePickRequest(id: requests.due, kind: .dueDate, selected: form.dueDate, allowsNone: true)))
    }

    /// The split editor; a scanned receipt's split opens in Exact with the amounts it came to.
    private func openSplit() {
        dismissKeyboard()
        showsSplitEditor = true
    }

    private func openGroup() {
        dismissKeyboard()
        router.open(.pickGroup(GroupPickRequest(id: requests.group, selected: form.groupId)))
    }

    private func openRepeat() {
        dismissKeyboard()
        router.requirePro(.repeatRule(RepeatRuleRequest(id: requests.repeatRule, current: form.repeatRule, startDate: form.date)))
    }

    /// An attached photo opens full screen; Pro reads a new receipt; free attaches a photo (§3.12).
    private func openReceipt() {
        dismissKeyboard()
        if let photo = form.receipt?.photoRef {
            router.open(.photoViewer(photo))
        } else if store.isPro {
            router.open(.scanReceipt(ScanRequest(id: requests.receipt, people: form.people)))
        } else {
            showsPhotoPicker = true
        }
    }

    private func attach(_ item: PhotosPickerItem?) async {
        guard let item, let data = try? await item.loadTransferable(type: Data.self),
              let name = PhotoFiles.save(data) else { return }
        form.receipt = Receipt(photo: name, asset: nil, addedBy: Person.me, addedAt: store.clock.now)
        pickedPhoto = nil
    }

    private func dismissKeyboard() {
        amountFocused = false
        titleFocused = false
    }

    private func close() {
        dismissKeyboard()
        if form.isDirty {
            showsDiscard = true
        } else {
            router.dismissModal()
        }
    }

    private func save() {
        dismissKeyboard()
        do {
            if let id = form.editing {
                try store.updateExpense(id, with: form.draft)
                Haptics.success()
                router.dismissModal()
            } else {
                let id = try store.addExpense(form.draft)
                Haptics.success()
                router.didSave(.expense(id), toast: "Expense added")
            }
        } catch {
            Haptics.warning()
            router.toast(error.localizedDescription)
        }
    }

    // MARK: Debug start screens (add-expense §13)

    static let startScreens: Set<ScreenID> = [
        .addExpenseFilled, .addExpenseSplitWith, .addExpensePaidBy, .addExpensePayers, .addExpenseSplitEqually,
        .addExpenseSplitExactError, .addExpenseCategory, .addExpenseCurrency, .addExpenseDueDate, .addExpenseDate,
        .addExpenseDiscard,
    ]

    /// The Olive Garden draft (§12), then the state the id names.
    private func applyStartScreen(_ screen: ScreenID) {
        form.amountText = "2800"
        form.setPeople([Person.me, "p-priya", "p-esha", "p-dev"])
        form.title = "Dinner at Olive Garden"
        form.category = .food
        form.dueDate = QuickDue.weekend.day(from: books.today)
        adoptOpenRequests()
        switch screen {
        case .addExpensePaidBy: sheet = .paidBy
        case .addExpenseCategory: sheet = .category
        case .addExpenseDiscard: showsDiscard = true
        case .addExpenseSplitEqually: showsSplitEditor = true
        case .addExpensePayers: showsPayerEditor = true
        case .addExpenseSplitExactError:
            form.splitMode = .exact
            form.splitValues = [Person.me: 70_000, "p-priya": 70_000, "p-esha": 70_000, "p-dev": 70_000]
            splitEditorEdit = SplitEditorPage.Edit(personId: "p-dev", text: "550")
            showsSplitEditor = true
        default: break
        }
    }

    /// Answers the picker a start screen already opened over the form.
    private func adoptOpenRequests() {
        guard let layer = router.modals.last else { return }
        for route in layer.path + [layer.sheet].compactMap(\.self) {
            switch route {
            case .pickPeople(let request): requests.people = request.id
            case .pickCurrency(let request): requests.currency = request.id
            case .pickDate(let request): if request.kind == .date { requests.date = request.id } else { requests.due = request.id }
            default: break
            }
        }
    }
}
