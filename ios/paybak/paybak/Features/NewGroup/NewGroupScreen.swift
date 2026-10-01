import PhotosUI
import SwiftUI

/// New group (record-lend-group §6): one draft, Group or Project. A group has a type, members,
/// currency and simplify debts (on by default); a project adds a description, cover photo, budget
/// and the contribution rule. Create opens the new group or project on the Groups tab.
struct NewGroupScreen: View {
    let mode: NewGroupMode

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @State private var form: GroupForm?
    @State private var requests = (people: RecordID.make(), currency: RecordID.make())
    @State private var showsDiscard = false
    @State private var showsPhotoPicker = false
    @State private var pickedPhoto: PhotosPickerItem?

    var body: some View {
        Group {
            if let form {
                content(form)
            } else {
                PBColor.bgPrimary
            }
        }
        .onAppear {
            if form == nil { form = GroupForm(mode: mode, currency: store.books.defaultCurrency) }
        }
    }

    private func content(_ form: GroupForm) -> some View {
        @Bindable var form = form
        return ScrollView {
            VStack(spacing: PBSpace.s16) {
                PBSegmentedControl(options: ["Group", "Project"], selection: Binding {
                    form.isProject ? 1 : 0
                } set: {
                    Haptics.selection()
                    form.mode = $0 == 0 ? .group : .project
                })
                .accessibilityIdentifier("newGroup.mode")
                VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                    if form.isProject {
                        projectDetails(form)
                    } else {
                        groupDetails(form)
                    }
                    GroupMembersCard(form: form, onAdd: { addPeople(form) })
                    settingsCard(form)
                }
            }
            .padding(.bottom, PBSpace.s32)
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBModalHeader("New group", actionLabel: "Create", isActionEnabled: form.canCreate, testIDPrefix: "newGroup",
                          onClose: { close(form) }, onAction: { create(form) })
        }
        .pbAlert(isPresented: $showsDiscard, title: form.isProject ? "Discard this project?" : "Discard this group?",
                 message: "Your changes won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard",
                 testIDPrefix: "newGroup.discardAlert", onAction: router.dismissModal)
        .photosPicker(isPresented: $showsPhotoPicker, selection: $pickedPhoto, matching: .images)
        .task(id: pickedPhoto) {
            guard let item = pickedPhoto, let data = try? await item.loadTransferable(type: Data.self) else { return }
            form.coverPhoto = PhotoFiles.save(data) ?? form.coverPhoto
            pickedPhoto = nil
        }
        .onRouteResult(requests.people) { if case .people(let ids) = $0 { form.setMembers(ids) } }
        .onRouteResult(requests.currency) { if case .currency(let code) = $0 { form.currency = code } }
        .onStartScreen([.newGroup, .newGroupProject]) { _ in
            form.name = "Weekend Trek"
            form.type = .trip
            form.setMembers(["p-esha", "p-dev", "p-kabir"])
        }
        .routeTestRoot("newGroup")
    }

    // MARK: Sections

    private func groupDetails(_ form: GroupForm) -> some View {
        @Bindable var form = form
        return VStack(alignment: .leading, spacing: PBSpace.s16) {
            nameField(form)
            VStack(alignment: .leading, spacing: PBSpace.s4) {
                Text("Type")
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                HStack(spacing: PBSpace.s8) {
                    ForEach(LedgerGroup.GroupType.allCases, id: \.self) { type in
                        PBCategoryChip(type.title, isSelected: form.type == type) {
                            Haptics.selection()
                            form.type = type
                        }
                        .accessibilityIdentifier("newGroup.type.\(type.rawValue)")
                    }
                }
            }
        }
    }

    private func projectDetails(_ form: GroupForm) -> some View {
        @Bindable var form = form
        return VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                nameField(form)
                PBTextField("Description", text: $form.details, prompt: "What’s it for?")
                    .accessibilityIdentifier("newGroup.description")
                PBSettingRow("Add cover photo", value: form.coverPhoto == nil ? nil : "Photo added", icon: .camera, showsDivider: false) {
                    showsPhotoPicker = true
                }
                .pbCard(padding: 0)
                .accessibilityIdentifier("newGroup.cover")
            }
            PBTextField("Budget", text: Binding {
                form.budgetText
            } set: {
                form.budgetText = PBAmountField.sanitize($0, allowsDecimals: Money.info(form.currency).exponent > 0)
            }, prompt: "\(Money.info(form.currency).symbol)0", helper: "Optional. Spending is tracked against it.")
                .keyboardType(.decimalPad)
                .accessibilityIdentifier("newGroup.budget")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                Text("Contribution")
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                PBSegmentedControl(options: ["Equal", "Percent", "Fixed"], selection: Binding {
                    [.equal, .percent, .fixed].firstIndex(of: form.rule) ?? 0
                } set: {
                    Haptics.selection()
                    form.setRule([Contribution.Rule.equal, .percent, .fixed][$0])
                })
                .accessibilityIdentifier("newGroup.contribution")
                Text(form.contributionHelper)
                    .textStyle(.footnote)
                    .foregroundStyle(form.contributionAddsUp ? PBColor.textSecondary : PBColor.textDestructive)
            }
        }
    }

    private func nameField(_ form: GroupForm) -> some View {
        @Bindable var form = form
        return PBTextField("Name", text: $form.name, prompt: form.isProject ? "Build a Drone" : "Weekend Trek")
            .submitLabel(.done)
            .accessibilityIdentifier("newGroup.name")
    }

    private func settingsCard(_ form: GroupForm) -> some View {
        @Bindable var form = form
        let symbol = Money.info(form.currency).symbol
        return VStack(spacing: 0) {
            PBSettingRow("Currency", value: symbol == form.currency ? symbol : "\(form.currency) \(symbol)",
                         icon: .exchange, showsDivider: !form.isProject) { openCurrency(form) }
                .accessibilityIdentifier("newGroup.currency")
            if !form.isProject {
                PBSettingRow("Simplify debts", subtitle: "Fewer payments when settling up", icon: .shuffle,
                             trailing: .toggle($form.simplifyDebts), showsDivider: false)
                    .accessibilityIdentifier("newGroup.simplify")
            }
        }
        .pbCard(padding: 0)
    }

    // MARK: Actions

    private func addPeople(_ form: GroupForm) {
        router.open(.pickPeople(PeoplePickRequest(id: requests.people, selected: form.members, title: "Add people", showsYou: false)))
    }

    private func openCurrency(_ form: GroupForm) {
        router.open(.pickCurrency(CurrencyPickRequest(id: requests.currency, selected: form.currency)))
    }

    private func close(_ form: GroupForm) {
        if form.isDirty { showsDiscard = true } else { router.dismissModal() }
    }

    private func create(_ form: GroupForm) {
        let id = store.addGroup(form.draft)
        Haptics.success()
        router.didCreateGroup(id, isProject: form.isProject)
    }
}

