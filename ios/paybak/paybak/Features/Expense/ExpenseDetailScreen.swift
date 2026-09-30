import PhotosUI
import SwiftUI

/// The Expense detail template (screens-activity §4, add-expense §11): hero, your share, the split,
/// the receipt, comments, the edit history and the actions, all from `Books.expenseDetail`. Edit
/// opens Add expense in edit mode; the group chip row opens the group.
struct ExpenseDetailScreen: View {
    let expenseId: ExpenseID
    let toast: String?

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @State private var comment = ""
    @State private var showsDelete = false
    @State private var showsFlag = false
    @State private var showsPhotoPicker = false
    @State private var pickedPhoto: PhotosPickerItem?
    @State private var receiptRequest = RecordID.make()
    @State private var didShowToast = false

    var body: some View {
        Group {
            if let detail = store.books.expenseDetail(expenseId) {
                content(detail)
            } else {
                missing
            }
        }
        .pinnedHeader {
            PBPushHeader("Expense", trailing: canEdit ? .text("Edit", action: edit) : .none, testIDPrefix: "expense", onBack: router.back)
        }
        .background(PBColor.bgPrimary)
        .task {
            // A toast handed over with the route shows once, not again after a push comes back.
            if let toast, !didShowToast { router.toast(toast) }
            didShowToast = true
        }
        .routeTestRoot("expense")
    }

    /// People on the expense can edit it while it isn't deleted.
    private var canEdit: Bool {
        guard let expense = store.ledger.expense(expenseId) else { return false }
        return !expense.isDeleted && expense.participantIds.contains(Person.me)
    }

