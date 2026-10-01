import Foundation
import Testing
@testable import paybak

/// Lane A, M6: the scheduled notifications, the filtered activity log and where timeline rows lead.
@MainActor
struct ActivityTests {
    // MARK: Upcoming alerts (domain.md §10.1)

    @Test func upcomingAlertsAreTheInboxItemsTickWillCreate() throws {
        let books = DemoFixture.load("eshaClaimsPayment")
        let alerts = books.upcomingAlerts()
        // Thursday's Rent puts Kabir in your debt overall, so the Goa Trip reminder never comes; the
        // first alert is the Olive Garden share that falls overdue.
        let first = try #require(alerts.first)
        #expect(first.fireAt == DemoFixture.moment(2026, 10, 5, 9, 0))
        guard case .inbox(let item) = first.content else { Issue.record("Expected an inbox alert"); return }
        #expect(books.inboxText(item) == ("Payment overdue", "Priya owes you ₹700 for Dinner at Olive Garden. It was due on 4 Oct."))
        #expect(alerts.allSatisfy { $0.fireAt > books.now })
        #expect(alerts.map(\.fireAt) == alerts.map(\.fireAt).sorted())
        // Planning never changes the books it reads.
        #expect(books.ledger == DemoFixture.load("eshaClaimsPayment").ledger)
    }

    @Test func aDebtYouOweIsRemindedOnItsDueDate() throws {
        var books = DemoFixture.load("eshaClaimsPayment")
        for index in books.ledger.recurringRules.indices {
            books.ledger.recurringRules[index].active = false
        }
        let reminder = try #require(books.upcomingAlerts().first { alert in
            if case .inbox(let item) = alert.content { item.type == .paymentReminder } else { false }
        })
        #expect(reminder.fireAt == DemoFixture.moment(2026, 10, 2, 21, 0))
        guard case .inbox(let item) = reminder.content else { return }
        #expect(books.inboxText(item) == ("Payment reminder", "You owe Kabir ₹1,400 for Goa Trip. It’s due today."))
        // Its notification pays Kabir by UPI, as the inbox row does.
        #expect(DeepLink(item, in: books.ledger)
            == .recordPayment(to: "p-kabir", amount: 140_000, context: .group("g-goa"), method: .upi))
    }

    @Test func upcomingAlertsIncludeTheMonthEndSummaryAndTheGasDraft() {
        let alerts = DemoFixture.load("eshaClaimsPayment").upcomingAlerts(days: 31)
        let summary = alerts.first { if case .inbox(let item) = $0.content { item.type == .monthlySummary } else { false } }
        #expect(summary?.fireAt == DemoFixture.moment(2026, 10, 31, 20, 0))
        let draft = alerts.first { if case .draft = $0.content { true } else { false } }
        #expect(draft?.fireAt == DemoFixture.moment(2026, 10, 28, 9, 0))
    }

    @Test func pushTogglesAndTheLimitFilterAlerts() {
        var books = DemoFixture.load("eshaClaimsPayment")
        books.ledger.settings.push.reminders = false
        let alerts = books.upcomingAlerts(days: 31)
        #expect(!alerts.isEmpty)
        #expect(alerts.allSatisfy { alert in
            switch alert.content {
            case .inbox(let item): item.type != .paymentReminder
            case .draft: false
            }
        })
        #expect(DemoFixture.load("eshaClaimsPayment").upcomingAlerts(limit: 2).count == 2)
    }

    @Test func settledDebtsGetNoReminders() {
        let alerts = DemoFixture.load("allSettled").upcomingAlerts()
        #expect(!alerts.contains { if case .inbox(let item) = $0.content { item.type == .paymentReminder } else { false } })
    }

    @Test func draftAlertCopy() throws {
        let books = DemoFixture.load()
        let draft = try #require(books.ledger.draft("d-gas-09"))
        #expect(books.draftAlertText(draft) == ("Cooking gas needs an amount", "Enter this month’s amount for Flat 302."))
    }

    // MARK: Activity log (activity §3.9)

    @Test func logFiltersByGroup() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let titles = books.activityLog(.group("g-goa")).flatMap(\.items).map(\.title)
        #expect(titles.contains("Kabir changed Villa (3 nights)"))
        #expect(titles.contains("Dev added Fuel"))
        #expect(!titles.contains("Meera added Electricity bill"))
        #expect(books.activityLogTitle(.group("g-goa")) == "Goa Trip · History")
    }

    @Test func logFiltersByPerson() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let days = books.activityLog(.person("p-rohan"))
        #expect(days.first?.header == "Today")
        #expect(days.first?.items.first?.title == "Reminder sent to Rohan")
        #expect(days.flatMap(\.items).allSatisfy { !$0.title.contains("Meera") })
        #expect(books.activityLogTitle(.person("p-rohan")) == "Rohan · History")
    }

    @Test func projectLogListsItsParts() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let camera = books.activityLog(.project("pj-drone")).flatMap(\.items).first { $0.title == "Dev bought Camera" }
        #expect(camera?.subtitle == "Build a Drone")
        if case .amount(let amount, _, let isIncoming) = camera?.trailing {
            #expect(amount == "₹7,500")
            #expect(!isIncoming)
        } else {
            Issue.record("Expected the camera's cost")
        }
        #expect(camera?.route == .project("pj-drone"))
        if case .icon(let icon) = camera?.leading { #expect(icon == .drone) } else { Issue.record("Expected the project's icon") }
    }

    /// Parts show in the whole timeline too; one only added (planned) has no actual cost yet.
    @Test func timelineListsProjectParts() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let items = books.activityDays(books.timelineDays(books.timeline())).flatMap(\.items)
        let gps = items.first { $0.title == "You added GPS module" }
        #expect(gps?.subtitle == "Build a Drone")
        if case .amount = gps?.trailing { Issue.record("A planned part has no actual cost") }
        #expect(items.contains { $0.title == "Dev bought Camera" })
    }

    @Test func categoryLogKeepsThatMonthsExpenses() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let filter = ActivityFilter.category(.food, month: YearMonth(year: 2026, month: 9))
        let titles = books.activityLog(filter).flatMap(\.items).map(\.title)
        #expect(titles.contains("You added Dinner at Olive Garden"))
        #expect(!titles.contains("Dev added Fuel"))
        #expect(books.activityLogTitle(filter) == "Food · September")
    }

    // MARK: Row destinations (activity §3.9)

    @Test func timelineRowsOpenWhatTheyAreAbout() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let items = books.activityDays(books.timelineDays(books.timeline())).flatMap(\.items)
        func route(_ title: String) -> Route? { items.first { $0.title == title }?.route }
        #expect(route("Kabir changed Villa (3 nights)") == .expense("e-goa-villa"))
        #expect(route("Reminder sent to Rohan") == .expense("e-movie"))
        #expect(route("Cooking gas draft created") == .recurring("g-flat302"))
        #expect(items.first { $0.title == "Priya paid you" }.map { if case .payment = $0.route { true } else { false } } == true)
    }
}
