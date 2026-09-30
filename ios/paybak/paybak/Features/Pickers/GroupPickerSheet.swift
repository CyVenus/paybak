import SwiftUI

/// The expense Group picker (add-expense §3.10, proposal): "No group", then your groups with their
/// type icons, checked on the current one. Answers `.group(id)` or `.group(nil)` and closes.
struct GroupPickerSheet: View {
    let request: GroupPickRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store

    var body: some View {
        let groups = store.expenseGroups.filter { !$0.isArchived }
        PBSheet(title: "Group", testIDPrefix: "group", onClose: router.dismissSheet) {
            ScrollView {
                VStack(spacing: 0) {
                    row("No group", icon: .groups, id: nil, isLast: groups.isEmpty)
                    ForEach(groups) { group in
                        row(group.name, icon: group.pbIcon, id: group.id, isLast: group.id == groups.last?.id)
                    }
                }
                .pbCard(padding: 0)
            }
            .scrollBounceBehavior(.basedOnSize)
            .frame(maxHeight: 56 * 7)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("group.sheet")
    }

    private func row(_ title: String, icon: PBIcon, id: GroupID?, isLast: Bool) -> some View {
        PBSettingRow(title, icon: icon, trailing: request.selected == id ? .check : .unchecked, showsDivider: !isLast) {
            Haptics.selection()
            router.complete(request.id, with: .group(id))
        }
        .accessibilityIdentifier("group.row.\(id ?? "none")")
    }
}
