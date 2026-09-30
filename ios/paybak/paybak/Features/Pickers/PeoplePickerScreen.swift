import SwiftUI

/// Pick friends (or guests) (add-expense §5). Multi-select ("Split with", New group members) hands
/// every change back live with `.people(ids)`, so Back, Done and the edge swipe all keep it; single
/// select (Record payment From / To, Lend money) answers `.person(id)` and closes. Search matches
/// names, usernames and contacts; no match offers to add the text as a guest.
struct PeoplePickerScreen: View {
    let request: PeoplePickRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @State private var selection: [PersonID] = []
    @State private var order: [PersonID] = []
    @State private var query = ""
    @State private var didLoad = false

    private var isMulti: Bool { request.mode == .multi }
    private var prefix: String { isMulti ? "splitWith" : "pickPerson" }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s16) {
                PBTextField(nil, text: $query, prompt: "Name, phone, email or @username", icon: .search, showsClearButton: true)
                    .accessibilityIdentifier("\(prefix).search")
                if isMulti, !chips.isEmpty, query.isEmpty {
                    selectedChips
                }
                if request.showsYou, query.isEmpty {
                    youCard
                }
                if isMulti, query.isEmpty {
                    PBSheetRow(title: "Add a new friend", subtitle: nil, icon: .userAdd) { router.open(.addFriend) }
                        .accessibilityIdentifier("\(prefix).addFriend")
                }
                friends
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBSpace.s32)
            .phoneContentWidth()
        }
        .scrollDismissesKeyboard(.interactively)
        .pinnedHeader {
            PBDoneHeader(title: request.title, testIDPrefix: prefix, onBack: router.back, onDone: router.back)
        }
        .background(PBColor.bgPrimary)
        .onAppear(perform: load)
        .onChange(of: store.ledger.people.map(\.id)) { old, new in
            // A friend added from "Add a new friend" (or a guest from search) joins the selection.
            for id in new where !old.contains(id) {
                order.append(id)
                if isMulti { toggle(id) }
            }
        }
        .routeTestRoot("pickPeople")
    }

    // MARK: Sections

    private var chips: [PersonID] { selection.filter { $0 != Person.me } }

    private var selectedChips: some View {
        ScrollView(.horizontal) {
            HStack(spacing: PBSpace.s8) {
                ForEach(chips, id: \.self) { id in
                    let person = store.ledger.person(id)
                    PBCategoryChip(person?.firstName ?? "", leading: .avatar(person?.avatarContent ?? .icon(.profile)),
                                   onRemove: { toggle(id) }, action: { toggle(id) })
                        .accessibilityIdentifier("\(prefix).chip.\(id)")
                }
            }
        }
        .scrollIndicators(.hidden)
    }

    private var youCard: some View {
        PBPersonRow(
            name: "You",
            avatar: profileStore.avatarContent,
            subtitle: profileStore.profile.name,
            isOnCard: true,
            trailing: isMulti ? .select(selection.contains(Person.me)) : (request.selected.first == Person.me ? .check : .none),
            showsDivider: false
        ) {
            pick(Person.me)
        }
        .accessibilityIdentifier("\(prefix).you")
        .pbCard(padding: 0)
    }

    @ViewBuilder
    private var friends: some View {
        let people = matches
        if people.isEmpty, !query.trimmingCharacters(in: .whitespaces).isEmpty {
            if request.allowsGuests {
                PBSheetRow(title: guestRowTitle, subtitle: nil, icon: .userAdd, action: addGuest)
                    .accessibilityIdentifier("\(prefix).addGuest")
            } else {
                Text("No friends match “\(query)”")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity)
            }
        } else if !people.isEmpty {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                if query.isEmpty {
                    PBSectionHeader("Friends")
                }
                VStack(spacing: 0) {
                    ForEach(people) { person in
                        PBPersonRow(
                            name: person.name,
                            avatar: person.avatarContent,
                            tag: person.isGuest ? "Guest" : nil,
                            isOnCard: true,
                            trailing: trailing(for: person.id),
                            showsDivider: person.id != people.last?.id
                        ) {
                            pick(person.id)
                        }
                        .accessibilityIdentifier("\(prefix).friend.\(person.id)")
                    }
                }
                .pbCard(padding: 0)
            }
        }
    }

    private func trailing(for id: PersonID) -> PBPersonRow.Trailing {
        if isMulti { return .select(selection.contains(id)) }
        return request.selected.first == id ? .check : .none
    }

    /// Friends in the picker's order, filtered by the search.
    private var matches: [Person] {
        let people = order.compactMap { store.ledger.person($0) }.filter { request.allowsGuests || !$0.isGuest }
        let needle = query.trimmingCharacters(in: .whitespaces).replacingOccurrences(of: "@", with: "")
        guard !needle.isEmpty else { return people }
        return people.filter { person in
            [person.name, person.username, person.contact].compactMap(\.self).contains {
                $0.range(of: needle, options: [.caseInsensitive, .diacriticInsensitive]) != nil
            }
        }
    }

    private var looksLikeContact: Bool {
        let text = query.trimmingCharacters(in: .whitespaces)
        return text.contains("@") && text.contains(".") || text.filter(\.isNumber).count >= 7
    }

    private var guestRowTitle: String {
        let text = query.trimmingCharacters(in: .whitespaces)
        return looksLikeContact ? "Invite \(text)" : "Add “\(text)” as a guest"
    }

    // MARK: Actions

    /// Selected people first (as the caller listed them), then everyone in the order they were added.
    private func load() {
        guard !didLoad else { return }
        didLoad = true
        selection = request.selected
        let ids = store.ledger.people.map(\.id)
        order = request.selected.filter(ids.contains) + ids.filter { !request.selected.contains($0) }
    }

    private func pick(_ id: PersonID) {
        Haptics.selection()
        if isMulti {
            toggle(id)
        } else {
            router.complete(request.id, with: .person(id))
        }
    }

    private func toggle(_ id: PersonID) {
        if let index = selection.firstIndex(of: id) {
            selection.remove(at: index)
        } else {
            selection.append(id)
        }
        router.results[request.id] = .people(selection)
    }

    private func addGuest() {
        let text = query.trimmingCharacters(in: .whitespaces)
        let name = looksLikeContact && text.contains("@") ? String(text.split(separator: "@").first ?? "") : text
        let id = store.addGuest(name: name.isEmpty ? text : name, contact: looksLikeContact ? text : nil)
        query = ""
        if !isMulti {
            router.complete(request.id, with: .person(id))
        }
    }
}
