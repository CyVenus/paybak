import SwiftUI

/// Add friend (screens-groups §7): search, an invite link, QR both ways, and your contacts: those on
/// Paybak (Added / Add) and those to invite, who join right away as guest friends.
struct AddFriendScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    @State private var query = ""
    @State private var contacts: [ContactsDirectory.Contact] = []
    @State private var isMyCodeShown = false
    @State private var isScanning = false
    @State private var share: ShareItem?

    var body: some View {
        let lists = AddFriendLists(contacts: contacts, people: ledgerStore.ledger.people, query: query)
        ScrollView {
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                VStack(spacing: PBSpace.s16) {
                    PBTextField(nil, text: $query, prompt: "Name, phone, email or @username", icon: .search, showsClearButton: true)
                        .submitLabel(.search)
                        .accessibilityIdentifier("addFriend.search")
                    options
                }
                if !lists.onPaybak.isEmpty {
                    onPaybak(lists.onPaybak)
                }
                if !lists.invite.isEmpty {
                    invite(lists.invite)
                }
                if lists.isEmpty && !query.trimmingCharacters(in: .whitespaces).isEmpty {
                    Text("No one matches “\(query.trimmingCharacters(in: .whitespaces))”.")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .accessibilityIdentifier("addFriend.noMatch")
                }
            }
            .pbPushContent()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBPushHeader("Add friend", testIDPrefix: "addFriend", onBack: router.back)
        }
        .task { contacts = await ContactsDirectory.contacts() }
        .pbSheet(isPresented: $isMyCodeShown) {
            MyQRCodeSheet { isMyCodeShown = false }
        }
        .systemShare(item: $share)
        .fullScreenCover(isPresented: $isScanning) {
            QRScannerView(onFinish: finishScan)
        }
        .onStartScreen([.myQrCode]) { _ in isMyCodeShown = true }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.addFriend")
    }

    private var options: some View {
        VStack(spacing: 0) {
            PBSettingRow("Invite with a link", icon: .link) {
                share = ShareItem(text: profileStore.profile.inviteMessage)
            }
            .accessibilityIdentifier("addFriend.inviteLink")
            PBSettingRow("Scan QR code", icon: .scan) {
                if QRScanner.isAvailable {
                    isScanning = true
                } else {
                    router.toast("Scanning isn’t available on this device")
                }
            }
            .accessibilityIdentifier("addFriend.scanQr")
            PBSettingRow("My QR code", icon: .qrCode, showsDivider: false) {
                isMyCodeShown = true
            }
            .accessibilityIdentifier("addFriend.myQr")
        }
        .pbCard(padding: 0)
    }

    private func onPaybak(_ rows: [AddFriendLists.OnPaybakRow]) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("On Paybak")
            VStack(spacing: 0) {
                ForEach(rows) { row in
                    let avatar: PBAvatar.Content = row.avatar.flatMap(PBPeepHead.init(rawValue:)).map { .art($0) }
                        ?? .initials(Person.initials(of: row.name))
                    PBPersonRow(name: row.name, avatar: avatar, subtitle: "@\(row.username)",
                                trailing: row.friendId == nil ? .button("Add") { add(row) } : .status("Added"),
                                showsDivider: row.id != rows.last?.id,
                                action: row.friendId.map { id in { router.open(.friend(id)) } })
                        .pbFlushRow()
                        .accessibilityIdentifier("addFriend.onPaybak.\(row.id)")
                }
            }
        }
    }

    private func invite(_ rows: [AddFriendLists.InviteRow]) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s16) {
            VStack(alignment: .leading, spacing: PBSpace.s4) {
                PBSectionHeader("Invite")
                VStack(spacing: 0) {
                    ForEach(rows) { row in
                        PBPersonRow(name: row.name, avatar: .initials(Person.initials(of: row.name)), subtitle: "Not on Paybak yet",
                                    trailing: .button("Invite") { invite(row) }, showsDivider: row.id != rows.last?.id)
                            .pbFlushRow()
                            .accessibilityIdentifier("addFriend.invite.\(row.id)")
                    }
                }
            }
            Text("People who aren’t on Paybak join as guests. You can still split with them.")
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
        }
    }

    // MARK: Actions

    private func add(_ row: AddFriendLists.OnPaybakRow) {
        guard let contact = row.contact else { return }
        ledgerStore.addFriend(contact)
        Haptics.success()
    }

    /// A guest friend opens their page; anyone else is added as a guest first.
    private func invite(_ row: AddFriendLists.InviteRow) {
        let id = row.guestId ?? ledgerStore.addGuest(name: row.name, contact: row.reach)
        query = ""
        router.open(.friend(id))
    }

    private func finishScan(_ code: String?) {
        isScanning = false
        guard let code else { return }
        switch ledgerStore.openInviteLink(code) {
        case .friend(let id): router.open(.friend(id))
        case .ownCode: router.toast("That’s your own code")
        case .notPaybak: router.toast("That isn’t a Paybak code")
        }
    }
}

#if DEBUG
#Preview("AddFriendScreen") {
    GroupsPreview {
        AddFriendScreen()
    }
}
#endif
