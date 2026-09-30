import SwiftUI

/// The form's #F5F5F5 card (add-expense §4.5): Category, Paid by, Split, Group, Due with its quick
/// chips, Repeat (Pro), Add receipt and Notes. Values come from the form; taps go to `actions`.
struct ExpenseFormCard: View {
    struct Actions {
        let category: () -> Void
        let paidBy: () -> Void
        let split: () -> Void
        let group: () -> Void
        let due: () -> Void
        let quickDue: (QuickDue?) -> Void
        let repeatRule: () -> Void
        let receipt: () -> Void
        let notes: () -> Void
        let today: LocalDay
    }

    let form: ExpenseForm
    let split: SplitPreview
    let isPro: Bool
    let actions: Actions

    @Environment(LedgerStore.self) private var store

    var body: some View {
        VStack(spacing: 0) {
            PBSettingRow("Category", value: form.category?.name ?? "Choose", icon: form.category?.pbIcon ?? .tag, action: actions.category)
                .accessibilityIdentifier("addExpense.row.category")
            PBSettingRow("Paid by", value: paidByValue, icon: .wallet, action: actions.paidBy)
                .accessibilityIdentifier("addExpense.row.paidBy")
            PBSettingRow("Split", value: split.formValue.text, icon: .split, action: actions.split)
                .accessibilityIdentifier("addExpense.row.split")
            PBSettingRow("Group", value: form.groupId.flatMap { store.ledger.group($0)?.name } ?? "No group", icon: .groups, action: actions.group)
                .accessibilityIdentifier("addExpense.row.group")
            PBSettingRow("Due", value: form.dueDate.map { Format.dayWithYear($0, today: actions.today) } ?? "None",
                         icon: .calendar, showsDivider: false, action: actions.due)
                .accessibilityIdentifier("addExpense.row.due")
            PBDueChips(selected: QuickDue.matching(form.dueDate, today: actions.today), testIDPrefix: "addExpense.due",
                       onSelect: actions.quickDue, onPickDate: actions.due)
            PBDivider().padding(.leading, 52)
            PBSettingRow("Repeat", value: repeatValue, icon: .repeat, badge: isPro ? nil : "Pro", action: actions.repeatRule)
                .accessibilityIdentifier("addExpense.row.repeat")
            receiptRow
                .accessibilityIdentifier("addExpense.row.receipt")
            PBSettingRow("Notes", value: notesValue, icon: .note, showsDivider: false, action: actions.notes)
                .accessibilityIdentifier("addExpense.row.notes")
        }
        .pbCard(padding: 0)
    }

    private var paidByValue: String {
        let ids = form.payerIds
        return ids.count == 1 ? store.books.firstName(ids[0]) : "\(ids.count) people"
    }

    private var repeatValue: String {
        switch form.repeatRule?.frequency {
        case .weekly: "Weekly"
        case .monthly: "Monthly"
        case .yearly: "Yearly"
        case nil: "Never"
        }
    }

    private var notesValue: String {
        let firstLine = form.notes.split(whereSeparator: \.isNewline).first.map(String.init) ?? ""
        return firstLine.isEmpty ? "Optional" : firstLine
    }

    /// "Add receipt", or "Receipt" · thumbnail · "Attached" once a photo is on the expense.
    @ViewBuilder
    private var receiptRow: some View {
        if form.receipt != nil {
            Button(action: actions.receipt) {
                HStack(spacing: PBSpace.s12) {
                    PBIconView(.camera)
                        .foregroundStyle(PBColor.iconPrimary)
                    Text("Receipt")
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    ReceiptImage(receipt: form.receipt)
                        .frame(width: 24.9, height: 32)
                        .clipShape(.rect(cornerRadius: 4.4))
                    Text("Attached")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textSecondary)
                    PBIconView(.chevronRight, size: PBSize.iconMd)
                        .foregroundStyle(PBColor.iconTertiary)
                }
                .padding(.vertical, PBSpace.s12)
                .padding(.horizontal, PBSpace.s16)
                .frame(minHeight: 56)
                .overlay(alignment: .bottom) { PBDivider().padding(.leading, 52) }
                .contentShape(.rect)
            }
            .buttonStyle(PBRowButtonStyle(surface: .card))
            .accessibilityElement(children: .combine)
        } else {
            PBSettingRow("Add receipt", icon: .camera, action: actions.receipt)
        }
    }
}