    private func content(_ detail: ExpenseDetail) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                VStack(alignment: .leading, spacing: PBSpace.s16) {
                    PBAmountHero(
                        leading: .avatar(.icon(detail.expense.category.pbIcon)),
                        title: detail.title,
                        amount: detail.amount,
                        meta: [detail.meta, detail.rateLine].compactMap(\.self).joined(separator: "\n"),
                        chips: [detail.groupName, detail.categoryName].compactMap(\.self),
                        status: detail.flag == nil ? nil : "Disputed"
                    )
                    if let flag = detail.flag {
                        PBNoticeCard(icon: .flag, title: flag.title, message: flag.note,
                                     primary: .init("Edit expense", action: edit),
                                     secondary: .init("Resolve") { resolve() })
                            .accessibilityIdentifier("expense.flagNotice")
                    }
                }
                shareCard(detail)
                split(detail)
                receipt(detail)
                comments(detail)
                history(detail)
                actions(detail)
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s48)
            .phoneContentWidth()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbAlert(
            isPresented: $showsDelete,
            title: "Delete this expense?",
            message: deleteMessage(detail),
            cancelLabel: "Cancel",
            actionLabel: "Delete",
            testIDPrefix: "expense.alert",
            onAction: delete
        )
        .pbSheet(isPresented: $showsFlag) {
            FlagExpenseSheet(onClose: { showsFlag = false }) { note in
                showsFlag = false
                try? store.flagExpense(expenseId, note: note)
            }
        }
        .photosPicker(isPresented: $showsPhotoPicker, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) { await attach(pickedPhoto) }
        .onRouteResult(receiptRequest) { result in
            guard case .receipt(let scanned) = result, var draft = store.ledger.expense(expenseId).map(ExpenseDraft.init) else { return }
            draft.receipt = scanned.draft.receipt ?? scanned.photo.map { Receipt(photo: $0, asset: nil, addedBy: Person.me, addedAt: store.clock.now) }
            try? store.updateExpense(expenseId, with: draft)
        }
    }

    // MARK: Sections

    private struct ShareRow: Identifiable {
        let id: String
        let title: String
        let value: String
        let icon: PBIcon
        /// The row opens this group.
        var groupId: GroupID?
    }

    /// Your share · Due · Your {group} balance, each only when it applies.
    private func shareCard(_ detail: ExpenseDetail) -> some View {
        var rows: [ShareRow] = []
        if let share = detail.yourShare {
            rows.append(ShareRow(id: "share", title: "Your share", value: share, icon: .wallet))
        }
        if let due = detail.due {
            rows.append(ShareRow(id: "due", title: "Due", value: due, icon: .calendar))
        }
        if let balance = detail.groupBalance, let groupId = detail.expense.groupId {
            let value = balance.net == 0 ? Money.format(0, balance.currency) : Money.format(balance.net, balance.currency, sign: .signed)
            rows.append(ShareRow(id: "groupBalance", title: balance.title, value: value, icon: .groups, groupId: groupId))
        }
        return VStack(spacing: 0) {
            ForEach(rows) { row in
                PBSettingRow(row.title, value: row.value, icon: row.icon, trailing: row.groupId == nil ? .none : .chevron,
                             showsDivider: row.id != rows.last?.id,
                             action: row.groupId.map { id in { router.open(.group(id)) } })
                    .accessibilityIdentifier("expense.\(row.id)")
            }
        }
        .pbCard(padding: 0)
    }

    private func split(_ detail: ExpenseDetail) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader(detail.splitHeader)
            VStack(spacing: 0) {
                ForEach(detail.splitLines) { line in
                    PBPersonRow(name: line.name, avatar: avatar(line.personId), subtitle: line.subtitle,
                                tag: store.ledger.person(line.personId)?.isGuest == true ? "Guest" : nil,
                                size: .compact, trailing: .amount(line.value),
                                showsDivider: line.id != detail.splitLines.last?.id,
                                action: line.personId == Person.me ? nil : { router.open(.friend(line.personId)) })
                        .accessibilityIdentifier("expense.split.\(line.personId)")
                }
            }
            .pbCard(padding: 0)
        }
    }

    private func receipt(_ detail: ExpenseDetail) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Receipt")
            if let caption = detail.receiptCaption {
                Button {
                    if let photo = detail.expense.receipt?.photoRef { router.open(.photoViewer(photo)) }
                } label: {
                    HStack(spacing: PBSpace.s12) {
                        ReceiptImage(receipt: detail.expense.receipt)
                            .frame(width: 56, height: 72)
                            .clipShape(.rect(cornerRadius: PBRadius.sm))
                        VStack(alignment: .leading, spacing: PBSpace.s2) {
                            Text("Receipt photo")
                                .textStyle(.headline)
                                .foregroundStyle(PBColor.textPrimary)
                            Text(caption)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textSecondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        PBIconView(.chevronRight, size: PBSize.iconMd)
                            .foregroundStyle(PBColor.iconTertiary)
                    }
                    .padding(PBSpace.s12)
                    .contentShape(.rect)
                }
                .buttonStyle(PBRowButtonStyle(surface: .card))
                .pbCard(padding: 0)
                .accessibilityIdentifier("expense.receipt")
            } else {
                PBSettingRow("Add receipt", icon: .camera, showsDivider: false, action: addReceipt)
                    .pbCard(padding: 0)
                    .accessibilityIdentifier("expense.receipt")
            }
        }
    }

    private func comments(_ detail: ExpenseDetail) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Comments")
            VStack(spacing: PBSpace.s12) {
                if !detail.comments.isEmpty {
                    VStack(spacing: 0) {
                        ForEach(detail.comments) { line in
                            PBCommentRow(name: line.name, avatar: avatar(line.personId), date: line.date, text: line.text)
                        }
                    }
                }
                PBComposer(text: $comment, placeholder: "Add a comment", testIDPrefix: "expense.composer") { text in
                    try? store.addComment(to: expenseId, text: text)
                }
            }
        }
    }

    private func history(_ detail: ExpenseDetail) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("History")
            VStack(spacing: 0) {
                ForEach(detail.history.indices, id: \.self) { index in
                    let line = detail.history[index]
                    PBHistoryRow(text: line.text, date: line.date, isLast: index == detail.history.count - 1)
                }
            }
        }
    }

    @ViewBuilder
    private func actions(_ detail: ExpenseDetail) -> some View {
        if !detail.expense.isDeleted {
            VStack(spacing: 0) {
                if detail.canFlag {
                    PBSettingRow("Flag an issue", icon: .flag) { showsFlag = true }
                        .accessibilityIdentifier("expense.flag")
                }
                PBSettingRow("Delete expense", icon: .delete, trailing: .none, tone: .destructive, showsDivider: false) {
                    showsDelete = true
                }
                .accessibilityIdentifier("expense.delete")
            }
            .pbCard(padding: 0)
        }
    }

    private var missing: some View {
        Text("This expense is no longer here.")
            .textStyle(.body)
            .foregroundStyle(PBColor.textSecondary)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: Actions

    private func avatar(_ person: PersonID) -> PBAvatar.Content {
        person == Person.me ? profileStore.avatarContent : store.ledger.person(person)?.avatarContent ?? .icon(.profile)
    }

    private func edit() {
        router.open(.addExpense(AddExpenseArgs(editing: expenseId, focusAmount: false)))
    }

    private func resolve() {
        try? store.resolveFlag(expenseId)
        Haptics.success()
    }

    /// "{title} moves to Recently deleted for 30 days. {group} balances update for everyone."
    private func deleteMessage(_ detail: ExpenseDetail) -> String {
        let who = detail.groupName.map { "\($0) balances update for everyone." } ?? "Balances update for everyone on it."
        return "\(detail.title) moves to Recently deleted for 30 days. \(who)"
    }

    private func delete() {
        do {
            try store.deleteExpense(expenseId)
            router.back()
            router.toast("Expense deleted")
        } catch {
            Haptics.warning()
        }
    }

    /// Free: attach a photo; Pro: read a receipt (add-expense §3.12).
    private func addReceipt() {
        if store.isPro {
            let people = store.ledger.expense(expenseId)?.participantIds ?? []
            router.open(.scanReceipt(ScanRequest(id: receiptRequest, people: people)))
        } else {
            showsPhotoPicker = true
        }
    }

    private func attach(_ item: PhotosPickerItem?) async {
        guard let item, let data = try? await item.loadTransferable(type: Data.self), let name = PhotoFiles.save(data),
              var draft = store.ledger.expense(expenseId).map(ExpenseDraft.init) else { return }
        draft.receipt = Receipt(photo: name, asset: nil, addedBy: Person.me, addedAt: store.clock.now)
        try? store.updateExpense(expenseId, with: draft)
        pickedPhoto = nil
    }
}

/// Flag an issue (screens-activity §4.3-G, proposal): what looks wrong, then "Flag expense".
private struct FlagExpenseSheet: View {
    let onClose: () -> Void
    let onFlag: (String) -> Void

    @State private var note = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        PBSheet(title: "Flag an issue", testIDPrefix: "flag", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBTextArea(nil, text: $note, prompt: "What looks wrong?", focus: $isFocused)
                PBButton("Flag expense", fillsWidth: true) { onFlag(note.trimmingCharacters(in: .whitespacesAndNewlines)) }
                    .disabled(note.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    .accessibilityIdentifier("flag.send")
            }
        }
        .task { isFocused = true }
    }
}
