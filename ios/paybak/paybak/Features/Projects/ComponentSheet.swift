import PhotosUI
import SwiftUI

/// Add component (screens-projects §5) and its edit twin (§1.5): name, estimated and actual cost,
/// Planned / Bought / Done, who paid, a receipt photo. New parts start Planned with you as the payer;
/// the button enables once there's a name (and, when bought or done, an actual cost). Typing an
/// actual cost marks a planned part bought. Closing with typed changes asks first.
struct ComponentSheet: View {
    /// Which sheet is open on the dashboard.
    enum Mode: Hashable {
        case add
        case edit(ComponentID)
    }

    let projectId: GroupID
    /// The part being edited; nil adds a new one.
    let editing: ProjectComponent?
    let onClose: () -> Void

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var form: ComponentForm
    @State private var isPayerPickerShown = false
    @State private var isPhotoPickerShown = false
    @State private var pickedPhoto: PhotosPickerItem?
    @State private var isDiscardShown = false
    @State private var isDeleteShown = false
    private let original: ComponentForm

    init(projectId: GroupID, editing: ProjectComponent?, currency: String, onClose: @escaping () -> Void) {
        self.projectId = projectId
        self.editing = editing
        self.onClose = onClose
        let start = editing.map { ComponentForm($0, currency: currency) } ?? ComponentForm()
        original = start
        _form = State(initialValue: start)
    }

    private var currency: String { ledgerStore.ledger.group(projectId)?.currency ?? ledgerStore.books.defaultCurrency }
    private var isDirty: Bool { form != original }

    var body: some View {
        PBSheet(title: editing == nil ? "Add component" : "Edit component", testIDPrefix: "addComponent", onClose: close) {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                PBTextField("Name", text: $form.name, prompt: "e.g. Spare propellers")
                    .textInputAutocapitalization(.sentences)
                    .submitLabel(.done)
                    .accessibilityIdentifier("addComponent.name")
                costs
                status
                VStack(spacing: 0) {
                    PBSettingRow("Paid by", value: ledgerStore.books.firstName(form.paidBy), icon: .wallet) {
                        isPayerPickerShown = true
                    }
                    .accessibilityIdentifier("addComponent.paidBy")
                    PBSettingRow("Add receipt", value: form.receipt == nil ? nil : "Photo added", icon: .camera, showsDivider: false) {
                        isPhotoPickerShown = true
                    }
                    .accessibilityIdentifier("addComponent.receipt")
                }
                .pbCard(padding: 0)
                Text("Shares update as soon as an actual cost is added.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
                PBButton(editing == nil ? "Add component" : "Save changes", fillsWidth: true, action: save)
                    .disabled(!form.canSave(currency: currency))
                    .accessibilityIdentifier("addComponent.add")
                if editing != nil {
                    PBTextButton("Delete component", style: .destructive) { isDeleteShown = true }
                        .frame(maxWidth: .infinity)
                        .accessibilityIdentifier("addComponent.delete")
                }
            }
            .padding(.horizontal, PBSpace.s4)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.projectAddComponent")
        .interactiveDismissDisabled(isDirty)
        .pbFullScreenAlert(isPresented: $isDiscardShown, title: editing == nil ? "Discard this component?" : "Discard changes?",
                           message: editing == nil ? "What you typed won’t be saved." : "Your changes to this component won’t be saved.",
                           cancelLabel: "Keep editing", actionLabel: "Discard", testIDPrefix: "addComponent.discardAlert", onAction: onClose)
        .pbFullScreenAlert(isPresented: $isDeleteShown, title: "Delete \(original.trimmedName)?", message: "Its cost comes off the project.",
                           cancelLabel: "Cancel", actionLabel: "Delete", testIDPrefix: "addComponent.deleteAlert", onAction: delete)
        .pbSheet(isPresented: $isPayerPickerShown) {
            payerPicker
        }
        .photosPicker(isPresented: $isPhotoPickerShown, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) {
            guard let item = pickedPhoto, let data = try? await item.loadTransferable(type: Data.self) else { return }
            form.receipt = PhotoFiles.save(data) ?? form.receipt
            pickedPhoto = nil
        }
    }

    // MARK: Fields

    private var costs: some View {
        HStack(alignment: .top, spacing: PBSpace.s8) {
            PBTextField("Estimated cost", text: costBinding(form.estimate) { form.estimate = $0 }, prompt: prompt)
                .keyboardType(.decimalPad)
                .accessibilityIdentifier("addComponent.estimate")
            PBTextField("Actual cost", text: costBinding(form.actual) { form.setActual($0) }, prompt: prompt,
                        helper: "Add it once it’s bought")
                .keyboardType(.decimalPad)
                .accessibilityIdentifier("addComponent.actual")
        }
    }

    /// "₹0".
    private var prompt: String { "\(Money.info(currency).symbol)0" }

    /// Shows the raw digits with the currency's symbol and grouping ("₹6,000") and keeps only what
    /// the decimal pad may type.
    private func costBinding(_ raw: String, set: @escaping (String) -> Void) -> Binding<String> {
        let currency = Currency(code: currency)
        let allowsDecimals = Money.info(currency.code).exponent > 0
        return Binding {
            raw.isEmpty ? "" : PBAmountField.display(raw, currency: currency)
        } set: {
            set(PBAmountField.sanitize($0, allowsDecimals: allowsDecimals))
        }
    }

    private var status: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            Text("Status")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
            PBSegmentedControl(options: ProjectComponent.Status.allCases.map(\.title), selection: Binding {
                ProjectComponent.Status.allCases.firstIndex(of: form.status) ?? 0
            } set: {
                Haptics.selection()
                form.status = ProjectComponent.Status.allCases[$0]
            }, testIDPrefix: "addComponent.status")
        }
    }

    /// The project's members, one picked (Add expense's Paid by, 06-04).
    private var payerPicker: some View {
        let members = ledgerStore.ledger.group(projectId)?.memberIds ?? [Person.me]
        return PBSheet(title: "Paid by", testIDPrefix: "paidBy", onClose: { isPayerPickerShown = false }) {
            VStack(spacing: 0) {
                ForEach(members, id: \.self) { id in
                    PBPersonRow(name: ledgerStore.books.firstName(id), avatar: ledgerStore.memberAvatar(id), size: .compact,
                                trailing: id == form.paidBy ? .check : .none, showsDivider: id != members.last) {
                        Haptics.selection()
                        form.paidBy = id
                        isPayerPickerShown = false
                    }
                    .accessibilityIdentifier("paidBy.row.\(id)")
                }
            }
            .pbCard(padding: 0)
        }
    }


    // MARK: Actions

    private func close() {
        if isDirty { isDiscardShown = true } else { onClose() }
    }

    private func save() {
        do {
            if let editing {
                try ledgerStore.editComponent(editing.id, form)
            } else {
                try ledgerStore.addComponent(to: projectId, form)
                router.toast("Component added")
            }
            Haptics.success()
            onClose()
        } catch {
            Haptics.warning()
        }
    }

    private func delete() {
        guard let editing else { return }
        try? ledgerStore.deleteComponent(editing.id)
        router.toast("Component deleted")
        onClose()
    }
}

#if DEBUG
#Preview("ComponentSheet") {
    @Previewable @State var isPresented = true
    GroupsPreview {
        PBButton("Add component") { isPresented = true }
            .pbSheet(isPresented: $isPresented) {
                ComponentSheet(projectId: "pj-drone", editing: nil, currency: "INR") { isPresented = false }
            }
    }
}
#endif
