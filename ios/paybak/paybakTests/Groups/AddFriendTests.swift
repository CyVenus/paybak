import Foundation
import Testing
@testable import paybak

/// Add friend's lists and search (screens-groups §7.4–7.5) and opening invite links (§2.11).
@MainActor
struct AddFriendTests {
    private let people = DemoFixture.load().ledger.people
    private let addressBook = ContactsDirectory.demoAddressBook

    @Test func demoListsMatchTheDesign() {
        let lists = AddFriendLists(contacts: addressBook, people: people, query: "")
        #expect(lists.onPaybak.map(\.name) == ["Kabir Singh", "Meera Iyer"])
        #expect(lists.onPaybak.map(\.username) == ["kabir", "meera"])
        #expect(lists.onPaybak.map(\.friendId) == ["p-kabir", "p-meera"])
        #expect(lists.invite.map(\.name) == ["Ananya Rao"])
        #expect(lists.invite.first?.guestId == "p-ananya")
    }

    @Test func aNewAccountCanAddAndInvite() {
        let lists = AddFriendLists(contacts: addressBook, people: [], query: "")
        #expect(lists.onPaybak.map(\.friendId) == [nil, nil])
        #expect(lists.onPaybak.compactMap(\.contact?.username) == ["kabir", "meera"])
        #expect(lists.invite.map(\.guestId) == [nil])
        #expect(lists.invite.first?.reach == "+91 98765 43210")
    }

    @Test func searchFiltersByNameUsernamePhoneAndEmail() {
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "mee").onPaybak.map(\.id) == ["p-meera"])
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "MEERA").invite.isEmpty)
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "@rohan").onPaybak.map(\.id) == ["p-rohan"])
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "98765").invite.map(\.id) == ["p-ananya"])
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "anañya").invite.map(\.id) == ["p-ananya"])
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "zzz").isEmpty)
    }

    @Test func aFullAddressThatMatchesNobodyCanBeInvited() {
        let lists = AddFriendLists(contacts: addressBook, people: people, query: "sam@example.com")
        #expect(lists.onPaybak.isEmpty)
        #expect(lists.invite.map(\.name) == ["sam@example.com"])
        #expect(lists.invite.first?.reach == "sam@example.com")
        #expect(AddFriendLists(contacts: addressBook, people: people, query: "@sam").invite.first?.reach == nil)
    }

    @Test func inviteLinks() {
        #expect(QRScanner.username(fromInviteLink: "https://paybak.app/i/meera") == "meera")
        #expect(QRScanner.username(fromInviteLink: "paybak.app/i/Arjun") == "arjun")
        #expect(QRScanner.username(fromInviteLink: "https://example.com/i/meera") == nil)
        #expect(QRScanner.username(fromInviteLink: "https://paybak.app/meera") == nil)

        let directory = FileManager.default.temporaryDirectory.appending(path: "AddFriendTests.\(UUID().uuidString)")
        let profile = ProfileStore(defaults: UserDefaults(suiteName: "AddFriendTests.\(UUID().uuidString)")!, directory: directory)
        profile.update { $0.name = "Arjun Mehta" }
        let store = LedgerStore(file: LedgerFile(url: directory.appending(path: "ledger.json")),
                                clock: AppClock(pinned: DemoFixture.figmaNow, calendar: DemoFixture.calendar), profileStore: profile)
        store.replace(with: DemoFixture.load().ledger)
        #expect(store.openInviteLink("https://paybak.app/i/meera") == .friend("p-meera"))
        #expect(store.openInviteLink("https://paybak.app/i/arjun") == .ownCode)
        #expect(store.openInviteLink("hello") == .notPaybak)
        guard case .friend(let id) = store.openInviteLink("https://paybak.app/i/zoya") else {
            Issue.record("A new username adds a friend")
            return
        }
        #expect(store.ledger.person(id)?.name == "Zoya")
        #expect(store.ledger.person(id)?.isGuest == false)
        #expect(store.openInviteLink("https://paybak.app/i/zoya") == .friend(id))
    }
}