/// Members: you (not removable), the people added (✕ on a group, their share on a project) and
/// "Add people" on a white tile.
private struct GroupMembersCard: View {
    let form: GroupForm
    let onAdd: () -> Void

    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("Members")
            VStack(spacing: 0) {
                ForEach(Array(form.everyone.enumerated()), id: \.element) { index, id in
                    row(id, index: index)
                }
                addPeopleRow
            }
            .pbCard(padding: 0)
        }
    }

    private func row(_ id: PersonID, index: Int) -> some View {
        let isMe = id == Person.me
        let person = store.ledger.person(id)
        return PBPersonRow(
            name: isMe ? "You" : person?.name ?? "Someone",
            avatar: isMe ? profileStore.avatarContent : person?.avatarContent ?? .icon(.profile),
            subtitle: isMe && !form.isProject ? profileStore.profile.name : nil,
            tag: person?.isGuest == true ? "Guest" : nil,
            size: .compact,
            trailing: trailing(id, index: index)
        )
        .overlay(alignment: .trailing) {
            if form.isProject, form.rule != .equal {
                shareField(id)
                    .padding(.trailing, PBSpace.s16)
            }
        }
        .accessibilityIdentifier("newGroup.member.\(index)")
    }

    private func trailing(_ id: PersonID, index: Int) -> PBPersonRow.Trailing {
        if form.isProject {
            return form.rule == .equal ? .amount(form.equalShareText) : .none
        }
        guard id != Person.me else { return .none }
        return .remove { form.members.removeAll { $0 == id } }
    }

    private func shareField(_ id: PersonID) -> some View {
        let isPercent = form.rule == .percent
        return PBInlineField(
            text: Binding {
                form.shares[id] ?? ""
            } set: {
                form.shares[id] = PBAmountField.sanitize($0, allowsDecimals: true)
            },
            accessibilityLabel: "\(store.books.firstName(id))’s share",
            currency: isPercent ? nil : form.currency,
            suffix: isPercent ? "%" : nil
        )
    }

    private var addPeopleRow: some View {
        Button(action: onAdd) {
            HStack(spacing: PBSpace.s8) {
                PBIconView(.userAdd)
                    .foregroundStyle(PBColor.iconPrimary)
                    .frame(width: PBSize.tap, height: PBSize.tap)
                    .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.tile))
                Text("Add people")
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                PBIconView(.chevronRight, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconTertiary)
            }
            .padding(.leading, PBSpace.s8)
            .padding(.trailing, PBSpace.s16)
            .frame(height: 56)
            .contentShape(.rect)
        }
        .buttonStyle(PBRowButtonStyle(surface: .card))
        .accessibilityIdentifier("newGroup.addPeople")
    }
}
